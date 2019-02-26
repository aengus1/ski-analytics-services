package ski.crunch.activity;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;


import org.apache.log4j.Logger;
import ski.crunch.activity.model.ApiGatewayResponse;
import ski.crunch.activity.service.*;
import ski.crunch.utils.LambdaProxyConfig;
import ski.crunch.utils.ParseException;

import java.util.*;


public class PutActivityHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

    private static final String WEATHER_API_PARAMETER_NAME="-weather-api-key";
    private String s3RawActivityBucket = null;
    private String s3ActivityBucket = null;
    private String region = null;
    private String activityTable = null;
    private S3Service s3 = null;
    private AWSCredentialsProvider credentialsProvider = null;
    private DynamoDBService dynamo = null;
    private ActivityService activityService = null;
    private SSMParameterService parameterService = null;

    public PutActivityHandler(){
        this.s3RawActivityBucket = System.getenv("s3RawActivityBucketName");
        this.s3ActivityBucket = System.getenv("s3ActivityBucketName");
        this.region = System.getenv("AWS_DEFAULT_REGION");
        // this.stage = System.getenv("stage");
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
        this.parameterService = new SSMParameterService(region, credentialsProvider);
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
