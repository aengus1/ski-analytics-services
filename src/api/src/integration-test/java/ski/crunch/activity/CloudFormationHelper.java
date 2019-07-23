package ski.crunch.activity;

import com.amazonaws.auth.AWSCredentialsProvider;
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

    private final static String AWS_PROFILE = "backend_dev";
    private final static String STAGING_AUTH_STACK_NAME="staging-ski-analytics-authentication-stack";
    private final static String STAGING_API_STACK_NAME="staging-ski-analytics-api-stack";
    private final static String USERPOOL_CLIENTID="UserPoolClientId";
    private AWSCredentialsProvider awsCreds = null;
    private AmazonCloudFormation acf = null;

    private ServerlessState serverlessState;

    public CloudFormationHelper(ServerlessState serverlessState){
    this.serverlessState = serverlessState;
        this.awsCreds = new ProfileCredentialsProvider(AWS_PROFILE);

        this.acf = AmazonCloudFormationClientBuilder
                .standard()
                .withCredentials(awsCreds)
                .withRegion(Regions.fromName(serverlessState.getStackRegion()))
                .build();


    }

    public String getStagingUserPoolClientId() throws NotFoundException {
        return getStackOutput(STAGING_AUTH_STACK_NAME, "UserPoolClientId");
    }

    public String getApiEndpoint() throws NotFoundException{
        return getStackOutput(STAGING_API_STACK_NAME, "ServiceEndpoint");
    }


    private String getStackOutput(String stackName, String key) throws NotFoundException {
        DescribeStacksResult result = acf.describeStacks();
        Optional<Stack> stack = result.getStacks().stream()
                .filter(x -> x.getStackName().equals(stackName)).findFirst();

        if(stack.isPresent()){
            Optional<Output> output =  stack.get().getOutputs().stream()
                    .filter(x -> x.getOutputKey().equals(key)).findFirst();
            if(output.isPresent()){
                return  output.get().getOutputValue();
            } else {
                throw new NotFoundException("cloudformation output " + key + " not found");
            }
        }else {
            throw new NotFoundException("cloudformation stack " + stackName + " not found");
        }

    }
}

