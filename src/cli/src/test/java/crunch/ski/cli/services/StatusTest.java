package crunch.ski.cli.services;

import crunch.ski.cli.App;
import crunch.ski.cli.Status;
import crunch.ski.cli.model.ModuleStatus;
import crunch.ski.cli.model.StatusOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ski.crunch.aws.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StatusTest {

    private EnvironmentManagementService environmentManagementService;

    @Mock
    private S3Facade s3Facade;

    @Mock
    private SSMParameterFacade ssmParameterFacade;

    @Mock
    private DynamoFacade dynamoFacade;

    @Mock
    private CloudformationFacade cloudformationFacade;

    @Mock
    private IAMFacade iamFacade;

    private StatusOptions options;

    @BeforeEach
    public void setUp() {
        options = new StatusOptions();
        Map<String, String> configMap = new HashMap<>();
        configMap.put("DATA_REGION", "ca-central-1");
        configMap.put("PROJECT_NAME", "crunch-ski");
        configMap.put("PROFILE_NAME", "default");
        options.setConfigMap(configMap);
        options.setEnvironment("dev");

        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testAllModulesUp() {

        when(s3Facade.bucketExists("dev-activity-crunch-ski")).thenReturn(true);
        when(s3Facade.bucketExists("dev-raw-activity-crunch-ski")).thenReturn(true);
        when(dynamoFacade.tableExists("dev-crunch-ski-Activity")).thenReturn(true);
        when(dynamoFacade.tableExists("dev-crunch-ski-User")).thenReturn(true);

        when(ssmParameterFacade.parameterExists("dev-weather-api-key")).thenReturn(true);
        when(ssmParameterFacade.parameterExists("dev-location-api-key")).thenReturn(true);
        when(ssmParameterFacade.parameterExists("dev-rockset-api-key")).thenReturn(true);

        when(cloudformationFacade.stackExists("dev-crunch-ski-api")).thenReturn(true);
        when(cloudformationFacade.stackExists("dev-crunch-ski-auth")).thenReturn(true);
        when(cloudformationFacade.stackExists("dev-crunch-ski-websocket")).thenReturn(true);
        when(cloudformationFacade.stackExists("dev-crunch-ski-graphql")).thenReturn(true);
        when(cloudformationFacade.stackExists("dev-crunch-ski-cf-rockset")).thenReturn(true);
        when(cloudformationFacade.stackExists("dev-crunch-ski-cf-userpool-trigger")).thenReturn(true);
        when(cloudformationFacade.stackExists("dev-crunch-ski-cf-bucket-notification")).thenReturn(true);

        environmentManagementService = new EnvironmentManagementService(s3Facade, ssmParameterFacade, dynamoFacade,
                cloudformationFacade, options);

        Map<String, ModuleStatus> statusMap = environmentManagementService.getStatus();
        for (String s : statusMap.keySet()) {
            assertTrue(statusMap.get(s).equals(ModuleStatus.UP));
        }

    }

    @Test
    public void testDataModuleError() {

        when(s3Facade.bucketExists("dev-activity-crunch-ski")).thenReturn(true);
        when(s3Facade.bucketExists("dev-raw-activity-crunch-ski")).thenReturn(true);
        when(dynamoFacade.tableExists("dev-crunch-ski-Activity")).thenReturn(true);
        when(dynamoFacade.tableExists("dev-crunch-ski-User")).thenReturn(false);

        when(ssmParameterFacade.parameterExists("dev-weather-api-key")).thenReturn(true);
        when(ssmParameterFacade.parameterExists("dev-location-api-key")).thenReturn(true);
        when(ssmParameterFacade.parameterExists("dev-rockset-api-key")).thenReturn(true);


        environmentManagementService = new EnvironmentManagementService(s3Facade, ssmParameterFacade, dynamoFacade,
                cloudformationFacade, options);

        Map<String, ModuleStatus> statusMap = environmentManagementService.getStatus();

        assertEquals(ModuleStatus.ERROR, statusMap.get("DATA"));

    }

    @Test
    public void testApplicationModuleDown() {


        when(cloudformationFacade.stackExists("dev-crunch-ski-api")).thenReturn(false);
        when(cloudformationFacade.stackExists("dev-crunch-ski-auth")).thenReturn(false);
        when(cloudformationFacade.stackExists("dev-crunch-ski-websocket")).thenReturn(false);
        when(cloudformationFacade.stackExists("dev-crunch-ski-graphql")).thenReturn(false);
        when(cloudformationFacade.stackExists("dev-crunch-ski-cf-rockset")).thenReturn(false);
        when(cloudformationFacade.stackExists("dev-crunch-ski-cf-userpool-trigger")).thenReturn(false);
        when(cloudformationFacade.stackExists("dev-crunch-ski-cf-bucket-notification")).thenReturn(false);

        environmentManagementService = new EnvironmentManagementService(s3Facade, ssmParameterFacade, dynamoFacade,
                cloudformationFacade, options);

        Map<String, ModuleStatus> statusMap = environmentManagementService.getStatus();
        assertEquals(ModuleStatus.DOWN, statusMap.get("APPLICATION"));

    }

    @Test
    public void testCommandLine() {
        StatusOptions statusOptions = new StatusOptions();
        HashMap<String, String> configMap = new HashMap<>();
        configMap.put("DATA_REGION", "ca-central-1");
        configMap.put("PROFILE_NAME", "default");
        configMap.put("PROJECT_NAME", "crunch-ski-test");
        statusOptions.setConfigMap(configMap);
        statusOptions.setEnvironment("dev");

        App app = new App();
        Status status = new Status();
        status.setParent(app);
        status.setStatusOptions(statusOptions);


        PrintStream originalOut = System.out;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        try {
            int res = status.call();
            assertEquals(1, res);
        }catch(Exception ex) {
            ex.printStackTrace();
        } finally {
            System.setOut(originalOut);
        }

        String expected = "\u001B[1;30mModule|Status\n" +
                "\u001B[1;31mFRONTEND|DOWN\n" +
                "\u001B[0m\u001B[1;31mDATA|DOWN\n" +
                "\u001B[0m\u001B[1;31mAPPLICATION|DOWN\n" +
                "\u001B[0m\u001B[1;31mAPI|DOWN\n" +
                "\u001B[0m";
        assertEquals(expected.replaceAll(" ", ""), outContent.toString().replaceAll(" ", ""));
    }

}
