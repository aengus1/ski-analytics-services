package crunch.ski.cli;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.util.json.Jackson;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import crunch.ski.cli.model.BackupType;
import crunch.ski.cli.model.Metadata;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import ski.crunch.aws.CredentialsProviderFactory;
import ski.crunch.aws.CredentialsProviderType;
import ski.crunch.aws.DynamoFacade;
import ski.crunch.model.ActivityItem;
import ski.crunch.model.UserSettingsItem;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "restore",
        aliases = {"rst"},
        mixinStandardHelpOptions = true,
        version = "1.0.0",
        description = "Restores a dump of user data to a live environment")
public class Restore implements Callable<Integer> {

    public static final Logger logger = LoggerFactory.getLogger(Restore.class);
    @CommandLine.ParentCommand
    private App parent;

    @CommandLine.Option(names = {"-a", "--transfer-acceleration"}, description = "Enable S3 Transfer Acceleration")
    private boolean transferAcceleration = false;

    @CommandLine.Option(names = {"--users"}, description = "Only restore specific user data from archive (email address or user-id, comma separated")
    private String usersString;

    @CommandLine.Option(names = {"--en"}, description = "Decryption key")
    private String decryptKey;

    @CommandLine.Option(names = {"-o", "--overwrite"}, description = "Overwrite existing data")
    private boolean overwrite;

    @CommandLine.Parameters(index = "0", description = "input file for archive to restore.  Fully qualified Local file or S3 location")
    private String backupArchive;

    @CommandLine.Parameters(index = "1", description = "name of environment to restore data to (e.g. dev / ci / prod)")
    private String environment;

    private CredentialsProviderFactory credentialsProviderFactory;
    private AWSCredentialsProvider credentialsProvider;
    private Map<String, String> configMap;
    private boolean isS3Source = false;
    private List<String> users;
    private File inputDir;
    private File archive;
    private long startTs;
    private long endTs;


    /**
     * no arg constructor required by picocli
     */
    public Restore() {

    }

    public Restore(App parent, CredentialsProviderFactory credentialsProviderFactory,
                   Map<String, String> configMap, String environment, String backupArchive,
                   boolean transferAcceleration,
                   String users,
                   String decryptKey
    ) {
        this.parent = parent;
        this.credentialsProviderFactory = credentialsProviderFactory;
        this.configMap = configMap;
        this.environment = environment;
        this.transferAcceleration = transferAcceleration;
        this.usersString = users;
        this.backupArchive = backupArchive;
        this.decryptKey = decryptKey;
    }

    @Override
    public Integer call() throws Exception {

        initialize();
        System.out.println("Restoring data....");

        // decrypt if necessary
        //determine if the archive is compressed and decompress if necessary
        if(inputDir.getName().endsWith(""))
        // archive = decrypted and decompressed file
        archive = inputDir;
        //parse metadata
        Metadata metadata = Metadata.fromArchive(new File(archive, ".metadata.json"));
        System.out.println("metadata: " + Jackson.toJsonPrettyString(metadata));
        System.out.println("Restoring backup ID: " + metadata.getBackupId() + " to " + environment);
        if (metadata.getBackupType().equals(BackupType.FULL)) {
            fullLocalRestore(metadata);

            //convert users.json to List<UserSettingsItems>
            // filter list to users if specified

            //load list to db

            //convert activities to List<ActivityItem>
            //load list to db

            //copy files to S3

            //deal with cognito
        } else {
            // for each user
        }

        return 0;
    }

    @VisibleForTesting
    String calcTableName(String tableType) {
        return new StringBuilder()
                .append(environment).append("-")
                .append(configMap.get("PROJECT_NAME")).append("-")
                .append(tableType)
                .toString();
    }

    @VisibleForTesting
    String calcBucketName(String bucketType) {
        return new StringBuilder()
                .append(environment).append("-")
                .append(bucketType).append("-")
                .append(configMap.get("PROJECT_NAME"))
                .toString();
    }


    private InjectableValues newActivityDeserializerInjectables() {
        DynamoFacade dynamoFacade = new DynamoFacade(configMap.get("DATA_REGION"), calcTableName("userTable"), CredentialsProviderFactory.getDefaultCredentialsProvider());
        return new InjectableValues.Std()
                .addValue("mapper", dynamoFacade.getMapper())
                .addValue("region", configMap.get("DATA_REGION"))
                .addValue("proc_bucket", calcBucketName("activity"))
                .addValue("raw_bucket", calcBucketName("raw-activity"));
    }

    /**
     * Performs full backup to local file system
     *
     * @throws Exception on error
     */
    private void fullLocalRestore(Metadata metadata) throws Exception {
        String usersJson = FileUtils.readFileToString(new File(archive, "users.json"), StandardCharsets.UTF_8);
        String activitiesJson = FileUtils.readFileToString(new File(archive, "activities.json"), StandardCharsets.UTF_8);
        ObjectMapper mapper = new ObjectMapper();
        List<UserSettingsItem> users = Arrays.asList(mapper.readValue(usersJson, UserSettingsItem[].class));
        List<ActivityItem> activityItems = Arrays.asList(mapper.reader(newActivityDeserializerInjectables()).forType(ActivityItem[].class).readValue(activitiesJson));

        // save to dynamo
        DynamoFacade dynamoFacade = new DynamoFacade(configMap.get("DATA_REGION"), calcTableName("userTable"),
                overwrite ? DynamoDBMapperConfig.SaveBehavior.CLOBBER : DynamoDBMapperConfig.SaveBehavior.UPDATE);

        dynamoFacade.getMapper().batchSave(users);
        dynamoFacade.updateTableName(calcTableName("Activity"));
        dynamoFacade.getMapper().batchSave(activityItems);

        // restore s3
        File rawDir = new File(archive, "raw_activities");
        File procDir = new File(archive, "processed_activities");
        for (ActivityItem activityItem : activityItems) {
            if (activityItem.getRawActivity() != null) {
                System.out.println("uploading " + activityItem.getRawActivity().getKey());
                activityItem.getRawActivity().uploadFrom(new File(rawDir, activityItem.getRawActivity().getKey()));
            }
            if (activityItem.getProcessedActivity() != null) {
                System.out.println("uploading " + activityItem.getProcessedActivity().getKey());
                activityItem.getProcessedActivity().uploadFrom(new File(procDir, activityItem.getProcessedActivity().getKey()));
            }
        }
    }

        /**
         * Parses configuration and sets up local variables
         */
        @VisibleForTesting
        void initialize () {
            try {
                startTs = System.currentTimeMillis();
                Config config = new Config();
                configMap = config.readConfiguration();
                if (credentialsProviderFactory == null) {
                    credentialsProviderFactory = CredentialsProviderFactory.getInstance();
                }
                if (parent.getProjectName() != null) {
                    configMap.put("PROJECT_NAME", parent.getProjectName());
                }
                if (parent.getDataRegion() != null) {
                    configMap.put("DATA_REGION", parent.getDataRegion());
                }
                if (parent.getAwsProfile() != null) {
                    configMap.put("PROFILE_NAME", parent.getAwsProfile());
                }
                credentialsProvider =
                        credentialsProviderFactory.newCredentialsProvider(CredentialsProviderType.PROFILE, Optional.of(configMap.get("PROFILE_NAME")));

                users = (usersString == null || usersString.isEmpty()) ? null : Arrays.asList(usersString.split(","));
                inputDir = new File(backupArchive);
            } catch (Exception ex) {
                ex.printStackTrace();
                logger.error("Initialization Error.  Ensure you have run crunch config", ex);
            }
        }
    }
