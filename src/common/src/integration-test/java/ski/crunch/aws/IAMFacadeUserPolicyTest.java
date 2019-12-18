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
public class IAMFacadeUserPolicyTest extends AbstractAwsTest {

    private IntegrationTestHelper integrationTestHelper;
    private IAMFacade iam;

    private String policyArn;
    private String userArn;
    private String accessKeyId;

    private static String USER_NAME="test-user";
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


    @BeforeAll()
    public void setUp() {
        super.setup();
        try {
            integrationTestHelper = new IntegrationTestHelper();
             iam = new IAMFacade(integrationTestHelper.getApiRegion());
        }catch(Exception ex ){
            LOG.error("error setting up test", ex);
            fail("error setting up test");
        }
    }

    @Test
    @Order(1)
    public void testCreateUser(){
        List<Tag> tags = new ArrayList<>();
        Tag stage = new Tag();
        stage.setKey("environment");
        stage.setValue("staging");
        tags.add(stage);
        CreateUserResult result = iam.createUser(USER_NAME, "/", tags);
        userArn = result.getUser().getArn();
        LOG.info("create user result: " + userArn);

        String findRole = iam.getUser(USER_NAME).getUser().getArn();
        assertEquals(userArn, findRole);

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
    public void testAttachPolicyToUser() {
        AttachUserPolicyResult result = iam.attachPolicyToUser(policyArn, USER_NAME);
        String foundArn = iam.getUserPolicies(USER_NAME).getAttachedPolicies().stream()
                .map( x -> x.getPolicyArn())
                .filter( x -> x.equals(policyArn))
                .findFirst().get();
        assertEquals(foundArn, policyArn);
    }

    @Test
    @Order(4)
    public void testDetachPolicyFromRole() {
        DetachUserPolicyResult result = iam.detachPolicyFromUser(policyArn, USER_NAME);
        Optional<String> foundArn= iam.getUserPolicies(USER_NAME).getAttachedPolicies().stream()
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
    public void testCreateAccessKey() {
        CreateAccessKeyResult result = iam.createAccessKey(USER_NAME);
        accessKeyId  = result.getAccessKey().getAccessKeyId();
        ListAccessKeysResult keysResult = iam.listAccessKeys(USER_NAME);
        assertNotNull(keysResult);
        assertEquals(keysResult.getAccessKeyMetadata().size(), 1);
        assertEquals(keysResult.getAccessKeyMetadata().get(0).getAccessKeyId(), accessKeyId);
    }


    @Test
    @Order(7)
    public void testDeleteAccessKey() {
        DeleteAccessKeyResult result = iam.deleteAccessKey(USER_NAME, accessKeyId);
        accessKeyId  = null;
        ListAccessKeysResult keysResult = iam.listAccessKeys(USER_NAME);
        assertNotNull(keysResult);
        assertEquals(keysResult.getAccessKeyMetadata().size(), 0);
    }

    @Test
    @Order(8)
    public void testDeleteUser(){
        DeleteUserResult result = iam.deleteUser(USER_NAME);
        userArn = result.toString();
        LOG.info("delete user result: " + result.toString());
        assertThrows(NoSuchEntityException.class, () -> iam.getUser(USER_NAME).getUser().getUserName());

    }

    @AfterAll()
    public void tearDown() {
        try {
            if(iam.getUser(USER_NAME).getUser() != null && !iam.listAccessKeys(USER_NAME).getAccessKeyMetadata().isEmpty()){
                LOG.info("deleting user access key");
                iam.deleteAccessKey(USER_NAME, accessKeyId);
            }
            if(iam.getUser(USER_NAME).getUser() != null) {
                LOG.info("deleting user");
                iam.deleteRole(USER_NAME);
            }
        }catch(Exception ignored ) {}

        try {
            if(iam.getPolicy(policyArn).getPolicy() != null) {
                LOG.info("deleting policy");
                iam.deletePolicy(policyArn);
            }
        }catch (Exception ignored) { }
    }
}
