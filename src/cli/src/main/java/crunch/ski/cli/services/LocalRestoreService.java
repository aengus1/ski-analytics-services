package crunch.ski.cli.services;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import crunch.ski.cli.model.BackupType;
import crunch.ski.cli.model.Metadata;
import crunch.ski.cli.model.Metrics;
import crunch.ski.cli.model.RestoreOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ski.crunch.aws.CredentialsProviderFactory;
import ski.crunch.aws.DynamoFacade;
import ski.crunch.aws.S3Facade;
import ski.crunch.dao.UserDAO;
import ski.crunch.model.ActivityItem;
import ski.crunch.model.UserSettingsItem;
import ski.crunch.utils.EncryptionUtils;
import ski.crunch.utils.FileUtils;
import ski.crunch.utils.GZipUtils;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class LocalRestoreService implements BackupRestoreService {

    private static final Logger logger = LoggerFactory.getLogger(LocalRestoreService.class);
    private RestoreOptions options;
    private AWSCredentialsProvider credentialsProvider;
    private String userTableName;
    private String activityTableName;
    private S3Facade s3Facade;
    private DynamoFacade dynamoFacade;
    private UserDAO userDAO;
    private Metadata metadata;
    private Metrics metrics;


    public LocalRestoreService(RestoreOptions options) {
        this.options = options;
        this.credentialsProvider = CredentialsProviderFactory.getDefaultCredentialsProvider();
        this.userTableName = calcTableName(USER_TABLE_IDENTIFIER, options);
        this.activityTableName = calcTableName(ACTIVITY_TABLE_IDENTIFIER, options);
        this.s3Facade = new S3Facade(options.getConfigMap().get("DATA_REGION"), credentialsProvider, options.isTransferAcceleration());
        this.dynamoFacade = new DynamoFacade(options.getConfigMap().get("DATA_REGION"), userTableName, credentialsProvider,
                options.isOverwrite() ? DynamoDBMapperConfig.SaveBehavior.CLOBBER : DynamoDBMapperConfig.SaveBehavior.UPDATE);
        this.userDAO = new UserDAO(dynamoFacade, userTableName);
        this.metrics = new Metrics();
    }

    public LocalRestoreService(AWSCredentialsProvider credentialsProvider, DynamoFacade dynamoFacade, S3Facade s3Facade,
                               UserDAO userDAO, RestoreOptions options) {
        this.options = options;
        this.credentialsProvider = credentialsProvider;
        this.s3Facade = s3Facade;
        this.dynamoFacade = dynamoFacade;
        this.userDAO = userDAO;
        this.userTableName = calcTableName(USER_TABLE_IDENTIFIER, options);
        this.activityTableName = calcTableName(ACTIVITY_TABLE_IDENTIFIER, options);
        this.metrics = new Metrics();
    }

    @Override
    public int apply() {

        try {


            // does archive exist ?
            File archive = new File(options.getBackupArchive());
            if (!archive.exists()) {
                logger.error("Error.  Backup archive " + options.getBackupArchive() + " not found");
                return 1;
            }

            // does destination environment exist?
            if (!checkEnvironment()) {
                logger.error("Error. Target environment does not exist");
                return 1;
            }

            decompress();

            readMetadata();

            if (options.isVerbose()) {
                System.out.println("Restoring backup: " + metadata.getBackupId());
                System.out.println("restoring data....");
            }

            if (options.getUsers() == null && metadata.getBackupType().equals(BackupType.FULL)) {
                fullLocalRestore();
            } else if (options.getUsers() == null && metadata.getBackupType().equals(BackupType.USER)) {
                userLocalRestore(metadata.getExportUsers(), false);
            } else if (options.getUsers() != null && metadata.getBackupType().equals(BackupType.USER)) {
                HashSet<String> optionSet = Sets.newHashSet(options.getUsers());
                HashSet<String> metadataSet = Sets.newHashSet(metadata.getExportUsers());
                userLocalRestore(optionSet.stream().filter(metadataSet::contains).collect(Collectors.toList()), false);
            } else if (options.getUsers() != null && metadata.getBackupType().equals(BackupType.FULL)) {
                userLocalRestore(options.getUsers(), true);
            }


            writeMetrics();

            // clean up if necessary (e.g. remove expanded archive from temp dir)
            cleanUp();

        } catch (IOException | GeneralSecurityException ex) {
            if (ex instanceof IOException) {
                logger.error("IO Error occurred attempting restore.  Exiting", ex);

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


    private InjectableValues newActivityDeserializerInjectables() {
        DynamoFacade dynamoFacade = new DynamoFacade(options.getConfigMap().get("DATA_REGION"), userTableName, credentialsProvider);
        return new InjectableValues.Std()
                .addValue("mapper", dynamoFacade.getMapper())
                .addValue("region", options.getConfigMap().get("DATA_REGION"))
                .addValue("proc_bucket", calcBucketName(ACTIVITY_BUCKET, options))
                .addValue("raw_bucket", calcBucketName(RAW_ACTIVITY_BUCKET, options));
    }

    /**
     * Performs full backup to local file system
     *
     * @throws Exception on error
     */
    private void fullLocalRestore() throws IOException, GeneralSecurityException {

        List<UserSettingsItem> users = deserializeUserJson(options.getSourceDir());
        List<ActivityItem> activityItems = deserializeActivityJson(options.getSourceDir());

        // save to dynamo
        dynamoFacade.updateTableName(userTableName);
        dynamoFacade.getMapper().batchSave(users);
        dynamoFacade.updateTableName(activityTableName);
        dynamoFacade.getMapper().batchSave(activityItems);

        // restore s3
        File rawDir = new File(options.getSourceDir(), RAW_ACTIVITY_FOLDER);
        File procDir = new File(options.getSourceDir(), PROCESSED_ACTIVITY_FOLDER);
        // temp dir for holding decrypted data
        File tempDir = new File(System.getProperty("java.io.tmpdir"), options.getRestoreId() + "_s3");
        tempDir.mkdir();

        uploadS3Files(activityItems, rawDir, procDir, tempDir);
    }

    /**
     * Perform restore on a specific set of users
     *
     * @param users  List<String> users to restore (email addresses or user-ids)
     * @param filter boolean flag indicating that a partial restore should be performed from a FULL backup.  i.e. the backup
     *               archive should be filtered to only the users indicated in the cli options
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public void userLocalRestore(List<String> users, boolean filter) throws IOException, GeneralSecurityException {
        for (String user : users) {
            restoreUser(user, filter);
        }
    }

    private void restoreUser(String user, boolean filter) throws IOException, GeneralSecurityException {
        if (user.contains("@")) {
            UserSettingsItem userItem = userDAO.lookupUser(user);
            user = userItem.getId();
        }

        File userDir =  filter ? new File(options.getBackupArchive()) : new File(options.getBackupArchive(), user);

        UserSettingsItem users = deserializeUserJson(userDir).get(0);
        List<ActivityItem> activities = deserializeActivityJson(userDir);

        if(filter) {
            activities = activities.stream().filter(x -> x.getUserId().equals(users.getId())).collect(Collectors.toList());
        }

        // save to dynamo
        dynamoFacade.updateTableName(userTableName);
        dynamoFacade.getMapper().save(users);
        dynamoFacade.updateTableName(activityTableName);
        dynamoFacade.getMapper().batchSave(activities);

        // restore s3
        File rawDir = new File(userDir, RAW_ACTIVITY_FOLDER);
        File procDir = new File(userDir, PROCESSED_ACTIVITY_FOLDER);
        // temp dir for holding decrypted data
        File tempDir = new File(System.getProperty("java.io.tmpdir"), options.getRestoreId() + "_s3");
        tempDir.mkdir();
        File tempUserDir = new File(tempDir, user);
        tempUserDir.mkdir();

        uploadS3Files(activities, rawDir, procDir, tempUserDir);

    }

    private List<UserSettingsItem> deserializeUserJson(File inputDir) throws IOException {
        File inputFile = new File(inputDir, USER_FILENAME);
        String json = FileUtils.readFileToString(inputFile);
        if (options.getDecryptKey() != null) {
            json = EncryptionUtils.decrypt(json, options.getDecryptKey());
        }
        ObjectMapper objectMapper = new ObjectMapper();
        return Arrays.asList(objectMapper.readValue(json, UserSettingsItem[].class));
    }

    private List<ActivityItem> deserializeActivityJson(File inputDir) throws IOException {
        File inputFile = new File(inputDir, ACTIVITY_FILENAME);
        String json = FileUtils.readFileToString(inputFile);
        if (options.getDecryptKey() != null) {
            json = EncryptionUtils.decrypt(json, options.getDecryptKey());
        }
        ObjectMapper objectMapper = new ObjectMapper();
        return Arrays.asList(objectMapper.reader(newActivityDeserializerInjectables()).forType(ActivityItem[].class).readValue(json));
    }

    private void uploadS3Files(List<ActivityItem> activities, File rawDir, File procDir, File tempDir) throws IOException, GeneralSecurityException {
        for (ActivityItem activityItem : activities) {
            if (activityItem.getRawActivity() != null) {
                File rawFile = new File(rawDir, activityItem.getRawActivity().getKey());
                if (options.isVerbose()) {
                    System.out.println("uploading " + activityItem.getRawActivity().getKey());
                }
                if (options.getDecryptKey() != null) {
                    File rawTemp = new File(tempDir, activityItem.getRawActivity().getKey());
                    EncryptionUtils.copyDecrypt(rawFile, rawTemp, options.getDecryptKey());
                    activityItem.getRawActivity().uploadFrom(rawTemp);
                } else {
                    activityItem.getRawActivity().uploadFrom(rawFile);
                }
            }
            if (activityItem.getProcessedActivity() != null) {
                File procFile = new File(procDir, activityItem.getProcessedActivity().getKey());
                if (options.isVerbose()) {
                    System.out.println("uploading " + activityItem.getProcessedActivity().getKey());
                }
                if (options.getDecryptKey() != null) {
                    File procTemp = new File(tempDir, activityItem.getProcessedActivity().getKey());
                    EncryptionUtils.copyDecrypt(procFile, procTemp, options.getDecryptKey());
                    activityItem.getProcessedActivity().uploadFrom(procTemp);
                } else {
                    activityItem.getProcessedActivity().uploadFrom(procFile);
                }

            }
        }
    }

    private boolean checkEnvironment() {
        try {
            s3Facade.listObjects(calcBucketName(ACTIVITY_BUCKET, options));
        } catch (SdkClientException ex) {
            return false;
        }
        return true;
    }

    private void decompress() throws IOException {
        if (!options.getBackupArchive().endsWith(".tar.gz")) {
            return;
        }
        File tempDecompressedDir = new File(System.getProperty("java.io.tmpdir"), options.getRestoreId());
        tempDecompressedDir.mkdir();
        GZipUtils.extractTarGZ(options.getSourceDir(), tempDecompressedDir);
        options.setSourceDir(tempDecompressedDir);
    }

    private void readMetadata() throws IOException {
        File metadataFile = new File(options.getBackupArchive(), ".metadata.json");
        metadata = Metadata.fromArchive(metadataFile);
    }

    private void writeMetrics() {
        try {

            options.setEndTs(System.currentTimeMillis());
            metrics.setTransferElapsed(String.format("%02d min, %02d sec",
                    TimeUnit.MILLISECONDS.toMinutes(options.getEndTs() - options.getStartTs()),
                    TimeUnit.MILLISECONDS.toSeconds(options.getEndTs() - options.getStartTs()) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(options.getEndTs() - options.getStartTs()))
            ));
            metrics.printMetrics(System.out);
        } catch (IOException ex) {
            logger.error("IOException occurred writing metrics. Not fatal");
            if (options.isVerbose()) {
                ex.printStackTrace();
            }
        }
    }

    private void cleanUp() {
        File temp = new File(System.getProperty("java.io.tmpdir"), options.getRestoreId());
        if (temp.exists()) {
            FileUtils.deleteDirectory(temp);
        }
        File tempDir = new File(System.getProperty("java.io.tmpdir"), options.getRestoreId()+ "_s3");
        if (tempDir.exists()) {
            FileUtils.deleteDirectory(tempDir);
        }
    }

    public S3Facade getS3() {
        return s3Facade;
    }

    public DynamoFacade getDynamo() {
        return dynamoFacade;
    }
}
