package ski.crunch.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import ski.crunch.aws.CredentialsProviderFactory;
import ski.crunch.aws.DynamoFacade;
import ski.crunch.testhelpers.TestPropertiesReader;
import ski.crunch.utils.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ActivityItemDeserializerTest {

    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    public static final SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:SS.sssZ");

    private String json;
    private String extraProperties;
    private String missingProperties;
    private String missingKeys;


    @BeforeAll
    public void setUp() throws IOException {
        InputStream is = ActivityItemDeserializerTest.class.getResourceAsStream("/activities.json");
        json = StreamUtils.convertStreamToString(is);

        InputStream iss = ActivityItemDeserializerTest.class.getResourceAsStream("/activityExtraProperties.json");
        extraProperties = StreamUtils.convertStreamToString(iss);

        InputStream isMissing = ActivityItemDeserializerTest.class.getResourceAsStream("/activityMissingProperties.json");
        missingProperties = StreamUtils.convertStreamToString(isMissing);

        InputStream isMissingKeys = ActivityItemDeserializerTest.class.getResourceAsStream("/activityMissingKeys.json");
        missingKeys = StreamUtils.convertStreamToString(isMissingKeys);
    }

    /**
     * "date" : "2020-04-10T20:45:01.566Z",
     * "rawFileType" : "fit",
     * "cognitoId" : "5ad104cd-6ed5-4b2b-bf34-77760e5344b6",
     * "maxHr" : 0,
     * "distance" : 5459,
     * "userAgent" : "Jersey/2.5.1 (HttpUrlConnection 11.0.5)",
     * "maxSpeed" : 15.120000000000001,
     * "avSpeed" : 13.061092757306227,
     * "userId" : "integration_test_user@crunch.ski",
     * "duration" : 2052,
     * "descent" : 222,
     * "lastUpdateTimestamp" : "2020-04-10T20:45:18.734Z",
     * "avHr" : -998,
     * "ascent" : 170,
     * "sourceIp" : "64.180.10.247",
     * "rawActivity" : "{\"s3\":{\"bucket\":\"ca-central-1\",\"key\":\"4feb05ae-e3de-44e6-8267-53080dfed3b7\",\"region\":null}}",
     * "processedActivity" : "{\"s3\":{\"bucket\":\"dev-activity-crunch-ski\",\"key\":\"5ad104cd-6ed5-4b2b-bf34-77760e5344b6/4feb05ae-e3de-44e6-8267-53080dfed3b7.pbf\",\"region\":null}}",
     * "id" : "4feb05ae-e3de-44e6-8267-53080dfed3b7",
     * "activitySubType" : "GENERIC_SUBSPORT",
     * "activityType" : "RUNNING",
     * "device" : "SUUNTO 20",
     * "status" : "COMPLETE"
     *
     * @throws IOException
     */
    @Test
    public void testDeserializeFullyPopulatedActivity() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<ActivityItem> activityItems = Arrays.asList(mapper.reader(newActivityDeserializerInjectables()).forType(ActivityItem[].class).readValue(json));
        ActivityItem item = activityItems.get(0);
        assertEquals("fit", item.getRawFileType());
        assertNotNull("cognitoId", item.getCognitoId());
        assertEquals("integration_test_user@crunch.ski", item.getUserId());
        String date = sdf.format(item.getDateOfUpload());
        System.out.println("date = " + date);
        assertTrue(date.startsWith("2020"));
        assertEquals(0, item.getMaxHr());
        assertEquals(5459, item.getDistance());
        assertTrue(item.getUserAgent().contains("Jersey/2.5.1 "));
        assertEquals(15.120000000000001, item.getMaxSpeed());
        assertEquals(13.061092757306227, item.getAvSpeed());
        assertEquals("integration_test_user@crunch.ski", item.getUserId());
        assertEquals(2052, item.getDuration());
        assertEquals("2020-04-10T20:45:18", sdf.format(item.getLastUpdateTimestamp()));
        assertEquals(-998, item.getAvHr());
        assertEquals(170, item.getAscent());
        assertEquals("dev-raw-activity-crunch-ski", item.getRawActivity().getBucketName());
        assertEquals("4feb05ae-e3de-44e6-8267-53080dfed3b7", item.getRawActivity().getKey());
        assertEquals("dev-activity-crunch-ski", item.getProcessedActivity().getBucketName());
        assertEquals("4feb05ae-e3de-44e6-8267-53080dfed3b7", item.getId());
        assertEquals("64.180.10.247", item.getSourceIp());
        assertEquals("GENERIC_SUBSPORT", item.getActivitySubType());
        assertEquals("RUNNING", item.getActivityType());
        assertEquals("SUUNTO 20", item.getDevice());
        assertEquals("COMPLETE", item.getStatus().name());
        assertEquals(5, item.getTags().size());
        assertEquals("tag1", item.getTags().iterator().next());
    }


    @Test
    public void testDeserializeWithExtraProperties() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<ActivityItem> activityItems = Arrays.asList(mapper.reader(newActivityDeserializerInjectables()).forType(ActivityItem[].class).readValue(extraProperties));
        for (ActivityItem item : activityItems) {
            assertEquals("fit", item.getRawFileType());
            assertNotNull("cognitoId", item.getCognitoId());
            assertEquals("integration_test_user@crunch.ski", item.getUserId());
            String date = sdf.format(item.getDateOfUpload());
            System.out.println("date = " + date);
            assertTrue(date.startsWith("2020"));
            assertEquals("64.180.10.247", item.getSourceIp());
            assertEquals("RUNNING", item.getActivityType());
            assertEquals("SUUNTO 20", item.getDevice());
        }
    }

    @Test
    public void testDeserializeWithMissingProperties() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<ActivityItem> activityItems = Arrays.asList(mapper.reader(newActivityDeserializerInjectables()).forType(ActivityItem[].class).readValue(missingProperties));
        for (ActivityItem item : activityItems) {
            assertEquals("fit", item.getRawFileType());
            assertNotNull("cognitoId", item.getCognitoId());
            assertEquals("integration_test_user@crunch.ski", item.getUserId());
            assertEquals("64.180.10.247", item.getSourceIp());
            assertEquals("RUNNING", item.getActivityType());
            assertEquals("SUUNTO 20", item.getDevice());
        }
    }


    @Test
    public void testDeserializeWithMissingKeys() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        Exception exception = assertThrows(IOException.class, () -> {
            Arrays.asList(mapper.reader(newActivityDeserializerInjectables()).forType(ActivityItem[].class).readValue(missingKeys));
        });

        String expected = "Missing key";
        String actual = exception.getMessage();
        assertTrue(actual.contains(expected));
    }


    @Test
    public void testDeserializeWithMissingInjectables() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Exception ex = assertThrows(InvalidDefinitionException.class, () -> {
            Arrays.asList(mapper.readValue(json, ActivityItem.class));
        });
        System.out.println(ex.getMessage());

        String expected = "No 'injectableValues' configured";
        assertTrue(ex.getMessage().contains(expected));


    }

    private InjectableValues newActivityDeserializerInjectables() throws IOException {
        String region = TestPropertiesReader.get("region");
        String userTable = TestPropertiesReader.get("userTable");
        String activityBucket = TestPropertiesReader.get("activityBucket");
        String rawActivityBucket = TestPropertiesReader.get("rawActivityBucket");
        DynamoFacade dynamoFacade = new DynamoFacade(region, userTable, CredentialsProviderFactory.getDefaultCredentialsProvider());
        return new InjectableValues.Std()
                .addValue("mapper", dynamoFacade.getMapper())
                .addValue("region", region)
                .addValue("proc_bucket", activityBucket)
                .addValue("raw_bucket", rawActivityBucket);
    }
}
