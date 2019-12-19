package ski.crunch.cloudformation;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class CloudformationResponse {
    private ResponseStatus status;
    private String physicalResourceId;
    private String stackId;
    private String requestId;
    private String logicalResourceId;
    private ObjectNode data;

    private final ObjectMapper mapper;

    public enum ResponseStatus {
        SUCCESS, FAILED
    };

    public CloudformationResponse() {
        this.mapper = new ObjectMapper();
        this.data = mapper.createObjectNode();
    }

    public static CloudformationResponse errorResponse(CloudformationRequest request) {
        return new CloudformationResponse()
                .withStackId(request.getStackId())
                .withRequestId(request.getRequestId())
                .withLogicalResourceId(request.getLogicalResourceId())
                .withPhysicalResourceId(request.getPhysicalResourceId())
                .withStatus(CloudformationResponse.ResponseStatus.FAILED);
    }

    public static CloudformationResponse successResponse(CloudformationRequest request) {
        return new CloudformationResponse()
                .withStackId(request.getStackId())
                .withRequestId(request.getRequestId())
                .withLogicalResourceId(request.getLogicalResourceId())
                .withStatus(CloudformationResponse.ResponseStatus.SUCCESS);
    }

    public CloudformationResponse withStatus(ResponseStatus status) {
        this.status = status;
        return this;
    }

    public CloudformationResponse withStackId(String stackId) {
        this.stackId = stackId;
        return this;
    }

    public CloudformationResponse withPhysicalResourceId(String physicalResourceId) {
        this.physicalResourceId = physicalResourceId;
        return this;
    }

    public CloudformationResponse withRequestId(String requestId) {
         this.requestId = requestId;
         return this;
    }

    public CloudformationResponse withLogicalResourceId(String logicalResourceId) {
        this.logicalResourceId = logicalResourceId;
        return this;
    }

    public CloudformationResponse withData(ObjectNode data) {
        this.data = data;
        return this;
    }

    public String build() throws JsonProcessingException {
        ObjectNode responseBody =mapper.createObjectNode();
        responseBody.put("Status", status.name());
        responseBody.put("PhysicalResourceId", physicalResourceId);
        responseBody.put("StackId", stackId);
        responseBody.put("RequestId", requestId);
        responseBody.put("LogicalResourceId", logicalResourceId);
        responseBody.set("Data", data);
        return mapper.writeValueAsString(responseBody);
    }

    public ResponseStatus getStatus() {
        return status;
    }

    public String getPhysicalResourceId() {
        return physicalResourceId;
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

    public ObjectNode getData() {
        return data;
    }
}
