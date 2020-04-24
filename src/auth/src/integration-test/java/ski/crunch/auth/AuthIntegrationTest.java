package ski.crunch.auth;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ski.crunch.auth.utils.PasswordUtil;
import ski.crunch.aws.DynamoFacade;
import ski.crunch.dao.UserDAO;
import ski.crunch.model.UserSettingsItem;
import ski.crunch.testhelpers.IntegrationTestHelper;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AuthIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(AuthIntegrationTest.class);

    private String accessKey = null;
    private String cognitoId = null;
    private String initialPwHash = null;
    private final String devUserName = "authTestUser@test.com";
    private final String devPassword = "authTestPassword123";
    private IntegrationTestHelper helper;
    private DynamoFacade dynamo = null;
    private UserDAO userDAO = null;



    @BeforeAll
    public void setup() throws IOException {
        // sign up a user

        this.helper = new IntegrationTestHelper();
        this.cognitoId = helper.signup(devUserName, devPassword).orElseThrow(() -> new RuntimeException("Error occurred signing up"));
        this.accessKey = helper.retrieveAccessToken(devUserName, devPassword);
        logger.info("ACCESS KEY: " + this.accessKey);

        String authRegion = helper.getServerlessState(helper.getPrefix()+"auth").getRegion();
        ProfileCredentialsProvider profileCredentialsProvider = helper.getCredentialsProvider();
        System.out.println("auth region = " + authRegion);

        this.dynamo = new DynamoFacade(helper.getServerlessState(helper.getPrefix()+"auth").getRegion(),
                helper.getUserTable(), helper.getCredentialsProvider());
        this.userDAO = new UserDAO(dynamo, helper.getUserTable());
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
        // verify that user migration will not allow a non-existent user to sign in
        // verify that user migration will not allow a user with the wrong password to sign in
    }

    @Test
    public void testUserMigrationRecreatesUser() {
        // delete the user from cognito
        // attempt sign in
        // verify that sign in is successful
        // verify that user is recreated in cognito
        // verify that the user attributes are the same as they were prior to initial deletion
    }

    @AfterAll
    public void tearDown() {
        try {
            //helper.destroyUser(devUserName);
        }catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}
