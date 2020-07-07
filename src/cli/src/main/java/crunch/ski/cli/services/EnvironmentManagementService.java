package crunch.ski.cli.services;

import crunch.ski.cli.model.ModuleStatus;
import crunch.ski.cli.model.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ski.crunch.aws.*;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EnvironmentManagementService {

    public static final String[] MODULES = new String[]{"DATA", "API", "FRONTEND", "APPLICATION"};
    private static final Logger logger = LoggerFactory.getLogger(EnvironmentManagementService.class);
    private S3Facade s3Facade;
    private SSMParameterFacade ssmParameterFacade;
    private DynamoFacade dynamoFacade;
    private CloudformationFacade cloudformationFacade;
    private CloudfrontFacade cloudfrontFacade;
    private CognitoFacade cognitoFacade;
    private StatusService statusService;
    private Options options;


    public EnvironmentManagementService(Options options) {
        this.options = options;
        this.s3Facade = new S3Facade(options.getConfigMap().get("DATA_REGION"));
        this.ssmParameterFacade = new SSMParameterFacade(options.getConfigMap().get("DATA_REGION"),
                CredentialsProviderFactory.getDefaultCredentialsProvider());
        this.dynamoFacade = new DynamoFacade(options.getConfigMap().get("DATA_REGION"), null);
        this.cloudformationFacade = new CloudformationFacade();
        this.cloudfrontFacade = new CloudfrontFacade(options.getConfigMap().get("DATA_REGION"));
        this.cognitoFacade = new CognitoFacade(options.getConfigMap().get("DATA_REGION"));
        this.statusService = new StatusService(options);
    }

    public EnvironmentManagementService(S3Facade s3Facade, SSMParameterFacade ssmParameterFacade, DynamoFacade dynamoFacade,
                                        CloudformationFacade cloudformationFacade, CloudfrontFacade cloudfrontFacade,
                                        CognitoFacade cognitoFacade, Options options) {
        this.s3Facade = s3Facade;
        this.ssmParameterFacade = ssmParameterFacade;
        this.dynamoFacade = dynamoFacade;
        this.cloudformationFacade = cloudformationFacade;
        this.options = options;
        this.cloudfrontFacade = cloudfrontFacade;
        this.cognitoFacade = cognitoFacade;
        this.statusService = new StatusService(options);
    }


    public Integer provision(Options options) {
        ProcessRunner processRunner = new ProcessRunner();
        // is the specified environment defined in source tree?
        String projectSrcDir = options.getConfigMap().get("PROJECT_SOURCE_DIR");
        File infraDir = new File(projectSrcDir + "/infra/envs");
        System.out.println("infradir = " + infraDir.getAbsolutePath());
        String[] envs = infraDir.list();
        if (!Arrays.stream(envs).filter(x -> x.equalsIgnoreCase(options.getEnvironment())).findAny().isPresent()) {
            System.err.println("Environment " + options.getEnvironment() + " configuration does not exist.  Run create first ");
            return 1;
        }



        Map<String, ModuleStatus> statusMap = getModuleStatuses(options);
        // error if environment has already been provisioned
        if (statusMap.containsValue(ModuleStatus.UP)) {
            logger.error("Environment {} already provisioned. ", options.getEnvironment());
            return 1;
        }
        //error if environment is in an error state already
        if (statusMap.containsValue(ModuleStatus.ERROR)) {
            logger.error("Environment {} already provisioned but is in an error state", options.getEnvironment());
            return 1;
        }


        // run terraform provisioning
        if (!options.getModule().equals("application")) {
            String initEnvPath = "./init_env.sh";
            String envToInit = options.getEnvironment();
            String moduleToInit = options.getModule();
            String[] cmdArray = moduleToInit.equals("all") ? new String[]{initEnvPath, envToInit}
                    : new String[]{initEnvPath, envToInit, moduleToInit};

            int tfProvision = processRunner.startProcess(cmdArray, infraDir, true);
            //exit on tf failure
            if (tfProvision != 0) {
                System.err.println("Environment " + options.getEnvironment() + "terraform provisioning failed");
                return 1;
            }

            statusMap = statusService.getStatus();
            statusMap.remove("APPLICATION");
            //exit on failed status check
            if (statusMap.containsValue(ModuleStatus.DOWN) || statusMap.containsValue(ModuleStatus.ERROR)) {
                System.err.println("Environment " + options.getEnvironment() + " not provisioned successfully.  Aborting. ");
                return 1;
            }

        }

        // run serverless provisioning
        if (options.getModule().equalsIgnoreCase("application")
                || options.getModule().equalsIgnoreCase("all")) {
            File rootDir = new File(projectSrcDir);
            System.out.println("root Dir = " + rootDir.getAbsolutePath());
            String gradlewPath = "./gradlew";
            String gradleClean = "clean";
            String gradleBuild = "build";
            String gradleDeploy = "deploy";
            //NOTE there is logic in settings.gradle to exclude CLI project from build when CLI_BUILD env var is set
            //String excludeFlag = "-x";
            //String excludeModule = "cli:test";
            String stage = "-Dstage=\"" + options.getEnvironment() + "\"";
            String[] cmdArray = new String[]{
                    //gradlewPath, gradleClean, gradleBuild, gradleDeploy, excludeFlag, excludeModule, stage
                    gradlewPath, gradleClean, gradleBuild, gradleDeploy, stage
            };
            int gradleProvision = processRunner.startProcess(cmdArray, rootDir, true);

            if (gradleProvision != 0) {
                System.err.println("Environment " + options.getEnvironment() + " serverless provisioning failed");
            }
        }

        statusMap = statusService.getStatus();
        if (statusMap.containsValue(ModuleStatus.DOWN) || statusMap.containsValue(ModuleStatus.ERROR)) {
            System.err.println("Environment " + options.getEnvironment() + " application module not provisioned successfully.");
            return 1;
        }

        return 0;
    }

    //TODO.  if a specific module is specified, then also delete modules that depend on it
    public Integer deProvision(Options options) {
        ProcessRunner processRunner = new ProcessRunner();
        logger.info("De provisioning environment {} with modules {}", options.getEnvironment(), options.getModule());

        //determine infrastructure directory
        String projectSrcDir = options.getConfigMap().get("PROJECT_SOURCE_DIR");
        File infraDir = new File(projectSrcDir + "/infra/envs");
        logger.info("Infra directory: {}", infraDir.getAbsolutePath());

        // get status of environment's modules
        Map<String, ModuleStatus> statusMap = getModuleStatuses(options);

        // filter out modules that are not currently provisioned
        List<String> modulesToDeprovision = statusMap.entrySet().stream()
                .filter( e -> e.getValue().equals(ModuleStatus.UP) || e.getValue().equals(ModuleStatus.ERROR))
                .map(x -> x.getKey())
                .collect(Collectors.toList());

        //error if no provisioned modules
        if(modulesToDeprovision.isEmpty()) {
            logger.error("Environment {} already de-provisioned. ", options.getEnvironment());
            return 1;
        }

        // run serverless de-provisioning first
        if (options.getModule().equalsIgnoreCase("application")
                || options.getModule().equalsIgnoreCase("all")) {

            try {
                //graphql module is not deleting this role.  I don't know why.
                IAMFacade iamFacade = new IAMFacade(options.getConfigMap().get("DATA_REGION"));
                iamFacade.deleteRole(options.getEnvironment() + "-appsync-dynamodb-role");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            File rootDir = new File(projectSrcDir);
            System.out.println("root Dir = " + rootDir.getAbsolutePath());
            String gradlewPath = "./gradlew";
            String gradleUndeploy = "undeploy";
            //NOTE there is logic in settings.gradle to exclude CLI project from build when CLI_BUILD env var is set
            //String excludeFlag = "-x";
            //String excludeModule = "cli:test";
            String stage = "-Dstage=\"" + options.getEnvironment() + "\"";
            String[] cmdArray = new String[]{
                    gradlewPath, gradleUndeploy, stage
            };

            int gradleDeProvision = processRunner.startProcess(cmdArray, rootDir, true);

            if (gradleDeProvision != 0) {
               logger.error("Environment {} serverless de-provisioning failed. Gradle undeploy returned an error code."
                       , options.getEnvironment());
               return 1;
            }

            //ensure application module is now down
            statusMap = getModuleStatuses(options);
            if(!statusMap.get("APPLICATION").equals(ModuleStatus.DOWN)) {
                logger.error("Environment {} application module not de-provisioned successfully. Status check shows {} ",
                        options.getEnvironment(), statusMap.get("APPLICATION"));
                return 1;
            }

        }


        // run terraform de-provisioning
        if (!options.getModule().equals("application")) {
            String destroyEnvPath = "./destroy_env.sh";
            String envToDestroy = options.getEnvironment();
            String moduleToDestroy = options.getModule();
            String[] cmdArray = moduleToDestroy.equals("all") ? new String[]{destroyEnvPath, envToDestroy}
                    : new String[]{destroyEnvPath, envToDestroy, moduleToDestroy};

            int tfDeProvision = processRunner.startProcess(cmdArray, infraDir, true);

            //exit on tf failure
            if (tfDeProvision != 0) {
               logger.error("Environment {} terraform de-provisioning returned error code", options.getEnvironment());
                return 1;
            }

            statusMap = getModuleStatuses(options);
            statusMap.remove("APPLICATION");
            //exit on failed status check
            if (moduleToDestroy.equals("all") && statusMap.containsValue(ModuleStatus.UP) || statusMap.containsValue(ModuleStatus.ERROR)) {
                logger.error("Environment {} not de-provisioned successfully. Status check failed", options.getEnvironment());
                return 1;
            }

            if (!moduleToDestroy.equals("all") && !statusMap.get(moduleToDestroy).equals(ModuleStatus.DOWN)) {
                logger.error("Environment {} not de-provisioned successfully. Status check failed", options.getEnvironment());
                return 1;
            }
        }

        return 0;
    }

    private Map<String, ModuleStatus> getModuleStatuses(Options options) {
        Map<String, ModuleStatus> statusMap;
        if (options.getModule() == null || options.getModule().equalsIgnoreCase("all")) {
            statusMap = statusService.getStatus();
        } else {
            statusMap = new HashMap<>();
            statusMap.put(options.getModule(), statusService.getModuleStatus(options.getModule()));
        }
        return statusMap;
    }


    private void checkSourceTreeDefined() {

    }


}
