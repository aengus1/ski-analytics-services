package ski.crunch.activity;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.apache.http.client.CredentialsProvider;
import org.apache.log4j.Logger;
import ski.crunch.activity.model.ActivityOuterClass;
import ski.crunch.activity.model.ApiGatewayResponse;
import ski.crunch.activity.parser.ActivityHolderAdapter;
import ski.crunch.activity.parser.fit.FitActivityHolderAdapter;
import ski.crunch.activity.processor.ActivityProcessor;
import ski.crunch.activity.processor.model.ActivityHolder;
import ski.crunch.activity.processor.summarizer.ActivitySummarizer;
import ski.crunch.activity.service.*;
import ski.crunch.utils.ConvertibleOutputStream;


import java.io.IOException;
import java.io.InputStream;

import java.text.ParseException;
import java.util.*;

public class ParseFitActivityLambda implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

    private static final String WEATHER_API_PARAMETER_NAME="-weather-api-key";
    private String region;
    private String s3ActivityBucket;
    private String s3RawActivityBucket;
    private S3Service s3;
    private DynamoDBService dynamo;
    private ActivityService activityService;
    private String activityTable;
    private DefaultAWSCredentialsProviderChain credentialsProvider;
    private WeatherService weatherService;
    private SSMParameterService parameterService;
    private String stage;

    private static final Logger LOG = Logger.getLogger(ParseFitActivityLambda.class);

    public ParseFitActivityLambda() {
        this.region = System.getenv("AWS_DEFAULT_REGION");
        this.s3ActivityBucket = System.getenv("s3ActivityBucketName");

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
        this.dynamo = new DynamoDBService(region,activityTable, credentialsProvider );
        this.s3RawActivityBucket = System.getenv("s3RawActivityBucketName");

        this.parameterService = new SSMParameterService(region, credentialsProvider);
        this.parameterService = new SSMParameterService(region, credentialsProvider);
        String weatherApiKey = stage+"-"+parameterService.getParameter(WEATHER_API_PARAMETER_NAME);
        this.weatherService = new DarkSkyWeatherService(weatherApiKey);
        this.activityService = new ActivityService( weatherService, s3, credentialsProvider, dynamo, region,
                s3RawActivityBucket,s3ActivityBucket, activityTable);
    }

    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {

        LOG.debug("ParseFitActivityHandler called");
        return this.activityService.processAndSaveActivity(input, context);

    }
}
