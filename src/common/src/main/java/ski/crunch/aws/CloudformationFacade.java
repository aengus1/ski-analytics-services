package ski.crunch.aws;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;
import com.amazonaws.services.cloudformation.model.AmazonCloudFormationException;
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksResult;

public class CloudformationFacade {

    private AmazonCloudFormation cloudFormation = AmazonCloudFormationClientBuilder.defaultClient();

    public boolean stackExists(String stackName) {
        try {
            DescribeStacksRequest describeStacksRequest = new DescribeStacksRequest();
            describeStacksRequest.setStackName(stackName);
            DescribeStacksResult result = cloudFormation.describeStacks(describeStacksRequest);
            return result.getStacks().stream().anyMatch(x -> x.getStackName().equalsIgnoreCase(stackName));
        }catch ( AmazonCloudFormationException ex) {
            return false;
        }
    }


}
