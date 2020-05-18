package ski.crunch.activity;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ski.crunch.activity.service.ActivityService;
import ski.crunch.activity.service.WeatherService;
import ski.crunch.aws.DynamoFacade;
import ski.crunch.aws.S3Facade;
import ski.crunch.aws.SSMParameterFacade;
import ski.crunch.utils.ApiGatewayResponse;

import java.util.Map;

public class GetActivityHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

    private static final String WEATHER_API_PARAMETER_NAME="-weather-api-key";
    private String stage = null;
    private String s3Bucket = null;
    private String region = null;
    private String activityTable = null;
    private String userTable = null;
    private S3Facade s3 = null;
    private AWSCredentialsProvider credentialsProvider = null;
    private DynamoFacade dynamo = null;
    private ActivityService activityService = null;
    private SSMParameterFacade parameterService = null;
    private WeatherService weatherService = null;

    private static final Logger logger = LoggerFactory.getLogger(GetActivityHandler.class);

    public GetActivityHandler() {
        this.s3Bucket = System.getenv("s3ActivityBucketName");
        this.region = System.getenv("AWS_DEFAULT_REGION");
        this.s3 = new S3Facade(region);
        String s3RawActivityBucket = System.getenv("s3RawActivityBucketName");
        this.s3Bucket = System.getenv("s3ActivityBucketName");
        this.region = System.getenv("AWS_DEFAULT_REGION");
        this.activityTable = System.getenv("activityTable");
        this.userTable = System.getenv("userTable");
        this.stage = System.getenv("currentStage");
        this.s3 = new S3Facade(region);

        try {
            this.credentialsProvider = DefaultAWSCredentialsProviderChain.getInstance();
            credentialsProvider.getCredentials();
            logger.debug("Obtained default aws credentials");
        } catch (AmazonClientException e) {
            logger.error("Unable to obtain default aws credentials", e);
        }
        this.dynamo = new DynamoFacade(region, activityTable, credentialsProvider);
        this.parameterService = new SSMParameterFacade(region, credentialsProvider);
        this.activityService = new ActivityService( s3, credentialsProvider, dynamo, region,
                s3RawActivityBucket, s3Bucket, activityTable, userTable);
    }


    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
        logger.debug("GetActivityHandler called");
        return activityService.getActivity(input, context);
    }

}
