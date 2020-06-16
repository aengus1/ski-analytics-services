package ski.crunch.activity.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ski.crunch.aws.CredentialsProviderFactory;
import ski.crunch.aws.DynamoFacade;
import ski.crunch.aws.S3Facade;
import ski.crunch.dao.ActivityDAO;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ActivityServiceTest {


    public ActivityService activityService;

    @Mock
    S3Facade s3Facade;

    @Mock
    ActivityDAO activityDAO;

    @Mock
    DynamoFacade dynamoFacade;

    private final String ACTIVITY_BUCKET = "dev-raw-activity-crunch-ski";
    private final String RAW_ACTIVITY_BUCKET = "dev-activity-crunch-ski";
    private final String key = "5aefa123sdf/2134easdfjds.fit";

    @BeforeEach
    public void setUp() throws IOException {

        MockitoAnnotations.initMocks(this);

//        doNothing().when(s3Facade.saveObjectToTmpDir(RAW_ACTIVITY_BUCKET, key));
        activityService = new ActivityService(s3Facade, CredentialsProviderFactory.getDefaultCredentialsProvider(),
                dynamoFacade, "ca-central-1", RAW_ACTIVITY_BUCKET,
                ACTIVITY_BUCKET, "dev-Activity", "dev-User");

    }

    @Test
    public void testExtractKeyAndBucketFromInput() {
        Map<String, Object> input = new HashMap<>();
    }

}
