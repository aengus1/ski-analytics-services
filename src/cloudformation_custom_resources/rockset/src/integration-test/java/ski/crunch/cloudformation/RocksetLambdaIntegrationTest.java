package ski.crunch.cloudformation;

import org.junit.jupiter.api.*;
import ski.crunch.aws.IAMFacade;
import ski.crunch.cloudformation.rockset.RocksetIntegrationLambda;
import ski.crunch.testhelpers.IntegrationTestHelper;
import ski.crunch.testhelpers.IntegrationTestPropertiesReader;
import ski.crunch.testhelpers.ServerlessState;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RocksetLambdaIntegrationTest {


    private ServerlessState cfRocksetServerlessState;
    private Map<String, Object> parameters;
    private String physicalIntegrationResourceId;
    private final static String INTEGRATION_NAME = "integration-test-integration";

    /**
     * To use this test make sure that the correct externalId and rocksetAccountId params are set
     * @throws IOException
     */
    public RocksetLambdaIntegrationTest() throws IOException {
        IntegrationTestHelper testHelper = new IntegrationTestHelper();
        this.cfRocksetServerlessState = testHelper.getServerlessStateWithoutPrefix("cloudformation_custom_resources/rockset");


        String apiSSM = cfRocksetServerlessState.getRootNode()
                .path("service")
                .path("resources")
                .path("Resources")
                .path("SsmParamForRocksetApiKey")
                .path("Properties")
                .path("Name").asText();


        parameters = new HashMap<>();
        parameters.put("Region", cfRocksetServerlessState.getRegion());
        parameters.put("Stage", "staging");
        parameters.put("RequestType", "CREATE");
        parameters.put("ResponseURL", "http://myResponseUrl");
        parameters.put("StackId", "stack-12345");
        parameters.put("RequestId", "12345-12345-12345");
        parameters.put("LogicalResourceId", "78910-12345");
        parameters.put("ResourceType", "Custom::RocksetIntegration");

        List<String> accessibleResources = new ArrayList<>();
        accessibleResources.add("arn:aws:dynamodb:*:*:table/*");
        accessibleResources.add("arn:aws:dynamodb:*:*:table/*/stream/*");

        List<LinkedHashMap<String, String>> tags = new ArrayList<>();
        LinkedHashMap<String, String> stage = new LinkedHashMap<>();
        stage.put("Key", "stage");
        stage.put("Value", "staging");

        tags.add(stage);


        HashMap<String, Object> resourceProperties = new HashMap<>();
        resourceProperties.put("Region", cfRocksetServerlessState.getRegion());
        resourceProperties.put("Name", INTEGRATION_NAME);
        resourceProperties.put("ApiKeySSM", apiSSM);
        //SET THESE
        resourceProperties.put("ExternalId", IntegrationTestPropertiesReader.get("externalId"));
        resourceProperties.put("RocksetAccountId", IntegrationTestPropertiesReader.get("rockset-account-id"));
        //
        resourceProperties.put("IntegrationType", "dynamodb");
        resourceProperties.put("AccessibleResources", accessibleResources);
        resourceProperties.put("Tags", tags);

        parameters.put("ResourceProperties", resourceProperties);

    }


    @Order(1)
    @Test()
    public void testRocksetCreation() throws Exception {

        try {
            CloudformationRequest request = new CloudformationRequest(parameters);
            RocksetIntegrationLambda rocksetIntegrationLambda = new RocksetIntegrationLambda();
            CloudformationResponse response = rocksetIntegrationLambda.doCreate(request);
            this.physicalIntegrationResourceId = response.getPhysicalResourceId();
            System.out.println("Create response message: " + response.getData().get("Message"));

            assertTrue(response.getData().get("Message").asText().contains("created_at"));
        }catch(Exception ex){
            fail("no exception expected", ex);
        }


    }

//    @Order(2)
//    @Test()
//    public void testRocksetUpdateIntegrationName() throws IOException {
//
//        Map<String, String> newParameters = new HashMap<>();
//        newParameters.put("Region", cfStackSS.getRegion());
//        newParameters.put("Stage", "staging");
//        newParameters.put("IntegrationName", "NewIntegrationName");
//        newParameters.put("RocksetAPISSM", apiSSM);
//        newParameters.put("RocksetUserName", userName);
//
//        String physicalId = rocksetClientIntegrationFacade.updateIntegration(newParameters, parameters);
//        Assert.assertEquals("NewIntegrationName", physicalId.substring(0, physicalId.indexOf("_")));
//
//        Assert.assertThrows(Exception.class, () -> {
//            rocksetClientIntegrationFacade.getIntegration(INTEGRATION_NAME);
//        });
//
//        try {
//            rocksetClientIntegrationFacade.getIntegration("NewIntegrationName");
//        } catch (Exception ex) {
//            fail("Expected new integration to exist ");
//        } finally{
//            parameters.put("IntegrationName", "NewIntegrationName");
//        }
//    }
//
//    @Order(3)
//    @Test()
//    public void testRocksetUpdateUserName() throws IOException {
//
//
//        Map<String, String> newParameters = new HashMap<>();
//        newParameters.put("Region", cfStackSS.getRegion());
//        newParameters.put("Stage", "staging");
//        newParameters.put("IntegrationName", "NewIntegrationName");
//        newParameters.put("RocksetAPISSM", apiSSM);
//        newParameters.put("RocksetUserName", "updatedRocksetUserName");
//
//        parameters.put("RocksetUserName", userName);
//
//        rocksetClientIntegrationFacade.updateIntegration(newParameters, parameters);
//        IAMFacade iam = new IAMFacade(cfStackSS.getRegion());
//        Assert.assertThrows(NoSuchEntityException.class, () -> {
//            iam.getUser(userName);
//        });
//        try {
//            iam.getUser("updatedRocksetUserName").getUser();
//        } catch (Exception ex) {
//            fail("expected new iam user to exist");
//        } finally {
//            parameters.put("RocksetUserName", "updatedRocksetUserName");
//        }
//
//    }
//

    @Order(4)
    @Test()
    public void testRocksetDeletion() throws IOException {

        assertNotNull(physicalIntegrationResourceId);
        parameters.put("RequestType", "DELETE");
        parameters.put("PhysicalResourceId", physicalIntegrationResourceId);
        //parameters.put("PhysicalResourceId", "0012345678");
        CloudformationRequest request = new CloudformationRequest(parameters);

        try {
            RocksetIntegrationLambda rocksetIntegrationLambda = new RocksetIntegrationLambda();
            CloudformationResponse response = rocksetIntegrationLambda.doDelete(request);
            System.out.println("Delete response message: " + response.getData().get("Message"));

            assertTrue(response.getData().get("Message").asText().contains("created_at"));
        } catch (Exception ex) {
            fail("Deleting integration should not throw an exception", ex);
        }

    }

    @AfterAll()
    public void tearDown() {
        IAMFacade iamFacade = new IAMFacade(this.cfRocksetServerlessState.getRegion());

        // ensure iam resources are properly deleted
        try{
            String arn = iamFacade.getRolePolicies("integration-test-integration_45-12345")
                    .getAttachedPolicies()
                    .stream()
                    .findFirst()
                    .get().getPolicyArn();
            iamFacade.detachPolicyFromRole(arn , "integration-test-integration_45-12345");
        }catch(Exception ex){

        }
        try{
            iamFacade.deleteRole("integration-test-integration_45-12345");
        }catch(Exception ex){

        }

        try{
            iamFacade.deletePolicy("integration-test-integration_45-12345");
        }catch(Exception ex){

        }

    }


}
