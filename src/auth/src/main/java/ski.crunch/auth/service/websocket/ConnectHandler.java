package ski.crunch.auth.service.websocket;

import org.apache.log4j.Logger;
import ski.crunch.auth.DynamoDBService;
import ski.crunch.auth.UserSettingsItem;

public class ConnectHandler implements WebSocketHandler {

    private DynamoDBService dynamoDBService;
    private Logger LOGGER = Logger.getLogger(ConnectHandler.class);


    @Override
    public Void handleMessage(WebSocketService.WebSocketRequestContext requestContext) {
        // save the connection id
        UserSettingsItem userSettings = dynamoDBService.getMapper().load(UserSettingsItem.class, requestContext.getUsername());
        userSettings.setConnectionId(requestContext.getConnectionId());
        dynamoDBService.getMapper().save(userSettings);

        LOGGER.info("connectionId: " + requestContext.getConnectionId() + " saved to user table for " + requestContext.getUsername());
        return null;
    }

    public void setDynamoDBService(DynamoDBService dynamoDBService) {
        this.dynamoDBService = dynamoDBService;
    }
}
