package ski.crunch.cloudformation.aws;

import ski.crunch.cloudformation.ResourceProperties;
import ski.crunch.utils.MissingRequiredParameterException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UserPoolTriggerResourceProperties extends ResourceProperties {

    private String triggerType;
    private String physicalResourceId;
    private String region;
    private String userPoolId;
    private String lambdaFunctionArn;
    private Optional<List<String>> filters;


    public UserPoolTriggerResourceProperties(Map<String, Object> input) {
        List<String> requiredParameters = Stream.of(
                "Region",
                "UserPool",
                "TriggerType",
                "LambdaFunctionArn"
        ).collect(Collectors.toList());

        checkRequiredParameters(input, requiredParameters);

        try{
            this.triggerType = (String) input.get("TriggerType");
            this.region = (String) input.get("Region");
            this.userPoolId = (String) input.get("UserPool");
            this.lambdaFunctionArn = (String) input.get("LambdaFunctionArn");

        }catch(Exception ex) {
            ex.printStackTrace();
            throw new MissingRequiredParameterException("Parameter missing in resourceProperties", ex);
        }
    }

    public String getTriggerType() {
        return triggerType;
    }

    public String getUserPoolId() {
        return userPoolId;
    }

    public String getLambdaFunctionArn() {
        return lambdaFunctionArn;
    }

    public String getRegion(){
        return region;
    }

    public String getPhysicalResourceId(){
        return this.physicalResourceId;
    }
}
