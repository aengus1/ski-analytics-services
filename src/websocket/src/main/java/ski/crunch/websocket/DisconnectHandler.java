package ski.crunch.websocket;

import org.apache.log4j.Logger;
import ski.crunch.aws.DynamoFacade;
import ski.crunch.model.UserSettingsItem;

public class DisconnectHandler<Void> implements WebSocketHandler {

    private DynamoFacade dynamo;
    private Logger LOGGER = Logger.getLogger(DisconnectHandler.class);

    public DisconnectHandler() {

    }


    @Override
    public Void handleMessage(WebSocketService.WebSocketRequestContext requestContext)  {
        //null out connection id
        LOGGER.debug("username = " + requestContext.getUsername());
        UserSettingsItem userSettings = dynamo.getMapper().load(UserSettingsItem.class, requestContext.getUsername());
        userSettings.setConnectionId("");
        this.dynamo.getMapper().save(userSettings);

        LOGGER.info("connectionId: " + requestContext.getConnectionId() + " cleared for user " + requestContext.getUsername());
        return null;
    }

     void setDynamo(DynamoFacade dynamo) {
        this.dynamo = dynamo;
    }
}
