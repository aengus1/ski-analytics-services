package crunch.ski.cli;

import com.google.common.annotations.VisibleForTesting;
import crunch.ski.cli.model.Options;
import crunch.ski.cli.services.SettingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.util.Properties;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "settings",
        aliases = {},
        mixinStandardHelpOptions = true,
        version = "1.0.0",
        description = "Provides list of connection settings for consumption by front end client")
public class Settings  implements Callable<Integer> {

    public static final Logger logger = LoggerFactory.getLogger(Settings.class);

    @CommandLine.ParentCommand
    private App parent;

    @CommandLine.Parameters(index = "0", description = "name of environment to query (e.g. dev / ci / prod)")
    private String environment;

    private SettingsService settingsService;
    private Options options;

    /**
     * no arg constructor required by picocli
     */
    public Settings() {
        options = new Options();
    }

    /**
     * test constructor
     * @param environment
     * @param module
     */
    public Settings(String environment, String module) {
        this();
        this.environment = environment;
    }

    @VisibleForTesting
    public void setOptions(Options options) {
        this.options = options;
    }

    @VisibleForTesting
    public void setParent(App app) {
        this.parent = app;
    }


    @Override
    public Integer call()  {
        initialize();
        this.settingsService = new SettingsService(options);
        try {
            printProperties(settingsService.getSettings());
            return 0;
        } catch(Exception ex) {
            ex.printStackTrace();
            return 1;
        }

    }

    public void initialize() {
        try {

            Config config = new Config();
            if (options.getConfigMap() == null) {
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

            if (parent.getProjectSourceDir() != null) {
                options.getConfigMap().put("PROJECT_SOURCE_DIR", parent.getProjectSourceDir());
            }

            options.setEnvironment(environment);

        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("Initialization Error.  Ensure you have run crunch config", ex);
        }
    }

    private void printProperties(Properties properties) {
        //TODO -> return as json
        for (Object o : properties.keySet()) {
            System.out.println(o + ":" + properties.get(o));
        }
    }
}
