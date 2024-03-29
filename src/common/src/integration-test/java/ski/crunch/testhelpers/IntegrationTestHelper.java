package ski.crunch.testhelpers;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ski.crunch.aws.DynamoFacade;
import ski.crunch.model.UserSettingsItem;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class IntegrationTestHelper {

    public static final String AWS_PROFILE = "backend_dev";

    public final static String[] MODULES = {
            "api",
            "auth",
            "websocket",
            "cloudformation_custom_resources/rockset",
            "cloudformation_custom_resources/bucket-notification"
    };
    private static final Logger logger = LoggerFactory.getLogger(IntegrationTestHelper.class);
    public static final String INTEGRATION_TEST_USERNAME = "integration_test_user@crunch.ski";
    private static final String INTEGRATION_TEST_PASSWORD = "abC123Def!";
    private ProfileCredentialsProvider credentialsProvider;
    private CloudFormationHelper cfHelper;
    private AuthenticationHelper authHelper;

    private Map<String, ServerlessState> serverlessStateMap = new HashMap<>();
    private String stage;
    private String region;
    private String projectName;
    private String dataStackName;
    private String prefix;

    public IntegrationTestHelper() throws IOException {
        this.stage = System.getProperty("stage");
        if(this.stage == null || this.stage.equals("")){
            this.stage = "dev";
        }

        System.out.println("stage = " + stage);
        //read region from data stack cf
        this.region = TerraformPropertiesReader.getTerraformVariable("primary_region", TerraformPropertiesReader.TerraformStack.GLOBAL);
        projectName =  TerraformPropertiesReader.getTerraformVariable("project_name", TerraformPropertiesReader.TerraformStack.GLOBAL);
        this.prefix = this.stage+"-"+projectName+"-";
        this.dataStackName = prefix+"data-var-stack";
        credentialsProvider = new ProfileCredentialsProvider(AWS_PROFILE);

        cfHelper = new CloudFormationHelper(credentialsProvider, region);
        authHelper = new AuthenticationHelper(
                cfHelper.getStackOutput(dataStackName, "UserPoolId"+this.stage),
                cfHelper.getStackOutput(dataStackName, "UserPoolClientId"+this.stage),
                region,
                AWS_PROFILE);
        readServerlessState();

    }


    public ProfileCredentialsProvider getCredentialsProvider() {
        return this.credentialsProvider;
    }

    public String getApiRegion() {
        return region;
    }
    public String getWebsocketEndpoint() {
        String webSocketStackName = this.prefix+"websocket";
        //return serverlessStateMap.get("websocket").getWebsocketEndpoint();
        return cfHelper.getStackOutput(webSocketStackName, "ServiceEndpointWebsocket");
    }

    public String getApiEndpoint() {
        String apiStackName = this.prefix+"api";
        return cfHelper.getStackOutput(apiStackName, "ServiceEndpoint");
    }

    public Optional<String> signup() {

        return authHelper.performAdminSignup(INTEGRATION_TEST_USERNAME, INTEGRATION_TEST_PASSWORD);
    }

    public Optional<String> signup(String userName, String password, boolean dontAutoConfirm) {

        if(dontAutoConfirm) {
            return authHelper.performSignup(userName, password);
        } else {
            return authHelper.performAdminSignup(userName, password);
        }
    }

    public void resetPassword(String userName, String newPassword, String userPoolId) {
         authHelper.performForgotPassword(userName, newPassword, userPoolId);
    }

    public String getCognitoId() {
        return authHelper.getCognitoId();
    }

    public String getUserPoolId() {
        return authHelper.getUserPoolID();
    }
    public String getDevAccessKey(String username, String password) throws Exception{
        return authHelper.performSRPAuthentication(username, password);
    }

    public String signIn(String username, String password) throws Exception {
        return authHelper.initiateUserPasswordAuthRequest(username, password);
    }
    public String retrieveAccessToken() throws Exception{
        return authHelper.performSRPAuthentication(INTEGRATION_TEST_USERNAME, INTEGRATION_TEST_PASSWORD);
    }

    public String retrieveAccessToken(String username, String password) throws  Exception{
        return authHelper.performSRPAuthentication(username, password);
    }

    public void insertUserSettings(String userId) {

        String dataStackName = this.prefix+"data-var-stack";
        DynamoFacade dynamo = new DynamoFacade(
                this.region,
                this.stage+"-crunch-ski-userTable",
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
            logger.error("Error writing user settings", e);

        }
    }

    public void removeUserSettings(String userId) {
        String dataStackName =this.prefix+"data-var-stack";
        DynamoFacade dynamo = new DynamoFacade(
                this.region,
                this.stage+"-crunch-ski-userTable",
                credentialsProvider
        );

        try {
            System.out.println("user id = " + userId);
            UserSettingsItem userSettings = new UserSettingsItem();
            userSettings.setId(userId);

            dynamo.getMapper().delete(userSettings);
        } catch (Exception e) {
            logger.error("Error removing user settings", e);

        }
    }

    public String getUsersWebsocketConnectionId(String userId) {
        String authStack = this.stage+"-"+projectName+"-auth";
        DynamoFacade dynamo = new DynamoFacade(
                this.region,
               this.stage+"-crunch-ski-userTable",
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

    public void destroyUser(String userName) {
        authHelper.deleteUser(userName);
    }


    public ServerlessState getServerlessState(String stackName) {
        return serverlessStateMap.get(stackName);
    }

    public ServerlessState getServerlessStateWithoutPrefix(String stackName) {
        return serverlessStateMap.get(this.stage+"-"+this.projectName+"-"+stackName);
    }


    private void readServerlessState() throws IOException {

        try {
            //is test being executed from build (test suite) or out (individual) directory
            // this is kinda nasty because it means CI needs to `sls package` before running integration tests
            String buildPath = IntegrationTestHelper.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            logger.debug("build path = " + buildPath);
            int i = 0;
            File srcDirFile = TestUtils.getSrcDirPath();


            boolean executedFromTestSuite = (buildPath.contains("/*/build/classes/java/test/")
                    || buildPath.contains("/*/build/classes/java/integration-test/"));



            for (String module: MODULES) {
                File serverlessStateForModule = new File(srcDirFile
                        + (executedFromTestSuite ? "" : "/" + module)
                        + "/.serverless/", "serverless-state.json");
                logger.debug("serverless-state.json for " + prefix+module + " = " + serverlessStateForModule.getPath());

                serverlessStateMap.put(prefix+module, ServerlessState.readServerlessState(serverlessStateForModule.getPath()));
            }
        }catch(Exception ex ){
            logger.error("error reading serverless state ", ex);
            ex.printStackTrace();
        }
    }


    public String getActivityTable() {
        String apiStackName = this.prefix+"api";
        return getServerlessState(apiStackName).getRootNode().path("service").path("custom").path("activityTable").asText();
    }

    public String getUserTable() {
        String apiStackName = this.prefix+"api";
        return getServerlessState(apiStackName).getRootNode().path("service").path("custom").path("userTable").asText();
    }

    public String getRawActivityBucketName() {
        String apiStackName = this.prefix+"api";
        return getServerlessState(apiStackName).getRootNode().path("service").path("custom").path("rawActivityBucketName").asText();
    }

    public String getActivityBucketName() {
        String apiStackName = this.prefix+"api";
        return getServerlessState(apiStackName).getRootNode().path("service").path("custom").path("activityBucketName").asText();
    }

    public void deleteUser(String username) {
        authHelper.deleteUser(username);
    }

    public String getPrefix(){
        return this.prefix;
    }

}
