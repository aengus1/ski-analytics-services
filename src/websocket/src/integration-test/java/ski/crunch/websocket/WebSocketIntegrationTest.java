package ski.crunch.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.log4j.Logger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.junit.jupiter.api.*;
import ski.crunch.services.OutgoingWebSocketService;
import ski.crunch.testhelpers.AbstractAwsTest;
import ski.crunch.testhelpers.IntegrationTestHelper;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test.  Generates a WS client and tests two-way communication between client and server
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
 class WebSocketIntegrationTest extends AbstractAwsTest {
    private static final Logger LOG = Logger.getLogger(WebSocketIntegrationTest.class);
    private   String endpoint;
    private String userId;
    private String connectionId;

    private TestClient testClient;
    private IntegrationTestHelper helper;

    private String jwtToken;

    @BeforeAll()
      void setUp() throws IOException{
        helper = new IntegrationTestHelper();
        this.userId = helper.signup().orElseThrow(() -> new RuntimeException("Error occurred signing up"));
        this.jwtToken = helper.retrieveAccessToken();

        helper.insertUserSettings(userId);  // TODO -> check if this is actually needed
        this.endpoint = helper.getWebsocketEndpoint();
        System.out.println("key = " + jwtToken);
    }

    @AfterAll()
     void destroy() {
        helper.destroySignupUser();
        helper.removeUserSettings(userId);
    }


    private static final String testPayload = "{\n" +
            "\t\"message\": \"successful connection\",\n" +
            "\t\"status\": \"OK\"\n" +
            "}";

    @Test()
     void testInitiateConnection() throws  URISyntaxException, InterruptedException {

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", jwtToken);
        System.out.println("CONNECTION TARGET = " + new URI(endpoint+"?token=" + jwtToken));
        testClient = new TestClient(new URI(endpoint+"?token=" + jwtToken), headers );
        testClient.connectBlocking();
        //Thread.currentThread().sleep(5000l);
        assertTrue(testClient.opened);
    }

    @Test()
     void testSendMessageToServer() throws JsonProcessingException, InterruptedException{

        String testMessage = buildTestMessage();
        testClient.send(testMessage);
        Thread.currentThread().sleep(2000l);

        this.connectionId = helper.getUsersWebsocketConnectionId(this.userId);
        assertNotNull(connectionId);
    }

    @Test()
    public void testSendResponseToClient() throws IOException, InterruptedException{
        String outgoingMessage = buildTestMessage();
        OutgoingWebSocketService outgoingWebSocketService = new OutgoingWebSocketService();
        outgoingWebSocketService.sendMessage(outgoingMessage, endpoint, connectionId, credentialsProvider );

        Thread.currentThread().sleep(2000l);
        String received = testClient.getReceivedMessage();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(received);

        assertEquals("https://crunch.ski/activity/id123", root.at("/message/url").asText());
        assertEquals("id123", root.at("/message/key").asText());
        assertEquals("message payload", root.at("/message/payload").asText());

    }


    private String buildTestMessage() throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode root = objectMapper.createObjectNode();
        ObjectNode messageBody = objectMapper.createObjectNode();
        messageBody.put("key", "id123");
        messageBody.put("payload", "message payload");
        messageBody.put("url", "https://crunch.ski/activity/id123");

        root.put("action", "action test");
        root.set("message", messageBody);

        String jsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
        System.out.println(jsonString);
        return jsonString;
    }



   static class TestClient extends WebSocketClient {

        boolean opened = false;
        String receivedMessage = null;


         TestClient( URI serverUri, Map<String, String> httpHeaders ) {
            super(serverUri, httpHeaders);
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            LOG.info("connection opened:  :" + handshakedata.getHttpStatusMessage() + " "  +
                    handshakedata.getHttpStatus());
            opened = true;

        }

        @Override
        public void onMessage(String message) {
            System.out.println("message received" + message);
            receivedMessage = message;
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {

        }

        @Override
        public void onError(Exception ex) {

        }

        public String getReceivedMessage() {
            return receivedMessage;
        }
    }
}
