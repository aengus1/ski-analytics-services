package ski.crunch.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ski.crunch.aws.DynamoFacade;
import ski.crunch.model.UserSettingsItem;

public class DisconnectHandler<Void> implements WebSocketHandler {

    private DynamoFacade dynamo;
    private Logger logger = LoggerFactory.getLogger(DisconnectHandler.class);

    public DisconnectHandler() {

    }


    @Override
    public Void handleMessage(WebSocketService.WebSocketRequestContext requestContext)  {
        //null out connection id
        logger.debug("username = " + requestContext.getUsername());
        UserSettingsItem userSettings = dynamo.getMapper().load(UserSettingsItem.class, requestContext.getUsername());
        userSettings.setConnectionId("");
        this.dynamo.getMapper().save(userSettings);

        logger.info("connectionId: " + requestContext.getConnectionId() + " cleared for user " + requestContext.getUsername());
        return null;
    }

     void setDynamo(DynamoFacade dynamo) {
        this.dynamo = dynamo;
    }
}
