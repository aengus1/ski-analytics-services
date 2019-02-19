package ski.crunch.activity.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import ski.crunch.activity.model.ActivityOuterClass;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;

//todo -> add logger

public class DarkSkyWeatherService implements WeatherService {



    public static final String DARK_SKY_API_URL = "https://api.darksky.net/forecast";
    public static final String DARK_SKY_API_KEY_NAME = "weather-api-key";
    private CloseableHttpClient httpClient;
    private HttpGet httpGet;
    private SSMParameterService ssmParameterService;
    private String API_KEY = null;
    String stage = null;

    public DarkSkyWeatherService(SSMParameterService parameterService, CloseableHttpClient httpClient, HttpGet httpGet) {
        this(parameterService);
        this.httpClient = httpClient;
        this.httpGet = httpGet;
    }

    public DarkSkyWeatherService(SSMParameterService parameterService) {
        this.httpClient = HttpClients.createDefault();
        this.httpGet = new HttpGet();
        this.ssmParameterService = parameterService;
        this.stage = System.getenv("currentStage");
        this.API_KEY = ssmParameterService.getParameter(this.stage+"-"+DARK_SKY_API_KEY_NAME);

    }

    @Override
    public ActivityOuterClass.Activity.Weather getWeather(double lat, double lon, String ts) {

        JsonNode result = queryWeatherAPI(lat, lon, ts);
        System.out.println(result.asText());


        return null;
    }

    public JsonNode queryWeatherAPI(double lat, double lon, String ts) {

        JsonNode result = null;


        String url = String.format("%s/%s/%f,%f,%s", DARK_SKY_API_URL, API_KEY, lat, lon, ts);
        System.out.println("url = " + url);

        this.httpGet.setURI(URI.create(url));

        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            HttpEntity entity = response.getEntity();


            ObjectMapper objectMapper = new ObjectMapper();
            result = objectMapper.readTree(entity.getContent());
            EntityUtils.consume(entity);

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return result;
    }
}