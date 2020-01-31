package ski.crunch.cloudformation.rockset;

import ski.crunch.cloudformation.AbstractCustomResourceLambda;
import ski.crunch.cloudformation.CloudformationRequest;
import ski.crunch.cloudformation.CloudformationResponse;
import ski.crunch.cloudformation.rockset.model.RocksetCollectionResourceProperties;

import java.util.UUID;

public class RocksetCollectionLambda extends AbstractCustomResourceLambda {

    @Override
    public CloudformationResponse doCreate(CloudformationRequest request) throws Exception {
        try {
            RocksetCollectionResourceProperties resourceProperties = new RocksetCollectionResourceProperties(request.getResourceProperties());
            String apiKey = RocksetService.getApiKey(resourceProperties.getRegion(), resourceProperties.getApiKeySSM(), credentialsProvider);
            RocksetService rocksetService = new RocksetService(resourceProperties.getRegion(), resourceProperties.getApiServer(), apiKey);

            return rocksetService.createCollection(request, resourceProperties, UUID.randomUUID().toString());

        }catch(Exception ex){
            ex.printStackTrace();
            return CloudformationResponse.errorResponse(request);
        }
    }

    @Override
    public CloudformationResponse doUpdate(CloudformationRequest request) throws Exception {
        CloudformationResponse response = doCreate(request);
        doDelete(request);
        return response;
    }

    @Override
    public CloudformationResponse doDelete(CloudformationRequest request) throws Exception {
        try {
            RocksetCollectionResourceProperties resourceProperties = new RocksetCollectionResourceProperties(request.getResourceProperties());
            String apiKey = RocksetService.getApiKey(resourceProperties.getRegion(), resourceProperties.getApiKeySSM(), credentialsProvider);
            RocksetService rocksetService = new RocksetService(resourceProperties.getRegion(), resourceProperties.getApiServer(), apiKey);

            return rocksetService.deleteCollection(request, resourceProperties, request.getPhysicalResourceId());
        }catch(Exception ex){
            return CloudformationResponse.errorResponse(request);
        }
    }
}
