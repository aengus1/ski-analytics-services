package crunch.ski.cli;

import com.google.common.annotations.VisibleForTesting;
import crunch.ski.cli.model.BackupOptions;
import crunch.ski.cli.services.BackupRestoreService;
import crunch.ski.cli.services.LocalBackupService;
import crunch.ski.cli.services.S3BackupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;


@Command(name = "backup",
        aliases = {"bck"},
        mixinStandardHelpOptions = true,
        version = "1.0.0",
        description = "Creates a backup dump of user data from a live environment to file or another S3 bucket")
public class Backup implements Callable<Integer> {

    public static final Logger logger = LoggerFactory.getLogger(Backup.class);

    @ParentCommand
    private App parent;

    @Option(names = {"-a", "--transfer-acceleration"}, description = "Enable S3 Transfer Acceleration")
    private boolean transferAcceleration = false;

    @Option(names = {"-t", "--threads"}, description = "Number of parallel threads to use for DynamoDB table scan")
    private int nThreads = 2;

    @Option(names = {"--users"}, description = "Limit backup to specific user data (email address or user-id, comma separated")
    private String usersString;

    @Option(names = {"-en", "--encryption"}, description = "Encryption key")
    private String encryptionKey;

    @Option(names = {"-u", "--uncompressed"}, description = "Do not compress the output")
    private boolean uncompressed;

    @Option(names = {"-v", "--verbose"}, description = "Verbose output")
    private boolean verbose;

    @Parameters(index = "0", description = "name of environment to backup (e.g. dev / ci / prod)")
    private String environment;

    @Parameters(index = "1", description = "destination path for backup archive.  Local file or S3 location")
    private String destination;

    private BackupRestoreService backupService;
    private BackupOptions backupOptions;

    /**
     * no arg constructor required by picocli
     */
    public Backup() {
        backupOptions = new BackupOptions();
    }
    public Backup(App parent, Map<String, String> configMap, String environment, String destination,
                  boolean transferAcceleration,
                  int nThreads,
                  String users,
                  String encryptionKey,
                  boolean uncompressed) {
        this();
        this.parent = parent;
        this.environment = environment;
        this.transferAcceleration = transferAcceleration;
        this.usersString = users;
        this.encryptionKey = encryptionKey;
        this.uncompressed = uncompressed;
        this.destination = destination;
        this.backupOptions = new BackupOptions();
        backupOptions.setConfigMap(configMap);
    }

    @Override
    public Integer call() throws Exception {
        initialize();
        if(backupOptions.isS3Destination()) {
            backupService  = new S3BackupService(backupOptions);
        } else {
            backupService = new LocalBackupService(backupOptions);
        }
        return backupService.apply();
    }


    /**
     * Parses configuration and sets up local variables
     */
    @VisibleForTesting
    void initialize() {
        try {
            backupOptions.setStartTs(System.currentTimeMillis());
            Config config = new Config();
            if(backupOptions.getConfigMap() == null) {
                backupOptions.setConfigMap(config.readConfiguration());
            }
            if (parent.getProjectName() != null) {
                backupOptions.getConfigMap().put("PROJECT_NAME", parent.getProjectName());
            }
            if (parent.getDataRegion() != null) {
                backupOptions.getConfigMap().put("DATA_REGION", parent.getDataRegion());
            }
            if (parent.getAwsProfile() != null) {
                backupOptions.getConfigMap().put("PROFILE_NAME", parent.getAwsProfile());
            }

            backupOptions.setBackupId(UUID.randomUUID().toString());
            backupOptions.setBackupDateTime(LocalDateTime.now());
            backupOptions.setUsers((usersString == null || usersString.isEmpty()) ? null : Arrays.asList(usersString.split(",")));
            backupOptions.setTransferAcceleration(transferAcceleration);
            backupOptions.setEnvironment(environment);
            backupOptions.setEncryptionKey(encryptionKey);
            backupOptions.setDestination(destination);
            backupOptions.setUncompressed(uncompressed);
            backupOptions.setVerbose(verbose);
            if (destination.startsWith("s3://")) {
                backupOptions.setS3Destination(true);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("Initialization Error.  Ensure you have run crunch config", ex);
        }
    }

    public BackupOptions getOptions() {
        return this.backupOptions;
    }

    public BackupRestoreService getService() {
        return this.backupService;
    }

}
