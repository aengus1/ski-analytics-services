package crunch.ski.cli.services;

import crunch.ski.cli.App;
import crunch.ski.cli.Backup;
import crunch.ski.cli.Restore;
import org.junit.jupiter.api.*;
import ski.crunch.aws.CredentialsProviderFactory;
import ski.crunch.aws.DynamoFacade;
import ski.crunch.aws.S3Facade;
import ski.crunch.aws.SSMParameterFacade;
import ski.crunch.dao.ActivityDAO;
import ski.crunch.dao.UserDAO;
import ski.crunch.model.ActivityItem;
import ski.crunch.testhelpers.IntegrationTestPropertiesReader;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Disabled
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LocalRestoreIntegrationTest {


    private static final String DATA_REGION = "ca-central-1";
    private static final String PROFILE_NAME = "default";
    private static final String PROJECT_NAME = "test";
    private static final String ENV = "backup";

    private Map<String, String> configMap;
    private TestDataLoader testDataLoader;
    private App parent;
    private Backup backup;
    private Restore restore;

    @BeforeAll
    public void setup() throws Exception {

        configMap = new HashMap<>();
        configMap.put("PROJECT_NAME", PROJECT_NAME);
        configMap.put("PROFILE_NAME", PROFILE_NAME);
        configMap.put("DATA_REGION", DATA_REGION);

        parent = new App();
        testDataLoader = new TestDataLoader();

        try {
            testDataLoader.createS3Buckets();
            testDataLoader.createUserTable();
            testDataLoader.createActivityTable();
            testDataLoader.createSSMParameters();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        testDataLoader.loadRawFiles();

        testDataLoader.loadUserData();

        testDataLoader.loadActivityData();



    }


    @Disabled
    @Test
    public void testFullRestoreNoEncryption() throws Exception {
        // take backup
        File destDir = new File(System.getProperty("java.io.tmpdir"), "clitest");
        backup = new Backup(parent, configMap, ENV, destDir.getAbsolutePath(), false, 1, "", null, true);
        backup.call();

        Thread.currentThread().sleep(5000l);

        testDataLoader.dropUserData();
        testDataLoader.dropActivityData();
        testDataLoader.dropRawFiles();

        testDataLoader.loadUserData();
        testDataLoader.loadActivityData();
        testDataLoader.loadRawFiles();

        System.out.println("config map state " + configMap.get("PROJECT_NAME"));
        restore = new Restore(parent, configMap, ENV, backup.getOptions().getDestDir().getAbsolutePath(), false, null, null);
        int status = restore.call();

        assertEquals(0, status);

        Thread.currentThread().sleep(5000l);


        //assert that raw activity data has been correctly restored
        S3Facade s3Facade = new S3Facade(IntegrationTestPropertiesReader.get("region"));
        List<String> rawActivities = s3Facade.listObjects(IntegrationTestPropertiesReader.get("test-raw-bucket"));
        for (String rawActivity : rawActivities) {
            System.out.println(rawActivity);
        }
        assertTrue(rawActivities.contains("integration-test-user@crunch.ski/act1.fit"));
        assertTrue(rawActivities.contains("integration-test-user@crunch.ski/act2.fit"));
        assertTrue(rawActivities.contains("integration-test-user2@crunch.ski/act3.fit"));
        assertTrue(s3Facade.getObject(IntegrationTestPropertiesReader.get("test-raw-bucket"), "integration-test-user@crunch.ski/act1.fit").length > 0);

        //assert that user entries have been correctly restored
        DynamoFacade dynamoFacade = new DynamoFacade(IntegrationTestPropertiesReader.get("region"), IntegrationTestPropertiesReader.get("test-table-user"));
        UserDAO userDAO = new UserDAO(dynamoFacade, IntegrationTestPropertiesReader.get("test-table-user"));

        assertNotNull(userDAO.lookupUser("123"));

        //assert that activity records have been correctly restored
        ActivityDAO activityDAO = new ActivityDAO(dynamoFacade, IntegrationTestPropertiesReader.get("test-table-act"));
        List<ActivityItem> restoredItems = activityDAO.getActivitiesByUser("integration-test-user@crunch.ski");
        assertEquals(2, restoredItems.size());

        //assert that SSM parameters have been correctly restored
        SSMParameterFacade ssmParameterFacade = new SSMParameterFacade(IntegrationTestPropertiesReader.get("region"),
                CredentialsProviderFactory.getDefaultCredentialsProvider());
        String expectedWeatherParam = ENV + "-weather-api-key";
        String expectedLocationParam = ENV + "-location-api-key";
        assertEquals("weather123", ssmParameterFacade.getParameter(expectedWeatherParam));
        assertEquals("location123", ssmParameterFacade.getParameter(expectedLocationParam));


    }


    @AfterAll
    public void tearDown() throws Exception {
        try {
            testDataLoader.dropUserTable();
        } catch (Exception ex) {
        }
        try {
            testDataLoader.dropActTable();
        } catch (Exception ex) {
        }
        try {
            testDataLoader.dropBucket(IntegrationTestPropertiesReader.get("test-raw-bucket"));
        } catch (Exception ex) {
        }
        try {
            testDataLoader.dropBucket(IntegrationTestPropertiesReader.get("test-act-bucket"));
        } catch (Exception ex) {
        }

        try {
            testDataLoader.deleteSSMParameters();
        } catch (Exception ex) {
        }
    }
}
