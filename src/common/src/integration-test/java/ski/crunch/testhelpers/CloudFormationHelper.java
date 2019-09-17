package ski.crunch.testhelpers;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;
import com.amazonaws.services.cloudformation.model.DescribeStacksResult;
import com.amazonaws.services.cloudformation.model.Output;
import com.amazonaws.services.cloudformation.model.Stack;
import ski.crunch.utils.NotFoundException;

import java.util.Optional;

public class CloudFormationHelper {


   private AmazonCloudFormation amazonCloudFormation;


   public CloudFormationHelper(ProfileCredentialsProvider credentialsProvider, String region) {

       this.amazonCloudFormation = AmazonCloudFormationClientBuilder
               .standard()
               .withCredentials(credentialsProvider)
               .withRegion(Regions.fromName(region))
               .build();
   }


  public  String getStackOutput(String stackName, String key) throws NotFoundException {
       DescribeStacksResult result = amazonCloudFormation.describeStacks();

       Optional<Stack> stack = result.getStacks().stream().filter(x -> x.getStackName().equals(stackName)).findFirst();
       stack.orElseThrow(() -> new NotFoundException("cloudformation stack " + stackName + " not found"));

       Optional<Output> output = stack.get().getOutputs().stream().filter(x -> x.getOutputKey().equals(key)).findFirst();
       return output.orElseThrow(() -> new NotFoundException("cloudformation output " + key + " not found")).getOutputValue();


   }
}

