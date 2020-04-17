package crunch.ski.cli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ski.crunch.aws.CredentialsProviderFactory;
import ski.crunch.aws.CredentialsProviderType;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class BackupTest {

    private Backup backup;

    @Mock
    private CredentialsProviderFactory credentialsProviderFactory;

    private App parent;

    private static final String DATA_REGION = "ca-central-1";
    private static final String PROFILE_NAME = "default";
    private static final String PROJECT_NAME = "test-project";
    private static final String ENV = "dev-test";

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        parent = new App();
    }

    @Test
    public void testInitializeNoOverrides()  {
        Map<String, String> configMap = new HashMap<>();
        configMap.put("PROJECT_NAME", PROJECT_NAME);
        configMap.put("PROFILE_NAME", PROFILE_NAME);
        configMap.put("DATA_REGION", DATA_REGION);
        File destDir = new File(System.getProperty("java.io.tmpdir"), "clitest");

        backup = new Backup(parent, configMap, ENV, destDir.getAbsolutePath(), false, 1, "", null, true);
        backup.initialize();

        verify(credentialsProviderFactory, times(1))
                .newCredentialsProvider(CredentialsProviderType.PROFILE, Optional.of(PROFILE_NAME));
        assertFalse( backup.getS3Backup().getS3Facade().getTransferAcceleration());
        assertEquals(DATA_REGION, backup.getS3Backup().getS3Facade().getRegion());
    }

    @Test
    public void testInitializationWithOverrides() {

        parent.setAwsProfile("newProfile");
        parent.setDataRegion("newRegion");
        parent.setProjectName("test-test-project");
        Map<String, String> configMap = new HashMap<>();
        configMap.put("PROJECT_NAME", PROJECT_NAME);
        configMap.put("PROFILE_NAME", PROFILE_NAME);
        configMap.put("DATA_REGION", DATA_REGION);
        File destDir = new File(System.getProperty("java.io.tmpdir"), "clitest");

        backup = new Backup(parent, configMap, ENV, destDir.getAbsolutePath(), false, 1, "", null, true);
        backup.initialize();

        verify(credentialsProviderFactory, times(1))
                .newCredentialsProvider(CredentialsProviderType.PROFILE, Optional.of("newProfile"));
        assertFalse(backup.getOptions().getTransferAcceleration());
        assertTrue(backup.getService().calcBucketName("my-bucket").endsWith("test-test-project"));
        assertEquals("newRegion", backup.getS3Backup().getS3Facade().getRegion());
    }

}
