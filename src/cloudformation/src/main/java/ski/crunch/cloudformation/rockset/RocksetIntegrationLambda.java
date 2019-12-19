package ski.crunch.cloudformation.rockset;

import com.google.common.annotations.VisibleForTesting;
import ski.crunch.aws.SSMParameterFacade;
import ski.crunch.cloudformation.AbstractCustomResourceLambda;
import ski.crunch.cloudformation.CloudformationRequest;
import ski.crunch.cloudformation.CloudformationResponse;

import java.util.UUID;

public class RocksetIntegrationLambda extends AbstractCustomResourceLambda {

    @Override
    public CloudformationResponse doCreate(CloudformationRequest request) {
        try {
            RocksetIntegrationResourceProperties resourceProperties = new RocksetIntegrationResourceProperties(request.getResourceProperties());
            String apiKey = getApiKey(resourceProperties.getRegion(), resourceProperties.getApiKeySSM());
            RocksetService rocksetService = new RocksetService(resourceProperties.getRegion(), resourceProperties.getApiServer(), apiKey);

            return rocksetService.createIntegration(request, resourceProperties, UUID.randomUUID().toString());

        }catch(Exception ex){
            ex.printStackTrace();
            return CloudformationResponse.errorResponse(request);
        }
    }

    @Override
    public CloudformationResponse doUpdate(CloudformationRequest request)  {
        return doCreate(request);
    }

    @Override
    public CloudformationResponse doDelete(CloudformationRequest request) {
        try {
            RocksetIntegrationResourceProperties resourceProperties = new RocksetIntegrationResourceProperties(request.getResourceProperties());
            String apiKey = getApiKey(resourceProperties.getRegion(), resourceProperties.getApiKeySSM());
            RocksetService rocksetService = new RocksetService(resourceProperties.getRegion(), resourceProperties.getApiServer(), apiKey);

            return rocksetService.deleteIntegration(request, resourceProperties);
        }catch(Exception ex){
            return CloudformationResponse.errorResponse(request);
        }
    }



    @VisibleForTesting
    public String getApiKey(String region, String ssmBucket) {
        SSMParameterFacade ssmParameterFacade = new SSMParameterFacade(region, credentialsProvider);
        return ssmParameterFacade.getParameter(ssmBucket);
    }




}

