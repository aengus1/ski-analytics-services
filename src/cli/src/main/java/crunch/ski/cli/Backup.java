package crunch.ski.cli;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.util.json.Jackson;
import crunch.ski.cli.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;
import ski.crunch.aws.CredentialsProviderFactory;
import ski.crunch.aws.CredentialsProviderType;
import ski.crunch.utils.FileUtils;
import ski.crunch.utils.GZipUtils;

import java.io.File;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private String usersString;

    @Option(names = {"-en", "--encryption"}, description = "Encryption type: NONE (default), AES_256")
    private String encryptionType;

    @Option(names = {"-c", "--compression"}, description = "Compression type: NONE (default), GZIP, BZIP2")
    private String compressionType;

    @Parameters(index = "0")
    private String destination;

    private CredentialsProviderFactory credentialsProviderFactory;
    private AWSCredentialsProvider credentialsProvider;
    private S3Backup s3Backup;
    private DynamoBackup dynamoBackup;
    private Map<String, String> configMap;
    private String backupId;
    private boolean isS3Destination = false;
    private LocalDateTime backupDateTime;
    private List<String> users;
    private File destDir;


    private long startTs;
    private long endTs;

    public Backup() {

    }


    public Backup(App parent, CredentialsProviderFactory credentialsProviderFactory,
                  Map<String, String> configMap, String environment, String destination,
                  boolean transferAcceleration,
                  int nThreads,
                  String users,
                  String encryptionType,
                  String compressionType) {
        this.parent = parent;
        this.credentialsProviderFactory = credentialsProviderFactory;
        this.configMap = configMap;
        this.environment = environment;
        this.transferAcceleration = transferAcceleration;
        this.nThreads = nThreads;
        this.usersString = users;
        this.encryptionType = encryptionType;
        this.compressionType = compressionType;
        this.destination = destination;
    }

    @Override
    public Integer call() throws Exception {
        // pre backup checks:  //connectivity, free space on destination, permissions,
        startTs = System.currentTimeMillis();
        initialize();
        backupId = UUID.randomUUID().toString();
        backupDateTime = LocalDateTime.now();
        users = usersString == null ? null : Arrays.asList(usersString.split(","));
        System.out.println("Backing up data....");
        System.out.println("Backup ID: " + backupId);
        if (destination.startsWith("s3://")) {
            isS3Destination = true;
        }
        String destPath = environment+"-"+configMap.get("PROJECT_NAME")+"-"+backupDateTime.toString();
        destDir = new File(destination, destPath );
        MetadataBuilder metadataBuilder = buildMetadata();
        if (users == null) {
            fullLocalBackup(isS3Destination);
        } else {
            userLocalBackup(users);
        }

        endTs = System.currentTimeMillis();
        Metrics metrics = new Metrics();
        Path folder = Paths.get(destDir.getAbsolutePath());
        metrics.setDataVolumeRaw(org.apache.commons.io.FileUtils.byteCountToDisplaySize(FileUtils.getFolderSizeBytes(folder)));
        metrics.setTransferElapsed(String.valueOf((endTs - startTs / 1000l)));
        metadataBuilder.setMetrics(metrics);
        Metadata metadata = metadataBuilder.createMetadata();
        String jsonMetadata = Jackson.toJsonPrettyString(metadata);
        FileUtils.writeStringToFile(jsonMetadata, destDir);

        File compressed = new File(destDir.getAbsolutePath()+".gzip");
        GZipUtils.compressGzipFile(destDir, compressed);








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

    public void fullLocalBackup(boolean isS3Destination) throws Exception {
        File rawDir = new File(destDir, "raw_activities");
        File procDir = new File(destDir, "processed_activities");
        rawDir.mkdir();
        procDir.mkdir();
        s3Backup.backupS3BucketToTempDir(backupId, calcBucketName("activity"), procDir);
        s3Backup.backupS3BucketToTempDir(backupId, calcBucketName("raw-activity"), rawDir);
        dynamoBackup.fullTableBackup(backupId, calcTableName("userTable"), 2, destDir, "users.json");
        dynamoBackup.fullTableBackup(backupId, calcTableName("Activity"), 2, destDir, "activities.json");
    }

    public void userLocalBackup(List<String> users) throws Exception {
        for (String user : users) {
            dynamoBackup.userDataBackup(user, backupId, calcTableName("userTable"),destDir );
        }
    }

   private void initialize() {
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

    private MetadataBuilder buildMetadata() {
        MetadataBuilder metadataBuilder = new MetadataBuilder();
        metadataBuilder.setProjectName(configMap.get("PROJECT_NAME"))
                .setProfile(configMap.get("PROFILE_NAME"))
                .setDataRegion(configMap.get("DATA_REGION"))
                .setBackupId(backupId)
                .setThreads(nThreads)
                .setTransferAcceleration(transferAcceleration)
                .setTimestamp(backupDateTime)
                .setEnvironment(environment)
                .setUser(System.getProperty("user.name"));
        try {
            metadataBuilder.setHost(java.net.InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException ex) {
            metadataBuilder.setHost("unknown");
        }
        metadataBuilder.setDestination(destination)
                .setDestinationType(destination.startsWith("s3://") ? DestinationType.S3 : DestinationType.LOCAL)
                .setEncryptionType(encryptionType == null || encryptionType.equalsIgnoreCase("NONE")
                        ? EncryptionType.NONE : EncryptionType.AES_256)
                .setCompressionType(compressionType == null ? CompressionType.NONE :
                        CompressionType.valueOf(compressionType));
        return metadataBuilder;
    }

    private File createArchive(List<String> users, String destination) {
        File destinationDirectory = new File(destination, environment+"-"+configMap.get("PROJECT_NAME")+backupDateTime.toString());
        destinationDirectory.mkdir();
        if(users == null || users.isEmpty()) {
            //FULL
            File rawActivityDir = new File(destinationDirectory, "raw_activities");
            File procActivityDir = new File(destinationDirectory, "processed_activities");
            rawActivityDir.mkdir();
            procActivityDir.mkdir();
        } else {
            //PER USER
            for (String user : users) {
                File userDir = new File(destinationDirectory, user);
                userDir.mkdir();
                File rawActivityDir = new File(userDir, "raw_activities");
                File procActivityDir = new File(userDir, "processed_activities");
                rawActivityDir.mkdir();
                procActivityDir.mkdir();
            }
        }
        return destinationDirectory;
    }

    private void moveToOutDir(File tmpDir) {

    }
    private void writeMetadataFile() {

    }

}
