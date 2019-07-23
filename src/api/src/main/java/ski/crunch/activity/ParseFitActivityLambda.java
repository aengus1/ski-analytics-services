package ski.crunch.activity;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.apache.log4j.Logger;
import ski.crunch.activity.service.*;
import ski.crunch.aws.DynamoDBService;
import ski.crunch.aws.S3Service;
import ski.crunch.aws.SSMParameterService;
import ski.crunch.utils.ApiGatewayResponse;

import java.util.Map;

public class ParseFitActivityLambda implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

    private static final String WEATHER_API_PARAMETER_NAME = "-weather-api-key";
    private static final String LOCATION_API_PARAMETER_NAME = "-location-api-key";
    private String region;
    private String s3ActivityBucket;
    private String s3RawActivityBucket;
    private S3Service s3;
    private DynamoDBService dynamo;
    private ActivityService activityService;
    private String activityTable;
    private DefaultAWSCredentialsProviderChain credentialsProvider;
    private WeatherService weatherService;
    private LocationService locationService;
    private SSMParameterService parameterService;

    private static final Logger LOG = Logger.getLogger(ParseFitActivityLambda.class);

    public ParseFitActivityLambda() {
        this.region = System.getenv("AWS_DEFAULT_REGION");
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
        this.dynamo = new DynamoDBService(region, activityTable, credentialsProvider);
        this.s3RawActivityBucket = System.getenv("s3RawActivityBucketName");

        this.parameterService = new SSMParameterService(region, credentialsProvider);
        this.parameterService = new SSMParameterService(region, credentialsProvider);
        this.activityService = new ActivityService(s3, credentialsProvider, dynamo, region,
                s3RawActivityBucket, s3ActivityBucket, activityTable);
    }

    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
        String stage = System.getenv("currentStage");
        LOG.info("stage = " + stage);

        String weatherApiKey = parameterService.getParameter(stage + WEATHER_API_PARAMETER_NAME);
        this.weatherService = new DarkSkyWeatherService(weatherApiKey);

        String locationApiKey = parameterService.getParameter(stage + LOCATION_API_PARAMETER_NAME);
        this.locationService = new LocationIqService(locationApiKey);

        LOG.debug("ParseFitActivityHandler called");
        return this.activityService.processAndSaveActivity(input, context, weatherService, locationService);

    }
}
