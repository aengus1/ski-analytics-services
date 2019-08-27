package ski.crunch.testhelpers;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import org.apache.log4j.Logger;
import ski.crunch.aws.DynamoDBService;
import ski.crunch.model.UserSettingsItem;
import ski.crunch.utils.NotFoundException;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class IntegrationTestHelper {
    //todo -> read these from a properties file in resources folder
    private static final String[] INCLUDE_MODULES = {"api", "auth", "common", "websocket"};
    private static final String AWS_PROFILE = "backend_dev";
    private static final String AUTH_STACK_NAME = "staging-ski-analytics-authentication-stack";
    private static final String WEBSOCKET_STACK_NAME = "staging-ski-analytics-websocket-stack";

    private static final Logger LOG = Logger.getLogger(IntegrationTestHelper.class);
    private static final String INTEGRATION_TEST_USERNAME = "integration_test_user@crunch.ski";
    private static final String INTEGRATION_TEST_PASSWORD = "abC123Def!";

    private ProfileCredentialsProvider credentialsProvider;
    private CloudFormationHelper cfHelper;
    private AuthenticationHelper authHelper;

    private Map<String, ServerlessState> serverlessStateMap = new HashMap<>();

    public IntegrationTestHelper() throws IOException {
        readServerlessState();
        String region = serverlessStateMap.get("auth").getRegion();

        credentialsProvider = new ProfileCredentialsProvider(AWS_PROFILE);
        cfHelper = new CloudFormationHelper(credentialsProvider, region);
        authHelper = new AuthenticationHelper(
                cfHelper.getStackOutput(AUTH_STACK_NAME, "UserPoolArn"),
                cfHelper.getStackOutput(AUTH_STACK_NAME, "UserPoolClientId"),
                "",
                region,
                AWS_PROFILE);

    }

    public String getWebsocketEndpoint() {
        //return serverlessStateMap.get("websocket").getWebsocketEndpoint();
        return cfHelper.getStackOutput(WEBSOCKET_STACK_NAME, "ServiceEndpointWebsocket");
    }

    public Optional<String> signup() {
        return authHelper.performAdminSignup(INTEGRATION_TEST_USERNAME, INTEGRATION_TEST_PASSWORD);

    }

    public String retrieveAccessToken() {
        return authHelper.performSRPAuthentication(INTEGRATION_TEST_USERNAME, INTEGRATION_TEST_PASSWORD);
    }

    public void insertUserSettings(String userId) {
        DynamoDBService dynamo = new DynamoDBService(
                serverlessStateMap.get("auth").getRegion(),
                cfHelper.getStackOutput(AUTH_STACK_NAME, "UserTableName"),
                credentialsProvider
        );

        try {
            UserSettingsItem userSettings = new UserSettingsItem();
            userSettings.setId(userId);
            userSettings.setGender("");
            userSettings.setHeight(0);
            userSettings.setWeight(0);
            List<Integer> zones = new ArrayList<>();
            Collections.addAll(zones, 60, 130, 145, 150, 171, 190);
            userSettings.setHrZones(zones);

            dynamo.getMapper().save(userSettings);
        } catch (Exception e) {
            LOG.error("Error writing user settings", e);

        }
    }

    public void removeUserSettings(String userId) {
        DynamoDBService dynamo = new DynamoDBService(
                serverlessStateMap.get("auth").getRegion(),
                cfHelper.getStackOutput(AUTH_STACK_NAME, "UserTableName"),
                credentialsProvider
        );

        try {
            UserSettingsItem userSettings = new UserSettingsItem();
            userSettings.setId(userId);

            dynamo.getMapper().delete(userSettings);
        } catch (Exception e) {
            LOG.error("Error writing user settings", e);

        }
    }

    public String getUsersWebsocketConnectionId(String userId) {
        DynamoDBService dynamo = new DynamoDBService(
                serverlessStateMap.get("auth").getRegion(),
                cfHelper.getStackOutput(AUTH_STACK_NAME, "UserTableName"),
                credentialsProvider
        );



        UserSettingsItem userSettings = new UserSettingsItem();
        userSettings.setId(userId);
        System.out.println("hash key = " + userId);

        UserSettingsItem populated = dynamo.getMapper().load(userSettings);
        return populated.getConnectionId();


    }

    public void destroySignupUser() {
        authHelper.deleteUser(INTEGRATION_TEST_USERNAME);
    }


    private void readServerlessState() throws IOException {

        //is test being executed from build (test suite) or out (individual) directory
        // this is kinda nasty because it means CI needs to `sls package` before running integration tests
        String buildPath = IntegrationTestHelper.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        LOG.debug("build path = " + buildPath);
        int i = 0;
        File srcDirFile = new File(IntegrationTestHelper.class.getResource("../").getFile());

        while (srcDirFile.getParent() != null && !srcDirFile.getParent().endsWith("/src") && i < 20) {
            i++;
            srcDirFile = srcDirFile.getParentFile();
        }
        if (i == 20 || srcDirFile.getPath().equals("/")) {
            LOG.error("can't find source directory");
            throw new NotFoundException("Error locating module source directory");
        }

        srcDirFile = srcDirFile.getParentFile();
        LOG.debug("src dir file: " + srcDirFile.getPath());


        boolean executedFromTestSuite = (buildPath.contains("/*/build/classes/java/test/")
                || buildPath.contains("/*/build/classes/java/integration-test/"));


        for (String includeModule : INCLUDE_MODULES) {
            File serverlessStateForModule = new File(srcDirFile
                    + (executedFromTestSuite ? "" : "/" + includeModule)
                    + "/.serverless/", "serverless-state.json");
            LOG.debug("serverless-state.json for " + includeModule + " = " + serverlessStateForModule.getPath());

            serverlessStateMap.put(includeModule, ServerlessState.readServerlessState(serverlessStateForModule.getPath()));
        }
    }

}
