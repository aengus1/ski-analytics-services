package crunch.ski.cli.services;

import com.amazonaws.auth.AWSCredentialsProvider;
import crunch.ski.cli.model.RestoreOptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ski.crunch.aws.DynamoFacade;
import ski.crunch.aws.S3Facade;
import ski.crunch.aws.SSMParameterFacade;
import ski.crunch.dao.UserDAO;
import ski.crunch.utils.FileUtils;

import java.io.File;
import java.time.LocalDateTime;
import java.util.HashMap;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LocalRestoreServiceTest {

    private RestoreOptions restoreOptions;

    private LocalRestoreService restoreService;

    @Mock
    AWSCredentialsProvider credentialsProvider;

    @Mock
    S3Facade s3Facade;

    @Mock
    DynamoFacade dynamoFacade;

    @Mock
    UserDAO userDAO;

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
        restoreOptions = new RestoreOptions();
        HashMap<String, String> config = new HashMap<>();
        config.put("DATA_REGION", "ca-central-1");
        config.put("PROJECT_NAME", "crunch-ski");
        config.put("PROFILE_NAME", "default");
        restoreOptions.setConfigMap(config);
        restoreOptions.setEnvironment("dev");
        restoreOptions.setRestoreId("12345");
        restoreOptions.setUsers(null);
        restoreOptions.setVerbose(false);
        restoreOptions.setDecryptKey(null);
        restoreOptions.setBackupArchive(System.getProperty("java.io.tmpdir") + "/localrestoretest");
        restoreOptions.setRestoreDateTime(LocalDateTime.now());

        MockitoAnnotations.initMocks(this);
        restoreService = new LocalRestoreService(credentialsProvider, dynamoFacade, s3Facade, userDAO,  ssmParameterFacade, restoreOptions);
    }


    @AfterEach
    public void tearDown() {
        try {
            FileUtils.deleteDirectory(restoreOptions.getSourceDir());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
