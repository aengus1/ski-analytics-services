package crunch.ski.cli.services;

import com.amazonaws.services.dynamodbv2.model.DeleteTableResult;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.simplesystemsmanagement.model.ParameterNotFoundException;
import crunch.ski.cli.model.WipeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ski.crunch.aws.CredentialsProviderFactory;
import ski.crunch.aws.DynamoFacade;
import ski.crunch.aws.S3Facade;
import ski.crunch.aws.SSMParameterFacade;

import java.util.Scanner;

public class WipeService {
    private static final Logger logger = LoggerFactory.getLogger(WipeService.class);
    private final S3Facade s3Facade;
    private final SSMParameterFacade ssmParameterFacade;
    private final WipeOptions options;

    public WipeService(WipeOptions options) {
        this.options = options;
        this.s3Facade = new S3Facade(options.getConfigMap().get("DATA_REGION"));
        this.ssmParameterFacade = new SSMParameterFacade(options.getConfigMap().get("DATA_REGION"), CredentialsProviderFactory.getDefaultCredentialsProvider());
    }
    public WipeService(S3Facade s3Facade, SSMParameterFacade ssmParameterFacade, WipeOptions options) {
        this.s3Facade = s3Facade;
        this.ssmParameterFacade = ssmParameterFacade;
        this.options = options;
    }

    public boolean wipeEnvironment() throws Exception {

        if (!options.isAutoApprove()) {
            System.out.println("Do you really want to wipe "
                    + (options.isDeploymentBucketOnly() ? " all application code " : "all data ")
                    + "from " + options.getEnvironment() + " ? (Y/n)");
            Scanner scanner = new Scanner(System.in);
            if (!scanner.nextLine().trim().equalsIgnoreCase("Y")) {
                logger.info("Aborting data wipe");
                return false;
            }
        }

        if (options.isDeploymentBucketOnly()) {
            String deploymentBucket = CliUtils.calcBucketName("deployment", options.getEnvironment(),
                    options.getConfigMap().get("PROJECT_NAME"));
            return emptyBucket(deploymentBucket);
        }

        if (options.isRemoteStateOnly()) {
            String remoteStateBucket = options.getEnvironment() + "-" + options.getConfigMap().get("PROJECT_NAME") + "-tf-backend-store";
            boolean success = emptyBucket(remoteStateBucket);

            String remoteStateTable = options.getEnvironment() + "-" + options.getConfigMap().get("PROJECT_NAME") + "-terraform-state-lock-dynamo";
            DynamoFacade dynamoFacade = new DynamoFacade("us-east-1", remoteStateTable);
            try {
                DeleteTableResult result = dynamoFacade.getTable(remoteStateTable).delete();
            } catch (Exception ex) {
                logger.info("error deleting remote state table ", ex);
                return false;
            }
            return success;
        }

        String rawActivityBucket = CliUtils.calcBucketName("raw-activity", options.getEnvironment(),
                options.getConfigMap().get("PROJECT_NAME"));

        String activityBucket = CliUtils.calcBucketName("activity", options.getEnvironment(),
                options.getConfigMap().get("PROJECT_NAME"));

        emptyBucket(rawActivityBucket);
        emptyBucket(activityBucket);


        if (!options.isForDeletionOnly()) {
            //SSM Parameters
            for (String ssmKey : BackupRestoreService.SSM_KEYS) {
                String paramName = options.getEnvironment() + "-" + ssmKey + "-api-key";
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
}