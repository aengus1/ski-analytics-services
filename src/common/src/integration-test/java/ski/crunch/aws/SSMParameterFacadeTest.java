package ski.crunch.aws;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;



@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Disabled
public class SSMParameterFacadeTest {
    private static final Logger logger = Logger.getLogger(SSMParameterFacadeTest.class);

    @Test
    public void testGetParameter() {

        final String  AWS_PROFILE = "backend_dev";
        AWSCredentialsProvider credentialsProvider = new ProfileCredentialsProvider(AWS_PROFILE);
        SSMParameterFacade service = new SSMParameterFacade("ca-central-1",credentialsProvider);
        String response = service.getParameter("staging-weather-api-key");
        logger.info("parameter response: " + response);
        assert(response != null);
    }
}
