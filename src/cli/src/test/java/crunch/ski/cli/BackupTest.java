package crunch.ski.cli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class BackupTest {

    private Backup backup;

    private App parent;

    private static final String DATA_REGION = "ca-central-1";
    private static final String PROFILE_NAME = "default";
    private static final String PROJECT_NAME = "test-project";
    private static final String ENV = "dev-test";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        parent = new App();
    }

    //TODO -> FIX
    @Disabled
    @Test
    public void testInitializeNoOverrides() throws Exception {
        Map<String, String> configMap = new HashMap<>();
        configMap.put("PROJECT_NAME", PROJECT_NAME);
        configMap.put("PROFILE_NAME", PROFILE_NAME);
        configMap.put("DATA_REGION", DATA_REGION);
        File destDir = new File(System.getProperty("java.io.tmpdir"), "clitest");

        backup = new Backup(parent, configMap, ENV, destDir.getAbsolutePath(), false, 1, "", null, true);
        backup.initialize();
        backup.call();
//        verify(credentialsProviderFactory, times(1))
//                .newCredentialsProvider(CredentialsProviderType.PROFILE, Optional.of(PROFILE_NAME));
        assertFalse( backup.getService().getS3().getTransferAcceleration());
        assertEquals(DATA_REGION, backup.getService().getS3().getRegion());
    }

    //TODO => FIX
    @Test
    @Disabled
    public void testInitializationWithOverrides() throws Exception{

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
        backup.call();
//        verify(credentialsProviderFactory, times(1))
//                .newCredentialsProvider(CredentialsProviderType.PROFILE, Optional.of("newProfile"));

        assertFalse(backup.getService().getS3().getTransferAcceleration());
        assertTrue(backup.getService().calcBucketName("my-bucket", backup.getOptions()).endsWith("test-test-project"));
        assertEquals("newRegion", backup.getService().getS3().getRegion());
    }

}
