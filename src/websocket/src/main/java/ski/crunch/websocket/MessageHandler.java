package ski.crunch.websocket;

import org.apache.log4j.Logger;
import ski.crunch.aws.DynamoFacade;

public class MessageHandler implements WebSocketHandler {

    private DynamoFacade dynamo;
    private Logger LOGGER = Logger.getLogger(MessageHandler.class);



    @Override
    public Void handleMessage(WebSocketService.WebSocketRequestContext requestContext)  {
        LOGGER.info("doing something useful with message..." + requestContext.getMessageContent());
        return null;
    }

     void setDynamo(DynamoFacade dynamo) {
        this.dynamo = dynamo;
    }
}
