package ski.crunch.cloudformation.rockset;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.identitymanagement.model.CreatePolicyResult;
import com.amazonaws.services.identitymanagement.model.Tag;
import com.google.common.annotations.VisibleForTesting;
import org.apache.log4j.Logger;
import ski.crunch.aws.IAMFacade;
import ski.crunch.aws.SSMParameterFacade;
import ski.crunch.cloudformation.CloudformationRequest;
import ski.crunch.cloudformation.CloudformationResponse;
import ski.crunch.cloudformation.rockset.model.RocksetCollectionResourceProperties;
import ski.crunch.cloudformation.rockset.model.RocksetIntegrationResourceProperties;
import ski.crunch.cloudformation.rockset.model.RocksetWorkspaceResourceProperties;
import ski.crunch.utils.StackTraceUtil;
import ski.crunch.utils.StreamUtils;

import javax.naming.OperationNotSupportedException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RocksetService {

    private static final Logger logger = Logger.getLogger(RocksetService.class);
    public static final String ROLE_DESCRIPTION = "Role for rockset x-account permissions";
    public static final String POLICY_DESCRIPTION = "Policy for rockset to access integration source";
    public static final String INTEGRATION_DESCRIPTION = " aws integration";

    private IAMFacade iamFacade;
    private RocksetRestClient rocksetRestClient;


    public RocksetService(String awsRegion, String rocksetApiServer, String rocksetApiKey) {
        this.iamFacade = new IAMFacade(awsRegion);
        this.rocksetRestClient = new RocksetRestClient(rocksetApiServer, rocksetApiKey);
    }

    /**x
     * Test constructor
     *
     * @param iamFacade
     * @param rocksetRestClient
     */
    public RocksetService(IAMFacade iamFacade, RocksetRestClient rocksetRestClient) {
        this.iamFacade = iamFacade;
        this.rocksetRestClient = rocksetRestClient;
    }


    public CloudformationResponse createIntegration(CloudformationRequest request,
                                                    RocksetIntegrationResourceProperties
                                                            resourceProperties, String physicalResourceId) {
        logger.debug("Creating rockset integration " + physicalResourceId);
        try {
            boolean useXAccountAuth = resourceProperties.getRocksetAwsAccount().isPresent()
                    && resourceProperties.getExternalId().isPresent();

            String nameSuffix = physicalResourceId.substring(physicalResourceId.length() - 8);
            String roleName = resourceProperties.getName() + "_"+ nameSuffix;

            if (useXAccountAuth) {
                String assumeRolePolicyDocument = fetchPolicyDocumentAsString("xaccount_assume_role_policy.json")
                        .replace("${accountId}", resourceProperties.getRocksetAwsAccount().get())
                        .replace("${externalId}", resourceProperties.getExternalId().get());
                List<Tag> tags = resourceProperties.getTags().orElse(new ArrayList<>());

                logger.debug("Creating IAM role " + roleName);
                iamFacade.createRole(roleName, assumeRolePolicyDocument, ROLE_DESCRIPTION, tags);

                String policy = fetchPolicyDocumentAsString("rockset_" + resourceProperties.getIntegrationType() + "_policy.json");
                String accessibleResourceString = resourceProperties.getAccessibleResources().stream().map(x -> "\n        \"" + x + "\"")
                        .collect(Collectors.joining(","));
                policy = policy.replaceAll(java.util.regex.Pattern.quote("\"${accessibleResource}\""), accessibleResourceString);

                logger.debug("Creating policy for " + roleName);
                CreatePolicyResult createPolicyResult = iamFacade.createPolicy(roleName, policy, POLICY_DESCRIPTION);


                logger.debug("Attaching policy " + createPolicyResult.getPolicy().getArn() + " to role");
                iamFacade.attachPolicyToRole(createPolicyResult.getPolicy().getArn(), roleName);

            } else {
                throw new OperationNotSupportedException("Currently only xaccount integration types are supported");
                //TODO -> deal with access key auth
            }

            String createIntegrationResponse = rocksetRestClient.createIntegration(
                    roleName,
                    (resourceProperties.getName() +"_"+ nameSuffix),
                    (resourceProperties.getIntegrationType().name() + "_" + INTEGRATION_DESCRIPTION),
                    resourceProperties.getIntegrationType(),
                    resourceProperties.getAwsAccountId()
            );

            CloudformationResponse response = CloudformationResponse.successResponse(request);
            response.withOutput("IntegrationName", (resourceProperties.getName() +"_"+ nameSuffix));
            response.withOutput("Message", "successfully created rockset integration " + createIntegrationResponse);
            response.withPhysicalResourceId(physicalResourceId);
            return response;

        } catch (Exception ex) {
            CloudformationResponse response = CloudformationResponse.errorResponse(request);
            response.withOutput("Message", "Error creating rockset integration: " + StackTraceUtil.getStackTrace(ex));
            response.withPhysicalResourceId(physicalResourceId);
            return response;
        }
    }

    public CloudformationResponse deleteIntegration(CloudformationRequest request,
                                                    RocksetIntegrationResourceProperties resourceProperties)
            throws RocksetApiException {

        try {
            logger.debug("Delete integration: physical resource id: " + request.getPhysicalResourceId());
            String nameSuffix = "_" + request.getPhysicalResourceId().substring(request.getPhysicalResourceId().length() - 8);
            String roleName = resourceProperties.getName() + nameSuffix;

            String policyArn = iamFacade.getRolePolicies(roleName)
                    .getAttachedPolicies()
                    .stream()
                    .filter(x -> x.getPolicyName().equals(roleName))
                    .findFirst()
                    .orElseThrow(() -> new Exception("Cannot find policy arn"))
                    .getPolicyArn();
            iamFacade.detachPolicyFromRole(policyArn, roleName);
            iamFacade.deletePolicy(policyArn);
            iamFacade.deleteRole(roleName);
            String responseStr = rocksetRestClient.deleteIntegration((resourceProperties.getName() + nameSuffix));

            CloudformationResponse response = CloudformationResponse.successResponse(request);
            response.withOutput("Message", "successfully deleted rockset integration " + responseStr);
            response.withPhysicalResourceId(request.getPhysicalResourceId());
            return response;
        } catch (Exception ex) {
            CloudformationResponse response = CloudformationResponse.errorResponse(request);
            response.withOutput("Message", "Error deleting rockset integration: " + StackTraceUtil.getStackTrace(ex));
            response.withPhysicalResourceId(request.getPhysicalResourceId());
            return response;
        }
    }

    public CloudformationResponse createWorkspace(CloudformationRequest request,
                                                  RocksetWorkspaceResourceProperties resourceProperties,
                                                  String physicalResourceId) {
        logger.debug("Creating rockset workspace");
        try {
            String result = rocksetRestClient.createWorkspace(resourceProperties.getName(), resourceProperties.getDescription().orElse(""));
            CloudformationResponse response = CloudformationResponse.successResponse(request);
            response.withOutput("WorkspaceName", resourceProperties.getName());
            response.withOutput("Message", "successfully created rockset workspace: " + result);
            response.withPhysicalResourceId(physicalResourceId);
            return response;
        } catch (Exception ex) {
            CloudformationResponse response = CloudformationResponse.errorResponse(request);
            response.withOutput("Message", "Error creating rockset workspace: " + StackTraceUtil.getStackTrace(ex));
            return response;
        }
    }

    public CloudformationResponse deleteWorkspace(CloudformationRequest request, RocksetWorkspaceResourceProperties resourceProperties,
                                                  String physicalResourceId) {
        logger.debug("Deleting rockset workspace");
        try {
            String result = rocksetRestClient.deleteWorkspace(resourceProperties.getName());
            CloudformationResponse response = CloudformationResponse.successResponse(request);
            response.withOutput("WorkspaceName", resourceProperties.getName());
            response.withOutput("Message", "successfully deleted rockset workspace: " + result);
            response.withPhysicalResourceId(physicalResourceId);
            return response;
        } catch (Exception ex) {
            CloudformationResponse response = CloudformationResponse.errorResponse(request);
            response.withOutput("Message", "Error deleting rockset workspace: " + StackTraceUtil.getStackTrace(ex));
            response.withPhysicalResourceId(physicalResourceId);
            return response;
        }
    }

    public CloudformationResponse createCollection(CloudformationRequest request, RocksetCollectionResourceProperties resourceProperties,
                                                   String physicalResourceId) {

        logger.debug("Creating rockset collection: " + physicalResourceId);
        try {
            String result = rocksetRestClient.createCollection(resourceProperties.getName(), resourceProperties.getWorkspace(),
                    resourceProperties.getDescription(), resourceProperties.getDataSources(), resourceProperties.getIntegrationName(),
                    resourceProperties.getRetentionTime(),resourceProperties.getEventTimeField(), resourceProperties.getEventTimeFormat(),
                    resourceProperties.getEventTimeZone(), resourceProperties.getFieldMappingList());
            if(result.contains("created_at")) {
                CloudformationResponse response = CloudformationResponse.successResponse(request);
                response.withOutput("CollectionName", resourceProperties.getName());
                response.withOutput("Message", "successfully created rockset collection: " + result);
                response.withPhysicalResourceId(physicalResourceId);
                return response;
            } else {
                throw new RocksetApiException("Unexpected response " + result);
            }
        } catch (Exception ex) {
            CloudformationResponse response = CloudformationResponse.errorResponse(request);
            response.withPhysicalResourceId(physicalResourceId);
            response.withOutput("Message", "Error creating rockset collection: " + StackTraceUtil.getStackTrace(ex));
            return response;
        }
    }

    public CloudformationResponse deleteCollection(CloudformationRequest request, RocksetCollectionResourceProperties resourceProperties, String physicalResourceId) {
        logger.debug("Deleting rockset collection: ");
        try {
            String result = rocksetRestClient.deleteCollection(resourceProperties.getName(), resourceProperties.getWorkspace());
            CloudformationResponse response = CloudformationResponse.successResponse(request);
            response.withOutput("CollectionName", resourceProperties.getName());
            response.withOutput("Message", "successfully deleted rockset collection: " + result);
            response.withPhysicalResourceId(physicalResourceId);
            return response;
        } catch (Exception ex) {
            CloudformationResponse response = CloudformationResponse.errorResponse(request);
            response.withOutput("Message", "Error deleting rockset collection: " + StackTraceUtil.getStackTrace(ex));
            response.withPhysicalResourceId(physicalResourceId);
            return response;
        }
    }
    @VisibleForTesting
    String fetchPolicyDocumentAsString(String policyFileName) throws IOException {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream policyDocumentStream = classloader.getResourceAsStream(policyFileName);
        return StreamUtils.convertStreamToString(policyDocumentStream);
    }

    @VisibleForTesting
    public static String getApiKey(String region, String ssmBucket, AWSCredentialsProvider credentialsProvider) {
        SSMParameterFacade ssmParameterFacade = new SSMParameterFacade(region, credentialsProvider);
        return ssmParameterFacade.getParameter(ssmBucket);
    }
}
