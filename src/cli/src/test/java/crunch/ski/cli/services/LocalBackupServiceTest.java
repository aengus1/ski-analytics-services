package crunch.ski.cli.services;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import crunch.ski.cli.model.BackupOptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ski.crunch.aws.DynamoFacade;
import ski.crunch.aws.S3Facade;
import ski.crunch.aws.SSMParameterFacade;
import ski.crunch.dao.ActivityDAO;
import ski.crunch.dao.UserDAO;
import ski.crunch.model.ActivityItem;
import ski.crunch.model.UserSettingsItem;
import ski.crunch.utils.ChecksumFailedException;
import ski.crunch.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LocalBackupServiceTest {

    private BackupOptions backupOptions;

    private LocalBackupService backupService;

    @Mock
    AWSCredentialsProvider credentialsProvider;

    @Mock
    S3Facade s3Facade;

    @Mock
    DynamoFacade dynamoFacade;

    @Mock
    UserDAO userDAO;

    @Mock
    ActivityDAO activityDAO;

    @Mock
    SSMParameterFacade ssmParameterFacade;

    @Captor
    ArgumentCaptor<String> keyCaptor;

    @Captor
    ArgumentCaptor<Boolean> boolCaptor;

    @Captor
    ArgumentCaptor<File> fileCaptor;

    @Captor
    ArgumentCaptor<Class> classCaptor;

    @BeforeEach
    public void setup() {

        // set options
        backupOptions = new BackupOptions();
        HashMap<String, String> config = new HashMap<>();
        config.put("DATA_REGION", "ca-central-1");
        config.put("PROJECT_NAME", "crunch-ski");
        config.put("PROFILE_NAME", "default");
        backupOptions.setConfigMap(config);
        backupOptions.setEnvironment("dev");
        backupOptions.setBackupId("12345");
        backupOptions.setUsers(null);
        backupOptions.setVerbose(false);
        backupOptions.setUncompressed(true);
        backupOptions.setEncryptionKey(null);
        backupOptions.setDestination(System.getProperty("java.io.tmpdir") + "/localbackuptest");
        backupOptions.setBackupDateTime(LocalDateTime.now());

        MockitoAnnotations.initMocks(this);
        backupService = new LocalBackupService(credentialsProvider, dynamoFacade, s3Facade, userDAO, activityDAO, ssmParameterFacade, backupOptions);
    }

    @Test
    public void testCalcBucketName() {
        String bucketName = backupService.calcBucketName("Activity", backupOptions);
        assertEquals("dev-Activity-crunch-ski", bucketName);
    }

    @Test
    public void testFullLocalBackupCallsS3FacadeWithCorrectArgs() throws IOException, ChecksumFailedException, GeneralSecurityException {

        File destRaw = new File(System.getProperty("java.io.tmpdir") + "/localbackuptest/dev-crunch-ski-"
                + BackupRestoreService.ISO_LOCAL_DATE_TIME_FILE.format(backupOptions.getBackupDateTime())
                + "/raw_activities");
        File destProc = new File(System.getProperty("java.io.tmpdir") + "/localbackuptest/dev-crunch-ski-"
                + BackupRestoreService.ISO_LOCAL_DATE_TIME_FILE.format(backupOptions.getBackupDateTime())
                + "/processed_activities");

        try {
            backupService.mkDestDir();
            backupService.fullLocalBackup();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        verify(s3Facade, times(2)).backupS3BucketToDirectory(keyCaptor.capture(), fileCaptor.capture(), boolCaptor.capture(), keyCaptor.capture());

        List<String> strings = keyCaptor.getAllValues();
        List<File> files = fileCaptor.getAllValues();
        List<Boolean> booleans = boolCaptor.getAllValues();

        assertEquals(4, strings.size());
        assertEquals(Arrays.asList("dev-activity-crunch-ski", null, "dev-raw-activity-crunch-ski", null), strings);

        assertEquals(2, files.size());
        assertEquals(Arrays.asList(destProc, destRaw), files);

        assertEquals(Arrays.asList(false, false), booleans);

    }


    @Test
    public void testFullLocalBackupCallsDynamoFacadeWithCorrectArgs() throws IOException, ChecksumFailedException, GeneralSecurityException {

        File dest = new File(System.getProperty("java.io.tmpdir") + "/localbackuptest/dev-crunch-ski-"
                + BackupRestoreService.ISO_LOCAL_DATE_TIME_FILE.format(backupOptions.getBackupDateTime())
        );

        try {
            backupService.mkDestDir();
            backupService.fullLocalBackup();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        verify(dynamoFacade, times(2)).fullTableBackup(classCaptor.capture(), keyCaptor.capture(), fileCaptor.capture(), keyCaptor.capture(), keyCaptor.capture());


        List<String> strings = keyCaptor.getAllValues();
        List<File> files = fileCaptor.getAllValues();
        List<Class> classes = classCaptor.getAllValues();

        assertEquals(Arrays.asList(UserSettingsItem.class, ActivityItem.class), classes);

        assertEquals(Arrays.asList(dest, dest), files);

        assertEquals(Arrays.asList("dev-crunch-ski-userTable", "users.json", null, "dev-crunch-ski-Activity", "activities.json", null), strings);

    }

    @Test
    public void testUserBackupCalledInLieuOfFullBackupWhenUserStringIsSet() throws IOException {

        // use a spy because can't call verify on a not-mock
        LocalBackupService service = spy(new LocalBackupService(credentialsProvider, dynamoFacade, s3Facade, userDAO, activityDAO, ssmParameterFacade, backupOptions));

        UserSettingsItem userSettingsItem = new UserSettingsItem();
        userSettingsItem.setEmail("aengusmccullough@hotmail.com");
        userSettingsItem.setId("123");
        when(userDAO.lookupUser("aengusmccullough@hotmail.com")).thenReturn(userSettingsItem);

        backupOptions.setUsers(Arrays.asList("aengusmccullough@hotmail.com"));
        service.apply();


        verify(service, times(1)).userDataBackup("aengusmccullough@hotmail.com", backupOptions.getDestDir());
    }


    @Test
    public void testFullLocalBackupCorrectlyCallsSSMFacade() throws IOException {
        backupOptions.setIncludeSSM(true);
        LocalBackupService service = spy(new LocalBackupService(credentialsProvider, dynamoFacade, s3Facade, userDAO, activityDAO, ssmParameterFacade, backupOptions));
        when(ssmParameterFacade.getParameter("dev-weather-api-key")).thenReturn("weatherkey123");
        when(ssmParameterFacade.getParameter("dev-rockset-api-key")).thenReturn("locationkey123");
        when(ssmParameterFacade.getParameter("dev-location-api-key")).thenReturn("rocksetkey123");

        service.apply();

        verify(service, times(1)).backupSSMParameters();
        verify(ssmParameterFacade, times(3)).getParameter(keyCaptor.capture());
        assertTrue(keyCaptor.getAllValues().contains("dev-weather-api-key"));
        assertTrue(keyCaptor.getAllValues().contains("dev-location-api-key"));
        assertTrue(keyCaptor.getAllValues().contains("dev-rockset-api-key"));

        //test name is written to file
        File ssmFile = new File(backupOptions.getDestDir(), "ssm.json");
        String ssmStr = FileUtils.readFileToString(ssmFile);
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode json = (ArrayNode) objectMapper.readTree(ssmStr);
        assertEquals(3, json.size());

        //test values are written correctly
        assertEquals("dev-weather-api-key", ((JsonNode) json.get(0)).get("key").asText());
        assertEquals("weatherkey123", ((JsonNode) json.get(0)).get("value").asText());

    }

    @Test
    public void testUserBackupCorrectlyBuildsActivitiesString() throws IOException {

        StringBuilder expectedSb = new StringBuilder();
        expectedSb.append("[ {\"id\":\"actid1\",\"date\":\"\",\"rawActivity\":{\"s3\":{\"bucket\":\"null\",\"key\":\"null\"}},\"processedActivity\":{\"s3\":{\"bucket\":\"null\",\"key\":\"null\"}},\"sourceIp\":\"\",\"userAgent\":\"\",\"userId\":\"aengusmccullough@hotmail.com\",\"status\":\"\",\"rawFileType\":\"\",\"timeOfDay\":\"\",\"activityType\":\"\",\"activitySubType\":\"\",\"activityDate\":\"\",\"device\":\"\",\"distance\":-998.0,\"duration\":-998.0,\"avHr\":-998,\"maxHr\":-998,\"avSpeed\":-998.0,\"maxSpeed\":-998.0,\"ascent\":10.0,\"descent\":-998.0,\"notes\":\"test notes\",\"lastUpdateTimestamp\":\"\"}")
                .append(", {\"id\":\"123\",\"date\":\"\",\"rawActivity\":{\"s3\":{\"bucket\":\"null\",\"key\":\"null\"}},\"processedActivity\":{\"s3\":{\"bucket\":\"null\",\"key\":\"null\"}},\"sourceIp\":\"\",\"userAgent\":\"\",\"userId\":\"aengusmccullough@hotmail.com\",\"status\":\"\",\"rawFileType\":\"\",\"timeOfDay\":\"\",\"activityType\":\"\",\"activitySubType\":\"\",\"activityDate\":\"\",\"device\":\"\",\"distance\":-998.0,\"duration\":-998.0,\"avHr\":-998,\"maxHr\":-998,\"avSpeed\":-998.0,\"maxSpeed\":-998.0,\"ascent\":12.0,\"descent\":-998.0,\"notes\":\"test again\",\"lastUpdateTimestamp\":\"\"}]");

        List<ActivityItem> activityItems = new ArrayList<>();
        ActivityItem item1 = new ActivityItem();
        item1.setUserId("aengusmccullough@hotmail.com");
        item1.setId("actid1");
        item1.setNotes("test notes");
        item1.setAscent(10d);

        ActivityItem item2 = new ActivityItem();
        item2.setId("123");
        item2.setUserId("aengusmccullough@hotmail.com");
        item2.setNotes("test again");
        item2.setAscent(12d);

        activityItems.add(item1);
        activityItems.add(item2);

        UserSettingsItem userSettingsItem = new UserSettingsItem();
        userSettingsItem.setEmail("aengusmccullough@hotmail.com");
        userSettingsItem.setId("123");
        backupOptions.setUsers(Arrays.asList("aengusmccullough@hotmail.com"));
        when(userDAO.lookupUser("aengusmccullough@hotmail.com")).thenReturn(userSettingsItem);

        when(activityDAO.getActivitiesByUser(userSettingsItem.getEmail())).thenReturn(activityItems);

        try {
            backupService.apply();
        } catch (Exception ex) {
            ex.printStackTrace();
        }


        File userDestination = new File(backupOptions.getDestDir() + "/" + userSettingsItem.getEmail(), "activities.json");
        assertEquals(expectedSb.toString().replaceAll(" ", "").replaceAll(System.lineSeparator(), ""),
                FileUtils.readFileToString(userDestination).replaceAll(" ", "").replaceAll(System.lineSeparator(), ""));
    }

    @AfterEach
    public void tearDown() {
        try {
            FileUtils.deleteDirectory(backupOptions.getDestDir());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
