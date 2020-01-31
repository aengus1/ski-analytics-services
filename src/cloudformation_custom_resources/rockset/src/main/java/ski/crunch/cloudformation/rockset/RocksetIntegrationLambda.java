package ski.crunch.cloudformation.rockset;

import ski.crunch.cloudformation.AbstractCustomResourceLambda;
import ski.crunch.cloudformation.CloudformationRequest;
import ski.crunch.cloudformation.CloudformationResponse;
import ski.crunch.cloudformation.rockset.model.RocksetIntegrationResourceProperties;

import java.util.UUID;

public class RocksetIntegrationLambda extends AbstractCustomResourceLambda {

    @Override
    public CloudformationResponse doCreate(CloudformationRequest request) {
        try {
            RocksetIntegrationResourceProperties resourceProperties = new RocksetIntegrationResourceProperties(request.getResourceProperties());
            String apiKey = RocksetService.getApiKey(resourceProperties.getRegion(), resourceProperties.getApiKeySSM(), credentialsProvider);
            RocksetService rocksetService = new RocksetService(resourceProperties.getRegion(), resourceProperties.getApiServer(), apiKey);

            return rocksetService.createIntegration(request, resourceProperties, UUID.randomUUID().toString());

        } catch (Exception ex) {
            ex.printStackTrace();
            return CloudformationResponse.errorResponse(request);
        }
    }

    @Override
    public CloudformationResponse doUpdate(CloudformationRequest request) {
        CloudformationResponse response = doCreate(request);
        if (response.getStatus().equals(CloudformationResponse.ResponseStatus.SUCCESS)) {
            CloudformationResponse deleteResponse = doDelete(request);
            if (deleteResponse.getStatus().equals(CloudformationResponse.ResponseStatus.SUCCESS)) {
                response.withOutput("Message", "Successfully updated integration");
            } else {
                response.withOutput("Message", "Failed to remove previous version of integration");
            }
        }

        return response;
    }

    @Override
    public CloudformationResponse doDelete(CloudformationRequest request) {
        try {
            RocksetIntegrationResourceProperties resourceProperties = new RocksetIntegrationResourceProperties(request.getResourceProperties());
            String apiKey = RocksetService.getApiKey(resourceProperties.getRegion(), resourceProperties.getApiKeySSM(), credentialsProvider);
            RocksetService rocksetService = new RocksetService(resourceProperties.getRegion(), resourceProperties.getApiServer(), apiKey);

            return rocksetService.deleteIntegration(request, resourceProperties);
        } catch (Exception ex) {
            return CloudformationResponse.errorResponse(request);
        }
    }


}

