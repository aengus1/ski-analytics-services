package ski.crunch.activity;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.apache.log4j.Logger;
import ski.crunch.activity.service.ActivityService;
import ski.crunch.aws.DynamoDBService;
import ski.crunch.aws.S3Service;
import ski.crunch.utils.ApiGatewayResponse;

import java.util.Map;


public class PutActivityHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

    private String s3RawActivityBucket;
    private String s3ActivityBucket;
    private String region;
    private String activityTable;
    private S3Service s3;
    private AWSCredentialsProvider credentialsProvider;
    private DynamoDBService dynamo;
    private ActivityService activityService;


    public PutActivityHandler(){
        this.s3RawActivityBucket = System.getenv("s3RawActivityBucketName");
        this.s3ActivityBucket = System.getenv("s3ActivityBucketName");
        this.region = System.getenv("AWS_DEFAULT_REGION");
        this.activityTable = System.getenv("activityTable");
        this.s3 = new S3Service(region);

        try {
            this.credentialsProvider = DefaultAWSCredentialsProviderChain.getInstance();
            credentialsProvider.getCredentials();
            LOG.debug("Obtained default aws credentials");
        } catch (AmazonClientException e) {
            LOG.error("Unable to obtain default aws credentials", e);
        }
        this.dynamo = new DynamoDBService(region,activityTable, credentialsProvider );
        this.activityService = new ActivityService( s3, credentialsProvider, dynamo, region,
                s3RawActivityBucket,s3ActivityBucket, activityTable);
    }

    private static final Logger LOG = Logger.getLogger(PutActivityHandler.class);


    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {

        LOG.debug("PutActivityHandler called");
        return activityService.saveRawActivity(input,context);
    }
}
