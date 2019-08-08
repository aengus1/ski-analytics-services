package ski.crunch.aws.websocket;

import org.apache.log4j.Logger;
import ski.crunch.aws.DynamoDBService;
import ski.crunch.model.UserSettingsItem;

public class DisconnectHandler<Void> implements WebSocketHandler {

    private DynamoDBService dynamoDBService;
    private Logger LOGGER = Logger.getLogger(DisconnectHandler.class);

    public DisconnectHandler() {

    }


    @Override
    public Void handleMessage(WebSocketService.WebSocketRequestContext requestContext)  {
        //null out connection id
        LOGGER.debug("username = " + requestContext.getUsername());
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
