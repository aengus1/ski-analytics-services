package ski.crunch.activity.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import ski.crunch.utils.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class StreamUtilsTest {

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

    private InputStream testInputStream;



    @BeforeEach()
    public void setUp() {
        testInputStream =  new ByteArrayInputStream( exampleString.getBytes() );
    }

    @Test
    public void convertInputStreamToStringHappyPath() throws IOException {
        assertEquals(exampleString, StreamUtils.convertStreamToString(testInputStream));
    }

    @Test
    public void convertInputStreamToStringNullEmptyStream() throws IOException {
        testInputStream.close();
        assertThrows(IOException.class, () -> StreamUtils.convertStreamToString(null));

        assertThrows(IOException.class, () -> StreamUtils.convertStreamToString(new FileInputStream("")));
    }

    @Test
    public void convertInputStreamToJson() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode exampleJson = objectMapper.readTree(exampleString);
        assertEquals(exampleJson, StreamUtils.convertStreamToJson(testInputStream));
    }

    @Test
    public void convertInputStreamToJsonException() throws IOException {
        String invalidString= exampleString.substring(0, exampleString.length() -10);
        testInputStream =  new ByteArrayInputStream( invalidString.getBytes() );
        assertThrows(IOException.class, () -> { StreamUtils.convertStreamToJson(testInputStream);});
    }
}
