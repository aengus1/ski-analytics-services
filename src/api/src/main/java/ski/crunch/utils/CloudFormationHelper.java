package ski.crunch.utils;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;
import com.amazonaws.services.cloudformation.model.DescribeStacksResult;
import com.amazonaws.services.cloudformation.model.Output;
import com.amazonaws.services.cloudformation.model.Stack;


import java.util.Optional;

public class CloudFormationHelper {

    private final static String AWS_PROFILE = "backend_dev";
    private final static String STAGING_AUTH_STACK_NAME="staging-ski-analytics-authentication-stack";
    private final static String USERPOOL_CLIENTID="UserPoolClientId";

    private ServerlessState serverlessState;

    public CloudFormationHelper(ServerlessState serverlessState){
    this.serverlessState = serverlessState;
    }

    public String getStagingUserPoolClientId() throws NotFoundException{
        AWSCredentialsProvider awsCreds = new ProfileCredentialsProvider(AWS_PROFILE);

        AmazonCloudFormation acf =  AmazonCloudFormationClientBuilder
                .standard()
                .withCredentials(awsCreds)
                .withRegion(Regions.fromName(serverlessState.getStackRegion()))
                .build();

        DescribeStacksResult result = acf.describeStacks();
//        result.getStacks().stream().map(x -> x.getStackName()).forEach(System.out::println);
        Optional<Stack> stack = result.getStacks().stream()
                .filter(x -> x.getStackName().equals(STAGING_AUTH_STACK_NAME)).findFirst();

        if(stack.isPresent()){
            Optional<Output> output =  stack.get().getOutputs().stream()
                    .filter(x -> x.getOutputKey().equals("UserPoolClientId")).findFirst();
            if(output.isPresent()){
               return  output.get().getOutputValue();
            } else {
                throw new NotFoundException("cloudformation output userPoolClientId not found");
            }
        }else {
            throw new NotFoundException("cloudformation stack staging-ski-analytics-authentication-stack not found");
        }
    }

}
