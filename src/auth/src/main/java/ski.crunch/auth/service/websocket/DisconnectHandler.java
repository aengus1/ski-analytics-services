package ski.crunch.auth.service.websocket;

import org.apache.log4j.Logger;
import ski.crunch.auth.DynamoDBService;
import ski.crunch.auth.UserSettingsItem;

public class DisconnectHandler<Void> implements WebSocketHandler {

    private DynamoDBService dynamoDBService;
    private Logger LOGGER = Logger.getLogger(DisconnectHandler.class);

    public DisconnectHandler() {

    }


    @Override
    public Void handleMessage(WebSocketService.WebSocketRequestContext requestContext)  {
        //null out connection id
        UserSettingsItem userSettings = dynamoDBService.getMapper().load(UserSettingsItem.class, requestContext.getUsername());
        userSettings.setConnectionId("");
        this.dynamoDBService.getMapper().save(userSettings);

        LOGGER.info("connectionId: " + requestContext.getConnectionId() + " cleared for user " + requestContext.getUsername());
        return null;
    }

    public void setDynamoDBService(DynamoDBService dynamoDBService) {
        this.dynamoDBService = dynamoDBService;
    }
}