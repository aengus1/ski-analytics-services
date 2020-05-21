package crunch.ski.cli.services;

import crunch.ski.cli.Status;
import crunch.ski.cli.model.StatusOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class StatusTest {

    private Status status;

    @Mock
    private EnvironmentManagementService environmentManagementService;

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

        environmentManagementService = new EnvironmentManagementService(options);
        MockitoAnnotations.initMocks(StatusTest.class);
    }

    @Test
    public void testAllModules() {
     //TODO -> mock out facades in the service
    }

}
