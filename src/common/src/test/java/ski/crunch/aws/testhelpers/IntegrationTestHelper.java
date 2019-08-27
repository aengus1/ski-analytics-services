package ski.crunch.aws.testhelpers;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import org.apache.log4j.Logger;
import ski.crunch.utils.NotFoundException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class IntegrationTestHelper {
    //todo -> read these from a properties file in resources folder
    private static final String[] INCLUDE_MODULES = {"api", "auth", "common"};
    private static final String AWS_PROFILE = "backend_dev";
    private static final String AUTH_STACK_NAME = "staging-ski-analytics-authentication-stack";

    private static final Logger LOG = Logger.getLogger(IntegrationTestHelper.class);
    private static final String INTEGRATION_TEST_USERNAME = "integration_test_user@crunch.ski";
    private static final String INTEGRATION_TEST_PASSWORD = "abC123Def!";

    private ProfileCredentialsProvider credentialsProvider;
    private CloudFormationHelper cfHelper;
    private AuthenticationHelper authHelper;
    ;
    private Map<String, ServerlessState> serverlessStateMap = new HashMap<>();

    public IntegrationTestHelper() throws IOException {
        readServerlessState();
        String region = serverlessStateMap.get("auth").getRegion();
        String userPoolId = serverlessStateMap.get("auth").getUserPoolId();
        credentialsProvider = new ProfileCredentialsProvider(AWS_PROFILE);
        cfHelper = new CloudFormationHelper(credentialsProvider, region);
        String userPoolClientId = cfHelper.getStackOutput(AUTH_STACK_NAME, "UserPoolClientId");

        authHelper = new AuthenticationHelper(userPoolId, userPoolClientId, "",  region, AWS_PROFILE);

    }


    public String signUpAndRetrieveAccessToken() {
        authHelper.performAdminSignup(INTEGRATION_TEST_USERNAME, INTEGRATION_TEST_PASSWORD);
        return authHelper.performSRPAuthentication(INTEGRATION_TEST_USERNAME, INTEGRATION_TEST_PASSWORD);
    }

    public void destroySignupUser() {
        authHelper.deleteUser(INTEGRATION_TEST_USERNAME);
    }


    /**
     *
     * @throws IOException
     */
    private void readServerlessState() throws IOException {

        //is test being executed from build (test suite) or out (individual) directory
        // this is kinda nasty because it means CI needs to `sls package` before running integration tests
        String buildPath = IntegrationTestHelper.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        LOG.debug("build path = " + buildPath);
        int i = 0;
        File srcDirFile = new File(IntegrationTestHelper.class.getResource("../").getFile());

        while (srcDirFile.getParent() != null && !srcDirFile.getParent().endsWith("/src") || i >= 20) {
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
                || buildPath.contains("/*/build/classes/java/integrationTest/"));


        for (String includeModule : INCLUDE_MODULES) {
            File serverlessStateForModule = new File(srcDirFile
                    + (executedFromTestSuite ? "" : "/" + includeModule)
                    + "/.serverless/", "serverless-state.json");
            LOG.debug("serverless-state.json for " + includeModule + " = " + serverlessStateForModule.getPath());

            serverlessStateMap.put(includeModule, ServerlessState.readServerlessState(serverlessStateForModule.getPath()));
        }
    }

}
