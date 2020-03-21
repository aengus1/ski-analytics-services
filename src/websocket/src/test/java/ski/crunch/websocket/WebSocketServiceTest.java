package ski.crunch.websocket;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


@TestInstance(TestInstance.Lifecycle.PER_METHOD)
 class WebSocketServiceTest {


    private static final String USERNAME = "0a109298-5b8e-482a-9138-4dcd2802ec53";
    private static final String CONNECTION_ID = "cih0Ge8sPHcCJIA=";
    private static final String ACTION = "test";
    private static final String PAYLOAD = "testmesg";
    private static final String MESSAGE = "{\\\"payload\\\": \\\""+PAYLOAD+"\\\"}";
    private WebSocketRequestType eventType = WebSocketRequestType.CONNECT;
    private static final String DEFAULT_REGION = "ca-canada-1";
    private static final String USER_TABLE_NAME="userTableName";



    private String sampleRequest = "{\n" +
            "    \"headers\": {\n" +
            "        \"Host\": \"c4at2w51lg.execute-api.us-west-2.amazonaws.com\",\n" +
            "        \"Sec-WebSocket-Extensions\": \"permessage-deflate; client_max_window_bits\",\n" +
            "        \"Sec-WebSocket-Key\": \"Mi9AiHC3yh2ENQUfaNzAJA==\",\n" +
            "        \"Sec-WebSocket-Version\": \"13\",\n" +
            "        \"X-Amzn-Trace-Id\": \"Root=1-5d241680-fcfe2e90e93f84fc20e4fd52\",\n" +
            "        \"X-Forwarded-For\": \"64.180.10.247\",\n" +
            "        \"X-Forwarded-Port\": \"443\",\n" +
            "        \"X-Forwarded-Proto\": \"https\"\n" +
            "    },\n" +
            "    \"multiValueHeaders\": {\n" +
            "        \"Host\": [\n" +
            "            \"c4at2w51lg.execute-api.us-west-2.amazonaws.com\"\n" +
            "        ],\n" +
            "        \"Sec-WebSocket-Extensions\": [\n" +
            "            \"permessage-deflate; client_max_window_bits\"\n" +
            "        ],\n" +
            "        \"Sec-WebSocket-Key\": [\n" +
            "            \"Mi9AiHC3yh2ENQUfaNzAJA==\"\n" +
            "        ],\n" +
            "        \"Sec-WebSocket-Version\": [\n" +
            "            \"13\"\n" +
            "        ],\n" +
            "        \"X-Amzn-Trace-Id\": [\n" +
            "            \"Root=1-5d241680-fcfe2e90e93f84fc20e4fd52\"\n" +
            "        ],\n" +
            "        \"X-Forwarded-For\": [\n" +
            "            \"64.180.10.247\"\n" +
            "        ],\n" +
            "        \"X-Forwarded-Port\": [\n" +
            "            \"443\"\n" +
            "        ],\n" +
            "        \"X-Forwarded-Proto\": [\n" +
            "            \"https\"\n" +
            "        ]\n" +
            "    },\n" +
            "    \"queryStringParameters\": {\n" +
            "        \"token\": \"eyJraWQiOiI1OE1cL0w5SjhJQm5yVnZieEVwTFFydVJKeFBmcXdDRWNuTkNrWjFLWE96Zz0iLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiIwYTEwOTI5OC01YjhlLTQ4MmEtOTEzOC00ZGNkMjgwMmVjNTMiLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiaXNzIjoiaHR0cHM6XC9cL2NvZ25pdG8taWRwLnVzLXdlc3QtMi5hbWF6b25hd3MuY29tXC91cy13ZXN0LTJfRnJIMFVkck56IiwiY29nbml0bzp1c2VybmFtZSI6IjBhMTA5Mjk4LTViOGUtNDgyYS05MTM4LTRkY2QyODAyZWM1MyIsImN1c3RvbTpmYW1pbHlOYW1lIjoiTWNDdWxsb3VnaCIsImF1ZCI6Ijc1NWY1ZDBlbHNnNWllOTYxMTY1NDBxbTN1IiwiZXZlbnRfaWQiOiI3YThiYzJlOC00ZDczLTRhNDQtODZlYi0xZWQ3ZDYzMjU2YmQiLCJ0b2tlbl91c2UiOiJpZCIsImF1dGhfdGltZSI6MTU2MjY0MjU4MywibmFtZSI6IkFlbmd1cyIsImV4cCI6MTU2MjY0NjE4NCwiaWF0IjoxNTYyNjQyNTg0LCJlbWFpbCI6ImFlbmd1c21jY3VsbG91Z2hAaG90bWFpbC5jb20ifQ.j87ur0lUKuP2tKnY4TYcrB0ys-jVACk_-RZ6eVEDxoAGsHvS2b3oJ3rVz6PMaj8q0o0V_YfDK1TxKe1GbsAeWQAp_ZsDeETlm9valZv50hy7b9aGjms81hrrfvPVEox5gyG7ACCScdyxPFEuWzHREfeHXB3ASDqgcdtxqWF60qL9qIk2SS-7i0I6QKRwDsDqUUC5kthW2bW6_mSLhZ_l0DtOYv0TgGywRFvoCY8uDFcIeJZELniBBvEO-ueUouXo2UPS4ODxXk90prg_hUXJli16zehoVPIytD8FJgjHsglg2iRBZzkpuwkqRgPAmB5yCG5f_OqgG11IdfLCoBrjHg\"\n" +
            "    },\n" +
            "    \"multiValueQueryStringParameters\": {\n" +
            "        \"token\": [\n" +
            "            \"eyJraWQiOiI1OE1cL0w5SjhJQm5yVnZieEVwTFFydVJKeFBmcXdDRWNuTkNrWjFLWE96Zz0iLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiIwYTEwOTI5OC01YjhlLTQ4MmEtOTEzOC00ZGNkMjgwMmVjNTMiLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiaXNzIjoiaHR0cHM6XC9cL2NvZ25pdG8taWRwLnVzLXdlc3QtMi5hbWF6b25hd3MuY29tXC91cy13ZXN0LTJfRnJIMFVkck56IiwiY29nbml0bzp1c2VybmFtZSI6IjBhMTA5Mjk4LTViOGUtNDgyYS05MTM4LTRkY2QyODAyZWM1MyIsImN1c3RvbTpmYW1pbHlOYW1lIjoiTWNDdWxsb3VnaCIsImF1ZCI6Ijc1NWY1ZDBlbHNnNWllOTYxMTY1NDBxbTN1IiwiZXZlbnRfaWQiOiI3YThiYzJlOC00ZDczLTRhNDQtODZlYi0xZWQ3ZDYzMjU2YmQiLCJ0b2tlbl91c2UiOiJpZCIsImF1dGhfdGltZSI6MTU2MjY0MjU4MywibmFtZSI6IkFlbmd1cyIsImV4cCI6MTU2MjY0NjE4NCwiaWF0IjoxNTYyNjQyNTg0LCJlbWFpbCI6ImFlbmd1c21jY3VsbG91Z2hAaG90bWFpbC5jb20ifQ.j87ur0lUKuP2tKnY4TYcrB0ys-jVACk_-RZ6eVEDxoAGsHvS2b3oJ3rVz6PMaj8q0o0V_YfDK1TxKe1GbsAeWQAp_ZsDeETlm9valZv50hy7b9aGjms81hrrfvPVEox5gyG7ACCScdyxPFEuWzHREfeHXB3ASDqgcdtxqWF60qL9qIk2SS-7i0I6QKRwDsDqUUC5kthW2bW6_mSLhZ_l0DtOYv0TgGywRFvoCY8uDFcIeJZELniBBvEO-ueUouXo2UPS4ODxXk90prg_hUXJli16zehoVPIytD8FJgjHsglg2iRBZzkpuwkqRgPAmB5yCG5f_OqgG11IdfLCoBrjHg\"\n" +
            "        ]\n" +
            "    },\n" +
            "    \"requestContext\": {\n" +
            "        \"routeKey\": \"$connect\",\n" +
            "        \"authorizer\": {\n" +
            "            \"principalId\": \"0a109298-5b8e-482a-9138-4dcd2802ec53\",\n" +
            "            \"cognito:username\": \"" + USERNAME + "\",\n" +
            "            \"integrationLatency\": 3639\n" +
            "        },\n" +
            "        \"messageId\": null,\n" +
            "        \"eventType\": \""+eventType+"\",\n" +
            "        \"extendedRequestId\": \"cih0GGa4PHcFygQ=\",\n" +
            "        \"requestTime\": \"09/Jul/2019:04:22:24 +0000\",\n" +
            "        \"messageDirection\": \"IN\",\n" +
            "        \"stage\": \"staging\",\n" +
            "        \"connectedAt\": 1562646144658,\n" +
            "        \"requestTimeEpoch\": 1562646144659,\n" +
            "        \"identity\": {\n" +
            "            \"cognitoIdentityPoolId\": null,\n" +
            "            \"cognitoIdentityId\": null,\n" +
            "            \"principalOrgId\": null,\n" +
            "            \"cognitoAuthenticationType\": null,\n" +
            "            \"userArn\": null,\n" +
            "            \"userAgent\": null,\n" +
            "            \"accountId\": null,\n" +
            "            \"caller\": null,\n" +
            "            \"sourceIp\": \"64.180.10.247\",\n" +
            "            \"accessKey\": null,\n" +
            "            \"cognitoAuthenticationProvider\": null,\n" +
            "            \"user\": null\n" +
            "        },\n" +
            "        \"requestId\": \"cih0GGa4PHcFygQ=\",\n" +
            "        \"domainName\": \"c4at2w51lg.execute-api.us-west-2.amazonaws.com\",\n" +
            "        \"connectionId\": \"" + CONNECTION_ID + "\",\n" +

            "        \"apiId\": \"c4at2w51lg\"\n" +
            "    },\n" +

            " \"body\": \"{\\\"action\\\":\\\"" + ACTION + "\\\", \\\"message\\\":" + MESSAGE +"}\"," +
            "    \"isBase64Encoded\": false\n" +
            "}\n";

    @Test
     void testParseRequest() throws Exception {

        ConnectHandler connect = new ConnectHandler();
        DisconnectHandler disconnect = new DisconnectHandler();
        MessageHandler message = new MessageHandler();
        WebSocketService wsService = new WebSocketService(connect, disconnect, message);

        InputStream is = new ByteArrayInputStream(sampleRequest.getBytes());
        WebSocketService.WebSocketRequestContext context = wsService.parseRequest(is);
        assertEquals(USERNAME, context.getUsername());
        assertEquals(PAYLOAD, context.getMessageContent());
        assertEquals(ACTION, context.getAction());
        assertEquals(CONNECTION_ID, context.getConnectionId());
        assertEquals(eventType, context.getEventType());

    }

    @Test
     void testProcessRequest() throws Exception {

        ConnectHandler mockedConnectHandler = mock(ConnectHandler.class);
        DisconnectHandler mockedDisconnectHandler = mock(DisconnectHandler.class);
        MessageHandler mockedMessageHandler = mock(MessageHandler.class);
        WebSocketService wsService = new WebSocketService(mockedConnectHandler, mockedDisconnectHandler, mockedMessageHandler);

        InputStream is = new ByteArrayInputStream(sampleRequest.getBytes());
        WebSocketService.WebSocketRequestContext context = wsService.parseRequest(is);
        wsService.processRequest(context);

        verify(mockedConnectHandler).handleMessage(context);


        eventType = WebSocketRequestType.DISCONNECT;
        String disconnect = sampleRequest.replace(WebSocketRequestType.CONNECT.name(),WebSocketRequestType.DISCONNECT.name());
        is = new ByteArrayInputStream(disconnect.getBytes());
        context = wsService.parseRequest(is);
        wsService.processRequest(context);
        verify(mockedDisconnectHandler).handleMessage(context);


        eventType = WebSocketRequestType.MESSAGE;
        String message = sampleRequest.replace(WebSocketRequestType.CONNECT.name(),WebSocketRequestType.MESSAGE.name());
        is = new ByteArrayInputStream(message.getBytes());
        context = wsService.parseRequest(is);
        wsService.processRequest(context);
        verify(mockedMessageHandler).handleMessage(context);



    }
}
