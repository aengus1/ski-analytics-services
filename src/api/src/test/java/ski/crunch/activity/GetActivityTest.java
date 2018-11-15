package ski.crunch.activity;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;
import com.amazonaws.services.cloudformation.model.DescribeStacksResult;
import com.amazonaws.services.cloudformation.model.Output;
import com.amazonaws.services.cloudformation.model.Stack;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import ski.crunch.utils.AuthenticationHelper;
import ski.crunch.utils.CloudFormationHelper;
import ski.crunch.utils.NotFoundException;
import ski.crunch.utils.ServerlessState;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import java.util.Optional;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GetActivityTest {
    private String accessKey = null;
    private AuthenticationHelper helper = null;
    private final String testUserName = "testUser@test.com";
    private final String testPassword = "testPassword123";
    private final static String AWS_PROFILE = "backend_dev";
    private static final Logger LOG = Logger.getLogger(GetActivityTest.class);

    public static List<String> fileList(String directory) {
        List<String> fileNames = new ArrayList<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(directory))) {
            for (Path path : directoryStream) {
                fileNames.add(path.toString());
            }
        } catch (IOException ex) {}
        return fileNames;
    }



    @BeforeAll
    void retrieveAccessKey(){

        // parse serverless-state output to retrieve userpool variables
        ServerlessState apiStackServerlessState = null;
        ServerlessState authStackServerlessState = null;
        try {
             apiStackServerlessState = ServerlessState.readServerlessState("./.serverless/serverless-state.json");
            authStackServerlessState = ServerlessState.readServerlessState("./../auth/.serverless/serverless-state.json");
        } catch (IOException e) {
            System.err.println("Failed to read serverless-state.json. Exiting test suite");
            e.printStackTrace();
            System.exit(1);
        }

        // retrieve clientID from cloudformation outputs
        String userPoolClientId = null;
        try{
            CloudFormationHelper cfHelper = new CloudFormationHelper(authStackServerlessState);
             userPoolClientId = cfHelper.getStagingUserPoolClientId();

        }catch(NotFoundException ex){
            ex.printStackTrace();
            System.exit(1);
        }



        String clientId = userPoolClientId;
        String userPoolId = apiStackServerlessState.getUserPoolId();
        String region = apiStackServerlessState.getUserPoolRegion();

        this.helper = new AuthenticationHelper(userPoolId, clientId, "", region, AWS_PROFILE);

        helper.PerformAdminSignup(testUserName, testPassword);

        this.accessKey = helper.PerformSRPAuthentication(testUserName, testPassword);
        LOG.info("ACCESS KEY: " + this.accessKey);

    }


    @Test
    void justAnExample() {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://foo").path("bar");
        Invocation.Builder invocationBuilder = target.request(MediaType.TEXT_PLAIN_TYPE);
//        Response response = invocationBuilder.get();
    }

//    @Test
//    void testAccessKey() {
//        AuthenticationHelper helper = new AuthenticationHelper("us-west-2_FrH0UdrNz",
//                "755f5d0elsg5ie96116540qm3u", "secretkey", "us-west-2");
//
//        String result = helper.PerformSRPAuthentication("aengusmccullough@hotmail.com", "Norwich123");
//        System.out.println("result = " + result);
//
//
//    }

//    @AfterAll
//    void tearDown() {
//        helper.deleteUser(this.testUserName);
//    }
}
