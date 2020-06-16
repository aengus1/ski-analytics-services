package crunch.ski.cli;

import com.google.common.annotations.VisibleForTesting;
import crunch.ski.cli.model.Colour;
import crunch.ski.cli.model.ModuleStatus;
import crunch.ski.cli.model.Options;
import crunch.ski.cli.services.StatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import ski.crunch.utils.NotFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "status",
        aliases = {"sts"},
        mixinStandardHelpOptions = true,
        version = "1.0.0",
        description = "Shows current deployment status of environment")
public class Status implements Callable<Integer> {

    public static final Logger logger = LoggerFactory.getLogger(Status.class);

    @CommandLine.ParentCommand
    private App parent;

    @CommandLine.Parameters(index = "0", description = "name of environment to query (e.g. dev / ci / prod)")
    private String environment;

    @CommandLine.Parameters(index = "1", defaultValue = "all", description = "name of environment module to query (e.g. data / frontend / api / app) ")
    private String module;

    private StatusService statusService;
    private Options options;

    /**
     * no arg constructor required by picocli
     */
    public Status() {
        options = new Options();
    }

    /**
     * test constructor
     * @param environment
     * @param module
     */
    public Status(String environment, String module) {
        this();
        this.environment = environment;
        this.module = module;
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
        this.statusService = new StatusService(options);
        Map<String, ModuleStatus> statusMap;

        if( module != null && !module.equalsIgnoreCase("all")) {
            try {
                ModuleStatus status = statusService.getModuleStatus(module);
                statusMap = new HashMap<>();
                statusMap.put(module, status);
            }catch(NotFoundException ex) {
                System.err.println("Module " + module + " doesn't exist");
                return 2;
            }
        } else {
            statusMap = statusService.getStatus();
        }

        printStatusMap(statusMap);

        if (statusMap.containsValue(ModuleStatus.DOWN)) {
            return 1;
        } else if (statusMap.containsValue(ModuleStatus.ERROR)) {
            return 2;
        }
        return 0;
    }

    private void printStatusMap(Map<String, ModuleStatus> statusMap) {
        System.out.print(Colour.BLACK_BOLD);
        System.out.println("   Module   |   Status   ");
        for (Map.Entry<String, ModuleStatus> entry : statusMap.entrySet()) {
            if(entry.getValue().equals(ModuleStatus.DOWN)) {
                System.out.print(Colour.RED_BOLD);
                System.out.println("   " + entry.getKey() + "   |   " + entry.getValue());
                System.out.print(Colour.RESET);
            }
            if( entry.getValue().equals(ModuleStatus.ERROR)) {
                System.out.print(Colour.YELLOW_BOLD);
                System.out.println("   " + entry.getKey() + "   |   " + entry.getValue());
                System.out.print(Colour.RESET);
            }
            if( entry.getValue().equals(ModuleStatus.UP)) {
                System.out.print(Colour.GREEN_BOLD);
                System.out.println("   " + entry.getKey() + "   |   " + entry.getValue());
                System.out.print(Colour.RESET);
            }
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

            options.setEnvironment(environment);
            options.setModule(module);
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("Initialization Error.  Ensure you have run crunch config", ex);
        }
    }
}