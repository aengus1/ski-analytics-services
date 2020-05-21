package crunch.ski.cli.services;

import com.amazonaws.services.dynamodbv2.model.DeleteTableResult;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.simplesystemsmanagement.model.ParameterNotFoundException;
import crunch.ski.cli.model.ModuleStatus;
import crunch.ski.cli.model.StatusOptions;
import crunch.ski.cli.model.WipeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ski.crunch.aws.CredentialsProviderFactory;
import ski.crunch.aws.DynamoFacade;
import ski.crunch.aws.S3Facade;
import ski.crunch.aws.SSMParameterFacade;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class EnvironmentManagementService {

    public static final String[] MODULES = new String[]{"DATA", "API", "FRONTEND", "APPLICATION"};
    private static final Logger logger = LoggerFactory.getLogger(EnvironmentManagementService.class);
    private S3Facade s3Facade;
    private SSMParameterFacade ssmParameterFacade;
    private WipeOptions wipeOptions;
    private StatusOptions statusOptions;


    public EnvironmentManagementService(WipeOptions wipeOptions) {
        this.s3Facade = new S3Facade(wipeOptions.getRegion());
        this.ssmParameterFacade = new SSMParameterFacade(wipeOptions.getRegion(),
                CredentialsProviderFactory.getDefaultCredentialsProvider());
        this.wipeOptions = wipeOptions;
    }

    public EnvironmentManagementService(StatusOptions statusOptions) {
        this.statusOptions = statusOptions;
    }

    public EnvironmentManagementService(S3Facade s3Facade, SSMParameterFacade ssmParameterFacade, WipeOptions wipeOptions) {
        this.s3Facade = s3Facade;
        this.ssmParameterFacade = ssmParameterFacade;
        this.wipeOptions = wipeOptions;
    }

    public boolean wipeEnvironment() throws Exception {

        if(!wipeOptions.isAutoApprove()) {
            System.out.println("Do you really want to wipe "
                            + (wipeOptions.isDeploymentBucketOnly() ? " all application code " : "all data ")
                    + "from " + wipeOptions.getEnvironment() + " ? (Y/n)");
            Scanner scanner = new Scanner(System.in);
            if(!scanner.nextLine().trim().equalsIgnoreCase("Y")){
                logger.info("Aborting data wipe");
                return false;
            }
        }

        if(wipeOptions.isDeploymentBucketOnly()) {
            String deploymentBucket = CliUtils.calcBucketName("deployment", wipeOptions.getEnvironment(),
                    wipeOptions.getConfigMap().get("PROJECT_NAME"));
            return emptyBucket(deploymentBucket);
        }

        if(wipeOptions.isRemoteStateOnly()) {
            String remoteStateBucket = wipeOptions.getEnvironment()+"-"+ wipeOptions.getConfigMap().get("PROJECT_NAME")+"-tf-backend-store";
            boolean success =  emptyBucket(remoteStateBucket);

            String remoteStateTable = wipeOptions.getEnvironment()+"-"+ wipeOptions.getConfigMap().get("PROJECT_NAME")+"-terraform-state-lock-dynamo";
            DynamoFacade dynamoFacade = new DynamoFacade("us-east-1",remoteStateTable);
            try {
                DeleteTableResult result = dynamoFacade.getTable(remoteStateTable).delete();
            }catch(Exception ex) {
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
                }catch(ParameterNotFoundException ex){
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

    public Map<String, ModuleStatus> getStatus()  {
        Map<String, ModuleStatus> result = new HashMap<>();
        for (String module : MODULES) {
            ModuleStatus status = getModuleStatus(module);
            result.put(module, status);
        }
        return result;
    }

    private boolean emptyBucket(String name) throws Exception{
        try {
            logger.info("Emptying bucket {}", name);
            s3Facade.emptyBucket(name);
        } catch (AmazonS3Exception ex) {
            logger.info(" Bucket {} doesn't exist", name);
            return false;
        }
        return true;
    }

    private ModuleStatus getModuleStatus(String moduleName) {
        switch (moduleName) {
            case "DATA" : {
                return getDataStatus();
            }
            case "API": {
                return getApiStatus();
            }
            case "FRONTEND": {
                return getFrontendStatus();
            }
            case "APPLICATION": {
                return getApplicationStatus();
            }
            default: {
                return null;
            }
        }
    }

    private ModuleStatus getApplicationStatus() {
        //TODO
        return null;
    }

    private ModuleStatus getFrontendStatus() {
        //TODO
        return null;
    }

    private ModuleStatus getApiStatus() {
        //TODO
        return null;
    }

    private ModuleStatus getDataStatus() {
        //TODO
        return null;
    }
}
