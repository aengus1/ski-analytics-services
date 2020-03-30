package ski.crunch.activity.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ski.crunch.model.ActivityOuterClass;
import ski.crunch.utils.HttpClientUtil;

import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.Map;

public class LocationIqService implements LocationService {
    private static final Logger logger = LoggerFactory.getLogger(LocationIqService.class);
    public static final String LOCATION_IQ_URL = "https://us1.locationiq.com/v1/reverse.php?";
    private HttpClientUtil httpClient;
    private HttpGet httpGet;
    private String apiKey = null;

    public LocationIqService(String apiKey) {
        this.apiKey = apiKey;
        CloseableHttpClient client = HttpClients.createDefault();
        this.httpClient = new HttpClientUtil(client);
    }


    /**
     * test entry point constructor
     *
     * @param apiKey
     * @param httpClient
     * @param httpGet
     */
    public LocationIqService(String apiKey, HttpClientUtil httpClient, HttpGet httpGet) {
        this.apiKey = apiKey;
        this.httpClient = httpClient;
        this.httpGet = httpGet;
    }

    @Override
    public ActivityOuterClass.Activity.Location getLocation(double lat, double lon) {
        ActivityOuterClass.Activity.Location location = null;
        try {
            JsonNode result = queryLocationApi(lat, lon);
            location = parseJsonResult(result);
        } catch (IOException ex) {
            logger.error("Error retrieving location data", ex);
        }


        return location;
    }


    public ActivityOuterClass.Activity.Location parseJsonResult(JsonNode json) {
        ActivityOuterClass.Activity.Location.Builder locationBuilder = ActivityOuterClass.Activity.Location.newBuilder();
        locationBuilder.setSource(ActivityOuterClass.Activity.LocationSource.LOCATION_IQ);
        Iterator it = json.fields();
        while (it.hasNext()) {
            Map.Entry next = ((Map.Entry) it.next());
//            System.out.println(next.getKey() + " " + next.getValue());
            String key = next.getKey().toString();
            switch(key) {
                case "display_name": {
                    TextNode value = (TextNode) next.getValue();
                    locationBuilder.setDisplayName(value.textValue());
                    break;
                }
                case "address": {
                    JsonNode value = (JsonNode) next.getValue();
                    Iterator itAddress = value.fields();
                    while(itAddress.hasNext()) {
                        Map.Entry keyNext = ((Map.Entry) itAddress.next());
                        String keyAddress = keyNext.getKey().toString();
                        System.out.println(keyAddress);
                        TextNode keyValue = (TextNode) keyNext.getValue();
                        switch (keyAddress) {
                            case "name" : {
                                locationBuilder.setName(keyValue.textValue());
                                break;
                            }
                            case "house_number": {
                                locationBuilder.setAddress1(keyValue.textValue());
                                break;
                            }
                            case "road": {
                                locationBuilder.setAddress1(locationBuilder.getAddress1() + " " + keyValue.textValue());
                                break;
                            }
                            case "county": {
                                locationBuilder.setCounty(keyValue.textValue());
                                break;
                            }
                            case "state": {
                                locationBuilder.setProv(keyValue.textValue());
                                break;
                            }
                            case "country": {
                                locationBuilder.setCountry(keyValue.textValue());
                                break;
                            }
                        }
                    }
                }

            }

        }

        return locationBuilder.build();
    }

    public JsonNode queryLocationApi(double lat, double lon) throws IOException {


        String latStr = String.format("%.5f", lat);
        String lonStr = String.format("%.5f", lon);
        String url = String.format("%skey=%s&lat=%s&lon=%s&format=json", LOCATION_IQ_URL, apiKey, latStr, lonStr);
        System.out.println("url = " + url);

        if (httpGet == null) {
            this.httpGet = new HttpGet();
        }
        httpGet.setURI(URI.create(url));

        return httpClient.getJsonNode(httpGet);

    }


}




