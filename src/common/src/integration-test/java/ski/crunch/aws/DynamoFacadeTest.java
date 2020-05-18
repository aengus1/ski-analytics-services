package ski.crunch.aws;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.assertTrue;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DynamoFacadeTest {

    //TODO -> create a test base class with access to credentialsn
    public static final String AWS_PROFILE="backend_dev";
    public static final String TABLE_REGION = "ca-central-1"; // this is fragile
    private static final Logger logger = LoggerFactory.getLogger(DynamoFacadeTest.class);
    private DynamoFacade service = null;

    @BeforeEach()
    public void setup() {
        AWSCredentialsProvider credentialsProvider = new ProfileCredentialsProvider(AWS_PROFILE);
        this.service = new DynamoFacade(TABLE_REGION, "staging-crunch-Activity",credentialsProvider);
    }


    @Test()
    public void testInsert() {
        assertTrue(true);
    }
}
