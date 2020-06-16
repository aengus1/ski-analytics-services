package crunch.ski.cli;

import com.google.common.annotations.VisibleForTesting;
import crunch.ski.cli.model.Options;
import crunch.ski.cli.services.EnvironmentManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.util.Map;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "up",
        mixinStandardHelpOptions = true,
        version = "1.0.0",
        description = "Provision a new environment")
public class Up implements Callable<Integer> {

    public static final Logger logger = LoggerFactory.getLogger(Up.class);

    @CommandLine.ParentCommand
    private App parent;

    @CommandLine.Option(names = {"-v", "--verbose"}, description = "Verbose output")
    private boolean verbose;

    @CommandLine.Parameters(index = "0", description = "name of environment to provision (e.g. dev / ci / prod)")
    private String environment;

    @CommandLine.Parameters(index = "1", defaultValue = "all", description = "name of module to provision (e.g. data / api / frontend / application)")
    private String module;

    private EnvironmentManagementService environmentManagementService;
    private Options upOptions;

    /**
     * no arg constructor required by picocli
     */
    public Up() {
        upOptions = new Options();
    }

    public Up(App parent, Map<String, String> configMap, String environment, String module) {
        this();
        this.parent = parent;
        this.environment = environment;
        this.module = module;
        upOptions.setConfigMap(configMap);
    }

    @Override
    public Integer call() throws Exception {
        initialize();
        environmentManagementService = new EnvironmentManagementService(upOptions);
        return environmentManagementService.provision(upOptions);

    }


    /**
     * Parses configuration and sets up local variables
     */
    @VisibleForTesting
    void initialize() {
        try {
            Config config = new Config();
            if(upOptions.getConfigMap() == null) {
                upOptions.setConfigMap(config.readConfiguration());
            }
            if (parent.getProjectName() != null) {
                upOptions.getConfigMap().put("PROJECT_NAME", parent.getProjectName());
            }
            if (parent.getDataRegion() != null) {
                upOptions.getConfigMap().put("DATA_REGION", parent.getDataRegion());
            }
            if (parent.getAwsProfile() != null) {
                upOptions.getConfigMap().put("PROFILE_NAME", parent.getAwsProfile());
            }

            if (parent.getAwsProfile() != null) {
                upOptions.getConfigMap().put("DOMAIN_NAME", parent.getAwsProfile());
            }
            if (parent.getProjectSourceDir() != null) {
                upOptions.getConfigMap().put("PROJECT_SOURCE_DIR", parent.getProjectSourceDir());
            }

            upOptions.setEnvironment(environment);
            upOptions.setModule(module == null ? "all" : module);
            upOptions.setVerbose(verbose);

        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("Initialization Error.  Ensure you have run crunch config", ex);
        }
    }

}