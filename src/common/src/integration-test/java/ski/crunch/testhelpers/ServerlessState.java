package ski.crunch.testhelpers;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class ServerlessState {
   private final ObjectMapper objectMapper = new ObjectMapper();
   private JsonNode rootNode;


   private ServerlessState(String body) {
       try {
           rootNode = objectMapper.readTree(body);
       } catch (IOException e) {
           e.printStackTrace();
       }
   }

    public String getRegion(){
       return rootNode.path("service").path("provider").path("region").asText();
   }


    public  JsonNode getRootNode() {
       return rootNode;
    }


     protected static ServerlessState readServerlessState(String filePath) throws IOException {
       String body = Files.lines(Paths.get(filePath),
               StandardCharsets.UTF_8).collect(Collectors.joining("\n"));
       return new ServerlessState(body);
   }

   public String getStackName(){
       return rootNode.path("service").path("service").asText();
   }
}
