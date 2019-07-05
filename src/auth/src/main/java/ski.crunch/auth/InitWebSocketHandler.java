package ski.crunch.auth;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;

public class InitWebSocketHandler implements RequestStreamHandler {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private String username = "";
    private DynamoDBService dynamoService;
    private String region;
    private String userTable;
    private String connectionId;

    private static final Logger LOGGER = Logger.getLogger(InitWebSocketHandler.class);
    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {

        //parse input
        int letter;
        String eventObject = "";

        while ((letter = inputStream.read()) > -1) {
            char inputChar= (char) letter;
            eventObject += inputChar;
        }
        LOGGER.debug("event object = " + eventObject);

        JsonNode eventJson = objectMapper.readTree(eventObject);
        JsonNode reqContext = eventJson.path("requestContext");
        JsonNode auth = reqContext.path("authorizer");

        Iterator it = auth.fields();
        while(it.hasNext()) {
            Map.Entry next = (Map.Entry) it.next();
            System.out.println(next.getKey() + " " + next.getValue());
            if(next.getKey().equals("cognito:username")) {
                username = ((TextNode) next.getValue()).asText();
                LOGGER.info("username set: " + username);
            }
        }

        Iterator itr = reqContext.fields();
        while(itr.hasNext()) {
            Map.Entry next = (Map.Entry) itr.next();
            if(next.getKey().equals("connectionId")) {
                connectionId = ((TextNode) next.getValue()).asText();
                LOGGER.debug("connectionId set: " + connectionId);
            }
        }


        //init dynamo service
        this.region = System.getenv("AWS_DEFAULT_REGION");
        this.userTable = System.getenv("userTable");

        dynamoService = new DynamoDBService(region, userTable);

        // save the connection id
        UserSettingsItem userSettings = dynamoService.getMapper().load(UserSettingsItem.class, this.username);

        // Update the item.
        userSettings.setConnectionId(connectionId);
        dynamoService.getMapper().save(userSettings);
        LOGGER.info("connectionId: " + connectionId + " saved to user table");


        //Passing a custom response as the output string
        String response = "{\n" +
                "    \"statusCode\": 200,\n" +
                "    \"headers\": {\"Content-Type\": \"application/json\"},\n" +
                "    \"body\": \"plain text response\"\n" +
                "}";
        outputStream.write(response.getBytes());

        System.out.println("Input-Event: " + eventObject);
    }
}

