package crunch.ski.cli;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.google.common.annotations.VisibleForTesting;
import crunch.ski.cli.model.RestoreOptions;
import crunch.ski.cli.services.BackupRestoreService;
import crunch.ski.cli.services.LocalRestoreService;
import crunch.ski.cli.services.S3RestoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import ski.crunch.aws.CredentialsProviderFactory;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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

    @CommandLine.Option(names = {"-v", "--verbose"}, description = "Verbose output")
    private boolean verbose;

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
    private RestoreOptions options;
    private BackupRestoreService service;


    /**
     * no arg constructor required by picocli
     */
    public Restore() {
        this.options = new RestoreOptions();
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
        if(options.isS3Source()) {
              service = new S3RestoreService(options);
        } else {
            service = new LocalRestoreService(options);
        }
        return service.apply();
    }

        /**
         * Parses configuration and sets up local variables
         */
        @VisibleForTesting
        void initialize () {
            try {
                options.setStartTs(System.currentTimeMillis());
                Config config = new Config();
                options.setConfigMap(config.readConfiguration());

                if (parent.getProjectName() != null) {
                    options.getConfigMap().put("PROJECT_NAME", parent.getProjectName());
                }
                if (parent.getDataRegion() != null) {
                    options.getConfigMap().put("DATA_REGION", parent.getDataRegion());
                }
                if (parent.getAwsProfile() != null) {
                    options.getConfigMap().put("PROFILE_NAME", parent.getAwsProfile());
                }

                options.setUsers((usersString == null || usersString.isEmpty()) ? null : Arrays.asList(usersString.split(",")));
                options.setSourceDir(new File(backupArchive));
                options.setRestoreDateTime(LocalDateTime.now());
                options.setTransferAcceleration(transferAcceleration);
                options.setEnvironment(environment);
                options.setDecryptKey(decryptKey);
                options.setRestoreId(UUID.randomUUID().toString());
                options.setVerbose(verbose);
                options.setOverwrite(overwrite);
                if (options.getBackupArchive().startsWith("s3://")) {
                    options.setS3Source(true);
                }


            } catch (Exception ex) {
                ex.printStackTrace();
                logger.error("Initialization Error.  Ensure you have run crunch config", ex);
            }
        }
    }
