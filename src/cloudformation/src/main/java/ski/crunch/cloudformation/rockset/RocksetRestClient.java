package ski.crunch.cloudformation.rockset;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.annotations.VisibleForTesting;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import ski.crunch.utils.StreamUtils;

import java.io.IOException;
import java.net.URI;

public class RocksetRestClient {

    private static final Logger logger = Logger.getLogger(RocksetRestClient.class);
    private static final String INTEGRATION_ENDPOINT = "/v1/orgs/self/integrations";
    private static final String COLLECTION_ENDPOINT = "/v1/orgs/self/ws/commons/collections";

    private String apiServer;
    private String apiKey;
    private CloseableHttpClient client;
    private ObjectMapper objectMapper;


    /**
     * @param apiServer String api server url
     * @param apiKey String api key
     */
    public RocksetRestClient(String apiServer, String apiKey) {
        this.apiServer = apiServer;
        this.apiKey = apiKey;
        this.client = HttpClients.createDefault();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * test constructor
     *
     * @param apiServer
     * @param apiKey
     * @param client
     */
    public RocksetRestClient(String apiServer, String apiKey, CloseableHttpClient client) {
        this(apiServer, apiKey);
        this.client = client;
    }

    /**
     * Constructs and posts rockset create integration request
     *
     * @param roleName        String the name of the AWS role that has been configured to use with Rockset
     * @param integrationName String the name of the AWS role that has been configured to use with Rockset     *
     * @param description     String description of integration
     * @param integrationType RocksetIntegrationType the AWS service to use as a data source
     * @return String response integrationResponse
     * @throws JsonProcessingException on error building JSON
     * @throws IOException             on error communicating with rockset
     */
    public String createIntegrationViaRest(String roleName, String integrationName, String description, RocksetIntegrationType integrationType) throws JsonProcessingException, IOException {

        JsonNode payload = constructCreateIntegrationRequest(roleName, integrationName, description, integrationType);
        String payloadStr = objectMapper.writer().writeValueAsString(payload);
        HttpEntity entity = EntityBuilder.create().setText(payloadStr).build();
        logger.debug("Create integration request: " + payloadStr);
        HttpPost httpPost = new HttpPost();
        httpPost.setURI(URI.create("https://" + apiServer + INTEGRATION_ENDPOINT));
        httpPost.setHeader("Authorization", "ApiKey " + apiKey);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setEntity(entity);
        CloseableHttpResponse response = client.execute(httpPost);
        String responseString = StreamUtils.convertStreamToString(response.getEntity().getContent());
        logger.debug("Create integration response" + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(responseString));

        return responseString;
    }


    /**
     * Constructs and posts rockset delete integration request
     *
     * @param integrationName String name of integration
     * @return String response
     * @throws IOException
     */
    public String deleteIntegrationViaRest(String integrationName) throws IOException {
        logger.debug("delete called for: " + integrationName);
        HttpDelete httpDelete = new HttpDelete();
        httpDelete.setURI(URI.create("https://" + apiServer + INTEGRATION_ENDPOINT + "/" + integrationName));
        httpDelete.setHeader("Authorization", "ApiKey " + apiKey);

        CloseableHttpResponse response = client.execute(httpDelete);
        String responseString = StreamUtils.convertStreamToString(response.getEntity().getContent());
        logger.debug("Delete integration response" + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(responseString));

        return responseString;
    }


    @VisibleForTesting
    JsonNode constructCreateIntegrationRequest(String roleName, String integrationName, String description, RocksetIntegrationType integrationType) throws JsonProcessingException {
        JsonNode root = objectMapper.createObjectNode();
        ObjectNode roleNode = objectMapper.createObjectNode();
        ObjectNode arnNode = objectMapper.createObjectNode();

        arnNode.put("aws_role_arn", roleName);
        roleNode.set("aws_role", arnNode);
        ((ObjectNode) root).put("name", integrationName);
        ((ObjectNode) root).put("description", description);
        ((ObjectNode) root).set(integrationType.name(), roleNode);
        logger.debug("Constructed create integration request" + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(root));

        return root;
    }


}
