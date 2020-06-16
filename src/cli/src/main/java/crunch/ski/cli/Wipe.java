package crunch.ski.cli;

import crunch.ski.cli.model.WipeOptions;
import crunch.ski.cli.services.WipeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "wipe",
        mixinStandardHelpOptions = true,
        version = "1.0.0",
        description = "Wipes all data from an environment so it can be deleted")
public class Wipe implements Callable<Integer> {

    public static final Logger logger = LoggerFactory.getLogger(Wipe.class);

    private WipeOptions options;

    @CommandLine.ParentCommand
    private App parent;

    @CommandLine.Option(names = {"--skip-backup"}, description = "when present no backup will be taken before wiping" +
            "data from this environment")
    private boolean skipBackup = false;

    @CommandLine.Option(names = {"--auto-approve", "--Y"}, description = "skips confirmation")
    private boolean autoApprove = false;


    @CommandLine.Option(names = {"--backup-dir"}, description = "put backup in this local filesystem location")
    private String backupDir = "";

    @CommandLine.Option(names = {"--deletion-only"}, description = "only wipe the data that is required for env deletion")
    private boolean deletionOnly;

    @CommandLine.Option(names = {"--deployment-bucket-only"}, description = "only wipe the deployment bucket")
    private boolean deploymentBucket;

    @CommandLine.Option(names = {"--remote-state-only"}, description = "only wipe the terraform remote state")
    private boolean remoteState;

    @CommandLine.Parameters(index = "0", description = "the name of the environment to wipe")
    private String environment;


    public Wipe() {
        this.options = new WipeOptions();
    }
    @Override
    public Integer call() throws Exception {
        initialize();

        WipeService wipeService = new WipeService(options);
        return wipeService.wipeEnvironment() ?  0 : 1;
    }

    public void initialize() throws Exception{

        Config config = new Config();
        if(options.getConfigMap() == null) {
            options.setConfigMap(config.readConfiguration());
        }
        if (parent.getProjectName() != null) {
            options.getConfigMap().put("PROJECT_NAME", parent.getProjectName());
        }
        if (parent.getDataRegion() != null) {
            options.getConfigMap().put("DATA_REGION", parent.getDataRegion());
        }
        if (parent.getAwsProfile() != null) {
            options.getConfigMap().put("PROFILE_NAME", parent.getAwsProfile());
        }
        options.setEnvironment(environment);
        options.setRegion(options.getConfigMap().get("DATA_REGION"));
        options.setSkipBackup(skipBackup);
        options.setBackupLocation(backupDir);
        options.setForDeletionOnly(deletionOnly);
        options.setAutoApprove(autoApprove);
        options.setDeploymentBucketOnly(deploymentBucket);
        options.setRemoteStateOnly(remoteState);

    }
}
