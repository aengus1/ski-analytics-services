package ski.crunch.activity;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.*;
import ski.crunch.activity.service.ActivityService;
import ski.crunch.aws.DynamoDBService;
import ski.crunch.aws.S3Service;
import ski.crunch.dao.ActivityDAO;
import ski.crunch.model.ActivityItem;
import ski.crunch.model.ActivityOuterClass;
import ski.crunch.utils.NotFoundException;

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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ActivityTests {
    private String accessKey = null;
    private AuthenticationHelper helper = null;
    private final String testUserName = "testUser@test.com";
    private final String testPassword = "testPassword123";
    private final String devUserName = "testDevUser@test.com";
    private final String devPassword = "testDevPassword123";
    //private String rawActivityBucket = null;
    private String activityId = null;
    private CloudFormationHelper authStackCfHelper = null;
    private CloudFormationHelper apiStackCfHelper = null;
    private ActivityService activityService = null;
    private S3Service s3 = null;
    private DynamoDBService dynamo = null;
    private ActivityDAO activityDAO = null;
    private String processedActivityBucket = null;

    private ProfileCredentialsProvider credentialsProvider = null;

    private final static String AWS_PROFILE = "backend_dev";
    private static final Logger LOG = Logger.getLogger(ActivityTests.class);

    public static List<String> fileList(String directory) {
        List<String> fileNames = new ArrayList<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(directory))) {
            for (Path path : directoryStream) {
                fileNames.add(path.toString());
            }
        } catch (IOException ignored) {
        }
        return fileNames;
    }


    @BeforeAll
    void retrieveAccessKey() throws Exception {

        // convert serverless-state output to retrieve userpool variables
        ServerlessState apiStackServerlessState;
        ServerlessState authStackServerlessState;

        try {

            //System.setProperty("currentStage", "staging");

            //is test being executed from build (test suite) or out (individual) directory
            // this is kinda nasty because it means CI needs to `sls package` before running integration tests
            String buildPath = ActivityTests.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            System.out.println("build path = " + buildPath);
            String apiPath;
            String authPath;
            String path;
            String ss = ActivityTests.class.getResource("../../../").getPath();
            File f = new File(ss);
            apiPath = f.getParentFile().getParentFile().getParentFile().getParentFile().getPath();

            if (buildPath.contains("/api/build/classes/java/test/")
                    || buildPath.contains("/api/build/classes/java/integrationTest/")) {
                LOG.debug("Searching for serverless-state from test suite build path");
                //test suite
                apiPath = f.getParentFile().getParentFile().getParentFile().getParentFile().getPath();
                System.out.println("API path  = " + apiPath);
                path = new File(apiPath + "/.serverless", "serverless-state.json").getPath();
                authPath = new File(apiPath + "/../auth/.serverless", "serverless-state.json").getPath();
            } else {
                //individual
                LOG.debug("Searching for serverless-state from individual test execution path");
                path = new File(apiPath + "/api/.serverless", "serverless-state.json").getPath();
                authPath = new File(apiPath + "/auth/.serverless", "serverless-state.json").getPath();
            }

            apiStackServerlessState = ServerlessState.readServerlessState(path);
            authStackServerlessState = ServerlessState.readServerlessState(authPath);
        } catch (IOException e) {
            System.err.println("Failed to read serverless-state.json. Ensure that 'sls package' is run first. Exiting test suite");
            e.printStackTrace();
            throw new Exception("Failed to read serverless-state.json");
        }

        // retrieve clientID from cloudformation outputs
        String clientId = null;
        try {
            authStackCfHelper = new CloudFormationHelper(authStackServerlessState);
            apiStackCfHelper = new CloudFormationHelper(apiStackServerlessState);
            clientId = authStackCfHelper.getStagingUserPoolClientId();

        } catch (NotFoundException | IllegalArgumentException ex) {
            ex.printStackTrace();
            System.exit(1);
        }


        String userPoolId = apiStackServerlessState.getUserPoolId();
        String region = apiStackServerlessState.getUserPoolRegion();
        LOG.info("region = " + region);

        this.helper = new AuthenticationHelper(userPoolId, clientId, "", region, AWS_PROFILE);
        this.credentialsProvider = new ProfileCredentialsProvider(AWS_PROFILE);
        helper.performAdminSignup(testUserName, testPassword);

        this.accessKey = helper.performSRPAuthentication(testUserName, testPassword);
        LOG.info("ACCESS KEY: " + this.accessKey);
        this.s3 = new S3Service(apiStackServerlessState.getRegion(), credentialsProvider);
        this.dynamo = new DynamoDBService(apiStackServerlessState.getRegion(), apiStackServerlessState.getActivityTable(),
                this.credentialsProvider);
        this.activityDAO =  new ActivityDAO(dynamo, apiStackServerlessState.getActivityTable());
        this.activityService = new ActivityService(s3, this.credentialsProvider, dynamo
                , apiStackServerlessState.getRegion(),
                apiStackServerlessState.getRawActivityBucketName(), apiStackServerlessState.getActivityBucketName(),
                apiStackServerlessState.getActivityTable(), authStackServerlessState.getUserTable());

        this.processedActivityBucket = apiStackServerlessState.getActivityBucketName();
        //this.rawActivityBucket = apiStackServerlessState.getRawActivityBucketName();
        System.out.println("printing variables...");
        System.out.println(apiStackServerlessState.getRawActivityBucketName());
        System.out.println(apiStackServerlessState.getActivityBucketName());
        System.out.println(apiStackServerlessState.getActivityTable());

    }


    @Test
    void putActivityAuthFailureTest() throws NotFoundException {

        String endpoint = apiStackCfHelper.getApiEndpoint();
        LOG.info("ENDPOINT: " + endpoint);

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


        assertEquals(401, response.getStatus());
    }


    @Test
    void putActivitySuccessTest() throws NotFoundException {

        String endpoint = apiStackCfHelper.getApiEndpoint();
        LOG.info("ENDPOINT: " + endpoint);

        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(endpoint).path("activity");


        InputStream is = ActivityTests.class.getResourceAsStream("/interval_test.fit");
        Entity payload = Entity.entity(is, MediaType.valueOf("application/fit"));

        Response response = target.request(MediaType.APPLICATION_OCTET_STREAM_TYPE)
                .accept("application/fit")
                .header("Content-Type", "application/json")
                .header("Accept", "application/fit")
                .header("Authorization", this.accessKey)
                .buildPut(payload).invoke();

        String result = response.readEntity(String.class);
        LOG.info("success result: " + result);
        assertEquals(200, response.getStatus());

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
            LOG.info("checking processed bucket " + processedActivityBucket + " for activity: " + activityId + ".pbf");
            assertTrue(s3.doesObjectExist(processedActivityBucket, activityId + ".pbf"));
        } catch (InterruptedException ex) {
            System.err.println("Thread interrupted");
        }

        try {
            byte[] buffer = s3.getObject(processedActivityBucket, activityId + ".pbf");
            ActivityOuterClass.Activity finalActivity = ActivityOuterClass.Activity.parseFrom(buffer);
            assertEquals(1, finalActivity.getSessionsCount());
            assertNotNull(finalActivity.getSessions(0).getSport());
            System.out.println("total distance = " + finalActivity.getSummary().getTotalDistance());

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    @Test
    void testSearchFieldsAreSet() {

        Optional<ActivityItem> item = activityDAO.getActivityItem(this.activityId);
        assertEquals(true, item.isPresent());

        assertEquals("RUNNING",item.get().getActivityType());
        assertEquals("GENERIC_SUBSPORT",item.get().getActivitySubType());
        assertEquals(new Double(5459), item.get().getDistance());
        assertEquals(new Double(2052), item.get().getDuration());
        assertEquals(new Double(231), item.get().getDescent());
        assertEquals(new Double(179), item.get().getAscent());
        assertEquals(new Integer(-998), item.get().getAvHr());
        assertEquals(new Integer(0), item.get().getMaxHr());
        assertNotEquals("", item.get().getLastUpdateTimestamp());

    }


    /**
     * Helper to generate an access key for debugging
     * Uncomment to run individually - not a part of the test suite
     */
    @Disabled
    @Test
    void generateDevAccessKey() {
        helper.performAdminSignup(devUserName, devPassword);

        String devAccessKey = helper.performSRPAuthentication(devUserName, devPassword);
        LOG.info("DEV ACCESS KEY: " + devAccessKey);
    }

    @Disabled
    @Test
    public void destroyDevUser() {
        helper.deleteUser(this.devUserName);
    }



    @Test
    public void testActivityIdExtract() {
        try {
            String id = activityService.extractActivityId("029781f1-4eac-453f-91e4-ae44f76c57d0.fit");
            System.out.println("id = " + id);
            System.out.println(id.concat(".pbf"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @AfterAll
    void tearDown() {
        activityDAO.deleteActivityItemById(activityId);
        activityService.deleteRawActivityFromS3(activityId + ".fit");
        try {
            Thread.currentThread().sleep(20000);
            LOG.info("deleting from processed bucket " + processedActivityBucket + " for activity: " + activityId + ".pbf");
            activityService.deleteProcessedActivityFromS3(activityId + ".pbf");
        } catch (InterruptedException ex) {
            System.err.println("Interrupted Thread");
        }

        helper.deleteUser(this.testUserName);
    }

}
