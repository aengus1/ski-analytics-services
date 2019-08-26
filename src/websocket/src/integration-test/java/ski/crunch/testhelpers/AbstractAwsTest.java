package ski.crunch.testhelpers;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractAwsTest {

    private static final String AWS_PROFILE="backend_dev";
    private static final String REGION = "ca-central-1"; // this is fragile
    private static final Logger LOG = Logger.getLogger(AbstractAwsTest.class);
    protected AWSCredentialsProvider credentialsProvider;

    @BeforeEach()
    public void setup() {
        this.credentialsProvider = new ProfileCredentialsProvider(AWS_PROFILE);
    }


}
