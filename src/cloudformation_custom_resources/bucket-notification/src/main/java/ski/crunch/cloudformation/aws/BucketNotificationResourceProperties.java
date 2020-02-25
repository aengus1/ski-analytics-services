package ski.crunch.cloudformation.aws;

import ski.crunch.cloudformation.ResourceProperties;
import ski.crunch.utils.MissingRequiredParameterException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BucketNotificationResourceProperties extends ResourceProperties {

    private String s3BucketName;
    private String physicalResourceId;
    private String region;
    private String s3Event;
    private String lambdaFunctionArn;
    private Optional<List<String>> filters;


    public BucketNotificationResourceProperties(Map<String, Object> input) {
        List<String> requiredParameters = Stream.of(
                "Region",
                "S3BucketName",
                "S3Event",
                "LambdaFunctionArn"
        ).collect(Collectors.toList());

        checkRequiredParameters(input, requiredParameters);

        try{
            this.s3BucketName = (String) input.get("S3BucketName");
            this.region = (String) input.get("Region");
            this.s3Event = (String) input.get("S3Event");
            this.lambdaFunctionArn = (String) input.get("LambdaFunctionArn");
            if(input.containsKey("Filters")){
                this.filters = Optional.of((List) (input.get("Filters")));
            }else {
                this.filters = Optional.empty();
            }
        }catch(Exception ex) {
            ex.printStackTrace();
            throw new MissingRequiredParameterException("Parameter missing in resourceProperties", ex);
        }
    }

    public String getS3BucketName() {
        return s3BucketName;
    }

    public String getS3Event() {
        return s3Event;
    }

    public String getLambdaFunctionArn() {
        return lambdaFunctionArn;
    }

    public Optional<List<String>> getFilters() {
        return filters;
    }
    public String getRegion(){
        return region;
    }

    public String getPhysicalResourceId(){
        return this.physicalResourceId;
    }
}
