package crunch.ski.cli;

import com.amazonaws.auth.AWSCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;
import ski.crunch.aws.CredentialsProviderFactory;
import ski.crunch.aws.CredentialsProviderType;
import ski.crunch.model.UserSettingsItem;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;


@Command(name = "backup",
        aliases = {"bck"},
        mixinStandardHelpOptions = true,
        version = "1.0.0",
        description = "Creates a backup dump of user data from a live environment to file or another S3 bucket")
public class Backup implements Callable<Integer> {

    public static final DateTimeFormatter dtf = DateTimeFormatter.ISO_DATE_TIME;
    private static Logger logger = LoggerFactory.getLogger(Backup.class);


    @ParentCommand
    private App parent;

    @Option(names = {"-e", "--env"}, required = true, description = "Name of the environment to backup.  E.g. dev, ci, prod")
    private String environment;

    @Option(names = {"-f", "--dest-dir"}, required = true, description = "Local directory to save backup to")
    private File destDir;

    @Option(names = {"-ta", "--transfer-acceleration"}, description = "Enable S3 Transfer Acceleration")
    private boolean transferAcceleration = false;

    private CredentialsProviderFactory credentialsProviderFactory;
    private AWSCredentialsProvider credentialsProvider;
    private S3Backup s3Backup;
    private DynamoBackup dynamoBackup;
    private Map<String, String> configMap;
    private String backupId;

    public Backup(){

    }


    public Backup(App parent, CredentialsProviderFactory credentialsProviderFactory,
                  Map<String, String> configMap, String environment, File destDir, boolean transferAcceleration) {
        this.parent = parent;
        this.credentialsProviderFactory = credentialsProviderFactory;
        this.configMap = configMap;
        this.environment = environment;
        this.destDir = destDir;
        this.transferAcceleration = transferAcceleration;
    }

    @Override
    public Integer call() throws Exception {
        initialize();
        System.out.println("Backing up data....");
        // pre backup checks:  //connectivity, free space on destination, permissions,
        // write backup metadata to dest -> ts, user, params, config, etc
        // individual users or full ?
        // if full....
        //  backup raw-activity bucket
        // backup processed-activity bucket
        // backup dynamodb tables
        // backup cognito data
        s3Backup.backupS3BucketToFile( backupId, calcBucketName("activity"));
        s3Backup.backupS3BucketToFile( backupId, calcBucketName("raw-activity"));
        dynamoBackup.fullTableBackup(UserSettingsItem.class,backupId, calcTableName("userTable"), 2);
        dynamoBackup.fullTableBackup(UserSettingsItem.class,backupId, calcTableName("Activity"), 2);

        return 0;
    }

    public String calcTableName(String tableType) {
        return new StringBuilder()
                .append(environment).append("-")
                .append(configMap.get("PROJECT_NAME")).append("-")
                .append(tableType)
                .toString();
    }
    public String calcBucketName(String bucketType) {
        return new StringBuilder()
                .append(environment).append("-")
                .append(bucketType).append("-")
                .append(configMap.get("PROJECT_NAME"))
                .toString();
    }

    public String getBackupId() {
        return this.backupId;
    }

    S3Backup getS3Backup() {
        return this.s3Backup;
    }

    void initialize() {
        try {
            Config config = new Config();
            configMap = config.readConfiguration();
            if(credentialsProviderFactory ==null) {
                credentialsProviderFactory = CredentialsProviderFactory.getInstance();
            }
            if(parent.getProjectName()!=null) {
                configMap.put("PROJECT_NAME", parent.getProjectName());
            }
            if(parent.getDataRegion()!=null){
                configMap.put("DATA_REGION", parent.getDataRegion());
            }
            if(parent.getAwsProfile()!=null) {
                configMap.put("PROFILE_NAME", parent.getAwsProfile());
            }
            credentialsProvider =
                    credentialsProviderFactory.newCredentialsProvider(CredentialsProviderType.PROFILE, Optional.of(configMap.get("PROFILE_NAME")));
            s3Backup = new S3Backup( configMap.get("DATA_REGION"),credentialsProvider,  transferAcceleration);
            dynamoBackup = new DynamoBackup(configMap.get("DATA_REGION"), credentialsProvider);
            //s3Facade = new S3Facade(configMap.get("DATA_REGION"), credentialsProvider, transferAcceleration);
            backupId = environment+"-"+dtf.format(LocalDateTime.now());
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("Initialization Error.  Ensure you have run crunch config", ex);
        }
    }


}
