package crunch.ski.cli;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.util.json.Jackson;
import com.google.common.annotations.VisibleForTesting;
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
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.temporal.ChronoField.*;


@Command(name = "backup",
        aliases = {"bck"},
        mixinStandardHelpOptions = true,
        version = "1.0.0",
        description = "Creates a backup dump of user data from a live environment to file or another S3 bucket")
public class Backup implements Callable<Integer> {

    public static final Logger logger = LoggerFactory.getLogger(Backup.class);
    public static final DateTimeFormatter ISO_LOCAL_DATE_TIME_FILE = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(ISO_LOCAL_DATE)
            .appendLiteral('T')
            .appendValue(HOUR_OF_DAY, 2)
            .appendLiteral('-')
            .appendValue(MINUTE_OF_HOUR, 2)
            .appendLiteral('-')
            .appendValue(SECOND_OF_MINUTE, 2)
            .toFormatter(Locale.ENGLISH);

    public static final DateTimeFormatter ISO_LOCAL_DATE_TIME_NO_NANO = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(ISO_LOCAL_DATE)
            .appendLiteral('T')
            .appendValue(HOUR_OF_DAY, 2)
            .appendLiteral(':')
            .appendValue(MINUTE_OF_HOUR, 2)
            .appendLiteral(':')
            .appendValue(SECOND_OF_MINUTE, 2)
            .toFormatter(Locale.ENGLISH);


    @ParentCommand
    private App parent;

    @Option(names = {"-a", "--transfer-acceleration"}, description = "Enable S3 Transfer Acceleration")
    private boolean transferAcceleration = false;

    @Option(names = {"-t", "--threads"}, description = "Number of parallel threads to use for DynamoDB table scan")
    private int nThreads = 2;

    @Option(names = {"--users"}, description = "Limit backup to specific user data (email address or user-id, comma separated")
    private String usersString;

    @Option(names = {"-en", "--encryption"}, description = "Encryption type: NONE (default), AES_256 - not yet supported!")
    private String encryptionType;

    @Option(names = {"-u", "--uncompressed"}, description = "Do not compress the output")
    private boolean uncompressed;

    @Parameters(index = "0", description = "name of environment to backup (e.g. dev / ci / prod)")
    private String environment;

    @Parameters(index = "1", description = "destination path for backup archive.  Local file or S3 location")
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

    /**
     * no arg constructor required by picocli
     */
    public Backup() {

    }
    public Backup(App parent, CredentialsProviderFactory credentialsProviderFactory,
                  Map<String, String> configMap, String environment, String destination,
                  boolean transferAcceleration,
                  int nThreads,
                  String users,
                  String encryptionType,
                  boolean uncompressed) {
        this.parent = parent;
        this.credentialsProviderFactory = credentialsProviderFactory;
        this.configMap = configMap;
        this.environment = environment;
        this.transferAcceleration = transferAcceleration;
        this.nThreads = nThreads;
        this.usersString = users;
        this.encryptionType = encryptionType;
        this.uncompressed = uncompressed;
        this.destination = destination;
    }

    @Override
    public Integer call() throws Exception {
        // pre backup checks:  //connectivity, free space on destination, permissions,

        initialize();

        System.out.println("Backing up data....");
        System.out.println("Backup ID: " + backupId);

        mkDestDir();

        MetadataBuilder metadataBuilder = buildMetadata();

        // do backup
        if (users == null) {
            fullLocalBackup();
        } else {
            userLocalBackup(users);
        }

        //write metadata
        Metadata metadata = metadataBuilder.createMetadata();
        String jsonMetadata = Jackson.toJsonPrettyString(metadata);
        FileUtils.writeStringToFile(jsonMetadata, new File(destDir, ".metadata.json"));

        // gzip
        if(!uncompressed) {
            GZipUtils.createTarGzFile(destDir);
        }

        endTs = System.currentTimeMillis();

        //write metrics
        writeMetrics();

        // clean up
        if(!uncompressed) {
            FileUtils.deleteDirectory(destDir);
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

    public String getBackupId() {
        return this.backupId;
    }

    /**
     * Performs full backup to local file system
     * @throws Exception on error
     */
    private void fullLocalBackup() throws Exception {
        File rawDir = new File(destDir, "raw_activities");
        File procDir = new File(destDir, "processed_activities");
        rawDir.mkdir();
        procDir.mkdir();
        s3Backup.backupS3BucketToDirectory( calcBucketName("activity"), procDir);
        s3Backup.backupS3BucketToDirectory( calcBucketName("raw-activity"), rawDir);
        dynamoBackup.fullTableBackup( calcTableName("userTable"), 2, destDir, "users.json");
        dynamoBackup.fullTableBackup( calcTableName("Activity"), 2, destDir, "activities.json");
    }

    /**
     * Performs user specific backup to local file system
     * @param users List<String> user-id's or email addresses
     * @throws Exception on error
     */
    private void userLocalBackup(List<String> users) throws Exception {
        for (String user : users) {
            dynamoBackup.userDataBackup(user, calcTableName("userTable"), calcTableName("Activity"), destDir);
        }
    }

    /**
     * Parses configuration and sets up local variables
     */
    @VisibleForTesting
    void initialize() {
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
            s3Backup = new S3Backup(configMap.get("DATA_REGION"), credentialsProvider, transferAcceleration);
            dynamoBackup = new DynamoBackup(configMap.get("DATA_REGION"), credentialsProvider);
            backupId = UUID.randomUUID().toString();
            backupDateTime = LocalDateTime.now();
            users = (usersString == null || usersString.isEmpty()) ? null : Arrays.asList(usersString.split(","));
            if (destination.startsWith("s3://")) {
                isS3Destination = true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("Initialization Error.  Ensure you have run crunch config", ex);
        }
    }

    /**
     * Build metadata file for output
     * @return MetadataBuilder builder
     */
    private MetadataBuilder buildMetadata() {
        MetadataBuilder metadataBuilder = new MetadataBuilder();
        metadataBuilder.setProjectName(configMap.get("PROJECT_NAME"))
                .setProfile(configMap.get("PROFILE_NAME"))
                .setDataRegion(configMap.get("DATA_REGION"))
                .setBackupId(backupId)
                .setBackupType(users == null ? BackupType.FULL : BackupType.USER)
                .setThreads(nThreads)
                .setTransferAcceleration(transferAcceleration)
                .setTimestamp(backupDateTime.format(ISO_LOCAL_DATE_TIME_NO_NANO))
                .setEnvironment(environment)
                .setExportUsers(users)
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
                .setCompressionType(uncompressed ? CompressionType.NONE :
                        CompressionType.GZIP);
        return metadataBuilder;
    }

    /**
     * Create the destination directory
     */
    private void mkDestDir() {
        String destPath = environment + "-" + configMap.get("PROJECT_NAME") + "-" + backupDateTime.format(ISO_LOCAL_DATE_TIME_FILE);
        destDir = new File(destination, destPath);
        destDir.mkdir();
    }

    /**
     * Output transfer metrics
     * @throws IOException on io error
     */
    private void writeMetrics() throws IOException {
        Metrics metrics = new Metrics();
        Path folder = Paths.get(destDir.getAbsolutePath());
        Path compressedFolder = Paths.get(new File(destDir+".tar.gz").getAbsolutePath());
        metrics.setDataVolumeRaw(org.apache.commons.io.FileUtils.byteCountToDisplaySize(FileUtils.getFolderSizeBytes(folder)));
        if(!uncompressed) {
            metrics.setDataVolumeCompressed(org.apache.commons.io.FileUtils.byteCountToDisplaySize(FileUtils.getFolderSizeBytes(compressedFolder)));
        }
        metrics.setTransferElapsed(String.format("%02d min, %02d sec",
                TimeUnit.MILLISECONDS.toMinutes(endTs - startTs),
                TimeUnit.MILLISECONDS.toSeconds(endTs - startTs) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTs - startTs))
        ));
        metrics.printMetrics(System.out);
    }

    @VisibleForTesting
    S3Backup getS3Backup() {
        return s3Backup;
    }

}
