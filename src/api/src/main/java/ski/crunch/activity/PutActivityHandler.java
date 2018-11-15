package ski.crunch.activity;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;


import com.amazonaws.util.Base64;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;


public class PutActivityHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

    private String s3Bucket = null;
    private String region = null;
    private String activityTable = null;
    private S3Service s3 = null;
    private AWSCredentialsProvider credentialsProvider = null;
    private DynamoDBService dynamo = null;
    private ActivityService activityService = null;

    public PutActivityHandler(){
        this.s3Bucket = System.getenv("s3RawActivityBucketName");
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
        this.activityService = new ActivityService(s3, credentialsProvider, dynamo, region, s3Bucket, activityTable);
    }

    private static final Logger LOG = Logger.getLogger(PutActivityHandler.class);


    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
        LOG.debug("PutActivityHandler called");
        return activityService.saveRawActivity(input,context);
    }
}
