package ski.crunch.cloudformation.rockset;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import ski.crunch.cloudformation.rockset.model.DataSource;
import ski.crunch.cloudformation.rockset.model.RocksetIntegrationType;
import ski.crunch.cloudformation.rockset.model.S3DataSource;
import ski.crunch.utils.StreamUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class RocksetRestClientTest {

    @Mock
    private CloseableHttpClient httpClient = Mockito.mock(CloseableHttpClient.class);

    private RocksetRestClient rocksetRestClient = null;

    @BeforeAll()
    public static void init() {
        MockitoAnnotations.initMocks(RocksetRestClientTest.class);
    }


    @BeforeEach()
    public void setUp() {

        rocksetRestClient = new RocksetRestClient("api.server.test","abc123", httpClient);
    }

    @Test()
    public void testHttpCallIsMadeOnCreateIntegration() throws Exception{
        CloseableHttpResponse mockResponse = Mockito.mock(CloseableHttpResponse.class);
        HttpEntity mockEntity = Mockito.mock(HttpEntity.class);
        when(mockEntity.getContent()).thenReturn(StreamUtils.convertStringToInputStream("test_response"));
        when(mockResponse.getEntity()).thenReturn(mockEntity);
        when(httpClient.execute(any())).thenReturn(mockResponse);
        rocksetRestClient.createIntegration("myRole", "myIntegration",
                "description", RocksetIntegrationType.dynamodb, "123456");

        verify(httpClient, times(1)).execute(any());

    }

    @Test()
    public void testCreateIntegrationJsonPayload() throws Exception{
        String expectedPayload = "{" +
                "\"name\":\"testIntegration\"," +
                "\"description\":\"testdescription\"," +
                "\"dynamodb\":{" +
                "\"aws_role\":{" +
                "\"aws_role_arn\":\"arn:aws:iam::123456:role/test_role\"" +
                "}" +
                "}" +
                "}";
        expectedPayload.replaceAll(" ", "");

        JsonNode result = rocksetRestClient.constructCreateIntegrationRequest("test_role", "testIntegration",
                "test description",  RocksetIntegrationType.dynamodb, "123456");
        ObjectMapper objectMapper = new ObjectMapper();
        assertEquals( expectedPayload, objectMapper.writer().writeValueAsString(result).replaceAll(" ", ""));
    }

    @Test
    public void testHttpCallIsMadeOnDeleteIntegration() throws Exception{
        CloseableHttpResponse mockResponse = Mockito.mock(CloseableHttpResponse.class);
        HttpEntity mockEntity = Mockito.mock(HttpEntity.class);
        when(mockEntity.getContent()).thenReturn(StreamUtils.convertStringToInputStream("test_response"));
        when(mockResponse.getEntity()).thenReturn(mockEntity);
        when(httpClient.execute(any())).thenReturn(mockResponse);
        rocksetRestClient.deleteIntegration("testIntegration");
        verify(httpClient, times(1)).execute(any());
    }

    @Test
    public void testCreateWorkspaceIntegrationJsonPayload() throws Exception {
        String expectedPayload = "{" +
                "\"name\":\"testWorkspace\"," +
                "\"description\":\"test description\"" +
                "}";
        JsonNode result = rocksetRestClient.constructCreateWorkspaceRequest("testWorkspace", "test description");
        ObjectMapper objectMapper = new ObjectMapper();
        assertEquals( expectedPayload, objectMapper.writer().writeValueAsString(result));
    }

    @Test
    public void testCreateCollectionJsonPayload() throws Exception {
        String expectedPayload = "{" +
                "\"name\":\"testCollection\"," +
                "\"description\":\"testdescription\"," +
                "\"sources\": [" +
                "{" +
                "\"integration_name\": \"myIntegration\"," +
                "\"s3\": {"+
                  "\"prefix\": \"prefix/to/keys\"," +
                "\"pattern\": \"prefix/to/**/keys/*.format\"," +
                "\"bucket\": \"s3://customer-account-info\"" +
                "}" +
                "}]," +
                "\"retention_secs\": 100,"+
                "\"event_time_info\": {" +
                "\"field\": \"timestamp\"," +
                "\"format\": \"seconds_since_epoch\"," +
                "\"time_zone\": \"UTC\"" +
                "}" +
                "}";
        expectedPayload = expectedPayload.replaceAll(" ", "");

        List<DataSource> dataSourceList = new ArrayList<>();
        S3DataSource ds = new S3DataSource();
        Map<String, Object> input = new HashMap<>();
        input.put("S3Bucket", "s3://customer-account-info");
        input.put("S3Pattern", "prefix/to/**/keys/*.format");
        input.put("S3Prefix", "prefix/to/keys");
        ds.parse(input);
        dataSourceList.add(ds);
        JsonNode result = rocksetRestClient.constructCreateCollectionRequest(
                "testCollection", "testdescription",dataSourceList,
        "myIntegration", Optional.of(100l), Optional.of("timestamp"), Optional.of("seconds_since_epoch"),
        Optional.of("UTC"), Optional.empty());
        ObjectMapper objectMapper = new ObjectMapper();
        assertEquals( expectedPayload, objectMapper.writer().writeValueAsString(result).replaceAll(" ", ""));

    }

    @Test
    public void testHttpCallIsMadeOnDeleteCollection() throws Exception {
        CloseableHttpResponse mockResponse = Mockito.mock(CloseableHttpResponse.class);
        HttpEntity mockEntity = Mockito.mock(HttpEntity.class);
        when(mockEntity.getContent()).thenReturn(StreamUtils.convertStringToInputStream("success"));
        when(mockResponse.getEntity()).thenReturn(mockEntity);
        when(httpClient.execute(any())).thenReturn(mockResponse);
        rocksetRestClient.deleteCollection("testCollection", "testWorkspace");
        verify(httpClient, times(1)).execute(any());
    }
    @Test
    public void testHttpCallIsMadeOnDeleteWorkspace() throws Exception {
        CloseableHttpResponse mockResponse = Mockito.mock(CloseableHttpResponse.class);
        HttpEntity mockEntity = Mockito.mock(HttpEntity.class);
        when(mockEntity.getContent()).thenReturn(StreamUtils.convertStringToInputStream("testWorkspace"));
        when(mockResponse.getEntity()).thenReturn(mockEntity);
        when(httpClient.execute(any())).thenReturn(mockResponse);
        rocksetRestClient.deleteWorkspace("testWorkspace");
        verify(httpClient, times(1)).execute(any());
    }
}
