package crunch.ski.cli.services;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.dynamodbv2.datamodeling.S3Link;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.util.json.Jackson;
import crunch.ski.cli.model.BackupOptions;
import crunch.ski.cli.model.Metadata;
import crunch.ski.cli.model.MetadataBuilder;
import crunch.ski.cli.model.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ski.crunch.aws.CredentialsProviderFactory;
import ski.crunch.aws.DynamoFacade;
import ski.crunch.aws.S3Facade;
import ski.crunch.dao.ActivityDAO;
import ski.crunch.dao.UserDAO;
import ski.crunch.model.ActivityItem;
import ski.crunch.model.UserSettingsItem;
import ski.crunch.utils.FileUtils;
import ski.crunch.utils.JsonUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class S3BackupService implements BackupRestoreService {
    public static final Logger logger = LoggerFactory.getLogger(S3BackupService.class);
    private BackupOptions options;
    private AWSCredentialsProvider credentialsProvider;
    private Metrics metrics;
    private S3Facade s3Facade;
    private DynamoFacade dynamoFacade;
    private UserDAO userDAO;
    private ActivityDAO activityDAO;
    private String userTableName;
    private String activityTableName;


    public S3BackupService(BackupOptions options) {
        this.options = options;
        this.credentialsProvider = CredentialsProviderFactory.getDefaultCredentialsProvider();
        this.userTableName  = calcTableName(USER_TABLE_IDENTIFIER, options);
        this.activityTableName  = calcTableName(ACTIVITY_TABLE_IDENTIFIER, options);
        this.s3Facade = new S3Facade(options.getConfigMap().get("DATA_REGION"), credentialsProvider, options.isTransferAcceleration());
        this.dynamoFacade = new DynamoFacade(options.getConfigMap().get("DATA_REGION"),userTableName, credentialsProvider);
        this.userDAO = new UserDAO(dynamoFacade, userTableName);
        this.activityDAO = new ActivityDAO(dynamoFacade, activityTableName);
        this.metrics = new Metrics();
    }

    @Override
    public int apply() {
        if (options.isVerbose()) {
            System.out.println("Backup ID: " + options.getBackupId());
            System.out.println("backing up data....");
        }

        MetadataBuilder metadataBuilder = buildMetadata(options);

        // do backup
        try {
            mkCloudDestDir();
            if (options.getUsers() == null) {
                fullCloudBackup(metadataBuilder);
            } else {
                userCloudBackup(metadataBuilder);
            }

            options.setEndTs(System.currentTimeMillis());

            //write metrics
            //calculate metrics
            ObjectListing listing = s3Facade.getS3Client().listObjects(options.getDestBucket(), options.getDestKey());
            long size = 0l;
            for (S3ObjectSummary objectSummary : listing.getObjectSummaries()) {
                size += objectSummary.getSize();
            }
            metrics.setDataVolumeRaw(org.apache.commons.io.FileUtils.byteCountToDisplaySize(size));
            writeMetrics();
            writeMetadata(metadataBuilder);

            // clean up
            FileUtils.deleteDirectory(new File(System.getProperty("java.io.tmpdir"), options.getBackupId()));

        } catch (IOException ex) {
            logger.error("Error occurred attempting backup.  Exiting");
            if (options.isVerbose()) {
                ex.printStackTrace();
            }
            return 1;
        }
        return 0;

    }

    public void userCloudBackup(MetadataBuilder metadataBuilder) throws IOException {
        for (String user : options.getUsers()) {
            userCloudBackup(user);
        }

    }
    private void userCloudBackup(String user) throws IOException {


        File tempDir = new File(System.getProperty("java.io.tmpdir") , options.getBackupId());
        tempDir.mkdir();

        UserSettingsItem userSettingsItem = userDAO.lookupUser(user);
        File userDir = new File(tempDir, userSettingsItem.getId());
        userDir.mkdir();
        JsonUtils.writeJsonToFile(userSettingsItem, new File(userDir, USER_FILENAME), false);

        List<ActivityItem> activityItems = activityDAO.getActivitiesByUser(userSettingsItem.getId());
        JsonUtils.writeJsonListToFile(activityItems, new File(userDir,ACTIVITY_FILENAME), false);

        File rawDir = new File(userDir, RAW_ACTIVITY_FOLDER);
        File procDir = new File(userDir, PROCESSED_ACTIVITY_FOLDER);
        rawDir.mkdir();
        procDir.mkdir();
        for (ActivityItem activityItem : activityItems) {
            S3Link rawActivityS3Link = activityItem.getRawActivity();
            if (rawActivityS3Link != null) {
                try {
                    rawActivityS3Link.getTransferManager().copy(RAW_ACTIVITY_BUCKET, rawActivityS3Link.getKey(),
                            options.getDestBucket(),
                            userSettingsItem.getId() + "/" + RAW_ACTIVITY_FOLDER + "/" +options.getDestKey());
                } catch (Exception ex) {
                    logger.warn("error downloading raw activity {} from {}", rawActivityS3Link.getKey(), rawActivityS3Link.getBucketName());
                }
            }
            S3Link processedActivityS3Link = activityItem.getProcessedActivity();
            if (processedActivityS3Link != null) {
                try {
                    processedActivityS3Link.getTransferManager().copy(PROCESSED_ACTIVITY_FOLDER, processedActivityS3Link.getKey(),
                            options.getDestBucket(),
                            userSettingsItem.getId() + "/" + PROCESSED_ACTIVITY_FOLDER + "/" +options.getDestKey());
                } catch (Exception ex) {
                    logger.warn("error downloading processed activity {} from {}", rawActivityS3Link.getKey(), rawActivityS3Link.getBucketName());
                }
            }
        }
        s3Facade.getS3Client().putObject(options.getDestBucket(), options.getDestKey() + "/" +userSettingsItem.getId() +"/" + USER_FILENAME, new File(userDir, USER_FILENAME));
        s3Facade.getS3Client().putObject(options.getDestBucket(), options.getDestKey() + "/" +userSettingsItem.getId() +"/" + ACTIVITY_FILENAME, new File(userDir, ACTIVITY_FILENAME));


    }

    private void fullCloudBackup(MetadataBuilder metadataBuilder) throws IOException {
        System.out.println("backing up to " + options.getDestBucket() + " key: " + options.getDestKey() + "/" + PROCESSED_ACTIVITY_FOLDER);

        // transfer data from S3 buckets
        s3Facade.backupS3BucketToS3(calcBucketName(ACTIVITY_BUCKET, options), options.getDestBucket(), options.getDestKey() + "/" + PROCESSED_ACTIVITY_FOLDER);
        s3Facade.backupS3BucketToS3(calcBucketName(RAW_ACTIVITY_BUCKET, options), options.getDestBucket(), options.getDestKey() + "/" + RAW_ACTIVITY_FOLDER);

        // write dynamodb contents to file and then push to S3
        File tempFile = new File(System.getProperty("java.io.tmpdir"), options.getBackupId());
        tempFile.mkdir();
        dynamoFacade.updateTableName(userTableName);
        dynamoFacade.fullTableBackup(UserSettingsItem.class, userTableName, tempFile, USER_FILENAME);
        dynamoFacade.updateTableName(activityTableName);
        dynamoFacade.fullTableBackup(ActivityItem.class,activityTableName, tempFile, ACTIVITY_FILENAME);

        s3Facade.getS3Client().putObject(options.getDestBucket(), options.getDestKey() + "/" + USER_FILENAME, new File(tempFile, USER_FILENAME));
        s3Facade.getS3Client().putObject(options.getDestBucket(), options.getDestKey() + "/" + ACTIVITY_FILENAME, new File(tempFile, ACTIVITY_FILENAME));


        //TODO -> ssm parameters


    }

    /**
     * Output transfer metrics
     *
     * @throws IOException on io error
     */
    private void writeMetrics() throws IOException {
        metrics.setDataVolumeCompressed(metrics.getDataVolumeRaw());

        metrics.setTransferElapsed(String.format("%02d min, %02d sec",
                TimeUnit.MILLISECONDS.toMinutes(options.getEndTs() - options.getStartTs()),
                TimeUnit.MILLISECONDS.toSeconds(options.getEndTs() - options.getStartTs()) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(options.getEndTs() - options.getStartTs()))
        ));
        metrics.printMetrics(System.out);
    }

    private void writeMetadata(MetadataBuilder metadataBuilder) throws IOException {
        //write metadata and push to S3
        Metadata metadata = metadataBuilder.createMetadata();
        String jsonMetadata = Jackson.toJsonPrettyString(metadata);
        File metadataFile = new File(System.getProperty("java.io.tmpdir")+"/"+options.getBackupId(), METADATA_FILENAME);
        FileUtils.writeStringToFile(jsonMetadata, new File(metadataFile, METADATA_FILENAME));
        s3Facade.getS3Client().putObject(options.getDestBucket(), options.getDestKey() + "/" + METADATA_FILENAME, metadataFile);
    }

    /**
     * Create the destination directory
     */
    private void mkCloudDestDir() {
        String destPath = options.getEnvironment() + "-" + options.getConfigMap().get("PROJECT_NAME") + "-"
                + options.getBackupDateTime().format(ISO_LOCAL_DATE_TIME_FILE);

        options.setDestination(options.getDestination().endsWith("/") ?
                options.getDestination().substring(0, options.getDestination().length() - 1) :
                options.getDestination());
        String s3Dest = options.getDestination().split("s3://")[1];
        options.setDestBucket(s3Dest.split("/")[0]);
        options.setDestKey(s3Dest.substring(s3Dest.indexOf("/") + 1) + "/" + destPath);
        options.setDestDir(new File(System.getProperty("java.io.tmpdir"), destPath));
        options.getDestDir().mkdir();
    }
}
