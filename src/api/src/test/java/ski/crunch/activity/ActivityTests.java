package ski.crunch.activity;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import ski.crunch.activity.model.ActivityOuterClass;
import ski.crunch.activity.service.ActivityService;
import ski.crunch.activity.service.DynamoDBService;
import ski.crunch.activity.service.S3Service;
import ski.crunch.utils.*;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


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
    private String processedActivityBucket = null;
    private String rawActivityBucket = null;

    private ProfileCredentialsProvider credentialsProvider = null;

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

            //System.setProperty("currentStage", "staging");

            //is test being executed from build (test suite) or out (individual) directory
            String buildPath = ActivityTests.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            System.out.println("build path = " + buildPath);
            String apiPath;
            String authPath;
            String path;
            String ss = ActivityTests.class.getResource("../../../").getPath();
            File f = new File(ss);
            apiPath =  f.getParentFile().getParentFile().getParentFile().getParentFile().getPath();

            if(buildPath.contains("/api/build/classes/java/test/")){
                //test suite
                 apiPath =  f.getParentFile().getParentFile().getParentFile().getParentFile().getPath();
                 path = new File(apiPath+"/.serverless","serverless-state.json").getPath();
                 authPath = new File(apiPath+"/../auth/.serverless","serverless-state.json").getPath();
            } else {
                //individual
                path = new File(apiPath+"/api/.serverless","serverless-state.json").getPath();
                authPath = new File(apiPath+"/auth/.serverless","serverless-state.json").getPath();
            }

            apiStackServerlessState = ServerlessState.readServerlessState(path);
            authStackServerlessState = ServerlessState.readServerlessState(authPath);
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
        this.credentialsProvider = new ProfileCredentialsProvider(AWS_PROFILE);
        helper.PerformAdminSignup(testUserName, testPassword);

        this.accessKey = helper.PerformSRPAuthentication(testUserName, testPassword);
        LOG.info("ACCESS KEY: " + this.accessKey);
        this.s3 = new S3Service(apiStackServerlessState.getRegion(), credentialsProvider);
        this.dynamo = new DynamoDBService(region, apiStackServerlessState.getActivityTable(),
                this.credentialsProvider);
        this.activityService = new ActivityService( s3, this.credentialsProvider, dynamo
                , apiStackServerlessState.getRegion(),
                apiStackServerlessState.getRawActivityBucketName(), apiStackServerlessState.getActivityBucketName(),
                apiStackServerlessState.getActivityTable());

        this.processedActivityBucket = apiStackServerlessState.getActivityBucketName();
        this.rawActivityBucket = apiStackServerlessState.getRawActivityBucketName();
        System.out.println("printing variables...");
        System.out.println(apiStackServerlessState.getRawActivityBucketName());
        System.out.println(apiStackServerlessState.getActivityBucketName());
        System.out.println(apiStackServerlessState.getActivityTable());

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
        LOG.info("success result: " + result);
        assert (response.getStatus() == 200);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode;

        try {
            rootNode = objectMapper.readTree(result);
            this.activityId = rootNode.path("activityId").asText();
            LOG.info("ACTIVITY ID: " + activityId);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // appears to be an eventual consistency issue here. S3 returning not found
        try {
            Thread.currentThread().sleep(20000);
            LOG.info("checking processed bucket " + processedActivityBucket + " for activity: " + activityId+".pbf");
            assert(s3.doesObjectExist(processedActivityBucket, activityId+".pbf") == true);
        } catch (InterruptedException ex) {

        }

        try {
            byte[] buffer = s3.getObject(processedActivityBucket, activityId + ".pbf");
            ActivityOuterClass.Activity finalActivity = ActivityOuterClass.Activity.parseFrom(buffer);
            assertEquals(1, finalActivity.getSessionsCount());
            assertTrue(finalActivity.getSessions(0).getSport()!=null);
            System.out.println("total distance = " + finalActivity.getSummary().getTotalDistance());

        }catch(IOException ex){
            ex.printStackTrace();
        }

    }

    /**
     * Helper to generate an access key for debugging
     * Uncomment to run individually - not a part of the test suite
     */
//    @Test
//    void generateDevAccessKey() {
//        helper.PerformAdminSignup(devUserName, devPassword);
//
//        this.devAccessKey = helper.PerformSRPAuthentication(devUserName, devPassword);
//        LOG.info("DEV ACCESS KEY: " + this.devAccessKey);
//    }
//
//    @Test
//    void destroyDevUser() {
//        helper.deleteUser(this.devUserName);
//    }


    private  String extractActivityId(String key) throws ParseException {
        String id = "";

        if (key != null && key.length() > 1 && key.contains(".")) {
            id = key.substring(0, key.indexOf("."));
            LOG.debug("extracted id: " + id);
        } else {
            LOG.error("invalid key name: " + key);
            throw new ParseException("invalid key name for activity " + key);
        }
        return id;
    }

    @Test
    public void testActivityIdExtract(){
        try {
            String id = this.extractActivityId("029781f1-4eac-453f-91e4-ae44f76c57d0.fit");
            System.out.println("id = " + id);
            System.out.println(id.concat(".pbf"));
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
    @AfterAll
    void tearDown() {
        activityService.deleteActivityItemById(activityId);
        activityService.deleteRawActivityFromS3(activityId + ".fit");
        try {
            Thread.currentThread().sleep(20000);
            LOG.info("deleting from processed bucket " + processedActivityBucket + " for activity: " + activityId + ".pbf");
            activityService.deleteProcessedActivityFromS3(activityId + ".pbf");
        } catch (InterruptedException ex) {

        }

        helper.deleteUser(this.testUserName);
    }

}
