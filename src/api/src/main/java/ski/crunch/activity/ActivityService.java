package ski.crunch.activity;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.util.Base64;
import org.apache.log4j.Logger;
import ski.crunch.utils.LambdaProxyConfig;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ActivityService {

    private static final Logger LOG  = Logger.getLogger(ActivityService.class);


    private String s3Bucket = null;
    private String region = null;
    private String activityTable = null;
    private S3Service s3 = null;
    private AWSCredentialsProvider credentialsProvider = null;
    private DynamoDBService dynamo = null;


    public ActivityService(S3Service s3Service, AWSCredentialsProvider credentialsProvider, DynamoDBService dynamo,
                           String region, String s3Bucket, String activityTable) {
        this.s3Bucket = s3Bucket;
        this.region = region;
        this.credentialsProvider = credentialsProvider;
        this.dynamo = dynamo;
        this.s3 = s3Service;
        this.activityTable = activityTable;

    }

        public ApiGatewayResponse saveRawActivity(Map<String, Object> input, Context context) {
             LambdaProxyConfig config = new LambdaProxyConfig(input);

            //CREATE UUID
            String activityId = UUID.randomUUID().toString();
            LOG.info("Creating Activity with ID: " + activityId);

            //Write to S3
            try {
               writeRawFileToS3(config.getBody(), activityId);
            } catch (IOException e) {
                LOG.error(e);
                e.printStackTrace();
                //todo -> return an error response if this fails
            }


            // Write metadata to activity table
            writeMetadataToActivityTable(config,activityId);

            //return response
            // Response responseBody = new Response("Go Serverless v1.x! Your function executed successfully!", input);
            Map<String, String> headers = new HashMap<>();
            headers.put("X-Powered-By", "AWS Lambda & Serverless");
            headers.put("Content-Type", "application/x-protobuf");
            //todo -> return a sensible 200 response
            return ApiGatewayResponse.builder()
                    .setStatusCode(200)
                    .setHeaders(headers)
                    .build();

        }


private void writeRawFileToS3(String bodyStr, String activityId) throws IOException {
    byte[] body = null;
    body = Base64.decode(bodyStr);
    LOG.debug("decoded raw activity base64 to binary");
    ByteArrayInputStream bais = new ByteArrayInputStream(body);
    s3.putObject(bais,s3Bucket,activityId,body.length, "rawActivity");
}

private void writeMetadataToActivityTable(LambdaProxyConfig config, String activityId) {

    LambdaProxyConfig.RequestContext.Identity identity = config.getRequestContext().getIdentity();
    ActivityItem activity =  new ActivityItem();
    activity.setId(activityId);
    activity.setUserId(identity.getEmail());
    activity.setDate(new Date(System.currentTimeMillis()));
    activity.setRawActivity(dynamo.getMapper().createS3Link(region ,activityId));
    activity.setUserAgent(identity.getUserAgent());
    activity.setSourceIp(identity.getSourceIp());
    activity.setStatus("PENDING");
    dynamo.getMapper().save(activity);
}



}
