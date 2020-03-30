package ski.crunch.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ski.crunch.aws.DynamoFacade;

public class MessageHandler implements WebSocketHandler {

    private DynamoFacade dynamo;
    private Logger logger = LoggerFactory.getLogger(MessageHandler.class);



    @Override
    public Void handleMessage(WebSocketService.WebSocketRequestContext requestContext)  {
        logger.info("doing something useful with message..." + requestContext.getMessageContent());
        return null;
    }

     void setDynamo(DynamoFacade dynamo) {
        this.dynamo = dynamo;
    }
}
