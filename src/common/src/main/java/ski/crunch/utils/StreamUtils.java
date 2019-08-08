package ski.crunch.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class StreamUtils {

    public static String convertStreamToString(InputStream is) throws IOException {

        if (is == null) {
            throw new IOException("InputStream is null");
        }
        StringBuilder sb = new StringBuilder(2048); // Define a size if you have an idea of it.
        char[] read = new char[128]; // Your buffer size.
        try (InputStreamReader ir = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            for (int i; -1 != (i = ir.read(read)); sb.append(read, 0, i));
        }
        return sb.toString();
    }

    public static JsonNode convertStreamToJson(InputStream inputStream) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        int letter;
        String eventObject;
        StringBuilder eventObjectSb = new StringBuilder();

        while ((letter = inputStream.read()) > -1) {
            char inputChar = (char) letter;
            eventObjectSb.append(inputChar);
        }
        eventObject = eventObjectSb.toString();
        return objectMapper.readTree(eventObject);
    }


    public static InputStream convertStringToInputStream(String string) throws IOException {
        return new ByteArrayInputStream(string.getBytes());
    }
}
