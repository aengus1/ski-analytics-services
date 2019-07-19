package ski.crunch.auth.service.websocket;

public interface WebSocketHandler<T> {

    T handleMessage(WebSocketService.WebSocketRequestContext requestContext) throws Exception;
}
