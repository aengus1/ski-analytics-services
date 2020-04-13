package ski.crunch.testhelpers;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class IntegrationTestPropertiesReader {

    public static String get(String variableName) throws IOException {
        InputStream is = IntegrationTestPropertiesReader.class.getResourceAsStream("/integration-test.properties");

        Properties properties = new Properties();
        properties.load(is);

        return properties.containsKey(variableName) ? properties.get(variableName).toString() : "";
    }
}
