package ski.crunch.aws;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SSMParameterFacadeTest {
    private static final Logger logger = LoggerFactory.getLogger(SSMParameterFacadeTest.class);

    @Test
    @Disabled
    public void testGetParameter() {

        final String  AWS_PROFILE = "backend_dev";
        AWSCredentialsProvider credentialsProvider = new ProfileCredentialsProvider(AWS_PROFILE);
        SSMParameterFacade service = new SSMParameterFacade("ca-central-1",credentialsProvider);
        String response = service.getParameter("staging-weather-api-key");
        logger.info("parameter response: " + response);
        assert(response != null);
    }
}
