package ski.crunch.testhelpers;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

 class ServerlessState {
    private final String json;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private JsonNode rootNode;


    private ServerlessState(String body) {
        this.json = body;
        try {
            rootNode = objectMapper.readTree(body);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

     String getRegion(){
        return rootNode.path("service").path("provider").path("region").asText();
    }

      static ServerlessState readServerlessState(String filePath) throws IOException {
        String body = Files.lines(Paths.get(filePath),
                StandardCharsets.UTF_8).collect(Collectors.joining("\n"));
        return new ServerlessState(body);
    }
}
