package ski.crunch.cloudformation.rockset;

import com.amazonaws.services.identitymanagement.model.*;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ski.crunch.aws.IAMFacade;
import ski.crunch.cloudformation.CloudformationRequest;
import ski.crunch.cloudformation.CloudformationResponse;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static ski.crunch.cloudformation.rockset.RocksetService.INTEGRATION_DESCRIPTION;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RocksetServiceTest {

    private RocksetService rocksetService;

    private static final Map<String, Object> resourceProperties = new HashMap<>();

    private static final Map<String, Object> requestParams = new HashMap<>();

    private  String physicalResourceId = null;
    private String nameSuffix = null;
    @Mock()
    private IAMFacade iamFacade;

    @Mock()
    private RocksetRestClient rocksetRestClient;

    @BeforeEach()
    public void init() {
        MockitoAnnotations.initMocks(this);

        rocksetService = new RocksetService(iamFacade, rocksetRestClient);


        resourceProperties.put("Region", "ca-central-1");
        resourceProperties.put("Name", "testIntegration");
        resourceProperties.put("ApiKeySSM", "keyssm");
        resourceProperties.put("ExternalId","extId");
        resourceProperties.put("IntegrationType", "dynamodb");
        resourceProperties.put("RocksetAccountId", "1234567890");
        resourceProperties.put("ExternalId", "55555");
        List<String> accessibleResources = new ArrayList<>();
        accessibleResources.add("arn:aws:dynamodb:*:*:table/*");
        accessibleResources.add("arn:aws:dynamodb:*:*:table/*/stream/*");
        resourceProperties.put("AccessibleResources",accessibleResources);

        List<LinkedHashMap<String, String>> tags = new ArrayList<>();
        LinkedHashMap<String, String> env = new LinkedHashMap<>();
        env.put("Key", "environment");
        env.put("Value", "staging");

        LinkedHashMap<String, String> module = new LinkedHashMap<>();
        module.put("Key", "module");
        module.put("Value", "staging");

        tags.add(env);
        tags.add(module);

        resourceProperties.put("Tags", tags);


        requestParams.put("RequestType", "Create");
        requestParams.put("ResponseURL", "http://testurl.com");
        requestParams.put("StackId", "my-stack-id");
        requestParams.put("RequestId", "12345-678910");
        requestParams.put("LogicalResourceId", "myLogicalResourceId");
        requestParams.put("PhysicalResourceId", "myPhysicalResourceId");
        requestParams.put("ResourceType", "AWS::CloudFormation::CustomResource");
        requestParams.put("ResourceProperties", resourceProperties);

    }



    @Order(1)
    @Test()
    public void testCreateIntegrationCalledWithCorrectParameters() throws IOException {


        this.physicalResourceId = UUID.randomUUID().toString();
        this.nameSuffix = "_"+physicalResourceId.substring(physicalResourceId.length() -8);
        String expectedRoleName = "testIntegration" + nameSuffix;
        RocksetIntegrationResourceProperties resourceProperties = new RocksetIntegrationResourceProperties(RocksetServiceTest.resourceProperties);
        CloudformationRequest request = new CloudformationRequest(requestParams);

        when(iamFacade.createRole(any(),any(),any(), any())).thenReturn(new CreateRoleResult());
        CreatePolicyResult policyResult = new CreatePolicyResult().withPolicy(new Policy().withArn("arn::123::456"));
        when(iamFacade.createPolicy(any(),any(),any())).thenReturn(policyResult);
        when(iamFacade.attachPolicyToRole(any(),any())).thenReturn(new AttachRolePolicyResult());
        when(rocksetRestClient.createIntegrationViaRest(expectedRoleName, "testIntegration"+nameSuffix,
                (resourceProperties.getIntegrationType().name() + "_" + INTEGRATION_DESCRIPTION),
                resourceProperties.getIntegrationType())).thenReturn("success");

        // act
        CloudformationResponse response = rocksetService.createIntegration(request, resourceProperties, physicalResourceId);




        // client method called once with correct parameters
        verify(rocksetRestClient,times(1))
                .createIntegrationViaRest(expectedRoleName, resourceProperties.getName()+nameSuffix,
                        (resourceProperties.getIntegrationType().name() + "_" + INTEGRATION_DESCRIPTION),
                        resourceProperties.getIntegrationType());

        verify(iamFacade,times(1)).createPolicy(any(), any(), any());
        verify(iamFacade,times(1)).createRole(any(), any(), any(), any());

        // correct response returned
        assertEquals("successfully created rockset integration success",response.getData().get("Message").asText());


        //policy document was created correctly
        String policyDoc = "{\n" +
                "  \"Version\": \"2012-10-17\",\n" +
                "  \"Statement\": [\n" +
                "    {\n" +
                "      \"Effect\": \"Allow\",\n" +
                "      \"Action\": [\n" +
                "        \"dynamodb:Scan\",\n" +
                "        \"dynamodb:DescribeStream\",\n" +
                "        \"dynamodb:GetRecords\",\n" +
                "        \"dynamodb:GetShardIterator\",\n" +
                "        \"dynamodb:DescribeTable\",\n" +
                "        \"dynamodb:UpdateTable\"\n" +
                "      ],\n" +
                "      \"Resource\": [\n        \n" +
                "        \"- arn:aws:dynamodb:*:*:table/*\",\n"+
                "        \"- arn:aws:dynamodb:*:*:table/*/stream/*\"\n"+
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}" ;
        verify(iamFacade, times(1)).createPolicy(any(),eq(policyDoc), any());



    }


    @Order(2)
    @Test()
    public void testRocksetClientCreateFails() throws IOException{

        String expectedRoleName = "testIntegration" + nameSuffix;
        RocksetIntegrationResourceProperties resourceProperties = new RocksetIntegrationResourceProperties(RocksetServiceTest.resourceProperties);
        CloudformationRequest request = new CloudformationRequest(requestParams);

        when(iamFacade.createRole(any(),any(),any(), any())).thenReturn(new CreateRoleResult());
        when(iamFacade.createPolicy(any(),any(),any())).thenReturn(new CreatePolicyResult().withPolicy(
                new Policy().withArn("arn::123::456")
        ));
        when(iamFacade.attachPolicyToRole(any(),any())).thenReturn(new AttachRolePolicyResult());
        when(rocksetRestClient.createIntegrationViaRest(expectedRoleName, "testIntegration"+nameSuffix,
                (resourceProperties.getIntegrationType().name() + "_" + INTEGRATION_DESCRIPTION),
                resourceProperties.getIntegrationType())).thenThrow(new IOException("ex"));

        // act
        CloudformationResponse response = rocksetService.createIntegration(request, resourceProperties, physicalResourceId);

        // error response is returned
        assertTrue(response.getData().get("Message").asText().startsWith("Error creating rockset integration: "));

    }


    @Order(3)
    @Test()
    public void testIamCreateRoleFails() throws IOException{

        String expectedRoleName = "testIntegration" + nameSuffix;
        RocksetIntegrationResourceProperties resourceProperties = new RocksetIntegrationResourceProperties(RocksetServiceTest.resourceProperties);
        CloudformationRequest request = new CloudformationRequest(requestParams);

        when(iamFacade.createRole(any(),any(),any(), any())).thenThrow(new EntityAlreadyExistsException("ex"));
        when(iamFacade.createPolicy(any(),any(),any())).thenReturn(new CreatePolicyResult().withPolicy(
                new Policy().withArn("arn::123::456")
        ));
        when(iamFacade.attachPolicyToRole(any(),any())).thenReturn(new AttachRolePolicyResult());
        when(rocksetRestClient.createIntegrationViaRest(expectedRoleName, "testIntegration"+nameSuffix,
                (resourceProperties.getIntegrationType().name() + "_" + INTEGRATION_DESCRIPTION),
                resourceProperties.getIntegrationType())).thenReturn("success");

        // act
        CloudformationResponse response = rocksetService.createIntegration(request, resourceProperties, physicalResourceId);

        // error response is returned
        assertTrue(response.getData().get("Message").asText().startsWith("Error creating rockset integration: "));
    }



    @Order(4)
    @Test()
    public void testDeleteIntegrationCalledWithCorrectParameters() throws IOException {

        requestParams.put("RequestType", "Delete");
        requestParams.put("PhysicalResourceId", physicalResourceId);
        RocksetIntegrationResourceProperties resourceProperties = new RocksetIntegrationResourceProperties(RocksetServiceTest.resourceProperties);
        CloudformationRequest request = new CloudformationRequest(requestParams);

        List<AttachedPolicy> attachedPolicies = new ArrayList<>();
        attachedPolicies.add(new AttachedPolicy().withPolicyArn("arn::123::456::678").withPolicyName("testIntegration" + nameSuffix));
        when(iamFacade.getRolePolicies(any())).thenReturn(new ListAttachedRolePoliciesResult().withAttachedPolicies(attachedPolicies));
        when(iamFacade.detachPolicyFromRole(any(),any())).thenReturn(new DetachRolePolicyResult());
        when(iamFacade.deleteRole(any())).thenReturn(new DeleteRoleResult());
        when(iamFacade.deletePolicy(any())).thenReturn(new DeletePolicyResult());
        when(rocksetRestClient.deleteIntegrationViaRest(resourceProperties.getName()+nameSuffix)).thenReturn("success");

        // act
        CloudformationResponse response = rocksetService.deleteIntegration(request, resourceProperties);

        // client method called once with correct parameters
        verify(rocksetRestClient,times(1))
                .deleteIntegrationViaRest(resourceProperties.getName()+nameSuffix);

        verify(iamFacade,times(1))
                .deleteRole(any());
        verify(iamFacade,times(1))
                .deletePolicy(any());

        // correct response returned
        assertEquals("successfully deleted rockset integration success",response.getData().get("Message").asText());

    }
}
