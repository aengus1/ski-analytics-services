package ski.crunch.cloudformation;


import org.apache.log4j.Logger;
import ski.crunch.utils.MissingRequiredParameterException;

import java.util.Map;
import java.util.Optional;

public class CloudformationRequest {

    protected Map<String, Object> input;
    private RequestLifecycleType requestType;
    private String serviceToken;
    private String responseURL;
    private String stackId;
    private String requestId;
    private String logicalResourceId;
    private String physicalResourceId;
    private String resourceType;
    private Map<String, Object> resourceProperties;
    private Optional<Map<String, Object>> oldResourceProperties;

    private static final Logger logger = Logger.getLogger(CloudformationRequest.class);

    public enum RequestLifecycleType {
        CREATE, UPDATE, DELETE
    };

    public CloudformationRequest(Map<String, Object> input) {
            this.input = input;

        if (logger.isDebugEnabled()) {
            logger.debug("input keys:");
            for (String s : input.keySet()) {
                System.out.println("key: " + s + " value: " + input.get(s));
            }
            if(input.containsKey("ResourceProperties")) {
                Map<String, Object> resourceProperties = (Map<String, Object>)input.get("ResourceProperties");
                logger.debug("resource properties keys:");
                for (String s : resourceProperties.keySet()) {
                    System.out.println("key: " + s + " value: " + resourceProperties.get(s));
                }
            }
        }

        checkRequiredParameters(input);
        this.requestType = RequestLifecycleType.valueOf(((String) input.get("RequestType")).toUpperCase());
        logger.info("request type = " + this.requestType);
        this.serviceToken = (String) input.get("ServiceToken");
        this.responseURL = (String) input.get("ResponseURL");
        this.stackId = (String) input.get("StackId");
        this.requestId = (String) input.get("RequestId");
        this.logicalResourceId = (String) input.get("LogicalResourceId");
        this.physicalResourceId = (String) input.get("PhysicalResourceId");
        this.resourceType = (String) input.get("ResourceType");
        this.resourceProperties = (Map<String, Object>) input.get("ResourceProperties");
        if (input.containsKey("OldResourceProperties")) {
            this.oldResourceProperties = Optional.of((Map<String, Object>) input.get("OldResourceProperties"));
        } else {
            this.oldResourceProperties = Optional.empty();
        }
    }


    private void checkRequiredParameters(Map<String, Object> input) throws MissingRequiredParameterException {
        if(input == null) {
            throw new MissingRequiredParameterException("no parameters found");
        }
        checkParameter("RequestType", input);
        checkParameter("ResponseURL", input);
        checkParameter("StackId", input);
        checkParameter("RequestId", input);
        checkParameter("LogicalResourceId", input);
        checkParameter("ResourceType", input);
        checkParameter("RequestType", input);
        checkParameter("ResourceProperties", input);

    }

    private void checkParameter(String parameter, Map<String, Object> input) throws MissingRequiredParameterException{
        if(!input.containsKey(parameter)) {
            throw new MissingRequiredParameterException("Parameter " + parameter + " not supplied");
        }
    }

    public RequestLifecycleType getRequestType() {
        return requestType;
    }

    public String getServiceToken() {
        return serviceToken;
    }

    public String getResponseURL() {
        return responseURL;
    }

    public String getStackId() {
        return stackId;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getLogicalResourceId() {
        return logicalResourceId;
    }

    public String getPhysicalResourceId() {
        return physicalResourceId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public Map<String, Object> getResourceProperties(){
        return this.resourceProperties;
    }

    public Optional<Map<String, Object>> getOldResourceProperties(){
        return this.oldResourceProperties;
    }


}
