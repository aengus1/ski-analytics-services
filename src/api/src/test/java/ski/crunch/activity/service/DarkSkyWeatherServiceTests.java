package ski.crunch.activity.service;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DarkSkyWeatherServiceTests {


    @Mock
    private CloseableHttpClient defaultHttpClient;

    @Mock
    private HttpGet httpGet;

    @Mock
    SSMParameterService ssmParameterService;

    @Mock
    CloseableHttpResponse response;


    public static DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    public DarkSkyWeatherService service = null;

    @BeforeAll
    public void init() {
//         System.setProperty("currentStage", "staging");
        MockitoAnnotations.initMocks(this);
    }



    @Test()
    public void testParameterRetrievalCall() {
        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
        verify(ssmParameterService.getParameter(argument.capture()));
        this.service = new DarkSkyWeatherService(ssmParameterService, defaultHttpClient, httpGet);
        System.out.println("ARG VALUE = " + argument.getValue());
        assertEquals("staging-weather-api-key", argument.getValue());
//        .thenReturn("abc123");

//        verify(ssmParameterService, Mockito.atLeast(1)).getParameter("prod-weather-api-key");
    }

    @Test()
    public void testHttpCall() {

        //build the url
        String ts = FORMATTER.format(LocalDateTime.now());
        double lat = 53.5;
        double lon = -113.5;
        String apiKey = "abc123";
        StringBuilder sb = new StringBuilder();
        String uri = sb.append(DarkSkyWeatherService.DARK_SKY_API_URL).append("/")
                .append(apiKey).append("/")
                .append(lat).append(",")
                .append(lon).append(",")
                .append(ts).toString();

        httpGet.setURI(URI.create(uri));


        try {
            when(ssmParameterService.getParameter("staging-weather-api-key")).thenReturn("abc123");
            when(defaultHttpClient.execute(httpGet)).thenReturn(response);
            this.service = new DarkSkyWeatherService(ssmParameterService, defaultHttpClient, httpGet);
            service.queryWeatherAPI(lat, lon, ts);

            verify(defaultHttpClient).execute(httpGet);
//            Mockito.verify(defaultHttpClient, Mockito.atMost(1)).execute(httpGet);


        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    private String jsonSuccess() {
        return "{success}";
    }
}
