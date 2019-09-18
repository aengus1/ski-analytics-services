package ski.crunch.aws;

import com.amazonaws.services.identitymanagement.model.Tag;
import com.amazonaws.services.identitymanagement.model.*;
import org.junit.jupiter.api.*;
import ski.crunch.testhelpers.AbstractAwsTest;
import ski.crunch.testhelpers.IntegrationTestHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class IAMFacadeTest extends AbstractAwsTest {

    private IntegrationTestHelper integrationTestHelper;
    private IAMFacade iam;

    private String policyArn;
    private String roleArn;

    private static String ROLE_NAME="test-role";
    private static String POLICY =
            "{" +
                    "  \"Version\": \"2012-10-17\"," +
                    "  \"Statement\": [" +
                    "    {" +
                    "        \"Effect\": \"Allow\"," +
                    "        \"Action\": \"logs:CreateLogGroup\"," +
                    "        \"Resource\": \"*\"" +
                    "    }," +
                    "    {" +
                    "        \"Effect\": \"Allow\"," +
                    "        \"Action\": [" +
                    "            \"dynamodb:DeleteItem\"," +
                    "            \"dynamodb:GetItem\"," +
                    "            \"dynamodb:PutItem\"," +
                    "            \"dynamodb:Scan\"," +
                    "            \"dynamodb:UpdateItem\"" +
                    "       ]," +
                    "       \"Resource\": [ \"arn:aws:dynamodb:*:*:table/staging-crunch-User\" ]" +
                    "    }" +
                    "   ]" +
                    "}";

    private static String ASSUME_ROLE_POLICY_DOCUMENT =
                "{" +
                    "                \"Version\": \"2012-10-17\"," +
                    "                \"Statement\": [" +
                    "                    {" +
                    "                        \"Effect\": \"Allow\"," +
                    "                        \"Principal\": {" +
                    "                            \"Service\": [" +
                    "                                \"lambda.amazonaws.com\"" +
                    "                            ]" +
                    "                        }," +
                    "                        \"Action\": [" +
                    "                            \"sts:AssumeRole\"" +
                    "                        ]" +
                    "                    }" +
                    "                ]"
                +"}";

    @BeforeEach()
    public void setup() {
        super.setup();
        try {
            integrationTestHelper = new IntegrationTestHelper();
             iam = new IAMFacade(integrationTestHelper.getApiRegion());
        }catch(Exception ex ){
            LOG.error("error setting up test", ex);
            assertTrue(false);
        }
    }

    @Test
    @Order(1)
    public void testCreateRole(){
        List<Tag> tags = new ArrayList<>();
        Tag stage = new Tag();
        stage.setKey("environment");
        stage.setValue("staging");
        tags.add(stage);
        CreateRoleResult result = iam.createRole(ROLE_NAME, ASSUME_ROLE_POLICY_DOCUMENT, "this is a test role created by integration test for IAMFacade", tags);
        roleArn = result.getRole().getArn();
        LOG.info("create role result: " + roleArn);

        String findRole = iam.getRole(ROLE_NAME).getRole().getArn();
        assertEquals(roleArn, findRole);

    }

    @Test
    @Order(2)
    public void testCreatePolicy() throws EntityAlreadyExistsException {
        CreatePolicyResult result = iam.createPolicy("test-policy", POLICY,
                "test policy - iamfacade integration test");
        policyArn = result.getPolicy().getArn();
        assertNotNull(policyArn);
        assertEquals(iam.getPolicy(policyArn).getPolicy().getArn(), policyArn);
    }

    @Test
    @Order(3)
    public void testAttachPolicyToRole() {
        AttachRolePolicyResult result = iam.attachPolicyToRole(policyArn, ROLE_NAME);
        String foundArn = iam.getRolePolicies(ROLE_NAME).getAttachedPolicies().stream()
                .map( x -> x.getPolicyArn())
                .filter( x -> x.equals(policyArn))
                .findFirst().get();
        assertEquals(foundArn, policyArn);
    }

    @Test
    @Order(4)
    public void testDetachPolicyFromRole() {
        DetachRolePolicyResult result = iam.detachPolicyFromRole(policyArn, ROLE_NAME);
        Optional<String> foundArn= iam.getRolePolicies(ROLE_NAME).getAttachedPolicies().stream()
                .map( x -> x.getPolicyArn())
                .filter( x -> x.equals(policyArn))
                .findFirst();

        assertEquals(Optional.empty(), foundArn);
    }

    @Test
    @Order(5)
    public void testDeletePolicy(){
        DeletePolicyResult result = iam.deletePolicy(policyArn);
        LOG.info("delete policy result: " + result.toString());
        assertThrows(NoSuchEntityException.class, () -> iam.getPolicy(policyArn));
    }



    @Test
    @Order(6)
    public void testDeleteRole(){
        DeleteRoleResult result = iam.deleteRole(ROLE_NAME);
        roleArn = result.toString();
        LOG.info("delete role result: " + result.toString());
        assertThrows(NoSuchEntityException.class, () -> iam.getRole(ROLE_NAME).getRole().getRoleName());

    }
}
