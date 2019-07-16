package ski.crunch.activity.service;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.assertTrue;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DynamoDBServiceTest {

    //TODO -> create a test base class with access to credentialsn
    public static final String AWS_PROFILE="backend_dev";
    public static final String TABLE_REGION = "ca-central-1"; // this is fragile
    private static final Logger LOG = Logger.getLogger(DynamoDBServiceTest.class);
    private DynamoDBService service = null;

    @BeforeEach()
    public void setup() {
        AWSCredentialsProvider credentialsProvider = new ProfileCredentialsProvider(AWS_PROFILE);
        this.service = new DynamoDBService(TABLE_REGION, "staging-crunch-Activity",credentialsProvider);
    }


    @Test()
    public void testInsert() {
        assertTrue(true);
    }
}
