package crunch.ski.cli.services;

import com.amazonaws.services.cloudformation.model.Export;
import crunch.ski.cli.App;
import crunch.ski.cli.Status;
import crunch.ski.cli.model.ModuleStatus;
import crunch.ski.cli.model.Options;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ski.crunch.aws.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StatusTest {

    private StatusService statusService;

    @Mock
    private S3Facade s3Facade;

    @Mock
    private SSMParameterFacade ssmParameterFacade;

    @Mock
    private DynamoFacade dynamoFacade;

    @Mock
    private CloudformationFacade cloudformationFacade;

    @Mock
    private CloudfrontFacade cloudfrontFacade;

    @Mock
    private CognitoFacade cognitoFacade;

    @Mock
    private IAMFacade iamFacade;

    private Options options;

    @BeforeEach
    public void setUp() {
        options = new Options();
        Map<String, String> configMap = new HashMap<>();
        configMap.put("DATA_REGION", "ca-central-1");
        configMap.put("PROJECT_NAME", "crunch-ski");
        configMap.put("PROFILE_NAME", "default");
        configMap.put("DOMAIN_NAME", "test-domain.ca");
        configMap.put("SECONDARY_REGION", "us-east-1");
        options.setConfigMap(configMap);
        options.setEnvironment("dev");

        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testAllModulesUp() {

        when(s3Facade.bucketExists("dev-activity-crunch-ski")).thenReturn(true);
        when(s3Facade.bucketExists("dev-raw-activity-crunch-ski")).thenReturn(true);
        when(s3Facade.bucketExists("dev-app.test-domain.ca")).thenReturn(true);

        when(dynamoFacade.tableExists("dev-crunch-ski-Activity")).thenReturn(true);
        when(dynamoFacade.tableExists("dev-crunch-ski-userTable")).thenReturn(true);

        when(ssmParameterFacade.parameterExists("dev-app-cfdistro-id")).thenReturn(true);
        when(ssmParameterFacade.getParameter("dev-app-cfdistro-id")).thenReturn("cf1234");
        when(ssmParameterFacade.parameterExists("dev-weather-api-key")).thenReturn(true);
        when(ssmParameterFacade.parameterExists("dev-location-api-key")).thenReturn(true);
        //when(ssmParameterFacade.parameterExists("dev-rockset-api-key")).thenReturn(true);

        when(cloudformationFacade.stackExists("dev-crunch-ski-api")).thenReturn(true);
        when(cloudformationFacade.stackExists("dev-crunch-ski-auth")).thenReturn(true);
        when(cloudformationFacade.stackExists("dev-crunch-ski-websocket")).thenReturn(true);
        when(cloudformationFacade.stackExists("dev-crunch-ski-graphql","us-east-1")).thenReturn(true);
        when(cloudformationFacade.stackExists("dev-crunch-ski-cf-rockset")).thenReturn(true);
        when(cloudformationFacade.stackExists("dev-crunch-ski-cf-userpool-trg")).thenReturn(true);
        when(cloudformationFacade.stackExists("dev-crunch-ski-cf-bucket-notif")).thenReturn(true);
        when(cloudformationFacade.stackExists("dev-crunch-ski-data-var-stack")).thenReturn(true);

        when(cloudfrontFacade.cfDistroExists("cf1234")).thenReturn(true);

        Export export = new Export();
        export.setExportingStackId(options.getEnvironment() + "-" + options.getConfigMap().get("PROJECT_NAME") + "-data-var-stack");
        export.setName("UserPoolArn" + options.getEnvironment());
        export.setValue("blah:userpool/myUserPoolId");
        when(cloudformationFacade.getExportedOutputs()).thenReturn(Optional.of(List.of(export)));
        when(cognitoFacade.userPoolExists("myUserPoolId")).thenReturn(true);


        statusService = new StatusService(s3Facade, ssmParameterFacade, dynamoFacade,
                cloudformationFacade, cloudfrontFacade, cognitoFacade, options);

        Map<String, ModuleStatus> statusMap = statusService.getStatus();
        for (String s : statusMap.keySet()) {
            assertTrue(statusMap.get(s).equals(ModuleStatus.UP));
        }

    }

    @Test
    public void testDataModuleError() {

        when(s3Facade.bucketExists("dev-activity-crunch-ski")).thenReturn(true);
        when(s3Facade.bucketExists("dev-raw-activity-crunch-ski")).thenReturn(true);
        when(s3Facade.bucketExists("dev-app.test-domain.ca")).thenReturn(true);
        when(dynamoFacade.tableExists("dev-crunch-ski-Activity")).thenReturn(true);
        when(dynamoFacade.tableExists("dev-crunch-ski-User")).thenReturn(false);

        when(ssmParameterFacade.parameterExists("dev-app-cfdistro-id")).thenReturn(true);
        when(ssmParameterFacade.parameterExists("dev-weather-api-key")).thenReturn(true);
        when(ssmParameterFacade.parameterExists("dev-location-api-key")).thenReturn(true);
        when(ssmParameterFacade.parameterExists("dev-rockset-api-key")).thenReturn(true);
        when(ssmParameterFacade.getParameter("dev-app-cfdistro-id")).thenReturn("cf1234");
        when(cloudfrontFacade.cfDistroExists("cf1234")).thenReturn(true);

        Export export = new Export();
        export.setExportingStackId(options.getEnvironment() + "-" + options.getConfigMap().get("PROJECT_NAME") + "-data-var-stack");
        export.setName("UserPoolArn" + options.getEnvironment());
        export.setValue("blah:userpool/myUserPoolId");
        when(cloudformationFacade.getExportedOutputs()).thenReturn(Optional.of(List.of(export)));
        when(cognitoFacade.userPoolExists("myUserPoolId")).thenReturn(true);

        statusService = new StatusService(s3Facade, ssmParameterFacade, dynamoFacade,
                cloudformationFacade, cloudfrontFacade, cognitoFacade, options);

        Map<String, ModuleStatus> statusMap = statusService.getStatus();

        assertEquals(ModuleStatus.ERROR, statusMap.get("DATA"));

    }

    @Test
    public void testApplicationModuleDown() {


        when(cloudformationFacade.stackExists("dev-crunch-ski-api")).thenReturn(false);
        when(cloudformationFacade.stackExists("dev-crunch-ski-auth")).thenReturn(false);
        when(cloudformationFacade.stackExists("dev-crunch-ski-websocket")).thenReturn(false);
        when(cloudformationFacade.stackExists("dev-crunch-ski-graphql","us-east-1")).thenReturn(false);
        when(cloudformationFacade.stackExists("dev-crunch-ski-cf-rockset")).thenReturn(false);
        when(cloudformationFacade.stackExists("dev-crunch-ski-cf-userpool-trg")).thenReturn(false);
        when(cloudformationFacade.stackExists("dev-crunch-ski-cf-bucket-notif")).thenReturn(false);
        when(cloudformationFacade.stackExists("dev-crunch-ski-data-var-stack")).thenReturn(true);

        Export export = new Export();
        export.setExportingStackId(options.getEnvironment() + "-" + options.getConfigMap().get("PROJECT_NAME") + "-data-var-stack");
        export.setName("UserPoolArn" + options.getEnvironment());
        export.setValue("blah:userpool/myUserPoolId");
        when(cloudformationFacade.getExportedOutputs()).thenReturn(Optional.of(List.of(export)));
        when(cognitoFacade.userPoolExists("myUserPoolId")).thenReturn(true);

        statusService = new StatusService(s3Facade, ssmParameterFacade, dynamoFacade,
                cloudformationFacade, cloudfrontFacade, cognitoFacade,  options);

        Map<String, ModuleStatus> statusMap = statusService.getStatus();
        assertEquals(ModuleStatus.DOWN, statusMap.get("APPLICATION"));

    }

    @Test
    @Disabled  // TODO > INVESTIGATE WHY THIS IS FAILING ON CI
    public void testCommandLine() {
        //Options options = new Options();
        HashMap<String, String> configMap = new HashMap<>();
        configMap.put("DATA_REGION", "ca-central-1");
        configMap.put("PROFILE_NAME", "default");
        configMap.put("PROJECT_NAME", "crunch-ski-test");
        configMap.put("DOMAIN_NAME", "test-domain.ca");
        configMap.put("SECONDARY_REGION", "us-east-1");
        //options.setConfigMap(configMap);
        //options.setEnvironment("dev");
        options.setConfigMap(configMap);
        App app = new App();
        Status status = new Status("dev", "all");

        status.setParent(app);
        status.setOptions(options);


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

    @Test
    @Disabled //used as entry point for debugging
    public void testStatus() {
        HashMap<String, String> config = new HashMap<>();
        config.put("DATA_REGION", "ca-central-1");
        config.put("PROJECT_NAME", "crunch-ski");
        config.put("PROFILE_NAME", "default");
        config.put("DOMAIN_NAME", "mccullough-solutions.ca");
        config.put("SECONDARY_REGION", "us-east-1");
        config.put("PROJECT_SOURCE_DIR", "/Users/aengus/code/ski-analytics-services");

        Options options = new Options();
        options.setEnvironment("staging");
        options.setModule("application");
        options.setConfigMap(config);
        StatusService service  = new StatusService(options);
        ModuleStatus status =  service.getModuleStatus("APPLICATION");

    }
}
