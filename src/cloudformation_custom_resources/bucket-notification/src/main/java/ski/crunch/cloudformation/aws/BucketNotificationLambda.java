package ski.crunch.cloudformation.aws;

import com.amazonaws.services.s3.model.*;
import org.apache.log4j.Logger;
import ski.crunch.aws.S3Facade;
import ski.crunch.cloudformation.AbstractCustomResourceLambda;
import ski.crunch.cloudformation.CloudformationRequest;
import ski.crunch.cloudformation.CloudformationResponse;

import java.util.UUID;

public class BucketNotificationLambda extends AbstractCustomResourceLambda {
    private static final Logger LOG = Logger.getLogger(BucketNotificationLambda.class);

    @Override
    public CloudformationResponse doCreate(CloudformationRequest request) throws Exception {
        String uuid = UUID.randomUUID().toString();

        try {
            BucketNotificationResourceProperties resourceProperties = new BucketNotificationResourceProperties(request.getResourceProperties());
            LOG.info("sending function arn: " + resourceProperties.getLambdaFunctionArn());
            LambdaConfiguration lambdaConfiguration = new LambdaConfiguration(resourceProperties.getLambdaFunctionArn(), resourceProperties.getS3Event());
            if(resourceProperties.getFilters().isPresent()){
                S3KeyFilter s3KeyFilter = new S3KeyFilter();
                for (String filter : resourceProperties.getFilters().get()) {
                    String[] split =  filter.split(":");
                    FilterRule filterRule = new FilterRule().withName(split[0]).withValue(split[1]);
                    s3KeyFilter.addFilterRule(filterRule);
                }
                Filter f = new Filter().withS3KeyFilter(s3KeyFilter);
                lambdaConfiguration.setFilter(f);
            }

            BucketNotificationConfiguration notificationConfiguration =
                    new BucketNotificationConfiguration(uuid, lambdaConfiguration);

            S3Facade s3Facade = new S3Facade(resourceProperties.getRegion());
            s3Facade.setBucketNotificationConfiguration(resourceProperties.getS3BucketName(), notificationConfiguration);
        }catch(Exception ex){
            ex.printStackTrace();
            return CloudformationResponse.errorResponse(request);
        }
        CloudformationResponse response = CloudformationResponse.successResponse(request);
        response.withOutput("NotificationId", uuid);
        response.withOutput("Message", "successfully created bucket notification");
        response.withPhysicalResourceId(uuid);
        return response;
    }

    @Override
    public CloudformationResponse doUpdate(CloudformationRequest request) throws Exception {
        CloudformationResponse response = doCreate(request);
        if (response.getStatus().equals(CloudformationResponse.ResponseStatus.SUCCESS)) {
            CloudformationResponse deleteResponse = doDelete(request);
            if (deleteResponse.getStatus().equals(CloudformationResponse.ResponseStatus.SUCCESS)) {
                response.withOutput("Message", "Successfully updated bucket notification");
            } else {
                response.withOutput("Message", "Failed to remove previous version of bucket notification");
            }
        }

        return response;
    }

    @Override
    public CloudformationResponse doDelete(CloudformationRequest request) throws Exception {
        try {
            BucketNotificationResourceProperties resourceProperties = new BucketNotificationResourceProperties(request.getResourceProperties());
            S3Facade s3Facade = new S3Facade(resourceProperties.getRegion());

            s3Facade.deleteBucketNotificationConfiguration(resourceProperties.getS3BucketName());
            CloudformationResponse response = CloudformationResponse.successResponse(request);

            response.withOutput("Message", "successfully deleted bucket notification");
            response.withPhysicalResourceId(resourceProperties.getPhysicalResourceId());
            return response;
        }catch(Exception ex){
            ex.printStackTrace();
            return CloudformationResponse.errorResponse(request);
        }
    }
}
