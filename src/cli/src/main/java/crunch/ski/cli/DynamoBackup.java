package crunch.ski.cli;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.datamodeling.S3Link;
import com.amazonaws.services.dynamodbv2.document.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ski.crunch.aws.DynamoFacade;
import ski.crunch.dao.ActivityDAO;
import ski.crunch.dao.TableScanner;
import ski.crunch.model.ActivityItem;
import ski.crunch.model.UserSettingsItem;
import ski.crunch.utils.Jsonable;
import ski.crunch.utils.NotFoundException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DynamoBackup {

    public static final Logger logger = LoggerFactory.getLogger(DynamoBackup.class);
    //private DynamoFacade dynamoFacade;
    private String region;
    private AWSCredentialsProvider credentialsProvider;

    public DynamoBackup(String region, AWSCredentialsProvider credentialsProvider) {
        this.region = region;
        this.credentialsProvider = credentialsProvider;
    }

    public void userDataBackup(String user, String backupId, String userTableName, File destDir) throws IOException{

        DynamoFacade dynamoFacade = new DynamoFacade(region, userTableName , credentialsProvider);
        ActivityDAO activityDAO = new ActivityDAO(dynamoFacade, userTableName);
        UserSettingsItem userSettingsItem = lookupUser(user, userTableName);
        writeResultToFile(userSettingsItem, new File(destDir, "user.json"));
        List<ActivityItem> activityItems = activityDAO.getActivitiesByUser(userSettingsItem.getId());
        writeResultSetToFile(activityItems, new File(destDir, "activities.json"));
        File rawDir = new File(destDir, "raw");
        File procDir = new File(destDir, "proc");
        rawDir.mkdir();
        procDir.mkdir();
        for (ActivityItem activityItem : activityItems) {
            S3Link rawActivityS3Link = activityItem.getRawActivity();
            rawActivityS3Link.downloadTo(rawDir);
            S3Link processedActivityS3Link = activityItem.getProcessedActivity();
            processedActivityS3Link.downloadTo(procDir);
        }
    }


    public void fullTableBackup(String backupId, String tableName, int numberOfThreads, File destination, String fileName) throws IOException {
       DynamoFacade dynamoFacade = new DynamoFacade(region, tableName, credentialsProvider);
        List<Item> results =  TableScanner.parallelScan(dynamoFacade, tableName,20, numberOfThreads );
        writeItemsToFile(results, new File(destination, fileName));
    }

    /**
     * Writes list of Jsonable objects to file
     * @param resultSet List<Item> resultset to write
     * @param file File to write to*
     * @throws IOException
     */
    private  void writeItemsToFile(List<Item> resultSet, File file) throws IOException{
        try (FileWriter fw = new FileWriter(file)) {
            try (BufferedWriter bufferedWriter = new BufferedWriter(fw)) {

                for (Item item : resultSet) {
                    try {
                        System.out.println(item.toJSON());
                        bufferedWriter.write(item.toJSON() + System.lineSeparator());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                bufferedWriter.flush();
            }
        }
    }


    /**
     * Writes list of Jsonable objects to file*
     * @param file File to write to
     * @param <E> Dynamodbv2 mapper type that implements Jsonable
     * @throws IOException
     */
    private <E extends Jsonable> void writeResultToFile(E result, File file) throws IOException{
        List<E> list = new ArrayList<>();
        list.add(result);
        writeResultSetToFile(list, file);
    }

    /**
     * Writes list of Jsonable objects to file
     * @param resultSet List<E> resultset to write
     * @param file File to write to
     * @param <E> Dynamodbv2 mapper type that implements Jsonable
     * @throws IOException
     */
    private <E extends Jsonable> void writeResultSetToFile(List<E> resultSet, File file) throws IOException{
        try (FileWriter fw = new FileWriter(file)) {
            try (BufferedWriter bufferedWriter = new BufferedWriter(fw)) {

                for (E item : resultSet) {
                    try {
                        System.out.println(item.toJsonString());
                        bufferedWriter.write(item.toJsonString() + System.lineSeparator());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                bufferedWriter.flush();
            }
        }
    }

    private UserSettingsItem lookupUser(String user, String userTableName) throws NotFoundException {
        DynamoFacade dynamoFacade = new DynamoFacade(region, userTableName , credentialsProvider);
        UserSettingsItem userItem = null;

        if (user.contains("@")) {
            //query email via gsi
            final UserSettingsItem userSettingsItem = new UserSettingsItem();
            userSettingsItem.setEmail(user);
            final DynamoDBQueryExpression<UserSettingsItem> queryExpression = new DynamoDBQueryExpression<>();
            queryExpression.setHashKeyValues(userSettingsItem);
            queryExpression.setIndexName("email-index");
            queryExpression.setConsistentRead(false);


            final PaginatedQueryList<UserSettingsItem> results =  dynamoFacade.getMapper().query(UserSettingsItem.class, queryExpression);
            assert(results.size() == 1);
            if(results.size() < 1) {
                throw new NotFoundException("user " + user + " not found via email");
            }
            userItem = results.get(0);
        } else {
            userItem = dynamoFacade.getMapper().load(UserSettingsItem.class, user);
        }
        return userItem;
    }
}
