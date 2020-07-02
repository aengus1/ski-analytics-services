package crunch.ski.cli.services;

import com.amazonaws.services.cloudformation.model.Export;
import crunch.ski.cli.model.ModuleStatus;
import crunch.ski.cli.model.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ski.crunch.aws.*;
import ski.crunch.utils.NotFoundException;

import java.util.*;

import static crunch.ski.cli.services.BackupRestoreService.ACTIVITY_TABLE_IDENTIFIER;
import static crunch.ski.cli.services.BackupRestoreService.USER_TABLE_IDENTIFIER;

public class StatusService {


    public static final String[] MODULES = new String[]{"DATA", "API", "FRONTEND", "APPLICATION"};
    private static final Logger logger = LoggerFactory.getLogger(EnvironmentManagementService.class);
    private S3Facade s3Facade;
    private SSMParameterFacade ssmParameterFacade;
    private DynamoFacade dynamoFacade;
    private CloudformationFacade cloudformationFacade;
    private CloudfrontFacade cloudfrontFacade;
    private CognitoFacade cognitoFacade;
    private Options options;


    public StatusService(Options options) {
        this.options = options;
        this.s3Facade = new S3Facade(options.getConfigMap().get("DATA_REGION"));
        this.ssmParameterFacade = new SSMParameterFacade(options.getConfigMap().get("DATA_REGION"),
                CredentialsProviderFactory.getDefaultCredentialsProvider());
        this.dynamoFacade = new DynamoFacade(options.getConfigMap().get("DATA_REGION"), null);
        this.cloudformationFacade = new CloudformationFacade();
        this.cloudfrontFacade = new CloudfrontFacade(options.getConfigMap().get("DATA_REGION"));
        this.cognitoFacade = new CognitoFacade(options.getConfigMap().get("DATA_REGION"));
    }

    public StatusService(S3Facade s3Facade, SSMParameterFacade ssmParameterFacade, DynamoFacade dynamoFacade,
                                        CloudformationFacade cloudformationFacade, CloudfrontFacade cloudfrontFacade,
                                        CognitoFacade cognitoFacade, Options options) {
        this.s3Facade = s3Facade;
        this.ssmParameterFacade = ssmParameterFacade;
        this.dynamoFacade = dynamoFacade;
        this.cloudformationFacade = cloudformationFacade;
        this.options = options;
        this.cloudfrontFacade = cloudfrontFacade;
        this.cognitoFacade = cognitoFacade;
    }

    public Map<String, ModuleStatus> getStatus() {
        Map<String, ModuleStatus> result = new HashMap<>();
        for (String module : MODULES) {
            ModuleStatus status = getModuleStatus(module);
            result.put(module, status);
        }
        return result;
    }



    public ModuleStatus getModuleStatus(String moduleName) throws NotFoundException {
        switch (moduleName) {
            case "DATA":
            case "data": {
                return getDataStatus();
            }
            case "API":
            case "api": {
                return getApiStatus();
            }
            case "FRONTEND":
            case "frontend": {
                return getFrontendStatus();
            }
            case "APPLICATION":
            case "application": {
                return getApplicationStatus();
            }
            default: {
                throw new NotFoundException("module not found");
            }
        }
    }

    private ModuleStatus getApplicationStatus() {
        List<Boolean> items = new ArrayList<>();
        items.add(cloudformationFacade.stackExists(CliUtils.calcStackName("api", options.getEnvironment(), options.getConfigMap().get("PROJECT_NAME"))));
        items.add(cloudformationFacade.stackExists(CliUtils.calcStackName("websocket", options.getEnvironment(), options.getConfigMap().get("PROJECT_NAME"))));
        items.add(cloudformationFacade.stackExists(CliUtils.calcStackName("graphql", options.getEnvironment(), options.getConfigMap().get("PROJECT_NAME")),options.getConfigMap().get("SECONDARY_REGION")));
        items.add(cloudformationFacade.stackExists(CliUtils.calcStackName("auth", options.getEnvironment(), options.getConfigMap().get("PROJECT_NAME"))));
        items.add(cloudformationFacade.stackExists(CliUtils.calcStackName("cf-bucket-notif", options.getEnvironment(), options.getConfigMap().get("PROJECT_NAME"))));
        items.add(cloudformationFacade.stackExists(CliUtils.calcStackName("cf-rockset", options.getEnvironment(), options.getConfigMap().get("PROJECT_NAME"))));
        items.add(cloudformationFacade.stackExists(CliUtils.calcStackName("cf-userpool-trg", options.getEnvironment(), options.getConfigMap().get("PROJECT_NAME"))));
        return determineStatus(items);

    }

    private ModuleStatus getFrontendStatus() {
        logger.info("Checking Frontend Module Status:");
        List<Boolean> items = new ArrayList<>();

        boolean hostingBucket = s3Facade.bucketExists(options.getEnvironment() + "-app." + options.getConfigMap().get("DOMAIN_NAME"));
        items.add(hostingBucket);
        logger.info("hosting bucket: {}", hostingBucket);

        boolean cfDistroSSM = ssmParameterFacade.parameterExists(options.getEnvironment() + "-cfdistro-name");
        logger.info("hosting cf distrossm: {}", cfDistroSSM);
        items.add(cfDistroSSM);

        boolean cfBucketSSM = ssmParameterFacade.parameterExists(options.getEnvironment() + "-app-bucket-name");
        logger.info("bucket cf distrossm: {}", cfBucketSSM);
        items.add(cfBucketSSM);

        boolean cfDistro = cfDistroSSM && cloudfrontFacade.cfDistroExists(
                ssmParameterFacade.getParameter(options.getEnvironment() + "-app-cfdistro-id")
        );
        logger.info("hosting cf distro: {}", cfDistro);
        items.add(cfDistro);

        return determineStatus(items);
    }

    private ModuleStatus getApiStatus() {
        logger.info("Checking API Module Status:");
        List<Boolean> items = new ArrayList<>();

        //todo -> implement
        //##                Userpool Domain
        //##                Api Domain
        //##                Websocket domain (+regional acm cert)
        //##                GraphQL domain (todo)
        //##                Cloudformation stack to export variables to Serverless

        //cognitoFacade.userPoolHasDomainNameConfigured()
        return getFrontendStatus();

    }

    private ModuleStatus getDataStatus() {
        logger.info("Checking Data Module Status:");
        List<Boolean> items = new ArrayList<>();

        boolean activityBucket = s3Facade.bucketExists(CliUtils.calcBucketName("activity", options.getEnvironment(), options.getConfigMap().get("PROJECT_NAME")));
        items.add(activityBucket);
        logger.info("activity bucket: {}", activityBucket);

        boolean rawActivityBucket = s3Facade.bucketExists(CliUtils.calcBucketName("raw-activity", options.getEnvironment(), options.getConfigMap().get("PROJECT_NAME")));
        items.add(rawActivityBucket);
        logger.info("raw activity bucket: {}", activityBucket);

        String activityTable = CliUtils.calcTableName(ACTIVITY_TABLE_IDENTIFIER, options.getEnvironment(), options.getConfigMap().get("PROJECT_NAME"));
        String userTable = CliUtils.calcTableName(USER_TABLE_IDENTIFIER, options.getEnvironment(), options.getConfigMap().get("PROJECT_NAME"));

        dynamoFacade.updateTableName(activityTable);
        boolean activityTableExists = dynamoFacade.tableExists(activityTable);
        items.add(activityTableExists);
        logger.info("activity table {} : {}", activityTable, activityTableExists);

        dynamoFacade.updateTableName(userTable);
        boolean userTableExists = dynamoFacade.tableExists(userTable);
        items.add(userTableExists);
        logger.info("user table {} : {}", userTable, userTableExists);

        boolean weatherSSM = ssmParameterFacade.parameterExists(CliUtils.calcSSMParameterName("weather", options.getEnvironment()));
        boolean locationSSM = ssmParameterFacade.parameterExists(CliUtils.calcSSMParameterName("location", options.getEnvironment()));
        //boolean rocksetSSM = ssmParameterFacade.parameterExists(CliUtils.calcSSMParameterName("rockset", options.getEnvironment()));
        items.add(weatherSSM);
        items.add(locationSSM);
        logger.info("SSM -> weather: {}, location: {}", weatherSSM, locationSSM);

        String cfVarStackName = options.getEnvironment() + "-" + options.getConfigMap().get("PROJECT_NAME") + "-data-var-stack";
        boolean cfVarStack = cloudformationFacade.stackExists(cfVarStackName);
        items.add(cfVarStack);
        logger.info("cloudformation variable stack: {}", cfVarStack);

        List<Export> res = cloudformationFacade.getExportedOutputs().get();
        Optional<Export> extractUserPoolId = res.stream().filter(x -> x.getExportingStackId().contains(cfVarStackName))
                .filter(x -> x.getName().equalsIgnoreCase("UserPoolArn" + options.getEnvironment()))
                .findFirst();
        if(extractUserPoolId.isPresent()) {
            String userPoolId = extractUserPoolId.get().getValue().split(":userpool/")[1];
            boolean cognitoPool = extractUserPoolId.isPresent() && cognitoFacade.userPoolExists(userPoolId);
            if (cognitoPool) {
                logger.info("has userpool domain: {}", cognitoFacade.userPoolHasDomainNameConfigured(userPoolId));
            }
            items.add(cognitoPool);
            logger.info("cognito user pool: {}", cognitoPool);
        } else {
            items.add(false);
        }


        return determineStatus(items);
    }

    private ModuleStatus determineStatus(List<Boolean> items) {
        boolean min = items.stream().min(Boolean::compareTo).get();
        boolean max = items.stream().max(Boolean::compareTo).get();
        if (min) {
            return ModuleStatus.UP;
        } else if (max) {
            return ModuleStatus.ERROR;
        } else {
            return ModuleStatus.DOWN;
        }
    }
}
