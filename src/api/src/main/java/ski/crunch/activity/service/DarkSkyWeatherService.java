package ski.crunch.activity.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.IntNode;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import ski.crunch.activity.model.ActivityOuterClass;
import ski.crunch.utils.HttpClientUtil;

import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

//todo -> add logger

public class DarkSkyWeatherService implements WeatherService {


    private static final Logger logger = Logger.getLogger(DarkSkyWeatherService.class);
    public static final String DARK_SKY_API_URL = "https://api.darksky.net/forecast";
    private HttpClientUtil httpClient;
    private HttpGet httpGet;
    private String apiKey = null;

    public DarkSkyWeatherService(String apiKey) {
        this.apiKey = apiKey;
        CloseableHttpClient client = HttpClients.createDefault();
        this.httpClient = new HttpClientUtil(client);
    }

    public DarkSkyWeatherService(String apiKey, HttpClientUtil httpClient, HttpGet httpGet ) {
        this.apiKey = apiKey;
        this.httpClient = httpClient;
        this.httpGet = httpGet;
    }


    @Override
    public ActivityOuterClass.Activity.Weather getWeather(double lat, double lon, String ts) {

        try {
            JsonNode result = queryWeatherAPI(lat, lon, ts);
            ActivityOuterClass.Activity.Weather weather = parseJsonResult(result);
        } catch (IOException ex) {
            logger.error("Error retrieving weather data", ex);
        }


        return null;
    }

    public ActivityOuterClass.Activity.Weather parseJsonResult(JsonNode json) {
        ActivityOuterClass.Activity.Weather.Builder weatherBuilder = ActivityOuterClass.Activity.Weather.newBuilder();
        Iterator it = json.fields();
        while(it.hasNext()) {
            Map.Entry next = ((Map.Entry) it.next());
            System.out.println(next.getKey() + " " + next.getValue());
            if(next.getKey().equals("currently")) {
                JsonNode currently = (JsonNode) next.getValue();
                Iterator<Map.Entry<String,JsonNode>> cit = currently.fields();
                while(cit.hasNext()) {
                    Map.Entry<String,JsonNode> entry = cit.next();
                    switch(entry.getKey()) {
                        case "summary": {
                            weatherBuilder.setSummary(entry.getValue().asText());
                            break;
                        }
                        case "icon": {
                            weatherBuilder.setIcon(iconMapper(entry.getValue().asText()));
                            break;
                        }
                        case "precipIntensity": {
                            weatherBuilder.setPrecipIntensity(entry.getValue().asDouble());
                            break;
                        }
                        case "temperature": {
                            weatherBuilder.setTemperature(entry.getValue().asDouble());
                            break;
                        }
                        case "apparentTemperature": {
                            weatherBuilder.setApparentTemperature(entry.getValue().asDouble());
                            break;
                        }
                        case "dewPoint": {
                            weatherBuilder.setDewPoint(entry.getValue().asDouble());
                            break;
                        }
                        case "humidity": {
                            weatherBuilder.setHumidity(entry.getValue().floatValue());
                            break;
                        }
                        case "windSpeed": {
                            weatherBuilder.setWindSpeed(entry.getValue().floatValue());
                            break;
                        }
                        case "windBearing": {
                            weatherBuilder.setWindDirection(entry.getValue().asInt());
                            break;
                        }
                        case "cloudCover": {
                            weatherBuilder.setCloudCover(entry.getValue().floatValue());
                            break;
                        }
                        case "visibility": {
                            weatherBuilder.setVisibility(entry.getValue().floatValue());
                            break;
                        }
                        case "precipType": {
                            weatherBuilder.setPrecipType(precipMapper(entry.getValue().asText()));
                            break;
                        }
                    }
                }
            }
        }
        return weatherBuilder.build();
    }

    public JsonNode queryWeatherAPI(double lat, double lon, String ts) throws IOException {


        String latStr = String.format("%.5f",lat);
        String lonStr = String.format("%.5f", lon);
        String url = String.format("%s/%s/%s,%s,%s", DARK_SKY_API_URL, apiKey, latStr, lonStr, ts);
        System.out.println("url = " + url);

        if(httpGet == null) {
            this.httpGet = new HttpGet();
        }
        httpGet.setURI(URI.create(url));

        return httpClient.getJsonNode(httpGet);

    }

    private ActivityOuterClass.Activity.WeatherIcon iconMapper(String value) {
        switch(value){
            case "clear-day": {
                return ActivityOuterClass.Activity.WeatherIcon.CLEAR_DAY;
            }
            case "clear-night": {
                return ActivityOuterClass.Activity.WeatherIcon.CLEAR_NIGHT;
            }
            case "rain": {
                return ActivityOuterClass.Activity.WeatherIcon.RAIN_ICON;
            }
            case "snow": {
                return ActivityOuterClass.Activity.WeatherIcon.SNOW_ICON;
            }
            case "sleet": {
                return ActivityOuterClass.Activity.WeatherIcon.SLEET_ICON;
            }
            case "wind": {
                return ActivityOuterClass.Activity.WeatherIcon.WIND;
            }
            case "fog": {
                return ActivityOuterClass.Activity.WeatherIcon.FOG;
            }
            case "cloudy": {
                return ActivityOuterClass.Activity.WeatherIcon.CLOUDY;
            }
            case "partly-cloudy-day": {
                return ActivityOuterClass.Activity.WeatherIcon.PARTLY_CLOUDY_DAY;
            }
            case "partly-cloudy-night": {
                return ActivityOuterClass.Activity.WeatherIcon.PARTLY_CLOUDY_NIGHT;
            }
        }
        return ActivityOuterClass.Activity.WeatherIcon.NA_ICON;
    }

    private ActivityOuterClass.Activity.PrecipType precipMapper(String value) {
        switch(value){
            case "rain": {
                return ActivityOuterClass.Activity.PrecipType.RAIN;
            }
            case "snow": {
                return ActivityOuterClass.Activity.PrecipType.SNOW;
            }
            case "sleet": {
                return ActivityOuterClass.Activity.PrecipType.SLEET;
            }
        }
        return ActivityOuterClass.Activity.PrecipType.NA_PRECIP;
    }
}