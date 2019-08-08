package ski.crunch.aws.websocket;

import org.apache.log4j.Logger;
import ski.crunch.aws.DynamoDBService;

public class MessageHandler implements WebSocketHandler {

    private DynamoDBService dynamoDBService;
    private Logger LOGGER = Logger.getLogger(MessageHandler.class);



    @Override
    public Void handleMessage(WebSocketService.WebSocketRequestContext requestContext)  {
        LOGGER.info("doing something useful with message..." + requestContext.getMessageContent());
        return null;
    }

    public void setDynamoDBService(DynamoDBService dynamoDBService) {
        this.dynamoDBService = dynamoDBService;
    }
}
