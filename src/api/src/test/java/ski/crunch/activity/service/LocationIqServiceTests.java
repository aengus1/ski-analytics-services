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

    private String jsonResponse = "{\"place_id\":\"331619225589\",\"osm_type\":\"way\",\"osm_id\":\"388949005\",\"" +
            "licence\":\"https:\\/\\/locationiq.com\\/attribution\",\"lat\":\"49.863053\",\"lon\":\"-119.714911\",\"" +
            "display_name\":\"Chalet, Central Okanagan, British Columbia, Canada\",\"boundingbox\":[\"49.8629696\",\"" +
            "49.8631364\",\"-119.7150765\",\"-119.7147442\"],\"importance\":0.175,\"address\":{\"name\":\"Chalet\",\"" +
            "county\":\"Central Okanagan\",\"state\":\"British Columbia\",\"country\":\"Canada\",\"country_code\":\"ca\"}}";

    @BeforeEach
    public void init() {
        httpGet = new HttpGet();
        clientUtil = Mockito.mock(HttpClientUtil.class);
        // MockitoAnnotations.initMocks(this);
        service = new LocationIqService(apiKey, clientUtil, httpGet);
        ObjectMapper mapper = new ObjectMapper();
        try {
            response = mapper.readTree(jsonResponse);
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
            when(clientUtil.getJsonNode(httpGet)).thenReturn(response);
            JsonNode result = service.queryLocationApi(lat, lon);
            verify(clientUtil,times(1)).getJsonNode(httpGet);

            ActivityOuterClass.Activity.Location location = service.parseJsonResult(result);

            assertEquals("Chalet", location.getAddress1());


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
