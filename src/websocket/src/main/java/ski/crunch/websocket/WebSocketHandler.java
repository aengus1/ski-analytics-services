package ski.crunch.websocket;

public interface WebSocketHandler<T> {

    T handleMessage(WebSocketService.WebSocketRequestContext requestContext) throws Exception;
}
