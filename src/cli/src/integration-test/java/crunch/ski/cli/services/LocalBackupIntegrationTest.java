package crunch.ski.cli.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
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
import ski.crunch.utils.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * loads data into environment
 * perform backup to local env
 * assert data is intact
 * perform restore to previous env
 * assert data equality with initial
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LocalBackupIntegrationTest {

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

        testDataLoader.createS3Buckets();
        testDataLoader.loadRawFiles();
        //testDataLoader.loadActFiles();
        testDataLoader.createUserTable();
        testDataLoader.loadUserData();
        testDataLoader.createActivityTable();
        testDataLoader.loadActivityData();
        testDataLoader.createSSMParameters();
        //Thread.currentThread().sleep(20000l);  // wait for raw files to become processed
    }


    @Test
    public void testFullBackupNoEncryption() throws Exception {

        File destDir = new File(System.getProperty("java.io.tmpdir"), "clitest");

        backup = new Backup(parent, configMap, ENV, destDir.getAbsolutePath(), false, 1, "", null, true);
        backup.call();

        //assert that data is properly backed up
        assertBackupCreatedSuccessfully();

        //skip processed files.  In this test environment there is no bucket notification trigger set up

        // remove backup
        removeBackup(backup);
    }

    @Test
    public void testFullBackupEncrypted() throws Exception{
        File destDir = new File(System.getProperty("java.io.tmpdir"), "clitest");

        backup = new Backup(parent, configMap, ENV, destDir.getAbsolutePath(), false, 1, "", "mySecretKey!", true);
        backup.call();

        assertEncryptedBackupCreatedSuccessfully();

        removeBackup(backup);
    }

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


    //TODO -> fix issue with backfilling email-index gsi
    @Disabled
    @Test
    public void testUserBackupNoEncryption() throws Exception{
        File destDir = new File(System.getProperty("java.io.tmpdir"), "clitest");

        backup = new Backup(parent, configMap, ENV, destDir.getAbsolutePath(), false, 1, "integration-test-user@crunch.ski", null, true);
        backup.call();
    }

    @Disabled
    @Test
    public void testUserBackupEncrypted() {

    }

    @AfterAll
    public void tearDown() throws IOException {

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


    private void removeBackup(Backup backup) {
        FileUtils.deleteDirectory(backup.getOptions().getDestDir());
    }

    private void assertBackupCreatedSuccessfully() throws Exception{

        File destDir = new File(System.getProperty("java.io.tmpdir"), "clitest");

        // does the backup directory exist?
        assertTrue(destDir.exists());
        assertTrue(backup.getOptions().getDestDir().exists());

        // do all the files exist?
        File metadata = new File(backup.getOptions().getDestDir(), ".metadata.json");
        File activities = new File(backup.getOptions().getDestDir(), "activities.json");
        File users = new File(backup.getOptions().getDestDir(), "users.json");
        File rawDir = new File(backup.getOptions().getDestDir(), "raw_activities");
        File procDir = new File(backup.getOptions().getDestDir(), "processed_activities");

        assertTrue(metadata.exists());
        assertTrue(activities.exists());
        assertTrue(users.exists());
        assertTrue(rawDir.exists());
        assertTrue(procDir.exists());

        //some basic validation of backup contents

        //activities json
        String activitiesJson = FileUtils.readFileToString(activities);
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode json = (ArrayNode) objectMapper.readTree(activitiesJson);
        assertTrue(json.isArray() && json.size() == 3);

        //users json
        String usersJson = FileUtils.readFileToString(users);
        ArrayNode jsonUsers = (ArrayNode) objectMapper.readTree(usersJson);
        assertTrue(jsonUsers.isArray());
        assertTrue(jsonUsers.size() == 2);


        //raw dir
        assertTrue(rawDir.isDirectory());

        File user1raw = new File(rawDir, "integration-test-user@crunch.ski");
        File user2raw = new File(rawDir, "integration-test-user2@crunch.ski");
        assertTrue(user1raw.isDirectory());
        assertTrue(user2raw.isDirectory());

        File act1 = new File(user1raw, "act1.fit");
        File act2 = new File(user1raw, "act2.fit");
        assertTrue(act1.exists());
        assertTrue(act2.exists());

        File act3 = new File(user2raw, "act3.fit");
        assertTrue(act3.exists());

        FileInputStream fis = new FileInputStream(act1);
        assertTrue(fis.readAllBytes().length > 0);
    }


    private void assertUserBackupCreatedSuccessfully() throws Exception{

        File destDir = new File(System.getProperty("java.io.tmpdir"), "clitest");

        // does the backup directory exist?
        assertTrue(destDir.exists());
        assertTrue(backup.getOptions().getDestDir().exists());

        // do all the files exist?
        File metadata = new File(backup.getOptions().getDestDir(), ".metadata.json");
        File activities = new File(backup.getOptions().getDestDir(), "activities.json");
        File users = new File(backup.getOptions().getDestDir(), "users.json");
        File rawDir = new File(backup.getOptions().getDestDir(), "raw_activities");
        File procDir = new File(backup.getOptions().getDestDir(), "processed_activities");

        assertTrue(metadata.exists());
        assertTrue(activities.exists());
        assertTrue(users.exists());
        assertTrue(rawDir.exists());
        assertTrue(procDir.exists());

        //some basic validation of backup contents

        //activities json
        String activitiesJson = FileUtils.readFileToString(activities);
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode json = (ArrayNode) objectMapper.readTree(activitiesJson);
        assertTrue(json.isArray() && json.size() == 2);

        //users json
        String usersJson = FileUtils.readFileToString(users);
        ArrayNode jsonUsers = (ArrayNode) objectMapper.readTree(usersJson);
        assertTrue(jsonUsers.isArray());
        assertTrue(jsonUsers.size() == 1);


        //raw dir
        assertTrue(rawDir.isDirectory());

        File user1raw = new File(rawDir, "integration-test-user@crunch.ski");
        File user2raw = new File(rawDir, "integration-test-user2@crunch.ski");
        assertTrue(user1raw.isDirectory());
        assertTrue(user2raw.isDirectory());

        File act1 = new File(user1raw, "act1.fit");
        File act2 = new File(user1raw, "act2.fit");
        assertTrue(act1.exists());
        assertTrue(act2.exists());

        File act3 = new File(user2raw, "act3.fit");
        assertFalse(act3.exists());

    }


    private void assertEncryptedBackupCreatedSuccessfully() throws Exception{

        File destDir = new File(System.getProperty("java.io.tmpdir"), "clitest");

        // does the backup directory exist?
        assertTrue(destDir.exists());
        assertTrue(backup.getOptions().getDestDir().exists());

        // do all the files exist?
        File metadata = new File(backup.getOptions().getDestDir(), ".metadata.json");
        File activities = new File(backup.getOptions().getDestDir(), "activities.json");
        File users = new File(backup.getOptions().getDestDir(), "users.json");
        File rawDir = new File(backup.getOptions().getDestDir(), "raw_activities");
        File procDir = new File(backup.getOptions().getDestDir(), "processed_activities");

        assertTrue(metadata.exists());
        assertTrue(activities.exists());
        assertTrue(users.exists());
        assertTrue(rawDir.exists());
        assertTrue(procDir.exists());

        //some basic validation of backup contents

        //activities json
        String activitiesJson = FileUtils.readFileToString(activities);
        ObjectMapper objectMapper = new ObjectMapper();
        assertThrows(JsonProcessingException.class, () -> {
            objectMapper.readTree(activitiesJson);
        });

        //users json
        String usersJson = FileUtils.readFileToString(users);
        assertThrows(JsonProcessingException.class, () -> {
            objectMapper.readTree(usersJson);
        });


        //raw dir
        assertTrue(rawDir.isDirectory());
    }

}
