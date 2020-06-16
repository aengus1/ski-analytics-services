package ski.crunch.aws;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;
import com.amazonaws.services.cloudformation.model.*;

import java.util.List;
import java.util.Optional;

public class CloudformationFacade {

    private AmazonCloudFormation cloudFormation = AmazonCloudFormationClientBuilder.defaultClient();

    public boolean stackExists(String stackName, String region) {
        cloudFormation = AmazonCloudFormationClientBuilder.standard().withRegion(region).build();
        try {
            DescribeStacksRequest describeStacksRequest = new DescribeStacksRequest();
            describeStacksRequest.setStackName(stackName);
            DescribeStacksResult result = cloudFormation.describeStacks(describeStacksRequest);
            return result.getStacks().stream().anyMatch(x -> x.getStackName().equalsIgnoreCase(stackName));
        }catch ( AmazonCloudFormationException ex) {
            return false;
        } finally {
            cloudFormation = AmazonCloudFormationClientBuilder.defaultClient();
        }
    }
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

    public Optional<List<Export>> getExportedOutputs() {
        try {
            ListExportsRequest listExportsRequest = new ListExportsRequest();
            ListExportsResult listExportsResult = cloudFormation.listExports(listExportsRequest);
            return Optional.of(listExportsResult.getExports());
        } catch( AmazonCloudFormationException ex) {
            return Optional.empty();
        }
    }




}
