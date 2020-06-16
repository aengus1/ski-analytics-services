package crunch.ski.cli;

import com.google.common.annotations.VisibleForTesting;
import crunch.ski.cli.model.Options;
import crunch.ski.cli.services.EnvironmentManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.util.Map;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "down",
        mixinStandardHelpOptions = true,
        version = "1.0.0",
        description = "Provision a new environment")
public class Down implements Callable<Integer> {

    public static final Logger logger = LoggerFactory.getLogger(Down.class);

    @CommandLine.ParentCommand
    private App parent;

    @CommandLine.Option(names = {"-v", "--verbose"}, description = "Verbose output")
    private boolean verbose;

    @CommandLine.Parameters(index = "0", description = "name of environment to de-provision (e.g. dev / ci / prod)")
    private String environment;

    @CommandLine.Parameters(index = "1", defaultValue = "all", description = "name of module to de-provision (e.g. data / api / frontend / application)")
    private String module;

    private EnvironmentManagementService environmentManagementService;
    private Options options;

    /**
     * no arg constructor required by picocli
     */
    public Down() {
        options = new Options();
    }

    public Down(App parent, Map<String, String> configMap, String environment, String module) {
        this();
        this.parent = parent;
        this.environment = environment;
        this.module = module;
        options.setConfigMap(configMap);
    }

    @Override
    public Integer call() throws Exception {
        initialize();
        environmentManagementService = new EnvironmentManagementService(options);
        return environmentManagementService.deProvision(options);

    }


    /**
     * Parses configuration and sets up local variables
     */
    @VisibleForTesting
    void initialize() {
        try {
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

            if (parent.getAwsProfile() != null) {
                options.getConfigMap().put("DOMAIN_NAME", parent.getAwsProfile());
            }
            if (parent.getProjectSourceDir() != null) {
                options.getConfigMap().put("PROJECT_SOURCE_DIR", parent.getProjectSourceDir());
            }

            options.setEnvironment(environment);
            options.setModule(module == null ? "all" : module);
            options.setVerbose(verbose);

        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("Initialization Error.  Ensure you have run crunch config", ex);
        }
    }

}