package ski.crunch.utils;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class ServerlessState {
    private final String json;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private JsonNode rootNode;


    ServerlessState(String body) {
        this.json = body;
        try {
            rootNode = objectMapper.readTree(body);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public String getStackRegion() {
        return rootNode.path("service").path("provider").path("region").asText();
    }

    public String getUserPoolClientId(){
return "";
    }

    private JsonNode getUserPoolAuthorizer(){
       return rootNode.path("service").path("functions").path("GetActivityLambda")
                .path("events").elements().next().path("http").path("authorizer");
    }

    public String getUserPoolRegion(){
        String arn = getUserPoolAuthorizer().path("arn").asText();
        return arn.split("arn:aws:cognito-idp:")[1].split(":")[0];
    }

    public String getUserPoolId(){
        String userPoolARN =  getUserPoolAuthorizer().path("arn").asText();
        return userPoolARN.split("/")[1];
    }

    public final static ServerlessState readServerlessState(String filePath) throws IOException {
        String body = Files.lines(Paths.get(filePath),
                StandardCharsets.UTF_8).collect(Collectors.joining("\n"));
        return new ServerlessState(body);
    }
}
