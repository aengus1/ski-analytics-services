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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    public void testInitializeNoOverrides() throws Exception {
        Map<String, String> configMap = new HashMap<>();
        configMap.put("PROJECT_NAME", PROJECT_NAME);
        configMap.put("PROFILE_NAME", PROFILE_NAME);
        configMap.put("DATA_REGION", DATA_REGION);
        File destDir = new File(System.getProperty("java.io.tmpdir"), "clitest");

        backup = new Backup(parent, credentialsProviderFactory, configMap, ENV, destDir, false);
        backup.initialize();

        verify(credentialsProviderFactory, times(1))
                .newCredentialsProvider(CredentialsProviderType.PROFILE, Optional.of(PROFILE_NAME));
        assertEquals(false, backup.getS3Facade().getTransferAcceleration());
        assertTrue(backup.getBackupId().startsWith(ENV + "-"));
        assertTrue(backup.getS3Facade().getRegion().equals(DATA_REGION));
    }

    @Test
    public void testInitializationWithOverrides() throws Exception{

        parent.setAwsProfile("newProfile");
        parent.setDataRegion("newRegion");
        parent.setProjectName("test-test-project");
        Map<String, String> configMap = new HashMap<>();
        configMap.put("PROJECT_NAME", PROJECT_NAME);
        configMap.put("PROFILE_NAME", PROFILE_NAME);
        configMap.put("DATA_REGION", DATA_REGION);
        File destDir = new File(System.getProperty("java.io.tmpdir"), "clitest");

        backup = new Backup(parent, credentialsProviderFactory, configMap, ENV, destDir, true);
        backup.initialize();

        verify(credentialsProviderFactory, times(1))
                .newCredentialsProvider(CredentialsProviderType.PROFILE, Optional.of("newProfile"));
        assertEquals(true, backup.getS3Facade().getTransferAcceleration());
        assertTrue(backup.calcBucketName("my-bucket").endsWith("test-test-project"));
        assertTrue(backup.getS3Facade().getRegion().equals("newRegion"));
    }

}
