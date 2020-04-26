package ski.crunch.auth;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.cognitoidp.model.AdminGetUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminGetUserResult;
import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.amazonaws.services.cognitoidp.model.UserNotFoundException;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ski.crunch.auth.utils.PasswordUtil;
import ski.crunch.aws.CognitoFacade;
import ski.crunch.aws.DynamoFacade;
import ski.crunch.dao.UserDAO;
import ski.crunch.model.UserSettingsItem;
import ski.crunch.testhelpers.IntegrationTestHelper;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AuthIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(AuthIntegrationTest.class);

    private String accessKey = null;
    private String cognitoId = null;
    private String initialPwHash = null;
    private final String devUserName = "success@simulator.amazonses.com";
    private final String devPassword = "authTestPassword123";
    private IntegrationTestHelper helper;
    private DynamoFacade dynamo = null;
    private UserDAO userDAO = null;
    private CognitoFacade cognitoFacade = null;



    @BeforeAll
    public void setup() throws IOException {
        // sign up a user

        this.helper = new IntegrationTestHelper();
        this.cognitoId = helper.signup(devUserName, devPassword, true).orElseThrow(() -> new RuntimeException("Error occurred signing up"));

        String authRegion = helper.getServerlessState(helper.getPrefix()+"auth").getRegion();
        ProfileCredentialsProvider profileCredentialsProvider = helper.getCredentialsProvider();
        System.out.println("auth region = " + authRegion);

        this.dynamo = new DynamoFacade(helper.getServerlessState(helper.getPrefix()+"auth").getRegion(),
                helper.getUserTable(), helper.getCredentialsProvider());
        this.userDAO = new UserDAO(dynamo, helper.getUserTable());
        this.cognitoFacade = new CognitoFacade(authRegion);
    }

    @Order(1)
    @Test
    public void testSignup() {

        // verify that user settings item is created
        Optional<UserSettingsItem> user = userDAO.getUserSettings(cognitoId);
        if(!user.isPresent()){
            fail("user " + cognitoId + " not found");
        }

        // verify that password hash is captured in dynamodb
        initialPwHash = user.get().getPwhash();
        assertNotNull(user.get().getPwhash());
        assertNotEquals("", user.get().getPwhash());

        // verify that password verification is working correctly
        assertTrue(PasswordUtil.verifyPassword(user.get().getPwhash(), devPassword));

    }

    @Order(2)
    @Test
    public void testForgotPassword() {
        String userPoolId = helper.getUserPoolId();
        helper.resetPassword(devUserName, "newPassword123", userPoolId);
        // verify that password hash is updated
        UserSettingsItem userSettingsItem = userDAO.getUserSettings(cognitoId).get();
        assertNotEquals(userSettingsItem.getPwhash(), initialPwHash);

    }

    @Order(3)
    @Test
    public void testUserMigrationIsSecure() {
        String nonExistantUser = "notArealUser";
        String nonExistantUsersPassword = "fakePassword12!";
        // verify that user migration will not allow a non-existent user to sign in
        assertThrows(Exception.class, () -> helper.retrieveAccessToken(nonExistantUser, nonExistantUsersPassword));

        // verify that user migration will not allow a user with the wrong password to sign in
        assertThrows(Exception.class, () -> helper.retrieveAccessToken(devUserName, nonExistantUsersPassword));
    }

    @Order(4)
    @Test
    public void testUserMigration() throws UserNotFoundException, Exception {
        // delete the user from cognito
            // attempt sign in
            //String accessToken = helper.signIn(devUserName, devPassword);
            assertThrows(Exception.class, () -> {
                String accessToken = helper.signIn("success-user1@simulator.amazonses.com", "crapPass12!");
                System.out.println("access token = " + accessToken);
                // verify that sign in is successful
                assertNull(accessToken);
            });

    }

    @Order(5)
    @Test
    public void testUserMigrationRecreatesUser() throws UserNotFoundException, Exception {
        // delete the user from cognito
        helper.destroyUser(devUserName);
        try {
            // attempt sign in
            //String accessToken = helper.signIn(devUserName, devPassword);
            String clientId = helper.signIn(devUserName, "crapPass12!");
            System.out.println("client id = " + clientId);
            // verify that sign in is successful
            assertNotNull(clientId);
        }catch(Exception ex) {
            fail("expected user migration trigger to allow sign in", ex);
        }

        // verify that user is recreated in cognito
        AdminGetUserRequest adminGetUserRequest = new AdminGetUserRequest().withUserPoolId(helper.getUserPoolId()).withUsername(devUserName);
        AdminGetUserResult result = cognitoFacade.adminGetUser(adminGetUserRequest);
        assertEquals("CONFIRMED", result.getUserStatus());

        // verify that the user attributes are the same as they were prior to initial deletion
        if( result.getUserAttributes().stream().filter(x -> x.getName().equals(("email"))).findFirst().isPresent() ) {

            Optional<AttributeType> email = result.getUserAttributes().stream().filter(x -> x.getName().equals("email")).findFirst();
            Optional<AttributeType> familyName = result.getUserAttributes().stream().filter(x -> x.getName().equals("custom:familyName")).findFirst();

            assertTrue(email.isPresent());
            //assertTrue(familyName.isPresent());

            UserSettingsItem user = userDAO.lookupUser(cognitoId);
            assertEquals(user.getEmail(), email.get().getValue());
            //assertEquals(user.getLastName(), familyName);
            //assertEquals(cognitoId, result.getUsername());

        }
    }

    @AfterAll
    public void tearDown() {
        try {
            helper.destroyUser(devUserName);
            helper.removeUserSettings(cognitoId);
        }catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}
