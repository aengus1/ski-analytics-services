package crunch.ski.cli;

import com.amazonaws.auth.AWSCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;
import ski.crunch.aws.CredentialsProviderFactory;
import ski.crunch.aws.CredentialsProviderType;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
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

    @Option(names = {"-ta", "--transfer-acceleration"}, description = "Enable S3 Transfer Acceleration")
    private boolean transferAcceleration = false;

    @Option(names = {"-t", "--threads"}, description = "Number of parallel threads to use for DynamoDB table scan")
    private int nThreads = 2;

    @Option(names = {"-u", "--users"}, description = "Limit backup to specific user data (email address of user-id, comma separated")
    private String users;

    @Parameters(index = "0")
    private String destination;

    private CredentialsProviderFactory credentialsProviderFactory;
    private AWSCredentialsProvider credentialsProvider;
    private S3Backup s3Backup;
    private DynamoBackup dynamoBackup;
    private Map<String, String> configMap;
    private String backupId;
    private boolean isS3Destination = false;

    public Backup() {

    }


    public Backup(App parent, CredentialsProviderFactory credentialsProviderFactory,
                  Map<String, String> configMap, String environment, File destDir,
                  boolean transferAcceleration,
                  int nThreads,
                  String users) {
        this.parent = parent;
        this.credentialsProviderFactory = credentialsProviderFactory;
        this.configMap = configMap;
        this.environment = environment;
        this.transferAcceleration = transferAcceleration;
        this.nThreads = nThreads;
        this.users = users;
    }

    @Override
    public Integer call() throws Exception {
        initialize();
        System.out.println("Backing up data....");
        backupId = UUID.randomUUID().toString();
        System.out.println("Backup ID: " + backupId);

        if(destination.startsWith("s3://")) {
            isS3Destination = true;
        }
        // pre backup checks:  //connectivity, free space on destination, permissions,
        // write backup metadata to dest -> ts, user, params, config, etc
        // individual users or full ?
        // if full....
        //  backup raw-activity bucket
        // backup processed-activity bucket
        // backup dynamodb tables
        // backup cognito data
        if (users == null) {
            fullBackup(isS3Destination);
        } else {
            partialBackup(users);
        }


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

    public void fullBackup(boolean isS3Destination) throws Exception {
        s3Backup.backupS3BucketToTempDir(backupId, calcBucketName("activity"));
        s3Backup.backupS3BucketToTempDir(backupId, calcBucketName("raw-activity"));
        dynamoBackup.fullTableBackup(backupId, calcTableName("userTable"), 2);
        dynamoBackup.fullTableBackup(backupId, calcTableName("Activity"), 2);
    }

    public void partialBackup(String userIds) throws Exception {
        List<String> users = Arrays.asList(userIds.split(","));
        for (String user : users) {
            // find user in user table or fail
            // create an output directory for user's backup
            // write user data to dir
            // lookup user's activities in activity table
            //  get user's files from S3
        }

    }

    void initialize() {
        try {
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
            s3Backup = new S3Backup(configMap.get("DATA_REGION"), credentialsProvider, transferAcceleration);
            dynamoBackup = new DynamoBackup(configMap.get("DATA_REGION"), credentialsProvider);
            //s3Facade = new S3Facade(configMap.get("DATA_REGION"), credentialsProvider, transferAcceleration);
            backupId = environment + "-" + dtf.format(LocalDateTime.now());
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("Initialization Error.  Ensure you have run crunch config", ex);
        }
    }


}
