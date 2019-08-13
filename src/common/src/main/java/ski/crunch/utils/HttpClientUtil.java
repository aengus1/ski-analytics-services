package ski.crunch.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class HttpClientUtil {

    private CloseableHttpClient httpClient = null;

    public HttpClientUtil(CloseableHttpClient httpClient){
        this.httpClient = httpClient;
    }

   public  JsonNode getJsonNode(HttpGet httpGet) throws IOException {
        JsonNode result = null;
        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            HttpEntity entity = response.getEntity();


            ObjectMapper objectMapper = new ObjectMapper();
            result = objectMapper.readTree(entity.getContent());
            EntityUtils.consume(entity);

        }
        return result;
    }
}
