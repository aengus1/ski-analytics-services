package ski.crunch.utils;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class HttpClientUtilTest {

    private HttpClientUtil httpClientUtil;

    @Mock
    private CloseableHttpClient httpClient;

    @Mock
    private HttpGet httpGet;

    @Mock
    private CloseableHttpResponse closeableHttpResponse;

    @Mock
    private HttpEntity httpEntity;


    final static String exampleString = "{\n" +
            "    \"glossary\": {\n" +
            "        \"title\": \"example glossary\",\n" +
            "\t\t\"GlossDiv\": {\n" +
            "            \"title\": \"S\",\n" +
            "\t\t\t\"GlossList\": {\n" +
            "                \"GlossEntry\": {\n" +
            "                    \"ID\": \"SGML\",\n" +
            "\t\t\t\t\t\"SortAs\": \"SGML\",\n" +
            "\t\t\t\t\t\"GlossTerm\": \"Standard Generalized Markup Language\",\n" +
            "\t\t\t\t\t\"Acronym\": \"SGML\",\n" +
            "\t\t\t\t\t\"Abbrev\": \"ISO 8879:1986\",\n" +
            "\t\t\t\t\t\"GlossDef\": {\n" +
            "                        \"para\": \"A meta-markup language, used to create markup languages such as DocBook.\",\n" +
            "\t\t\t\t\t\t\"GlossSeeAlso\": [\"GML\", \"XML\"]\n" +
            "                    },\n" +
            "\t\t\t\t\t\"GlossSee\": \"markup\"\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}\n";

    @BeforeEach()
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        httpClientUtil = new HttpClientUtil(httpClient);
    }


    @Test()
    public void getJsonNodeShouldReturnValidJson() throws IOException {

        when(httpEntity.getContent()).thenReturn(StreamUtils.convertStringToInputStream(exampleString));
        when(closeableHttpResponse.getEntity()).thenReturn(httpEntity);
        when(httpClient.execute(httpGet)).thenReturn(closeableHttpResponse);

        try {
            this.httpClientUtil.getJsonNode(httpGet);

        }catch (IOException ex) {
            fail("IOException thrown attempting to read Json");
        }
    }


    @Test()
    public void getJsonNodeShouldThrowWhenJsonNotValid() throws IOException{

        when(httpEntity.getContent()).thenReturn(StreamUtils.convertStringToInputStream(exampleString.substring(0, exampleString.length() -5)));
        when(closeableHttpResponse.getEntity()).thenReturn(httpEntity);
        when(httpClient.execute(httpGet)).thenReturn(closeableHttpResponse);

        assertThrows(IOException.class,
                () -> {
                    this.httpClientUtil.getJsonNode(httpGet);
                });

    }


    @Test()
    public void getJsonNodeShouldThrowWhenJsonEmpty() throws IOException{

        when(httpEntity.getContent()).thenReturn(StreamUtils.convertStringToInputStream(" "));
        when(closeableHttpResponse.getEntity()).thenReturn(httpEntity);
        when(httpClient.execute(httpGet)).thenReturn(closeableHttpResponse);

        assertThrows(IOException.class,
                () -> {
                    this.httpClientUtil.getJsonNode(httpGet);
                });

    }
}
