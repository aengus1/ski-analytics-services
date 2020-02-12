package ski.crunch.activity;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.*;
import ski.crunch.activity.service.ActivityService;
import ski.crunch.aws.DynamoFacade;
import ski.crunch.aws.S3Facade;
import ski.crunch.dao.ActivityDAO;
import ski.crunch.model.ActivityItem;
import ski.crunch.model.ActivityOuterClass;
import ski.crunch.testhelpers.IntegrationTestHelper;
import ski.crunch.utils.NotFoundException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ActivityTests {
    private String accessKey = null;
    private final String devUserName = "testDevUser@test.com";
    private final String devPassword = "testDevPassword123";
    private String activityId = null;
    private IntegrationTestHelper helper;
    private ActivityService activityService = null;
    private S3Facade s3 = null;
    private DynamoFacade dynamo = null;
    private ActivityDAO activityDAO = null;
    private String processedActivityBucket = null;
    private static final Logger LOG = Logger.getLogger(ActivityTests.class);


    @BeforeAll
    void retrieveAccessKey() throws Exception {
        this.helper = new IntegrationTestHelper();
        helper.signup();
        this.accessKey = helper.retrieveAccessToken();
        LOG.info("ACCESS KEY: " + this.accessKey);


        String authRegion = helper.getServerlessState(helper.getPrefix()+"auth").getRegion();
        ProfileCredentialsProvider profileCredentialsProvider = helper.getCredentialsProvider();
        System.out.println("auth region = " + authRegion);
        System.out.println("profile creds = " + profileCredentialsProvider.toString());
        this.s3 = new S3Facade(authRegion, profileCredentialsProvider);
        this.dynamo = new DynamoFacade(helper.getServerlessState(helper.getPrefix()+"api").getRegion(), helper.getActivityTable(),
                helper.getCredentialsProvider());

        this.activityDAO =  new ActivityDAO(dynamo, helper.getActivityTable());
        this.activityService = new ActivityService(s3, helper.getCredentialsProvider(), dynamo
                , helper.getServerlessState(helper.getPrefix()+"api").getRegion(),
                helper.getRawActivityBucketName(), helper.getActivityBucketName(),
                helper.getActivityTable(), helper.getUserTable());

        this.processedActivityBucket = helper.getActivityBucketName();

        System.out.println("printing variables...");
        System.out.println(helper.getRawActivityBucketName());
        System.out.println(helper.getActivityBucketName());
        System.out.println(helper.getActivityTable());

    }


    @Test
    void putActivityAuthFailureTest() throws NotFoundException {

        String endpoint = helper.getApiEndpoint();
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

       LOG.debug(response.getStatus());
        LOG.debug(response.getStatusInfo().getStatusCode());

        String result = response.readEntity(String.class);
        LOG.debug("res = " + result);

        assertEquals(401, response.getStatus());
    }


    @Test
    void putActivitySuccessTest() throws NotFoundException {

        String endpoint = helper.getApiEndpoint();
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
            int sleep = 20;
            boolean exists = false;
            while((sleep -=5) >=0 && !exists) {
                Thread.currentThread().sleep(5000l);
                exists = s3.doesObjectExist(processedActivityBucket, activityId + ".pbf");
            }
                LOG.info("checking processed bucket " + processedActivityBucket + " for activity: " + activityId + ".pbf");
                assertTrue(exists);
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
        helper.signup();

        String devAccessKey = helper.getDevAccessKey(devUserName, devPassword);
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

        try {
            activityDAO.deleteActivityItemById(activityId);
            activityService.deleteRawActivityFromS3(activityId + ".fit");
            Thread.currentThread().sleep(20000);
            LOG.info("deleting from processed bucket " + processedActivityBucket + " for activity: " + activityId + ".pbf");
            activityService.deleteProcessedActivityFromS3(activityId + ".pbf");
        } catch (InterruptedException ex) {
            System.err.println("Interrupted Thread");
        } finally {
            LOG.info("destroying signup user ");
            helper.destroySignupUser();
        }
    }

}
