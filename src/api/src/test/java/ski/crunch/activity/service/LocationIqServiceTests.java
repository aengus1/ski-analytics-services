package ski.crunch.activity.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.HttpGet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.mockito.Mockito;
import ski.crunch.activity.model.ActivityOuterClass;
import ski.crunch.utils.HttpClientUtil;

import java.io.IOException;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LocationIqServiceTests {

    @Mock
    private HttpClientUtil clientUtil;

    private HttpGet httpGet;

    private LocationIqService service = null;

    private double lat = 53.50111, lon = -113.55511;
    private String apiKey = "abc123";
    private JsonNode response = null;
    private JsonNode response2 = null;

    private String jsonResponse = "{\"place_id\":\"331619225589\",\"osm_type\":\"way\",\"osm_id\":\"388949005\",\"" +
            "licence\":\"https:\\/\\/locationiq.com\\/attribution\",\"lat\":\"49.863053\",\"lon\":\"-119.714911\",\"" +
            "display_name\":\"Chalet, Central Okanagan, British Columbia, Canada\",\"boundingbox\":[\"49.8629696\",\"" +
            "49.8631364\",\"-119.7150765\",\"-119.7147442\"],\"importance\":0.175,\"address\":{\"name\":\"Chalet\",\"" +
            "county\":\"Central Okanagan\",\"state\":\"British Columbia\",\"country\":\"Canada\",\"country_code\":\"ca\"}}";

    private String jsonResponse2 = "{\"place_id\":\"331608132585\",\"licence\":\"https:\\/\\/locationiq.com\\/attribution\"" +
            ",\"lat\":\"49.860432\",\"lon\":\"-112.711782\",\"display_name\":\"9, Range Road 210, Lethbridge, Alberta, Canada\"" +
            ",\"boundingbox\":[\"49.860432\",\"49.860432\",\"-112.711782\",\"-112.711782\"],\"importance\":0.15,\"address\":" +
            "{\"house_number\":\"9\",\"road\":\"Range Road 210\",\"county\":\"Lethbridge\",\"state\":\"Alberta\",\"country\"" +
            ":\"Canada\",\"country_code\":\"ca\"}}";


    @BeforeEach
    public void init() {
        httpGet = new HttpGet();
        clientUtil = Mockito.mock(HttpClientUtil.class);
        // MockitoAnnotations.initMocks(this);
        service = new LocationIqService(apiKey, clientUtil, httpGet);
        ObjectMapper mapper = new ObjectMapper();
        try {
            response = mapper.readTree(jsonResponse);
            response2 = mapper.readTree(jsonResponse2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Test()
    public void testHttpCallIsMade() {


        httpGet.setURI(URI.create(LocationIqService.LOCATION_IQ_URL + "key=" + apiKey + "&lat="
                + lat + "&lon=" + lon + "&format=json"));

        try {
            JsonNode result = service.queryLocationApi(lat, lon);
            verify(clientUtil,times(1)).getJsonNode(httpGet);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @Test()
    public void testParse() {

        httpGet.setURI(URI.create(LocationIqService.LOCATION_IQ_URL + "key=" + apiKey + "&lat="
                + lat + "&lon=" + lon + "&format=json"));

        try {
            when(clientUtil.getJsonNode(httpGet)).thenReturn(response2);
            JsonNode result = service.queryLocationApi(lat, lon);
            verify(clientUtil,times(1)).getJsonNode(httpGet);

            ActivityOuterClass.Activity.Location location = service.parseJsonResult(result);

            assertEquals("9 Range Road 210", location.getAddress1());


        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Test()
    public void testParse2() {

        httpGet.setURI(URI.create(LocationIqService.LOCATION_IQ_URL + "key=" + apiKey + "&lat="
                + lat + "&lon=" + lon + "&format=json"));

        try {
            when(clientUtil.getJsonNode(httpGet)).thenReturn(response);
            JsonNode result = service.queryLocationApi(lat, lon);
            verify(clientUtil,times(1)).getJsonNode(httpGet);

            ActivityOuterClass.Activity.Location location = service.parseJsonResult(result);

            assertEquals("Chalet, Central Okanagan, British Columbia, Canada", location.getDisplayName());
            assertEquals("Chalet", location.getName());
            assertEquals("Canada", location.getCountry());
            assertEquals("Central Okanagan", location.getCounty());
            assertEquals("British Columbia", location.getProv());


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
