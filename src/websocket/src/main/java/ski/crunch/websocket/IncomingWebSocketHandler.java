package ski.crunch.websocket;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ski.crunch.aws.DynamoFacade;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IncomingWebSocketHandler implements RequestStreamHandler {



    private DynamoFacade dynamo;
    private ConnectHandler connectHandler = new ConnectHandler();
    private DisconnectHandler disconnectHandler = new DisconnectHandler();
    private MessageHandler messageHandler  = new MessageHandler();
    private WebSocketService wsService = new WebSocketService(connectHandler, disconnectHandler, messageHandler);

    private static final Logger logger = LoggerFactory.getLogger(IncomingWebSocketHandler.class);




    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {


        WebSocketService.WebSocketRequestContext wsContext = wsService.parseRequest(inputStream);
        // init dynamo
        dynamo = new DynamoFacade(wsContext.getRegion(), wsContext.getUserTable());

        connectHandler.setDynamo(dynamo);
        disconnectHandler.setDynamo(dynamo);
        messageHandler.setDynamo(dynamo);

        try {
            wsService.processRequest(wsContext);
        } catch (Exception e) {
            logger.error("Error occurred processing the request");
            e.printStackTrace();
        }


        //Passing a custom response as the output string
        String response = "{\n" +
                "    \"statusCode\": 200,\n" +
                "    \"headers\": {\"Content-Type\": \"application/json\"},\n" +
                "    \"body\": \"plain text response\"\n" +
                "}";
        outputStream.write(response.getBytes());

    }
}

