package crunch.ski.cli.services;

import crunch.ski.cli.model.Options;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import ski.crunch.aws.*;

import java.util.HashMap;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class EnvironmentManagementServiceTest {

    private Options options;
    private EnvironmentManagementService service;

    @Mock
    private S3Facade s3Facade;

    @Mock
    private SSMParameterFacade ssmParameterFacade;

    @Mock
    private DynamoFacade dynamoFacade;

    @Mock
    private CloudfrontFacade cloudfrontFacade;

    @Mock
    private CognitoFacade cognitoFacade;

    @Mock
    private CloudformationFacade cloudformationFacade;

//    @BeforeEach
//    public void setup() {
//        options = new Options();
//        HashMap<String, String> config = new HashMap<>();
//        config.put("DATA_REGION", "ca-central-1");
//        config.put("PROJECT_NAME", "crunch-ski");
//        config.put("PROFILE_NAME", "default");
//        config.put("DOMAIN_NAME", "mccullough-solutions.ca");
//        config.put("PROJECT_SOURCE_DIR", "/Users/aengus/code/ski-analytics-services/");
//        options.setConfigMap(config);
//        options.setEnvironment("dev");
//
//
//        MockitoAnnotations.initMocks(this);
//        service = new EnvironmentManagementService(s3Facade, ssmParameterFacade,dynamoFacade, cloudformationFacade,
//                cloudfrontFacade, cognitoFacade, options);
//    }


    //TODO -> add other tests



    @Test
    @Disabled //used as entry point for debugging
    public void testProvision() {
        HashMap<String, String> config = new HashMap<>();
        config.put("DATA_REGION", "ca-central-1");
        config.put("PROJECT_NAME", "crunch-test");
        config.put("PROFILE_NAME", "default");
        config.put("DOMAIN_NAME", "mccullough-solutions.ca");
        config.put("PROJECT_SOURCE_DIR", "/Users/aengus/code/ski-analytics-services");

        Options options = new Options();
        options.setEnvironment("staging");
        options.setModule("application");
        options.setConfigMap(config);
        service  = new EnvironmentManagementService(options);
        service.provision(options);
    }


}
