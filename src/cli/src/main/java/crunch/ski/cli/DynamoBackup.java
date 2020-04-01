package crunch.ski.cli;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ski.crunch.aws.DynamoFacade;
import ski.crunch.model.Jsonable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class DynamoBackup {

    public static final Logger logger = LoggerFactory.getLogger(DynamoBackup.class);
    private DynamoFacade dynamoFacade;
    private String region;
    private AWSCredentialsProvider credentialsProvider;

    public DynamoBackup(String region, AWSCredentialsProvider credentialsProvider) {
        this.region = region;
        this.credentialsProvider = credentialsProvider;
    }

//    public <E extends Jsonable> void partialTableBackup(Class<E> typeParamClass, String backupId, String tableName,
//                                                        int numberOfThreads, String filterExpression,
//                                                        Map<String, AttributeValue> entityAttributeValues) {
//        String tmpDirKey = tableName + "-" + backupId;
//        File tmpDir = new File(System.getProperty("java.io.tmpdir"), tmpDirKey);
//
//        Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
//        eav.put(":val1", new AttributeValue().withS("id"));
//        List<AttributeValue> ids = new ArrayList<>();
//        ids.add(new AttributeValue().withL);
//        ids.add("789");
//        eav.put(":val2", new AttributeValue().withL(ids));
//
//        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
//                .withFilterExpression("ProductCategory = :val1 and BicycleType = :val2").withExpressionAttributeValues(eav);
//
//        List<E> scanResult = dynamoFacade.getMapper().parallelScan(typeParamClass, scanExpression, numberOfThreads);
//        for (E item : scanResult) {
//            try {
//                System.out.println(item.toJsonString());
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        }
//    }


    /**
     * Performs full table scan
     *
     * @param typeParamClass
     * @param backupId
     * @param tableName
     * @param numberOfThreads
     * @param <E>
     */
    public <E extends Jsonable> void fullTableBackup(Class<E> typeParamClass, String backupId, String tableName, int numberOfThreads) throws IOException {
        dynamoFacade = new DynamoFacade(region, tableName, credentialsProvider);
        String tmpDirKey = tableName + "-" + backupId;
        File tmpDir = new File(System.getProperty("java.io.tmpdir"), tmpDirKey);

        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();

        List<E> scanResult = dynamoFacade.getMapper().parallelScan(typeParamClass, scanExpression, numberOfThreads);
        try(FileWriter fw = new FileWriter(tmpDirKey)) {
            try (BufferedWriter bufferedWriter = new BufferedWriter(fw)) {

                for (E item : scanResult) {
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
}
