package ski.crunch.aws.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import org.apache.log4j.Logger;
import ski.crunch.utils.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Class for processing incoming websocket requests.  Delegates messages to handlers based on request type
 */
public class WebSocketService {

    private Map<WebSocketRequestType, WebSocketHandler> handlers;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger LOGGER = Logger.getLogger(WebSocketService.class);


    public WebSocketService(WebSocketHandler connectHandler, WebSocketHandler disconnectHandler, WebSocketHandler messageHandler) {
        handlers = new HashMap<>();
        handlers.put(WebSocketRequestType.CONNECT, connectHandler);
        handlers.put(WebSocketRequestType.DISCONNECT, disconnectHandler);
        handlers.put(WebSocketRequestType.MESSAGE, messageHandler);
    }

    public void processRequest(WebSocketRequestContext context) throws Exception{
        WebSocketHandler handler = handlers.get(context.getEventType());
        handler.handleMessage(context);
    }

    public WebSocketRequestContext parseRequest(InputStream is) throws IOException {

        WebSocketRequestContext context = new WebSocketRequestContext();
        JsonNode eventJson = StreamUtils.convertStreamToJson(is);

        LOGGER.info("parsed json: " + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(eventJson));
//        is.reset();
//        String request = StreamUtils.convertStreamToString(is);
//        System.out.println("request = " + request);

        String body = eventJson.path(WebSocketRequestContext.body).asText();
        JsonNode reqContext = eventJson.path(WebSocketRequestContext.requestContext);
        JsonNode auth = reqContext.path(WebSocketRequestContext.authorizer);

        // obtain username
        Iterator it = auth.fields();
        while (it.hasNext()) {
            Map.Entry next = (Map.Entry) it.next();
            System.out.println(next.getKey() + " " + next.getValue());
            if (next.getKey().equals(WebSocketRequestContext.cognitoUsername)) {
                context.setUsername(((TextNode) next.getValue()).asText());
                LOGGER.debug("username set: " + context.getUsername());
            }
        }

        // obtain connection id
        Iterator itr = reqContext.fields();
        while (itr.hasNext()) {
            Map.Entry next = (Map.Entry) itr.next();
            if (next.getKey().equals("connectionId")) {
                context.setConnectionId(((TextNode) next.getValue()).asText());
                LOGGER.debug("connectionId set: " + context.getConnectionId());
            }
            if (next.getKey().equals("eventType")) {
                context.setEventType(Enum.valueOf(WebSocketRequestType.class, ((TextNode) next.getValue()).asText().toUpperCase()));
                LOGGER.debug("eventType set: " + context.getEventType());
            }
        }

        // obtain message
        if( body != null && !body.isEmpty() ) {
            try {
                body = body.replace("\\\"", "\"");
                JsonNode jsonBody = objectMapper.readTree(body);
                LOGGER.info("parsed body: " + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonBody));
                Iterator bodyIt = jsonBody.fields();
                while (bodyIt.hasNext()) {
                    Map.Entry next = (Map.Entry) bodyIt.next();
                    if (next.getKey().equals(WebSocketRequestContext.message)) {
                        context.setMessageContent(((TextNode) next.getValue()).asText());
                        LOGGER.debug("message content set: " + context.getMessageContent());
                    }
                    if (next.getKey().equals(WebSocketRequestContext.action)) {
                        context.setAction(((TextNode) next.getValue()).asText());
                        LOGGER.debug("message action set: " + context.getAction());
                    }
                }
            }catch(Exception ex) {
                LOGGER.error("error obtaining messageBody");
                throw ex;
            }
        }


        // grab environment
        context.setRegion(System.getenv("AWS_DEFAULT_REGION"));
        context.setUserTable(System.getenv("userTable"));

        return context;
    }




   public  class WebSocketRequestContext {

       private static final String body = "body";
       private static final String requestContext = "requestContext";
       private static final String authorizer = "authorizer";
       private static final String cognitoUsername = "cognito:username";
       private static final String message = "message";
       private static final String action = "action";

        private String region;
        private String username;
        private String userTable;
        private String connectionId;
        private String messageContent;
        private String actionContent;
        private WebSocketRequestType eventType;


       public String getRegion() {
           return region;
       }

       public void setRegion(String region) {
           this.region = region;
       }

       public String getUsername() {
           return username;
       }

       public void setUsername(String username) {
           this.username = username;
       }

       public String getUserTable() {
           return userTable;
       }

       public void setUserTable(String userTable) {
           this.userTable = userTable;
       }

       public String getConnectionId() {
           return connectionId;
       }

       public void setConnectionId(String connectionId) {
           this.connectionId = connectionId;
       }


       public WebSocketRequestType getEventType() {
           return eventType;
       }

       public void setEventType(WebSocketRequestType eventType) {
           this.eventType = eventType;
       }

       public String getMessageContent() {
           return messageContent;
       }

       public void setMessageContent(String messageContent) {
           this.messageContent = messageContent;
       }

       public String getAction() {
           return actionContent;
       }

       public void setAction(String action) {
           this.actionContent = action;
       }
   }
}
