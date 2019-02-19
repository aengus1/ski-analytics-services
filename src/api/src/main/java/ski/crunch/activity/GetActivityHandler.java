package ski.crunch.activity;

import java.util.*;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import org.apache.log4j.Logger;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import ski.crunch.activity.model.ApiGatewayResponse;
import ski.crunch.activity.service.*;

public class GetActivityHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

    private static final String WEATHER_API_PARAMETER_NAME="-weather-api-key";
    private String stage = null;
    private String s3Bucket = null;
    private String region = null;
    private String activityTable = null;
    private S3Service s3 = null;
    private AWSCredentialsProvider credentialsProvider = null;
    private DynamoDBService dynamo = null;
    private ActivityService activityService = null;
    private SSMParameterService parameterService = null;
    private WeatherService weatherService = null;

    private static final Logger LOG = Logger.getLogger(GetActivityHandler.class);

    public GetActivityHandler() {
        this.s3Bucket = System.getenv("s3ActivityBucketName");
        this.region = System.getenv("AWS_DEFAULT_REGION");
        this.s3 = new S3Service(region);
        String s3RawActivityBucket = System.getenv("s3RawActivityBucketName");
        this.s3Bucket = System.getenv("s3ActivityBucketName");
        this.region = System.getenv("AWS_DEFAULT_REGION");
        this.activityTable = System.getenv("activityTable");
        this.stage = System.getenv("currentStage");
        this.s3 = new S3Service(region);

        try {
            this.credentialsProvider = DefaultAWSCredentialsProviderChain.getInstance();
            credentialsProvider.getCredentials();
            LOG.debug("Obtained default aws credentials");
        } catch (AmazonClientException e) {
            LOG.error("Unable to obtain default aws credentials", e);
        }
        this.dynamo = new DynamoDBService(region, activityTable, credentialsProvider);
        this.parameterService = new SSMParameterService(region, credentialsProvider);
        String weatherApiKey = stage+"-"+parameterService.getParameter(WEATHER_API_PARAMETER_NAME);
        this.weatherService = new DarkSkyWeatherService(weatherApiKey);
        this.activityService = new ActivityService( weatherService, s3, credentialsProvider, dynamo, region,
                s3RawActivityBucket, s3Bucket, activityTable);
    }


    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
        LOG.debug("GetActivityHandler called");
        return activityService.getActivity(input, context);
    }

}
