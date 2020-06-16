package ski.crunch.auth;

import com.amazonaws.services.cognitoidp.model.AdminGetUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminGetUserResult;
import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.amazonaws.services.cognitoidp.model.UserNotFoundException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import ski.crunch.auth.utils.PasswordUtil;
import ski.crunch.aws.CognitoFacade;
import ski.crunch.aws.DynamoFacade;
import ski.crunch.dao.UserDAO;
import ski.crunch.model.UserSettingsItem;
import ski.crunch.testhelpers.IntegrationTestHelper;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class provides integration tests for the cognito triggers that are set up.  Collectively they verify that
 * the user migration trigger, post confirmation trigger and pre-signup triggers are behaving as expected.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Execution(ExecutionMode.SAME_THREAD)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class AuthIntegrationTest {


    private String cognitoId = null;
    private String initialPwHash = null;
    private final String devUserName = "success-user1@simulator.amazonses.com";
    private final String devPassword = "authTestPassword123";
    private IntegrationTestHelper helper;
    private DynamoFacade dynamo = null;
    private UserDAO userDAO = null;
    private CognitoFacade cognitoFacade = null;



    @BeforeEach
    public void setup() throws IOException {
        // sign up a user

        this.helper = new IntegrationTestHelper();
        this.cognitoId = helper.signup(devUserName, devPassword, true).orElseThrow(() -> new RuntimeException("Error occurred signing up"));

        String authRegion = helper.getServerlessState(helper.getPrefix()+"auth").getRegion();

        this.dynamo = new DynamoFacade(helper.getServerlessState(helper.getPrefix()+"auth").getRegion(),
                helper.getUserTable(), helper.getCredentialsProvider());
        this.userDAO = new UserDAO(dynamo, helper.getUserTable());
        this.cognitoFacade = new CognitoFacade(authRegion);
    }

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

    @Test
    public void testForgotPassword() {
        String userPoolId = helper.getUserPoolId();
        helper.resetPassword(devUserName, "newPassword123", userPoolId);
        // verify that password hash is updated
        UserSettingsItem userSettingsItem = userDAO.getUserSettings(cognitoId).get();
        assertNotEquals(userSettingsItem.getPwhash(), initialPwHash);

    }

    @Test
    public void testUserMigrationIsSecure() {
        String nonExistantUser = "notArealUser";
        String nonExistantUsersPassword = "fakePassword12!";
        // verify that user migration will not allow a non-existent user to sign in
        assertThrows(Exception.class, () -> helper.retrieveAccessToken(nonExistantUser, nonExistantUsersPassword));

        // verify that user migration will not allow a user with the wrong password to sign in
        assertThrows(Exception.class, () -> helper.retrieveAccessToken(devUserName, nonExistantUsersPassword));
    }

    @Test
    public void testUserMigration() throws UserNotFoundException, Exception {
        // delete the user from cognito
            helper.destroyUser(devUserName);
            // attempt sign in
            assertThrows(Exception.class, () -> {
                String accessToken = helper.signIn("success-user1@simulator.amazonses.com", "crapPass12!");
                System.out.println("access token = " + accessToken);
                // verify that sign in is successful
                assertNull(accessToken);
            });

    }


    @Test
    public void testUserMigrationRecreatesUser() throws UserNotFoundException, Exception {

        // delete the user from cognito
        helper.destroyUser(devUserName);
        try {
            // attempt sign in
            //String accessToken = helper.signIn(devUserName, devPassword);
            String accessToken = helper.signIn(devUserName, devPassword);
            System.out.println("access token = " + accessToken);
            // verify that sign in is successful
            assertNotNull(accessToken);
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

            result.getUserAttributes().stream().forEach(x -> System.out.println(x.getName()));

            assertTrue(email.isPresent());
            //assertTrue(familyName.isPresent());

            UserSettingsItem user = userDAO.lookupUser(cognitoId);
            assertEquals(user.getEmail(), email.get().getValue());
            //assertEquals(user.getLastName(), familyName);
            //assertEquals(cognitoId, result.getUsername());

        } else {
            fail("did not find email user attribute");
        }

        // remove the user settings item
        try{
            dynamo.getMapper().delete(userDAO.lookupUser(devUserName));
        } catch (Exception ex) {

        }
    }


    @Test
    public void testUserMigrationDisallowsWrongPassword() throws UserNotFoundException, Exception {
        // delete the user from cognito
        helper.destroyUser(devUserName);
        assertThrows(Exception.class, () -> {
            helper.signIn(devUserName, "wrongPassword12!");
        });

    }


    @AfterEach
    public void tearDown() {
        try {
            helper.destroyUser(devUserName);
        }catch(Exception ex) {
            ex.printStackTrace();
        }

        try {
            helper.removeUserSettings(cognitoId);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}
