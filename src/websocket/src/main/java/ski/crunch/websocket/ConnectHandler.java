package ski.crunch.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ski.crunch.aws.DynamoFacade;
import ski.crunch.model.UserSettingsItem;

public class ConnectHandler implements WebSocketHandler {

    private DynamoFacade dynamo;
    private Logger LOGGER = LoggerFactory.getLogger(ConnectHandler.class);


    @Override
    public Void handleMessage(WebSocketService.WebSocketRequestContext requestContext) {
        // save the connection id
        UserSettingsItem userSettings = dynamo.getMapper().load(UserSettingsItem.class, requestContext.getUsername());
        userSettings.setConnectionId(requestContext.getConnectionId());
        dynamo.getMapper().save(userSettings);

        LOGGER.info("connectionId: " + requestContext.getConnectionId() + " saved to user table for " + requestContext.getUsername());
        return null;
    }

     void setDynamo(DynamoFacade dynamo) {
        this.dynamo = dynamo;
    }
}
