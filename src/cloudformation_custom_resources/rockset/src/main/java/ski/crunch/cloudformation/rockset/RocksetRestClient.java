package ski.crunch.cloudformation.rockset;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
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
import ski.crunch.cloudformation.rockset.model.DataSource;
import ski.crunch.cloudformation.rockset.model.FieldMapping;
import ski.crunch.cloudformation.rockset.model.RocksetIntegrationType;
import ski.crunch.utils.StreamUtils;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;

public class RocksetRestClient {

    private static final Logger logger = Logger.getLogger(RocksetRestClient.class);
    private static final String INTEGRATION_ENDPOINT = "/v1/orgs/self/integrations";
    private static final String ENDPOINT = "/v1/orgs/self/ws/";

    private String apiServer;
    private String apiKey;
    private CloseableHttpClient client;
    private ObjectMapper objectMapper;


    /**
     * @param apiServer String api server url
     * @param apiKey    String api key
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
     * @param awsAccountId String AWS Account ID of this account
     * @return String response integrationResponse
     * @throws JsonProcessingException on error building JSON
     * @throws IOException             on error communicating with rockset
     */
    public String createIntegration(String roleName, String integrationName, String description, RocksetIntegrationType integrationType, String awsAccountId) throws JsonProcessingException, IOException {

        JsonNode payload = constructCreateIntegrationRequest(roleName, integrationName, description, integrationType, awsAccountId);
        String payloadStr = objectMapper.writer().writeValueAsString(payload);
        logger.debug("Create integration request: " + payloadStr);
        String responseString = doPost(payloadStr, "https://" + apiServer + INTEGRATION_ENDPOINT);
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
    public String deleteIntegration(String integrationName) throws IOException {
        logger.debug("delete called for: " + integrationName);
        String responseString = doDelete("https://" + apiServer + INTEGRATION_ENDPOINT + "/" + integrationName);
        logger.debug("Delete integration response" + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(responseString));
        return responseString;
    }


    /**
     * Constructs and posts rockset create workspace request
     *
     * @param workspaceName String name of workspace to create
     * @param description   String description of workspace
     * @return String response
     * @throws IOException
     */
    public String createWorkspace(String workspaceName, String description) throws IOException {
        logger.debug("Create workspace called: " + workspaceName);
        JsonNode payload = constructCreateWorkspaceRequest(workspaceName, description);
        String payloadStr = objectMapper.writer().writeValueAsString(payload);
        String responseString = doPost(payloadStr, "https://" + apiServer + ENDPOINT);
        logger.debug("Create workspace response" + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(responseString));
        return responseString;
    }


    /**
     * Constructs and posts rockset delete workspace request
     * @param workspaceName String workspace name
     * @return String response
     * @throws IOException
     */
    public String deleteWorkspace(String workspaceName) throws IOException {
        logger.debug("delete workspace called for: " + workspaceName);
        String responseString = doDelete("https://" + apiServer + ENDPOINT + "/" + workspaceName);
        logger.debug("Delete workspace response" + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(responseString));

        return responseString;
    }

    /**
     * Constructs and posts rockset create collection request
     * @param name String collection name
     * @param workspace String workspace name
     * @param description String collection description
     * @param dataSourceList List<DataSource> datasource list
     * @param integrationName String name of integration
     * @param retentionSecs Optional<Long> retention time seconds
     * @param eventTimeField String event time field
     * @param eventTimeFormat String event time format
     * @param eventTimeZone String event timezone
     * @param fieldMappings List<FieldMapping> fieldmappings
     * @return String response
     * @throws IOException
     */
    public String createCollection(String name, String workspace, String description, List<DataSource> dataSourceList, String integrationName,
                                   Optional<Long> retentionSecs, Optional<String> eventTimeField, Optional<String> eventTimeFormat, Optional<String> eventTimeZone,
                                   Optional<List<FieldMapping>> fieldMappings) throws IOException {
        logger.debug("Create collection called for: " + name);
        JsonNode request = constructCreateCollectionRequest(name, description, dataSourceList, integrationName, retentionSecs,
                eventTimeField, eventTimeFormat, eventTimeZone, fieldMappings);
        String payloadStr = objectMapper.writer().writeValueAsString(request);
        String response = doPost(payloadStr,"https://" + apiServer + ENDPOINT + "/" + workspace + "/collections" );
        logger.debug("Create collection response" + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
        return response;
    }

    /**
     * Constructs and posts a delete collection request
     * @param collectionName String collection name
     * @param workspace String workspace name
     * @return String response
     * @throws IOException
     */
    public String deleteCollection(String collectionName, String workspace) throws IOException {
        logger.debug("delete collection called for: " + collectionName);
        String responseString = doDelete("https://" + apiServer + ENDPOINT + "/" + workspace + "/collections/" + collectionName);
        logger.debug("Delete collection response" + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(responseString));
        return responseString;
    }

    @VisibleForTesting
    JsonNode constructCreateIntegrationRequest(String roleName, String integrationName, String description, RocksetIntegrationType integrationType, String awsAccountId) throws JsonProcessingException {
        JsonNode root = objectMapper.createObjectNode();
        ObjectNode roleNode = objectMapper.createObjectNode();
        ObjectNode arnNode = objectMapper.createObjectNode();
        arnNode.put("aws_role_arn", "arn:aws:iam::" + awsAccountId +":role/" +roleName);
        roleNode.set("aws_role", arnNode);
        ((ObjectNode) root).put("name", integrationName);
        ((ObjectNode) root).put("description", description);
        ((ObjectNode) root).set(integrationType.name(), roleNode);
        logger.debug("Constructed create integration request" + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(root));

        return root;
    }


    @VisibleForTesting
    JsonNode constructCreateWorkspaceRequest(String name, String description) {
        JsonNode root = objectMapper.createObjectNode();
        ((ObjectNode) root).put("name", name);
        ((ObjectNode) root).put("description", description);
        return root;
    }

    @VisibleForTesting
    JsonNode constructCreateCollectionRequest(String name, String description, List<DataSource> dataSourceList, String integrationName,
                                              Optional<Long> retentionSecs, Optional<String> eventTimeField, Optional<String> eventTimeFormat, Optional<String> eventTimeZone,
                                              Optional<List<FieldMapping>> fieldMappings) {
        JsonNode root = objectMapper.createObjectNode();
        ((ObjectNode) root).put("name", name);
        ((ObjectNode) root).put("description", description);

        ArrayNode sources = objectMapper.createArrayNode();
        JsonNode source = objectMapper.createObjectNode();
        ((ObjectNode) source).put("integration_name", integrationName);
        for (DataSource dataSource : dataSourceList) {

            ((ObjectNode) source).set(dataSource.getIntegrationType().name(), dataSource.toJson(objectMapper));
        }
        sources.add(source);
        ((ObjectNode) root).set("sources", sources);

        if(retentionSecs.isPresent()) {
            ((ObjectNode) root).put("retention_secs", retentionSecs.get());
        }
        if (eventTimeField.isPresent()) {
            ObjectNode eventTimeInfo = objectMapper.createObjectNode();
            eventTimeInfo.put("field", eventTimeField.get());
            eventTimeInfo.put("format", eventTimeFormat.get());
            eventTimeInfo.put("time_zone", eventTimeZone.get());
            ((ObjectNode) root).set("event_time_info", eventTimeInfo);
        }


        if (fieldMappings.isPresent()) {
            ArrayNode fieldMappingArray = objectMapper.createArrayNode();
            for (FieldMapping fieldMapping : fieldMappings.get()) {
                JsonNode mappingNode = fieldMapping.toJson(objectMapper);
                fieldMappingArray.add(mappingNode);
            }
            ((ObjectNode) root).set("field_mappings", fieldMappingArray);
        }

        return root;
    }

    private String doPost(String entityPayload, String endpoint) throws IOException {
        HttpEntity entity = EntityBuilder.create().setText(entityPayload).build();
        HttpPost httpPost = new HttpPost();
        httpPost.setURI(URI.create(endpoint));
        httpPost.setHeader("Authorization", "ApiKey " + apiKey);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setEntity(entity);
        CloseableHttpResponse response = client.execute(httpPost);
        return StreamUtils.convertStreamToString(response.getEntity().getContent());
    }

    private String doDelete(String endpoint) throws IOException {
        HttpDelete httpDelete = new HttpDelete();
        httpDelete.setURI(URI.create(endpoint));
        httpDelete.setHeader("Authorization", "ApiKey " + apiKey);
        CloseableHttpResponse response = client.execute(httpDelete);
        return StreamUtils.convertStreamToString(response.getEntity().getContent());
    }
}
