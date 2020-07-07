package ski.crunch.cloudformation.rockset;

import com.amazonaws.services.identitymanagement.model.*;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ski.crunch.aws.IAMFacade;
import ski.crunch.cloudformation.CloudformationRequest;
import ski.crunch.cloudformation.CloudformationResponse;
import ski.crunch.cloudformation.rockset.model.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static ski.crunch.cloudformation.rockset.RocksetService.INTEGRATION_DESCRIPTION;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RocksetServiceTest {

    private RocksetService rocksetService;

    private static final Map<String, Object> integrationResourceProperties = new HashMap<>();

    private static final Map<String, Object> requestParams = new HashMap<>();

    private String physicalResourceId = null;
    private String nameSuffix = null;
    @Mock()
    private IAMFacade iamFacade;

    @Mock()
    private RocksetRestClient rocksetRestClient;

    @BeforeEach()
    public void init() {
        MockitoAnnotations.initMocks(this);

        rocksetService = new RocksetService(iamFacade, rocksetRestClient);


        integrationResourceProperties.put("Region", "ca-central-1");
        integrationResourceProperties.put("Name", "testIntegration");
        integrationResourceProperties.put("ApiKeySSM", "keyssm");
        integrationResourceProperties.put("ExternalId", "extId");
        integrationResourceProperties.put("IntegrationType", "dynamodb");
        integrationResourceProperties.put("RocksetAccountId", "1234567890");
        integrationResourceProperties.put("ExternalId", "55555");
        List<String> accessibleResources = new ArrayList<>();
        accessibleResources.add("arn:aws:dynamodb:*:*:table/*");
        accessibleResources.add("arn:aws:dynamodb:*:*:table/*/stream/*");
        integrationResourceProperties.put("AccessibleResources", accessibleResources);

        List<LinkedHashMap<String, String>> tags = new ArrayList<>();
        LinkedHashMap<String, String> env = new LinkedHashMap<>();
        env.put("Key", "environment");
        env.put("Value", "staging");

        LinkedHashMap<String, String> module = new LinkedHashMap<>();
        module.put("Key", "module");
        module.put("Value", "staging");

        tags.add(env);
        tags.add(module);

        integrationResourceProperties.put("Tags", tags);


        requestParams.put("RequestType", "Create");
        requestParams.put("ResponseURL", "http://testurl.com");
        requestParams.put("StackId", "my-stack-id");
        requestParams.put("RequestId", "12345-678910");
        requestParams.put("LogicalResourceId", "myLogicalResourceId");
        requestParams.put("PhysicalResourceId", "myPhysicalResourceId");
        requestParams.put("ResourceType", "AWS::CloudFormation::CustomResource");
        requestParams.put("ResourceProperties", integrationResourceProperties);

    }


    @Order(1)
    @Test()
    public void testCreateIntegrationCalledWithCorrectParameters() throws IOException {


        this.physicalResourceId = UUID.randomUUID().toString();
        this.nameSuffix = "_" + physicalResourceId.substring(physicalResourceId.length() - 8);
        String expectedRoleName = "testIntegration" + nameSuffix;
        RocksetIntegrationResourceProperties resourceProperties = new RocksetIntegrationResourceProperties(RocksetServiceTest.integrationResourceProperties);
        resourceProperties.setAwsAccountId("123456");
        CloudformationRequest request = new CloudformationRequest(requestParams);

        when(iamFacade.createRole(any(), any(), any(), any())).thenReturn(new CreateRoleResult());
        CreatePolicyResult policyResult = new CreatePolicyResult().withPolicy(new Policy().withArn("arn::123::456"));
        when(iamFacade.createPolicy(any(), any(), any())).thenReturn(policyResult);
        when(iamFacade.attachPolicyToRole(any(), any())).thenReturn(new AttachRolePolicyResult());
        when(rocksetRestClient.createIntegration(expectedRoleName, "testIntegration" + nameSuffix,
                (resourceProperties.getIntegrationType().name() + "_" + INTEGRATION_DESCRIPTION),
                resourceProperties.getIntegrationType(),"123456")).thenReturn("success");

        // act
        CloudformationResponse response = rocksetService.createIntegration(request, resourceProperties, physicalResourceId);


        // client method called once with correct parameters
        verify(rocksetRestClient, times(1))
                .createIntegration(expectedRoleName, resourceProperties.getName() + nameSuffix,
                        (resourceProperties.getIntegrationType().name() + "_" + INTEGRATION_DESCRIPTION),
                        resourceProperties.getIntegrationType(),"123456");

        verify(iamFacade, times(1)).createPolicy(any(), any(), any());
        verify(iamFacade, times(1)).createRole(any(), any(), any(), any());

        // correct response returned
        assertEquals("successfully created rockset integration success", response.getData().get("Message").asText());


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
                "        \"arn:aws:dynamodb:*:*:table/*\",\n" +
                "        \"arn:aws:dynamodb:*:*:table/*/stream/*\"\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        verify(iamFacade, times(1)).createPolicy(any(), eq(policyDoc), any());


    }


    @Order(2)
    @Test()
    public void testRocksetClientCreateFails() throws IOException {

        String expectedRoleName = "testIntegration" + nameSuffix;
        RocksetIntegrationResourceProperties resourceProperties = new RocksetIntegrationResourceProperties(RocksetServiceTest.integrationResourceProperties);
        resourceProperties.setAwsAccountId("123456");
        CloudformationRequest request = new CloudformationRequest(requestParams);

        when(iamFacade.createRole(any(), any(), any(), any())).thenReturn(new CreateRoleResult());
        when(iamFacade.createPolicy(any(), any(), any())).thenReturn(new CreatePolicyResult().withPolicy(
                new Policy().withArn("arn::123::456")
        ));
        when(iamFacade.attachPolicyToRole(any(), any())).thenReturn(new AttachRolePolicyResult());
        when(rocksetRestClient.createIntegration(expectedRoleName, "testIntegration" + nameSuffix,
                (resourceProperties.getIntegrationType().name() + "_" + INTEGRATION_DESCRIPTION),
                resourceProperties.getIntegrationType(), resourceProperties.getAwsAccountId())).thenThrow(new IOException("ex"));

        // act
        CloudformationResponse response = rocksetService.createIntegration(request, resourceProperties, physicalResourceId);

        // error response is returned
        assertTrue(response.getData().get("Message").asText().startsWith("Error creating rockset integration: "));

    }


    @Order(3)
    @Test()
    public void testIamCreateRoleFails() throws IOException {

        String expectedRoleName = "testIntegration" + nameSuffix;
        RocksetIntegrationResourceProperties resourceProperties = new RocksetIntegrationResourceProperties(RocksetServiceTest.integrationResourceProperties);
        CloudformationRequest request = new CloudformationRequest(requestParams);

        when(iamFacade.createRole(any(), any(), any(), any())).thenThrow(new EntityAlreadyExistsException("ex"));
        when(iamFacade.createPolicy(any(), any(), any())).thenReturn(new CreatePolicyResult().withPolicy(
                new Policy().withArn("arn::123::456")
        ));
        when(iamFacade.attachPolicyToRole(any(), any())).thenReturn(new AttachRolePolicyResult());
        when(rocksetRestClient.createIntegration(expectedRoleName, "testIntegration" + nameSuffix,
                (resourceProperties.getIntegrationType().name() + "_" + INTEGRATION_DESCRIPTION),
                resourceProperties.getIntegrationType(), "123456")).thenReturn("success");

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
        RocksetIntegrationResourceProperties resourceProperties = new RocksetIntegrationResourceProperties(RocksetServiceTest.integrationResourceProperties);
        CloudformationRequest request = new CloudformationRequest(requestParams);

        List<AttachedPolicy> attachedPolicies = new ArrayList<>();
        attachedPolicies.add(new AttachedPolicy().withPolicyArn("arn::123::456::678").withPolicyName("testIntegration" + nameSuffix));
        when(iamFacade.getRolePolicies(any())).thenReturn(new ListAttachedRolePoliciesResult().withAttachedPolicies(attachedPolicies));
        when(iamFacade.detachPolicyFromRole(any(), any())).thenReturn(new DetachRolePolicyResult());
        when(iamFacade.deleteRole(any())).thenReturn(new DeleteRoleResult());
        when(iamFacade.deletePolicy(any())).thenReturn(new DeletePolicyResult());
        when(rocksetRestClient.deleteIntegration(resourceProperties.getName() + nameSuffix)).thenReturn("success");

        // act
        CloudformationResponse response = rocksetService.deleteIntegration(request, resourceProperties);

        // client method called once with correct parameters
        verify(rocksetRestClient, times(1))
                .deleteIntegration(resourceProperties.getName() + nameSuffix);

        verify(iamFacade, times(1))
                .deleteRole(any());
        verify(iamFacade, times(1))
                .deletePolicy(any());

        // correct response returned
        assertEquals("successfully deleted rockset integration success", response.getData().get("Message").asText());

    }

    @Order(5)
    @Test
    public void testCreateWorkspaceCalledWithCorrectParameters() throws Exception{
        Map<String, Object> workspaceResourceProperties = new HashMap<>();
        workspaceResourceProperties.put("Name", "testWorkspace");
        workspaceResourceProperties.put("Region", "ca-central-1");
        workspaceResourceProperties.put("ApiKeySSM", "keyssm");


        RocksetWorkspaceResourceProperties resourceProperties = new RocksetWorkspaceResourceProperties(workspaceResourceProperties);
        requestParams.put("ResourceProperties", workspaceResourceProperties);
        CloudformationRequest request = new CloudformationRequest(requestParams);


        when(rocksetRestClient.createWorkspace("testWorkspace", "")).thenReturn("success");

        // act
        CloudformationResponse response = rocksetService.createWorkspace(request, resourceProperties, physicalResourceId);

        //assert
        assertEquals("successfully created rockset workspace: success", response.getData().get("Message").asText());
    }


    @Order(6)
    @Test()
    public void testDeleteWorkspaceCalledWithCorrectParameters() throws IOException {

        Map<String, Object> workspaceResourceProperties = new HashMap<>();
        workspaceResourceProperties.put("Name", "testWorkspace");
        workspaceResourceProperties.put("Region", "ca-central-1");
        workspaceResourceProperties.put("ApiKeySSM", "keyssm");
        workspaceResourceProperties.put("RocksetAccountId", "1234567890");
        requestParams.put("RequestType", "Delete");
        requestParams.put("PhysicalResourceId", physicalResourceId);
        RocksetWorkspaceResourceProperties resourceProperties = new RocksetWorkspaceResourceProperties(workspaceResourceProperties);
        CloudformationRequest request = new CloudformationRequest(requestParams);

        when(rocksetRestClient.deleteWorkspace(resourceProperties.getName())).thenReturn("success");

        // act
        CloudformationResponse response = rocksetService.deleteWorkspace(request, resourceProperties, physicalResourceId);

        // client method called once with correct parameters
        verify(rocksetRestClient, times(1))
                .deleteWorkspace(resourceProperties.getName());
        // correct response returned
        assertEquals("successfully deleted rockset workspace: success", response.getData().get("Message").asText());

    }

    @Order(7)
    @Test
    public void testCreateCollectionIsCalledWithCorrectParameters() throws IOException {
        Map<String, Object> collectionRp = new HashMap<>();
        collectionRp.put("Name", "testCollection");
        collectionRp.put("Region", "ca-central-1");
        collectionRp.put("ApiKeySSM", "keyssm");
        collectionRp.put("Workspace", "commons");
        collectionRp.put("IntegrationName", "myIntegration");
        collectionRp.put("Stage", "staging");
        Map<String, Object> dynamods = new HashMap<>();
        dynamods.put("DynamoDbTableName", "myTable");
        dynamods.put("DynamoDbAwsRegion", "ca-central-1");
        dynamods.put("DynamoDbRcu", "5");
        collectionRp.put("DynamoDbDataSource",dynamods );
        collectionRp.put("RetentionTime", 100);

        RocksetCollectionResourceProperties resourceProperties = new RocksetCollectionResourceProperties(collectionRp);
        requestParams.put("ResourceProperties", collectionRp);
        requestParams.put("RequestType", "Create");
        CloudformationRequest request = new CloudformationRequest(requestParams);

        DynamoDbDataSource dds = new DynamoDbDataSource();
        dds.parse(dynamods);
        List<DataSource> ds = new ArrayList<>();
        ds.add(dds);
        when(rocksetRestClient.createCollection("testCollection", "commons",
                "",resourceProperties.getDataSources(), "myIntegration",
                Optional.of(100l), Optional.empty(), Optional.empty(),Optional.empty(), Optional.empty())).thenReturn("created_at");

        // act
        CloudformationResponse response = rocksetService.createCollection(request, resourceProperties, UUID.randomUUID().toString(), false);

        //assert
        assertEquals("successfully created rockset collection: created_at", response.getData().get("Message").asText());
    }

    @Order(8)
    @Test
    public void fieldMappingTest(){
        Map<String, Object> collectionRp = new HashMap<>();
        collectionRp.put("Name", "testCollection");
        collectionRp.put("Region", "ca-central-1");
        collectionRp.put("ApiKeySSM", "keyssm");
        collectionRp.put("Workspace", "commons");
        collectionRp.put("IntegrationName", "myIntegration");
        collectionRp.put("Stage", "staging");

        List<Object> fmList = new ArrayList<>();
        Map<String, Object> fm = new HashMap<>();

        List<Object> fmInputFields = new ArrayList<>();
        Map<String, Object> inputField = new HashMap<>();
        inputField.put("FieldName", "address.city.zipcode");
        inputField.put("IfMissing", "[\"SKIP\", \"PASS\"]");
        inputField.put("IsDrop", true);
        inputField.put("Param", "zip");
        fmInputFields.add(inputField);

        Map<String, Object> output = new HashMap<>();
        output.put("FieldName", "outField");
        output.put("Value", "SHA256(:zip)");
        output.put("OnError", "[\"FAIL\"]");

        fm.put("Name", "myTestMapping");
        fm.put("InputFields", fmInputFields);
        fm.put("OutputField", output);
        fmList.add(fm);

        collectionRp.put("FieldMappings", fmList);

        RocksetCollectionResourceProperties collectionResourceProperties = new RocksetCollectionResourceProperties(collectionRp);
        List<FieldMapping> expectedFieldMapping = new ArrayList<>();
        FieldMapping fme = new FieldMapping();
        fme.setName("myTestMapping");
        List<FieldMapping.Action> actions = Stream.of(FieldMapping.Action.SKIP, FieldMapping.Action.PASS).collect(Collectors.toList());
        fme.addInputField("address.city.zipcode", actions, true, "zip");
        fme.setOutputField("outField", "SHA256(:zip)", Stream.of(FieldMapping.Action.FAIL).collect(Collectors.toList()));
        expectedFieldMapping.add(fme);

        assertEquals(collectionResourceProperties.getFieldMappingList().get().get(0).getName(), expectedFieldMapping.get(0).getName());
        assertEquals(collectionResourceProperties.getFieldMappingList().get().get(0).getInputFields().get(0).getFieldName(), expectedFieldMapping.get(0).getInputFields().get(0).getFieldName());
        assertEquals(collectionResourceProperties.getFieldMappingList().get().get(0).getInputFields().get(0).getIfMissing(), expectedFieldMapping.get(0).getInputFields().get(0).getIfMissing());
        assertEquals(collectionResourceProperties.getFieldMappingList().get().get(0).getInputFields().get(0).getParam(), expectedFieldMapping.get(0).getInputFields().get(0).getParam());
        assertEquals(collectionResourceProperties.getFieldMappingList().get().get(0).getOutputField().getFieldName(), expectedFieldMapping.get(0).getOutputField().getFieldName());
        assertEquals(collectionResourceProperties.getFieldMappingList().get().get(0).getOutputField().getOnError(), expectedFieldMapping.get(0).getOutputField().getOnError());
        assertEquals(collectionResourceProperties.getFieldMappingList().get().get(0).getOutputField().getValue(), expectedFieldMapping.get(0).getOutputField().getValue());

    }
}
