package ski.crunch.cloudformation.rockset;

import ski.crunch.cloudformation.AbstractCustomResourceLambda;
import ski.crunch.cloudformation.CloudformationRequest;
import ski.crunch.cloudformation.CloudformationResponse;
import ski.crunch.cloudformation.rockset.model.RocksetWorkspaceResourceProperties;

import java.util.UUID;

public class RocksetWorkspaceLambda extends AbstractCustomResourceLambda {

    @Override
    public CloudformationResponse doCreate(CloudformationRequest request) throws Exception {
        try {
            RocksetWorkspaceResourceProperties resourceProperties = new RocksetWorkspaceResourceProperties(request.getResourceProperties());
            String apiKey = RocksetService.getApiKey(resourceProperties.getRegion(), resourceProperties.getApiKeySSM(), credentialsProvider);
            RocksetService rocksetService = new RocksetService(resourceProperties.getRegion(), resourceProperties.getApiServer(), apiKey);

            return rocksetService.createWorkspace(request, resourceProperties, UUID.randomUUID().toString());

        }catch(Exception ex){
            ex.printStackTrace();
            return CloudformationResponse.errorResponse(request);
        }
    }

    @Override
    public CloudformationResponse doUpdate(CloudformationRequest request) throws Exception {
        CloudformationResponse response = doCreate(request);
        if (response.getStatus().equals(CloudformationResponse.ResponseStatus.SUCCESS)) {
            CloudformationResponse deleteResponse = doDelete(request);
            if (deleteResponse.getStatus().equals(CloudformationResponse.ResponseStatus.SUCCESS)) {
                response.withOutput("Message", "Successfully updated workspace");
            } else {
                response.withOutput("Message", "Failed to remove previous version of workspace");
            }
        }

        return response;
    }

    @Override
    public CloudformationResponse doDelete(CloudformationRequest request) throws Exception {
        try {
            RocksetWorkspaceResourceProperties resourceProperties = new RocksetWorkspaceResourceProperties(request.getResourceProperties());
            String apiKey = RocksetService.getApiKey(resourceProperties.getRegion(), resourceProperties.getApiKeySSM(), credentialsProvider);
            RocksetService rocksetService = new RocksetService(resourceProperties.getRegion(), resourceProperties.getApiServer(), apiKey);

            return rocksetService.deleteWorkspace(request, resourceProperties, request.getPhysicalResourceId());

        }catch(Exception ex){
            ex.printStackTrace();
            return CloudformationResponse.errorResponse(request);
        }
    }
}
