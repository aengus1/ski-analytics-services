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
import ski.crunch.utils.StreamUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;

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
        rocksetRestClient.createIntegrationViaRest("myRole", "myIntegration", "description", RocksetIntegrationType.dynamodb);

        verify(httpClient, times(1)).execute(any());

    }

    @Test()
    public void testCreateIntegrationJsonPayload() throws Exception{
        String expectedPayload = "{" +
                "\"name\":\"testIntegration\"," +
                "\"description\":\"testdescription\"," +
                "\"dynamodb\":{" +
                "\"aws_role\":{" +
                "\"aws_role_arn\":\"test_role\"" +
                "}" +
                "}" +
                "}";
        expectedPayload.replaceAll(" ", "");

        JsonNode result = rocksetRestClient.constructCreateIntegrationRequest("test_role", "testIntegration", "test description",  RocksetIntegrationType.dynamodb);
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
        rocksetRestClient.deleteIntegrationViaRest("testIntegration");
        verify(httpClient, times(1)).execute(any());
    }
}
