package crunch.ski.cli.services;

import com.amazonaws.services.simplesystemsmanagement.model.DeleteParameterResult;
import crunch.ski.cli.model.WipeOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ski.crunch.aws.S3Facade;
import ski.crunch.aws.SSMParameterFacade;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class EnvironmentManagementServiceTest {

    private WipeOptions options;
    private EnvironmentManagementService service;

    @Mock
    private S3Facade s3Facade;

    @Mock
    private SSMParameterFacade ssmParameterFacade;

    @BeforeEach
    public void setup() {
        options = new WipeOptions();
        HashMap<String, String> config = new HashMap<>();
        config.put("DATA_REGION", "ca-central-1");
        config.put("PROJECT_NAME", "crunch-ski");
        config.put("PROFILE_NAME", "default");
        options.setConfigMap(config);
        options.setEnvironment("dev");
        options.setForDeletionOnly(true);
        options.setSkipBackup(false);
        options.setRegion("ca-central-1");
        options.setAutoApprove(true);
        options.setBackupLocation(System.getProperty("java.io.tmpdir")+"/backups");

        MockitoAnnotations.initMocks(this);
        service = new EnvironmentManagementService(s3Facade, ssmParameterFacade, options);
    }


    @Test
    public void testWipeEnvironment() throws  Exception {

        doNothing().when(s3Facade).emptyBucket(any());
        when(ssmParameterFacade.deleteParameter(any())).thenReturn(new DeleteParameterResult());

        service.wipeEnvironment();


        verify(s3Facade, times(1)).emptyBucket("dev-raw-activity-crunch-ski");
        verify(s3Facade, times(1)).emptyBucket("dev-activity-crunch-ski");

        verify(ssmParameterFacade, times(0)).deleteParameter(any());
    }

    @Test
    public void testAutoApprove() throws  Exception {

        options.setAutoApprove(false);

        ByteArrayInputStream in = new ByteArrayInputStream("n".getBytes());
        InputStream sysInBackup = System.in;
        System.setIn(in);
        doNothing().when(s3Facade).emptyBucket(any());
        when(ssmParameterFacade.deleteParameter(any())).thenReturn(new DeleteParameterResult());

        assertFalse(service.wipeEnvironment());

        in = new ByteArrayInputStream("Y".getBytes());
        System.setIn(in);
        assertTrue(service.wipeEnvironment());

    }


}
