package crunch.ski.cli.services;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.dynamodbv2.datamodeling.S3Link;
import com.amazonaws.util.json.Jackson;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.annotations.VisibleForTesting;
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
import ski.crunch.utils.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Backs up an environment to local file system
 */
public class LocalBackupService implements BackupRestoreService {

    public static final Logger logger = LoggerFactory.getLogger(LocalBackupService.class);

    private BackupOptions options;
    private AWSCredentialsProvider credentialsProvider;
    private String userTableName;
    private String activityTableName;
    private S3Facade s3Facade;
    private DynamoFacade dynamoFacade;
    private UserDAO userDAO;
    private ActivityDAO activityDAO;
    private Metrics metrics;


    public LocalBackupService(BackupOptions options) {
        this.options = options;
        CredentialsProviderFactory credentialsProviderFactory = CredentialsProviderFactory.getInstance();
        credentialsProvider = credentialsProviderFactory.newCredentialsProvider(CredentialsProviderType.PROFILE,
                Optional.of(options.getConfigMap().get("PROFILE_NAME")));
        this.userTableName = calcTableName(USER_TABLE_IDENTIFIER, options);
        this.activityTableName = calcTableName(ACTIVITY_TABLE_IDENTIFIER, options);
        this.s3Facade = new S3Facade(options.getConfigMap().get("DATA_REGION"), credentialsProvider, options.isTransferAcceleration());
        this.dynamoFacade = new DynamoFacade(options.getConfigMap().get("DATA_REGION"), userTableName, credentialsProvider);
        this.userDAO = new UserDAO(dynamoFacade, userTableName);
        this.activityDAO = new ActivityDAO(dynamoFacade, activityTableName);
        this.metrics = new Metrics();
    }

    /**
     * Test constructor
     */
    public LocalBackupService(AWSCredentialsProvider credentialsProvider, DynamoFacade dynamoFacade, S3Facade s3Facade,
                              UserDAO userDAO, ActivityDAO activityDAO, BackupOptions options) {
        this.credentialsProvider = credentialsProvider;
        this.dynamoFacade = dynamoFacade;
        this.s3Facade = s3Facade;
        this.options = options;
        this.userDAO = userDAO;
        this.activityDAO = activityDAO;
        this.userTableName = calcTableName(USER_TABLE_IDENTIFIER, options);
        this.activityTableName = calcTableName(ACTIVITY_TABLE_IDENTIFIER, options);
        this.metrics = new Metrics();
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

        } catch (IOException | GeneralSecurityException ex) {

            if (ex instanceof IOException) {
                logger.error("IO Error occurred attempting backup.  Exiting");
            }
            if (ex instanceof GeneralSecurityException) {
                logger.error("Security exception occurred", ex);
            }

            if (options.isVerbose()) {
                ex.printStackTrace();
            }
            metrics.setErrors(new String[]{ex.getMessage()});
            writeMetrics();
            return 1;
        }
        return 0;
    }


    /**
     * Performs full backup to local file system
     *
     * @throws Exception on error
     */
    @VisibleForTesting
    void fullLocalBackup() throws IOException, GeneralSecurityException {
        File rawDir = new File(options.getDestDir(), RAW_ACTIVITY_FOLDER);
        File procDir = new File(options.getDestDir(), PROCESSED_ACTIVITY_FOLDER);
        rawDir.mkdir();
        procDir.mkdir();

        try {
            s3Facade.backupS3BucketToDirectory(calcBucketName(ACTIVITY_BUCKET, options), procDir, options.isVerbose(), options.getEncryptionKey());
            s3Facade.backupS3BucketToDirectory(calcBucketName(RAW_ACTIVITY_BUCKET, options), rawDir, options.isVerbose(), options.getEncryptionKey());
        } catch (ChecksumFailedException ex) {
            logger.warn("checksum failed on S3 backup", ex);
            throw new IOException(ex);
        }
        dynamoFacade.updateTableName(userTableName);
        dynamoFacade.fullTableBackup(UserSettingsItem.class, calcTableName(USER_TABLE_IDENTIFIER, options), options.getDestDir(), USER_FILENAME, options.getEncryptionKey());
        dynamoFacade.updateTableName(calcTableName(ACTIVITY_TABLE_IDENTIFIER, options));
        dynamoFacade.fullTableBackup(ActivityItem.class, calcTableName(ACTIVITY_TABLE_IDENTIFIER, options), options.getDestDir(), ACTIVITY_FILENAME, options.getEncryptionKey());

        //TODO -> backup SSM parameters
    }

    /**
     * Performs user specific backup to local file system
     *
     * @throws Exception on error
     */
    @VisibleForTesting
    void userLocalBackup() throws IOException {

        for (String user : options.getUsers()) {
            userDataBackup(user, options.getDestDir());
        }
    }


    /**
     * Create the destination directory
     */
    @VisibleForTesting
    void mkDestDir() {
        if (!new File(options.getDestination()).exists()) {
            File f = new File(options.getDestination());
            f.mkdir();
        }
        String destPath = options.getEnvironment() + "-" + options.getConfigMap().get("PROJECT_NAME") + "-"
                + options.getBackupDateTime().format(ISO_LOCAL_DATE_TIME_FILE);
        options.setDestDir(new File(options.getDestination(), destPath));
        options.getDestDir().mkdir();
    }


    public S3Facade getS3() {
        return this.s3Facade;
    }

    public DynamoFacade getDynamo() {
        return this.dynamoFacade;
    }

    /**
     * Output transfer metrics
     *
     * @throws IOException on io error
     */
    private void writeMetrics() {
        try {
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
        } catch (IOException ex) {
            logger.error("IO Exception writing metrics.  Not fatal");
            if (options.isVerbose()) {
                ex.printStackTrace();
            }
        }
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
     * @param user    String user to backup (email or id)*
     * @param destDir File destination directory
     * @throws IOException on ioerror
     */
    @VisibleForTesting
    void userDataBackup(String user, File destDir) throws IOException {

        UserSettingsItem userSettingsItem = userDAO.lookupUser(user);
        File userDestination = new File(destDir, userSettingsItem.getId());
        userDestination.mkdir();
        String userStr = options.getEncryptionKey() == null ? userSettingsItem.toJsonString() :
                EncryptionUtils.encrypt(userSettingsItem.toJsonString(), options.getEncryptionKey());

        List<ActivityItem> activityItems = activityDAO.getActivitiesByUser(userSettingsItem.getId());

        String activitiesStr = activityItems.stream().map(x -> {
            try {
                return x.toJsonString();
            } catch (JsonProcessingException ex) {
                if (options.isVerbose()) {
                    ex.printStackTrace();
                }
                logger.error("error converting activities to Json", ex);
                return null;
            }
        }).collect(Collectors.joining(","));

        FileUtils.writeStringToFile(userStr, new File(userDestination, USER_FILENAME));
        FileUtils.writeStringToFile(options.getEncryptionKey() == null ? "[" + activitiesStr + "]"
                        : EncryptionUtils.encrypt("[" + activitiesStr + "]", options.getEncryptionKey()),
                new File(userDestination, ACTIVITY_FILENAME));


        backupS3ActivityFiles(activityItems, userSettingsItem, userDestination);

    }

    @VisibleForTesting
    void backupS3ActivityFiles(List<ActivityItem> activityItems, UserSettingsItem userSettingsItem, File userDestination) {
        File rawDir = new File(userDestination, RAW_ACTIVITY_FOLDER);
        File procDir = new File(userDestination, PROCESSED_ACTIVITY_FOLDER);
        File tempDir = new File(System.getProperty("java.io.tmpdir", userSettingsItem.getId()));
        tempDir.mkdir();
        File tempRaw = new File(tempDir, RAW_ACTIVITY_FOLDER);
        File tempProc = new File(tempDir, PROCESSED_ACTIVITY_FOLDER);
        rawDir.mkdir();
        procDir.mkdir();
        tempRaw.mkdir();
        tempProc.mkdir();

        for (ActivityItem activityItem : activityItems) {
            System.out.println(activityItem.getId());
            S3Link rawActivityS3Link = activityItem.getRawActivity();
            if (rawActivityS3Link != null) {
                try {
                    rawActivityS3Link.downloadTo(options.getEncryptionKey() == null ? rawDir : tempRaw);
                    if (options.getEncryptionKey() != null) {
                        EncryptionUtils.copyEncrypt(new File(tempRaw, rawActivityS3Link.getKey()), new File(rawDir, rawActivityS3Link.getKey()), options.getEncryptionKey());
                    }
                } catch (Exception ex) {
                    logger.warn("error downloading raw activity {} from {}", rawActivityS3Link.getKey(), rawActivityS3Link.getBucketName());
                }
            }
            S3Link processedActivityS3Link = activityItem.getProcessedActivity();
            if (processedActivityS3Link != null) {
                try {
                    processedActivityS3Link.downloadTo(options.getEncryptionKey() == null ? procDir : tempProc);
                    if (options.getEncryptionKey() != null) {
                        EncryptionUtils.copyEncrypt(new File(tempProc, processedActivityS3Link.getKey()), new File(procDir, processedActivityS3Link.getKey()), options.getEncryptionKey());
                    }
                } catch (Exception ex) {
                    logger.warn("error downloading processed activity {} from {}", rawActivityS3Link.getKey(), rawActivityS3Link.getBucketName());
                }
            }
        }

        FileUtils.deleteDirectory(tempProc);
        FileUtils.deleteDirectory(tempRaw);
        FileUtils.deleteDirectory(tempDir);
    }

}
