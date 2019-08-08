package ski.crunch.aws.websocket;

public interface WebSocketHandler<T> {

    T handleMessage(WebSocketService.WebSocketRequestContext requestContext) throws Exception;
}
