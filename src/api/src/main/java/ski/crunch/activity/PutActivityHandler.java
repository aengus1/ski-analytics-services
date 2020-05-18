package ski.crunch.activity;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ski.crunch.activity.service.ActivityService;
import ski.crunch.aws.DynamoFacade;
import ski.crunch.aws.S3Facade;
import ski.crunch.utils.ApiGatewayResponse;

import java.util.Map;


public class PutActivityHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

    private String s3RawActivityBucket;
    private String s3ActivityBucket;
    private String region;
    private String activityTable;
    private String userTable;
    private S3Facade s3;
    private AWSCredentialsProvider credentialsProvider;
    private DynamoFacade dynamo;
    private ActivityService activityService;


    public PutActivityHandler(){
        this.s3RawActivityBucket = System.getenv("s3RawActivityBucketName");
        this.s3ActivityBucket = System.getenv("s3ActivityBucketName");
        this.region = System.getenv("AWS_DEFAULT_REGION");
        this.activityTable = System.getenv("activityTable");
        this.userTable = System.getenv("userTable");
        this.s3 = new S3Facade(region);

        try {
            this.credentialsProvider = DefaultAWSCredentialsProviderChain.getInstance();
            credentialsProvider.getCredentials();
            logger.debug("Obtained default aws credentials");
        } catch (AmazonClientException e) {
            logger.error("Unable to obtain default aws credentials", e);
        }
        this.dynamo = new DynamoFacade(region,activityTable, credentialsProvider );
        this.activityService = new ActivityService( s3, credentialsProvider, dynamo, region,
                s3RawActivityBucket,s3ActivityBucket, activityTable, userTable);
    }

    private static final Logger logger = LoggerFactory.getLogger(PutActivityHandler.class);



    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {

        //debugging to print classpath for layer issue...
        //urlClassLoader not supported with java11.  Need to rework this
        //https://community.oracle.com/thread/4011800
//        if( LOG.isDebugEnabled()) {
//            ClassLoader cl = ClassLoader.getSystemClassLoader();
//
//            URL[] urls = ((URLClassLoader) cl).getURLs();
//            LOG.debug("printing classpath: ");
//            for (URL url : urls) {
//                LOG.debug(url.getFile());
//            }
//        }
        logger.debug("PutActivityHandler called");
        return activityService.saveRawActivity(input,context);
    }
}
