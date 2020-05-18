package ski.crunch.testhelpers;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.LoggerFactory;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractAwsTest {

    protected static final String AWS_PROFILE="backend_dev";
    public static final String REGION = "ca-central-1"; // this is fragile
    protected static final org.slf4j.Logger logger = LoggerFactory.getLogger(AbstractAwsTest.class);
    protected AWSCredentialsProvider credentialsProvider;

    @BeforeEach()
    public void setup() {
        this.credentialsProvider = new ProfileCredentialsProvider(AWS_PROFILE);
    }


}
