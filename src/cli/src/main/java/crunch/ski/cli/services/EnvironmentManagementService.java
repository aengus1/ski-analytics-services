package crunch.ski.cli.services;

import com.amazonaws.services.dynamodbv2.model.DeleteTableResult;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.simplesystemsmanagement.model.ParameterNotFoundException;
import crunch.ski.cli.model.ModuleStatus;
import crunch.ski.cli.model.StatusOptions;
import crunch.ski.cli.model.WipeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ski.crunch.aws.*;
import ski.crunch.utils.NotFoundException;

import java.util.*;

public class EnvironmentManagementService {

    public static final String[] MODULES = new String[]{"DATA", "API", "FRONTEND", "APPLICATION"};
    private static final Logger logger = LoggerFactory.getLogger(EnvironmentManagementService.class);
    private S3Facade s3Facade;
    private SSMParameterFacade ssmParameterFacade;
    private DynamoFacade dynamoFacade;
    private WipeOptions wipeOptions;
    private CloudformationFacade cloudformationFacade;
    private StatusOptions statusOptions;


    public EnvironmentManagementService(WipeOptions wipeOptions) {
        this.s3Facade = new S3Facade(wipeOptions.getRegion());
        this.ssmParameterFacade = new SSMParameterFacade(wipeOptions.getRegion(),
                CredentialsProviderFactory.getDefaultCredentialsProvider());
        this.dynamoFacade = new DynamoFacade(wipeOptions.getRegion(), null);
        this.cloudformationFacade = new CloudformationFacade();
        this.wipeOptions = wipeOptions;
    }

    public EnvironmentManagementService(StatusOptions statusOptions) {
        this.statusOptions = statusOptions;
        this.s3Facade = new S3Facade(statusOptions.getConfigMap().get("DATA_REGION"));
        this.ssmParameterFacade = new SSMParameterFacade(statusOptions.getConfigMap().get("DATA_REGION"),
                CredentialsProviderFactory.getDefaultCredentialsProvider());
        this.dynamoFacade = new DynamoFacade(statusOptions.getConfigMap().get("DATA_REGION"), null);
        this.cloudformationFacade = new CloudformationFacade();
    }

    public EnvironmentManagementService(S3Facade s3Facade, SSMParameterFacade ssmParameterFacade, DynamoFacade dynamoFacade,
                                        CloudformationFacade cloudformationFacade, StatusOptions statusOptions) {
        this.s3Facade = s3Facade;
        this.ssmParameterFacade = ssmParameterFacade;
        this.dynamoFacade = dynamoFacade;
        this.cloudformationFacade = cloudformationFacade;
        this.statusOptions = statusOptions;
    }

    public EnvironmentManagementService(S3Facade s3Facade, SSMParameterFacade ssmParameterFacade, WipeOptions wipeOptions) {
        this.s3Facade = s3Facade;
        this.ssmParameterFacade = ssmParameterFacade;
        this.wipeOptions = wipeOptions;
    }

    public boolean wipeEnvironment() throws Exception {

        if (!wipeOptions.isAutoApprove()) {
            System.out.println("Do you really want to wipe "
                    + (wipeOptions.isDeploymentBucketOnly() ? " all application code " : "all data ")
                    + "from " + wipeOptions.getEnvironment() + " ? (Y/n)");
            Scanner scanner = new Scanner(System.in);
            if (!scanner.nextLine().trim().equalsIgnoreCase("Y")) {
                logger.info("Aborting data wipe");
                return false;
            }
        }

        if (wipeOptions.isDeploymentBucketOnly()) {
            String deploymentBucket = CliUtils.calcBucketName("deployment", wipeOptions.getEnvironment(),
                    wipeOptions.getConfigMap().get("PROJECT_NAME"));
            return emptyBucket(deploymentBucket);
        }

        if (wipeOptions.isRemoteStateOnly()) {
            String remoteStateBucket = wipeOptions.getEnvironment() + "-" + wipeOptions.getConfigMap().get("PROJECT_NAME") + "-tf-backend-store";
            boolean success = emptyBucket(remoteStateBucket);

            String remoteStateTable = wipeOptions.getEnvironment() + "-" + wipeOptions.getConfigMap().get("PROJECT_NAME") + "-terraform-state-lock-dynamo";
            DynamoFacade dynamoFacade = new DynamoFacade("us-east-1", remoteStateTable);
            try {
                DeleteTableResult result = dynamoFacade.getTable(remoteStateTable).delete();
            } catch (Exception ex) {
                logger.info("error deleting remote state table ", ex);
                return false;
            }
            return success;
        }

        String rawActivityBucket = CliUtils.calcBucketName("raw-activity", wipeOptions.getEnvironment(),
                wipeOptions.getConfigMap().get("PROJECT_NAME"));

        String activityBucket = CliUtils.calcBucketName("activity", wipeOptions.getEnvironment(),
                wipeOptions.getConfigMap().get("PROJECT_NAME"));

        emptyBucket(rawActivityBucket);
        emptyBucket(activityBucket);


        if (!wipeOptions.isForDeletionOnly()) {
            //SSM Parameters
            for (String ssmKey : BackupRestoreService.SSM_KEYS) {
                String paramName = wipeOptions.getEnvironment() + "-" + ssmKey + "-api-key";
                try {
                    ssmParameterFacade.deleteParameter(paramName);
                } catch (ParameterNotFoundException ex) {
                    logger.info("Parameter {} doesn't exist", paramName);

                }
            }

            // TODO -> delete data from DynamoDB tables

//            String activityTable = CliUtils.calcTableName("activity",
//                    options.getEnvironment(), options.getConfigMap().get("PROJECT_NAME"));
//            String userTable = CliUtils.calcTableName("User",
//                    options.getEnvironment(), options.getConfigMap().get("PROJECT_NAME"));
//
//            DynamoFacade dynamoFacade = new DynamoFacade(options.getRegion(), activityTable);
//            ActivityDAO activityDAO = new ActivityDAO(dynamoFacade, activityTable);

            //TODO -> delete users from cognito
        }


        return true;
    }

    public Map<String, ModuleStatus> getStatus() {
        Map<String, ModuleStatus> result = new HashMap<>();
        for (String module : MODULES) {
            ModuleStatus status = getModuleStatus(module);
            result.put(module, status);
        }
        return result;
    }

    private boolean emptyBucket(String name) throws Exception {
        try {
            logger.info("Emptying bucket {}", name);
            s3Facade.emptyBucket(name);
        } catch (AmazonS3Exception ex) {
            logger.info(" Bucket {} doesn't exist", name);
            return false;
        }
        return true;
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
        items.add(cloudformationFacade.stackExists(CliUtils.calcStackName("api", statusOptions.getEnvironment(), statusOptions.getConfigMap().get("PROJECT_NAME"))));
        items.add(cloudformationFacade.stackExists(CliUtils.calcStackName("websocket", statusOptions.getEnvironment(), statusOptions.getConfigMap().get("PROJECT_NAME"))));
        items.add(cloudformationFacade.stackExists(CliUtils.calcStackName("graphql", statusOptions.getEnvironment(), statusOptions.getConfigMap().get("PROJECT_NAME"))));
        items.add(cloudformationFacade.stackExists(CliUtils.calcStackName("auth", statusOptions.getEnvironment(), statusOptions.getConfigMap().get("PROJECT_NAME"))));
        items.add(cloudformationFacade.stackExists(CliUtils.calcStackName("cf-bucket-notification", statusOptions.getEnvironment(), statusOptions.getConfigMap().get("PROJECT_NAME"))));
        items.add(cloudformationFacade.stackExists(CliUtils.calcStackName("cf-rockset", statusOptions.getEnvironment(), statusOptions.getConfigMap().get("PROJECT_NAME"))));
        items.add(cloudformationFacade.stackExists(CliUtils.calcStackName("cf-userpool-trigger", statusOptions.getEnvironment(), statusOptions.getConfigMap().get("PROJECT_NAME"))));
        return determineStatus(items);

    }

    private ModuleStatus getFrontendStatus() {
        //TODO
        return getDataStatus();
    }

    private ModuleStatus getApiStatus() {
        //TODO
        return getDataStatus();
    }

    private ModuleStatus getDataStatus() {

        List<Boolean> items = new ArrayList<>();
        items.add(s3Facade.bucketExists(CliUtils.calcBucketName("activity", statusOptions.getEnvironment(), statusOptions.getConfigMap().get("PROJECT_NAME"))));
        items.add(s3Facade.bucketExists(CliUtils.calcBucketName("raw-activity", statusOptions.getEnvironment(), statusOptions.getConfigMap().get("PROJECT_NAME"))));
        String activityTable = CliUtils.calcTableName("Activity", statusOptions.getEnvironment(), statusOptions.getConfigMap().get("PROJECT_NAME"));
        String userTable = CliUtils.calcTableName("User", statusOptions.getEnvironment(), statusOptions.getConfigMap().get("PROJECT_NAME"));

        dynamoFacade.updateTableName(activityTable);
        items.add(dynamoFacade.tableExists(activityTable));
        dynamoFacade.updateTableName(userTable);
        items.add(dynamoFacade.tableExists(userTable));

        items.add(ssmParameterFacade.parameterExists(CliUtils.calcSSMParameterName("weather", statusOptions.getEnvironment())));
        items.add(ssmParameterFacade.parameterExists(CliUtils.calcSSMParameterName("location", statusOptions.getEnvironment())));
        items.add(ssmParameterFacade.parameterExists(CliUtils.calcSSMParameterName("rockset", statusOptions.getEnvironment())));
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
