package crunch.ski.cli.services;

import crunch.ski.cli.model.Options;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class SettingsServiceTest {

    private Options options;

    private SettingsService settingsService;



    @BeforeEach
    public void setup() {

        // set options
        options = new Options();
        HashMap<String, String> config = new HashMap<>();
        config.put("DATA_REGION", "ca-central-1");
        config.put("PROJECT_NAME", "crunch-ski");
        config.put("PROFILE_NAME", "default");
        config.put("PROJECT_SOURCE_DIR", "/Users/aengus/code/ski-analytics-services");
        options.setConfigMap(config);
        options.setEnvironment("dev");
        options.setVerbose(false);

        settingsService = new SettingsService(options);
    }

    @Disabled("entry point for debugging")
    @Test
    public void testSettingsService() throws IOException{

        Properties properties = settingsService.getSettings();
        assertNotNull(properties);


    }



}
