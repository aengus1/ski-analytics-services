package ski.crunch.activity.service;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;



@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SSMParameterServiceTest {
    private static final Logger logger = Logger.getLogger(SSMParameterServiceTest.class);

    @Test
    public void testGetParameter() {

        final String  AWS_PROFILE = "backend_dev";
        AWSCredentialsProvider credentialsProvider = new ProfileCredentialsProvider(AWS_PROFILE);
        SSMParameterService service = new SSMParameterService("ca-central-1",credentialsProvider);
        String response = service.getParameter("staging-weather-api-key");
        logger.info("parameter response: " + response);
        assert(response != null);
    }
}
