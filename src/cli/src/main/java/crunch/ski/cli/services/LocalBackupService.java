package crunch.ski.cli.services;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.dynamodbv2.datamodeling.S3Link;
import com.amazonaws.util.json.Jackson;
import crunch.ski.cli.model.BackupOptions;
import crunch.ski.cli.model.Metadata;
import crunch.ski.cli.model.MetadataBuilder;
import crunch.ski.cli.model.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ski.crunch.aws.CredentialsProviderFactory;
import ski.crunch.aws.CredentialsProviderType;
import ski.crunch.aws.DynamoFacade;
import ski.crunch.aws.S3Facade;
import ski.crunch.dao.ActivityDAO;
import ski.crunch.dao.UserDAO;
import ski.crunch.model.ActivityItem;
import ski.crunch.model.UserSettingsItem;
import ski.crunch.utils.ChecksumFailedException;
import ski.crunch.utils.FileUtils;
import ski.crunch.utils.GZipUtils;
import ski.crunch.utils.JsonUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class LocalBackupService implements BackupRestoreService {


    public static final Logger logger = LoggerFactory.getLogger(LocalBackupService.class);


    private BackupOptions options;
    private CredentialsProviderFactory credentialsProviderFactory;
    private AWSCredentialsProvider credentialsProvider;
    private String userTableName;
    private String activityTableName;
    private S3Facade s3Facade;
    private DynamoFacade dynamoFacade;


    public LocalBackupService(BackupOptions options) {
        this.options = options;
        this.credentialsProviderFactory = CredentialsProviderFactory.getInstance();
        credentialsProvider = credentialsProviderFactory.newCredentialsProvider(CredentialsProviderType.PROFILE,
                Optional.of(options.getConfigMap().get("PROFILE_NAME")));
        this.userTableName = calcTableName(USER_TABLE_IDENTIFIER, options);
        this.activityTableName = calcTableName(ACTIVITY_TABLE_IDENTIFIER, options);
        this.s3Facade = new S3Facade(options.getConfigMap().get("DATA_REGION"), credentialsProvider, options.isTransferAcceleration());
        this.dynamoFacade = new DynamoFacade(options.getConfigMap().get("DATA_REGION"),userTableName, credentialsProvider);
    }


    public int apply() {
        if (options.isVerbose()) {
            System.out.println("Backup ID: " + options.getBackupId());
            System.out.println("backing up data....");
        }

        MetadataBuilder metadataBuilder = buildMetadata(options);

        try {
            mkDestDir();
            if (options.getUsers() == null) {
                fullLocalBackup();
            } else {
                userLocalBackup();

            }
            writeMetadata(metadataBuilder);

            // gzip
            if (!options.isUncompressed()) {
                GZipUtils.createTarGzFile(options.getDestDir());
            }

            options.setEndTs(System.currentTimeMillis());

            //write metrics
            writeMetrics();

            // clean up
            if (!options.isUncompressed()) {
                FileUtils.deleteDirectory(options.getDestDir());
            }

        } catch (IOException ex) {
            logger.error("Error occurred attempting backup.  Exiting");
            if (options.isVerbose()) {
                ex.printStackTrace();
            }
            return 1;
        }
        return 0;
    }


    /**
     * Performs full backup to local file system
     *
     * @throws Exception on error
     */
    private void fullLocalBackup() throws IOException {
        File rawDir = new File(options.getDestDir(), RAW_ACTIVITY_FOLDER);
        File procDir = new File(options.getDestDir(), PROCESSED_ACTIVITY_FOLDER);
        rawDir.mkdir();
        procDir.mkdir();

        try {
            s3Facade.backupS3BucketToDirectory(calcBucketName(ACTIVITY_BUCKET, options), procDir);
            s3Facade.backupS3BucketToDirectory(calcBucketName(RAW_ACTIVITY_BUCKET, options), rawDir);
        } catch (ChecksumFailedException ex) {
            logger.warn("checksum failed on S3 backup", ex);
            throw new IOException(ex);
        }
        dynamoFacade.updateTableName(userTableName);
        dynamoFacade.fullTableBackup(UserSettingsItem.class, calcTableName(USER_TABLE_IDENTIFIER, options),  options.getDestDir(), USER_FILENAME);
        dynamoFacade.updateTableName(calcTableName(ACTIVITY_TABLE_IDENTIFIER, options));
        dynamoFacade.fullTableBackup(ActivityItem.class, calcTableName(ACTIVITY_TABLE_IDENTIFIER, options),  options.getDestDir(), ACTIVITY_FILENAME);

        //TODO -> backup SSM parameters
    }

    /**
     * Performs user specific backup to local file system
     *
     * @throws Exception on error
     */
    private void userLocalBackup() throws IOException {

        for (String user : options.getUsers()) {
            userDataBackup(user,userTableName, activityTableName, options.getDestDir());
        }
    }


    /**
     * Create the destination directory
     */
    private void mkDestDir() {
        String destPath = options.getEnvironment() + "-" + options.getConfigMap().get("PROJECT_NAME") + "-"
                + options.getBackupDateTime().format(ISO_LOCAL_DATE_TIME_FILE);
        options.setDestDir(new File(options.getDestination(), destPath));
        options.getDestDir().mkdir();
    }


    /**
     * Output transfer metrics
     *
     * @throws IOException on io error
     */
    private void writeMetrics() throws IOException {
        Metrics metrics = new Metrics();
        Path folder = Paths.get(options.getDestDir().getAbsolutePath());
        Path compressedFolder = Paths.get(new File(options.getDestDir() + ".tar.gz").getAbsolutePath());
        metrics.setDataVolumeRaw(org.apache.commons.io.FileUtils.byteCountToDisplaySize(FileUtils.getFolderSizeBytes(folder)));
        if (!options.isUncompressed()) {
            metrics.setDataVolumeCompressed(org.apache.commons.io.FileUtils.byteCountToDisplaySize(FileUtils.getFolderSizeBytes(compressedFolder)));
        }
        metrics.setTransferElapsed(String.format("%02d min, %02d sec",
                TimeUnit.MILLISECONDS.toMinutes(options.getEndTs() - options.getStartTs()),
                TimeUnit.MILLISECONDS.toSeconds(options.getEndTs() - options.getStartTs()) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(options.getEndTs() - options.getStartTs()))
        ));
        metrics.printMetrics(System.out);
    }

    private void writeMetadata(MetadataBuilder metadataBuilder) throws IOException {
        //write metadata
        Metadata metadata = metadataBuilder.createMetadata();
        String jsonMetadata = Jackson.toJsonPrettyString(metadata);
        FileUtils.writeStringToFile(jsonMetadata, new File(options.getDestDir(), METADATA_FILENAME));
    }

    /**
     * Performs backup from dynamodb user and activity tables of specific user to local file system
     *
     * @param user                String user to backup (email or id)
     * @param userTableName       String name of user table
     * @param activitiesTableName String name of activity table
     * @param destDir             File destination directory
     * @throws IOException on ioerror
     */
    private void userDataBackup(String user, String userTableName, String activitiesTableName, File destDir) throws IOException {

        UserDAO userDAO = new UserDAO(dynamoFacade, userTableName);
        UserSettingsItem userSettingsItem = userDAO.lookupUser(user);

        File userDestination = new File(destDir, userSettingsItem.getId());
        JsonUtils.writeJsonToFile(userSettingsItem, new File(userDestination, USER_FILENAME), false);

        ActivityDAO activityDAO = new ActivityDAO(dynamoFacade, activitiesTableName);
        List<ActivityItem> activityItems = activityDAO.getActivitiesByUser(userSettingsItem.getId());

        JsonUtils.writeJsonListToFile(activityItems, new File(userDestination, ACTIVITY_FILENAME), true);
        File rawDir = new File(userDestination, RAW_ACTIVITY_FOLDER);
        File procDir = new File(userDestination, PROCESSED_ACTIVITY_FOLDER);
        rawDir.mkdir();
        procDir.mkdir();
        for (ActivityItem activityItem : activityItems) {
            System.out.println(activityItem.getId());
            S3Link rawActivityS3Link = activityItem.getRawActivity();
            if (rawActivityS3Link != null) {
                try {
                    rawActivityS3Link.downloadTo(rawDir);
                } catch (Exception ex) {
                    logger.warn("error downloading raw activity {} from {}", rawActivityS3Link.getKey(), rawActivityS3Link.getBucketName());
                }
            }
            S3Link processedActivityS3Link = activityItem.getProcessedActivity();
            if (processedActivityS3Link != null) {
                try {
                    processedActivityS3Link.downloadTo(procDir);
                } catch (Exception ex) {
                    logger.warn("error downloading processed activity {} from {}", rawActivityS3Link.getKey(), rawActivityS3Link.getBucketName());
                }
            }
        }

    }



}
