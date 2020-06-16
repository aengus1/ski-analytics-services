package crunch.ski.cli.services;

import crunch.ski.cli.model.ModuleStatus;
import crunch.ski.cli.model.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ski.crunch.aws.*;

import java.io.File;
import java.io.IOException;
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


    public Integer provision(Options upOptions) {

        // is the specified environment defined in source tree?
        String projectSrcDir = upOptions.getConfigMap().get("PROJECT_SOURCE_DIR");
        File infraDir = new File(projectSrcDir + "/infra/envs");
        System.out.println("infradir = " + infraDir.getAbsolutePath());
        String[] envs = infraDir.list();
        if (!Arrays.stream(envs).filter(x -> x.equalsIgnoreCase(upOptions.getEnvironment())).findAny().isPresent()) {
            System.err.println("Environment " + upOptions.getEnvironment() + " configuration does not exist.  Run create first ");
            return 1;
        }


        // has the environment already been provisioned?
        Map<String, ModuleStatus> statusMap;
        if (upOptions.getModule() == null || upOptions.getModule().equalsIgnoreCase("all")) {
            statusMap = statusService.getStatus();
        } else {
            statusMap = new HashMap<>();
            statusMap.put(upOptions.getModule(), statusService.getModuleStatus(upOptions.getModule()));
        }
        if (statusMap.containsValue(ModuleStatus.UP)) {
            System.err.println("Environment " + upOptions.getEnvironment() + " already provisioned. ");
            return 1;
        }
        if (statusMap.containsValue(ModuleStatus.ERROR)) {
            System.err.println("Environment " + upOptions.getEnvironment() + " already provisioned but is in an error state.");
            return 1;
        }


        // run terraform provisioning
        if (!upOptions.getModule().equals("application")) {
            String initEnvPath = "./init_env.sh";
            String envToInit = upOptions.getEnvironment();
            String moduleToInit = upOptions.getModule();
            String[] cmdArray = moduleToInit.equals("all") ? new String[]{initEnvPath, envToInit}
                    : new String[]{initEnvPath, envToInit, moduleToInit};

            int tfProvision = startProcess(cmdArray, infraDir);
            //exit on tf failure
            if (tfProvision != 0) {
                System.err.println("Environment " + upOptions.getEnvironment() + "terraform provisioning failed");
                return 1;
            }

            statusMap = statusService.getStatus();
            statusMap.remove("APPLICATION");
            //exit on failed status check
            if (statusMap.containsValue(ModuleStatus.DOWN) || statusMap.containsValue(ModuleStatus.ERROR)) {
                System.err.println("Environment " + upOptions.getEnvironment() + " not provisioned successfully.  Aborting. ");
                return 1;
            }

        }

        // run serverless provisioning
        if (upOptions.getModule().equalsIgnoreCase("application")
                || upOptions.getModule().equalsIgnoreCase("all")) {
            File rootDir = new File(projectSrcDir);
            System.out.println("root Dir = " + rootDir.getAbsolutePath());
            String gradlewPath = "./gradlew";
            String gradleClean = "clean";
            String gradleBuild = "build";
            String gradleDeploy = "deploy";
            //NOTE there is logic in settings.gradle to exclude CLI project from build when CLI_BUILD env var is set
            //String excludeFlag = "-x";
            //String excludeModule = "cli:test";
            String stage = "-Dstage=\"" + upOptions.getEnvironment() + "\"";
            String[] cmdArray = new String[]{
                    //gradlewPath, gradleClean, gradleBuild, gradleDeploy, excludeFlag, excludeModule, stage
                    gradlewPath, gradleClean, gradleBuild, gradleDeploy, stage
            };
            int gradleProvision = startProcess(cmdArray, rootDir);

            if (gradleProvision != 0) {
                System.err.println("Environment " + upOptions.getEnvironment() + " serverless provisioning failed");
            }
        }

        statusMap = statusService.getStatus();
        if (statusMap.containsValue(ModuleStatus.DOWN) || statusMap.containsValue(ModuleStatus.ERROR)) {
            System.err.println("Environment " + upOptions.getEnvironment() + " application module not provisioned successfully.");
            return 1;
        }

        return 0;
    }

    public Integer deProvision(Options options) {

        //TODO status, backup, wipe, undeploy, tfdestroy, remove remote state, status

        //determine infrastructure directory
        String projectSrcDir = options.getConfigMap().get("PROJECT_SOURCE_DIR");
        File infraDir = new File(projectSrcDir + "/infra/envs");
        System.out.println("infradir = " + infraDir.getAbsolutePath());


        // does the environment exist?
        Map<String, ModuleStatus> statusMap;
        if (options.getModule() == null || options.getModule().equalsIgnoreCase("all")) {
            statusMap = statusService.getStatus();
        } else {
            statusMap = new HashMap<>();
            statusMap.put(options.getModule(), statusService.getModuleStatus(options.getModule()));
        }

        List<String> modulesToDeprovision = statusMap.entrySet().stream()
                .filter( e -> e.getValue().equals(ModuleStatus.UP) || e.getValue().equals(ModuleStatus.ERROR))
                .map(x -> x.getKey())
                .collect(Collectors.toList());
        if(modulesToDeprovision.isEmpty()) {
            System.err.println("Environment " + options.getEnvironment() + " already de-provisioned. ");
            return 1;
        }

        //LocalBackupService localBackupService = new LocalBackupService();


        return 0;
    }

    private int startProcess(String[] cmdArray, File directory) {
        String output = "";
        String error = "";
        Process process;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(cmdArray);
            processBuilder.environment().put("CLI_BUILD", "TRUE");
            processBuilder.directory(directory);
            processBuilder.inheritIO();
            process = processBuilder.start();
            process.waitFor();
//            InputStream is = process.getInputStream();
//            InputStream es = process.getErrorStream();
//            output = StreamUtils.convertStreamToString(is);
//            error = StreamUtils.convertStreamToString(es);
            return process.exitValue();
        } catch (IOException e) {
            e.printStackTrace();
            return 1;
        } catch (InterruptedException ex) {
            ex.printStackTrace();
            return 1;
//        } finally {
//            System.out.println(output);
//            System.err.println(error);
        }

    }

    private void checkSourceTreeDefined() {

    }


}
