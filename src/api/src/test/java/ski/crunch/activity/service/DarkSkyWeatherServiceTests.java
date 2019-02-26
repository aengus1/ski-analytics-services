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
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DarkSkyWeatherServiceTests {


    @Mock
    private HttpClientUtil clientUtil;

    private HttpGet httpGet;

    private DarkSkyWeatherService service = null;

    private double lat = 53.50111, lon = -113.55511;
    private String ts = "2018-01-01T12:02:02";
    private String apiKey = "abc123";
    private JsonNode response = null;

    private String jsonResponse = "{\"latitude\":37.8267,\"longitude\":-122.4233,\"timezone\":\"America/Los_Angeles\"," +
            "\"currently\":{\"time\":1483301522,\"summary\":\"Partly Cloudy\",\"icon\":\"partly-cloudy-day\"," +
            "\"precipIntensity\":0,\"precipProbability\":0,\"temperature\":51.4,\"apparentTemperature\":51.4," +
            "\"dewPoint\":37.88,\"humidity\":0.6,\"pressure\":1014,\"windSpeed\":4.02,\"windGust\":10.27,\"windBearing\":279," +
            "\"cloudCover\":0.28,\"uvIndex\":2,\"visibility\":10},\"daily\":{\"data\":[{\"time\":1483257600," +
            "\"summary\":\"Mostly cloudy throughout the day.\",\"icon\":\"partly-cloudy-day\",\"sunriseTime\":1483284394," +
            "\"sunsetTime\":1483318969,\"moonPhase\":0.11,\"precipIntensity\":0.0002,\"precipIntensityMax\":0.0024," +
            "\"precipIntensityMaxTime\":1483333200,\"precipProbability\":0.16,\"precipType\":\"rain\",\"temperatureHigh\"" +
            ":53.42,\"temperatureHighTime\":1483311600,\"temperatureLow\":43.06,\"temperatureLowTime\":1483369200,\"appa" +
            "rentTemperatureHigh\":53.42,\"apparentTemperatureHighTime\":1483311600,\"apparentTemperatureLow\":43.06,\"a" +
            "pparentTemperatureLowTime\":1483369200,\"dewPoint\":39.19,\"humidity\":0.71,\"pressure\":1013.87,\"windSpee" +
            "d\":4.06,\"windGust\":13.09,\"windGustTime\":1483315200,\"windBearing\":272,\"cloudCover\":0.68,\"uvIndex\"" +
            ":2,\"uvIndexTime\":1483297200,\"visibility\":9.99,\"temperatureMin\":45.99,\"temperatureMinTime\":148334040" +
            "0,\"temperatureMax\":53.42,\"temperatureMaxTime\":1483311600,\"apparentTemperatureMin\":45.1,\"apparentTempe" +
            "ratureMinTime\":1483326000,\"apparentTemperatureMax\":53.42,\"apparentTemperatureMaxTime\":1483311600}]},\"" +
            "flags\":{\"sources\":[\"cmc\",\"gfs\",\"hrrr\",\"icon\",\"isd\",\"madis\",\"nam\",\"sref\"],\"nearest-stati" +
            "on\":2.582,\"units\":\"us\"},\"offset\":-8}";


    @BeforeEach
    public void init() {
        httpGet = new HttpGet();
        clientUtil = Mockito.mock(HttpClientUtil.class);
        // MockitoAnnotations.initMocks(this);
        service = new DarkSkyWeatherService(apiKey, clientUtil, httpGet);
        ObjectMapper mapper = new ObjectMapper();
        try {
            response = mapper.readTree(jsonResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Test()
    public void testHttpCallIsMade() {


        httpGet.setURI(URI.create(DarkSkyWeatherService.DARK_SKY_API_URL + "/" + apiKey + "/"
                + lat + "," + lon + "," + ts));

        try {
            JsonNode result = service.queryWeatherAPI(lat, lon, ts);
            verify(clientUtil,times(1)).getJsonNode(httpGet);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @Test()
    public void testParse() {

        httpGet.setURI(URI.create(DarkSkyWeatherService.DARK_SKY_API_URL + "/" + apiKey + "/"
                + lat + "," + lon + "," + ts));

        try {
            when(clientUtil.getJsonNode(httpGet)).thenReturn(response);
            JsonNode result = service.queryWeatherAPI(lat, lon, ts);
            verify(clientUtil,times(1)).getJsonNode(httpGet);

            ActivityOuterClass.Activity.Weather weather = service.parseJsonResult(result);

            assertEquals(51.5, weather.getApparentTemperature());
            assertEquals("Partly Cloudy", weather.getSummary());
            assertEquals(0, weather.getPrecipIntensity());
            assertEquals(51.4, weather.getTemperature());
            assertEquals(37.88,weather.getDewPoint());
            assertEquals(0.6,weather.getHumidity());
            assertEquals(1014, weather.getPressure());
            assertEquals(4.02, weather.getWindSpeed());
            assertEquals(0.28, weather.getCloudCover());
            assertEquals(10, weather.getVisibility());
            assertEquals(ActivityOuterClass.Activity.PrecipType.NA_PRECIP, weather.getPrecipType());
            assertEquals(ActivityOuterClass.Activity.WeatherIcon.NA_ICON, weather.getIcon());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
