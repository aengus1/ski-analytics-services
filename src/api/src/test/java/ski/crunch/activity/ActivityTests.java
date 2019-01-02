package ski.crunch.activity;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import ski.crunch.activity.model.PutActivityResponse;
import ski.crunch.activity.service.ActivityService;
import ski.crunch.activity.service.DynamoDBService;
import ski.crunch.activity.service.S3Service;
import ski.crunch.utils.AuthenticationHelper;
import ski.crunch.utils.CloudFormationHelper;
import ski.crunch.utils.NotFoundException;
import ski.crunch.utils.ServerlessState;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ActivityTests {
    private String accessKey = null;
    private String devAccessKey = null;
    private AuthenticationHelper helper = null;
    private final String testUserName = "testUser@test.com";
    private final String testPassword = "testPassword123";
    private final String devUserName = "testDevUser@test.com";
    private final String devPassword = "testDevPassword123";
    private String activityId = null;
    private CloudFormationHelper authStackCfHelper = null;
    private CloudFormationHelper apiStackCfHelper = null;
    private ActivityService activityService = null;
    private S3Service s3 = null;
    private DynamoDBService dynamo = null;

    private final static String AWS_PROFILE = "backend_dev";
    private static final Logger LOG = Logger.getLogger(ActivityTests.class);

    public static List<String> fileList(String directory) {
        List<String> fileNames = new ArrayList<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(directory))) {
            for (Path path : directoryStream) {
                fileNames.add(path.toString());
            }
        } catch (IOException ex) {
        }
        return fileNames;
    }


    @BeforeAll
    void retrieveAccessKey() {

        // convert serverless-state output to retrieve userpool variables
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
        try {
            authStackCfHelper = new CloudFormationHelper(authStackServerlessState);
            apiStackCfHelper = new CloudFormationHelper(apiStackServerlessState);
            userPoolClientId = authStackCfHelper.getStagingUserPoolClientId();

        } catch (NotFoundException ex) {
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
        this.s3 = new S3Service(apiStackServerlessState.getRegion());
        this.dynamo = new DynamoDBService(region, apiStackServerlessState.getActivityTable(),
                new ProfileCredentialsProvider(AWS_PROFILE));
        this.activityService = new ActivityService(s3, new ProfileCredentialsProvider(AWS_PROFILE),dynamo
                ,apiStackServerlessState.getRegion(),
                apiStackServerlessState.getRawActivityBucketName(),apiStackServerlessState.getActivityBucketName(),
                apiStackServerlessState.getActivityTable() );

        System.out.println("printing variables...");
        System.out.println(apiStackServerlessState.getRawActivityBucketName());
        System.out.println(apiStackServerlessState.getActivityBucketName());
        System.out.println(apiStackServerlessState.getActivityTable() );

    }

    @Test
    void generateDevAccessKey() {
        helper.PerformAdminSignup(devUserName, devPassword);

        this.devAccessKey = helper.PerformSRPAuthentication(devUserName, devPassword);
        LOG.info("DEV ACCESS KEY: " + this.devAccessKey);
    }

    @Test
    void testJsonWriter() {
        String resp = null;
        try {
            final ObjectMapper objectMapper = new ObjectMapper();
            resp = objectMapper.writeValueAsString(new PutActivityResponse("test124"));
            System.out.println(resp);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    void putActivityAuthFailureTest() {

        String endpoint = null;
        try {
            endpoint = apiStackCfHelper.getApiEndpoint();
            LOG.info("ENDPOINT: " + endpoint);
        } catch (NotFoundException ex) {
            ex.printStackTrace();
        }
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(endpoint).path("activity");


        InputStream is = ActivityTests.class.getResourceAsStream("/261217.fit");
        Entity payload = Entity.entity(is, MediaType.APPLICATION_OCTET_STREAM_TYPE);

        Response response = target.request(MediaType.APPLICATION_OCTET_STREAM_TYPE)
                .accept("application/fit")
                .header("Content-Type", "application/json")
                .header("Accept", "application/fit")
                .header("Authorization", "12345invalidAuthKey")
                .buildPut(payload).invoke();

        System.out.println(response.getStatus());
        System.out.println(response.getStatusInfo().getStatusCode());

        String result = response.readEntity(String.class);
        System.out.println("res = " + result);


        assert (response.getStatus() == 401);
    }


    @Test
    void putActivitySuccessTest() {

        String endpoint = null;
        try {
            endpoint = apiStackCfHelper.getApiEndpoint();
            LOG.info("ENDPOINT: " + endpoint);
        } catch (NotFoundException ex) {
            ex.printStackTrace();
        }
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(endpoint).path("activity");


        InputStream is = ActivityTests.class.getResourceAsStream("/261217.fit");
        Entity payload = Entity.entity(is, MediaType.valueOf("application/fit"));

        Response response = target.request(MediaType.APPLICATION_OCTET_STREAM_TYPE)
                .accept("application/fit")
                .header("Content-Type", "application/json")
                .header("Accept", "application/fit")
                .header("Authorization", this.accessKey)
                .buildPut(payload).invoke();

        String result = response.readEntity(String.class);
        System.out.println("res = " + result);
        assert (response.getStatus() == 200);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode;

        try {
            rootNode = objectMapper.readTree(result);
            this.activityId = rootNode.path("activityId").asText();
            System.out.println("activityId = " + activityId);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

//    @Test
//    void getActivitySuccessTest() {
//
//        String endpoint = null;
//        try {
//            endpoint = apiStackCfHelper.getApiEndpoint();
//            LOG.info("ENDPOINT: " + endpoint);
//        } catch (NotFoundException ex) {
//            ex.printStackTrace();
//        }
//        Client client = ClientBuilder.newClient();
////        WebTarget target = client.target(endpoint).path("activity/"+this.activityId);
//        WebTarget target = client.target(endpoint).path("activity/1"+this.activityId);
//
//
//        Response response = target.request(MediaType.valueOf("application/x-protobuf"))
//                .accept("application/x-protobuf")
//                .header("Content-Type", "application/x-empty")
//                .header("Accept", "application/x-protobuf")
//                .header("Authorization", this.accessKey)
//                .buildGet().invoke();
//
//        System.out.println("res = " + response.getStatus());
//        try(InputStream  result = response.readEntity(InputStream.class)){
//            byte[] bytes = IOUtils.toByteArray(result);
//            assert(bytes.length > 0);
//
//        }catch(IOException ex){
//            ex.printStackTrace();
//            assert(false);
//        }
//
//        assert (response.getStatus() == 200);
//
//    }


    //todo -> clean up activity table
    // todo -> clean up raw activity bucket
    //todo -> clean up processed activity path

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


    @Test
    void destroyDevUser() {
        helper.deleteUser(this.devUserName);
    }

    @AfterAll
    void tearDown() {
        activityService.deleteActivityItemById(activityId);
        // activityService.deleteRawActivityFromS3(activityId+".fit");
        helper.deleteUser(this.testUserName);
    }

}
