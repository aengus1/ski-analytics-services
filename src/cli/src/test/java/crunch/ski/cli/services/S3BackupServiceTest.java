package crunch.ski.cli.services;

import com.amazonaws.auth.AWSCredentialsProvider;
import crunch.ski.cli.model.BackupOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ski.crunch.aws.DynamoFacade;
import ski.crunch.aws.S3Facade;
import ski.crunch.dao.ActivityDAO;
import ski.crunch.dao.UserDAO;
import ski.crunch.model.ActivityItem;
import ski.crunch.model.UserSettingsItem;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class S3BackupServiceTest {

    private BackupOptions backupOptions;

    private S3BackupService s3BackupService;

    @Mock
    private DynamoFacade dynamoFacade;

    @Mock
    private S3Facade s3Facade;

    @Mock
    private UserDAO userDAO;

    @Mock
    ActivityDAO activityDAO;

    @Mock
    AWSCredentialsProvider credentialsProvider;

    @Captor
    ArgumentCaptor<String> keyCaptor;

    @Captor
    ArgumentCaptor<String> keyCaptor2;

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
        backupOptions.setDestination("s3://faux-test-bucket/testkey");
        backupOptions.setS3Destination(true);
        backupOptions.setBackupDateTime(LocalDateTime.of(2020, 04, 11, 00, 02, 02));

        MockitoAnnotations.initMocks(this);
        s3BackupService = new S3BackupService(credentialsProvider, dynamoFacade, s3Facade, userDAO, activityDAO, backupOptions);
    }

    @Test
    public void testMkCloudDestDir() {

        s3BackupService.mkCloudDestDir();

        assertEquals("faux-test-bucket", backupOptions.getDestBucket());
        assertTrue(backupOptions.getDestKey().startsWith("testkey/dev-crunch-ski-"));
        assertTrue(backupOptions.getDestDir().exists());
    }

    @Test
    public void testFullS3Backup() throws IOException, GeneralSecurityException {

        try {
            s3BackupService.mkCloudDestDir();
            s3BackupService.fullCloudBackup();
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("exception thrown applying full cloud backup", ex);
        }
        verify(s3Facade, times(2)).backupS3BucketToS3(keyCaptor.capture(), keyCaptor.capture(), keyCaptor.capture(), keyCaptor.capture());
        verify(s3Facade, times(2)).putObject(any(), any(), any(File.class));

        List<String> keys = keyCaptor.getAllValues();

        assertEquals(Arrays.asList("dev-activity-crunch-ski", "faux-test-bucket", "testkey/dev-crunch-ski-2020-04-11T00-02-02/processed_activities", null,
                "dev-raw-activity-crunch-ski", "faux-test-bucket", "testkey/dev-crunch-ski-2020-04-11T00-02-02/raw_activities", null), keys);

        File tempDir = new File(System.getProperty("java.io.tmpdir"), backupOptions.getBackupId());
        assertTrue(tempDir.exists() && tempDir.isDirectory());


        verify(dynamoFacade, times(2)).fullTableBackup(classCaptor.capture(), keyCaptor2.capture(), fileCaptor.capture(), keyCaptor2.capture(), keyCaptor2.capture());

        keys = keyCaptor2.getAllValues();
        List<Class> classes = classCaptor.getAllValues();
        List<File> files = fileCaptor.getAllValues();

        assertEquals(Arrays.asList("dev-crunch-ski-userTable", "users.json", null, "dev-crunch-ski-Activity", "activities.json", null), keys);
        assertEquals(Arrays.asList(UserSettingsItem.class, ActivityItem.class), classes);
        assertEquals(Arrays.asList(tempDir, tempDir), files);

    }


}
