package ski.crunch.testhelpers;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import org.apache.log4j.Logger;
import ski.crunch.aws.DynamoFacade;
import ski.crunch.model.UserSettingsItem;
import ski.crunch.utils.NotFoundException;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class IntegrationTestHelper {

    public static final String AWS_PROFILE = "backend_dev";

    //todo -> read these from a properties file in resources folder
    public enum IncludeModules {
        api("staging-ski-analytics-api-stack"),
        auth("staging-ski-analytics-authentication-stack"),
        websocket("staging-ski-analytics-websocket-stack"),
        common("staging-ski-analytics-common-stack"),
        cloudformation("staging-ski-analytics-cloudformation-stack");

        private String name;

        IncludeModules(String name) {
            this.name = name;
        }

        public String getStackName() {
            return this.name;
        }
    }

    private static final Logger LOG = Logger.getLogger(IntegrationTestHelper.class);
    private static final String INTEGRATION_TEST_USERNAME = "integration_test_user@crunch.ski";
    private static final String INTEGRATION_TEST_PASSWORD = "abC123Def!";

    private ProfileCredentialsProvider credentialsProvider;
    private CloudFormationHelper cfHelper;
    private AuthenticationHelper authHelper;

    private Map<String, ServerlessState> serverlessStateMap = new HashMap<>();

    public IntegrationTestHelper() throws IOException {
        readServerlessState();
        String region = serverlessStateMap.get(IncludeModules.auth.getStackName()).getRegion();

        credentialsProvider = new ProfileCredentialsProvider(AWS_PROFILE);
        cfHelper = new CloudFormationHelper(credentialsProvider, region);
        authHelper = new AuthenticationHelper(
                cfHelper.getStackOutput(IncludeModules.auth.getStackName(), "UserPoolArn"),
                cfHelper.getStackOutput(IncludeModules.auth.getStackName(), "UserPoolClientId"),
                region,
                AWS_PROFILE);

    }


    public ProfileCredentialsProvider getCredentialsProvider() {
        return this.credentialsProvider;
    }

    public String getApiRegion() {
        return serverlessStateMap.get(IncludeModules.api.getStackName()).getRegion();
    }
    public String getWebsocketEndpoint() {
        //return serverlessStateMap.get("websocket").getWebsocketEndpoint();
        return cfHelper.getStackOutput(IncludeModules.websocket.getStackName(), "ServiceEndpointWebsocket");
    }

    public String getApiEndpoint() {
        return cfHelper.getStackOutput(IncludeModules.api.getStackName(), "ServiceEndpoint");
    }

    public Optional<String> signup() {
        return authHelper.performAdminSignup(INTEGRATION_TEST_USERNAME, INTEGRATION_TEST_PASSWORD);
    }

    public String getDevAccessKey(String username, String password) {
        return authHelper.performSRPAuthentication(username, password);
    }

    public String retrieveAccessToken() {
        return authHelper.performSRPAuthentication(INTEGRATION_TEST_USERNAME, INTEGRATION_TEST_PASSWORD);
    }

    public void insertUserSettings(String userId) {
        DynamoFacade dynamo = new DynamoFacade(
                serverlessStateMap.get(IncludeModules.auth.getStackName()).getRegion(),
                cfHelper.getStackOutput(IncludeModules.auth.getStackName(), "UserTableName"),
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
        DynamoFacade dynamo = new DynamoFacade(
                serverlessStateMap.get(IncludeModules.auth.getStackName()).getRegion(),
                cfHelper.getStackOutput(IncludeModules.auth.getStackName(), "UserTableName"),
                credentialsProvider
        );

        try {
            System.out.println("user id = " + userId);
            UserSettingsItem userSettings = new UserSettingsItem();
            userSettings.setId(userId);

            dynamo.getMapper().delete(userSettings);
        } catch (Exception e) {
            LOG.error("Error writing user settings", e);

        }
    }

    public String getUsersWebsocketConnectionId(String userId) {
        DynamoFacade dynamo = new DynamoFacade(
                serverlessStateMap.get(IncludeModules.auth.getStackName()).getRegion(),
                cfHelper.getStackOutput(IncludeModules.auth.getStackName(), "UserTableName"),
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


    public ServerlessState getServerlessState(String stackName) {
        return serverlessStateMap.get(stackName);
    }

    private void readServerlessState() throws IOException {

        try {
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


            for (IncludeModules includeModule : IncludeModules.values()) {
                File serverlessStateForModule = new File(srcDirFile
                        + (executedFromTestSuite ? "" : "/" + includeModule)
                        + "/.serverless/", "serverless-state.json");
                LOG.debug("serverless-state.json for " + includeModule + " = " + serverlessStateForModule.getPath());

                serverlessStateMap.put(includeModule.getStackName(), ServerlessState.readServerlessState(serverlessStateForModule.getPath()));
            }
        }catch(Exception ex ){
            LOG.error("error reading serverless state ", ex);
            ex.printStackTrace();
        }
    }


    public String getActivityTable() {
        return getServerlessState(IncludeModules.api.getStackName()).getRootNode().path("service").path("custom").path("activityTable").asText();
    }

    public String getUserTable() {
        return getServerlessState(IncludeModules.api.getStackName()).getRootNode().path("service").path("custom").path("userTable").asText();
    }

    public String getRawActivityBucketName() {
        return getServerlessState(IncludeModules.api.getStackName()).getRootNode().path("service").path("custom").path("rawActivityBucketName").asText();
    }

    public String getActivityBucketName() {
        return getServerlessState(IncludeModules.api.getStackName()).getRootNode().path("service").path("custom").path("activityBucketName").asText();
    }

    public void deleteUser(String username) {
        authHelper.deleteUser(username);
    }

}
