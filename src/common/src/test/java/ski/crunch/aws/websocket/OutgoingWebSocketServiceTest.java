package ski.crunch.aws.websocket;

import org.junit.jupiter.api.*;
import ski.crunch.aws.testhelpers.AbstractAwsTest;
import ski.crunch.aws.testhelpers.IntegrationTestHelper;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Integration test.  Generates a WS client and tests two-way communication between client and server
 */
@Disabled()
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class OutgoingWebSocketServiceTest extends AbstractAwsTest {

    private OutgoingWebSocketService outgoingWebSocketService;
    private IntegrationTestHelper helper;


    @BeforeAll()
    public  void setUp() throws IOException{
        helper = new IntegrationTestHelper();
        String key = helper.signUpAndRetrieveAccessToken();
        System.out.println("key = " + key);
    }

    @AfterAll()
    public void destroy() throws IOException {
        helper.destroySignupUser();
    }


    private static final String testPayload = "{\n" +
            "\t\"message\": \"successful connection\",\n" +
            "\t\"status\": \"OK\"\n" +
            "}";

    //TODO -> make this an integration test that first opens a socket and tests sending / receiving
    // create user
    // obtain jwt token
    // use a ws lib to open connection
    // send message from client to server -> acknowledge receipt
    // send message from server to client -> acknowledge receipt
    @Disabled()
    @Test()
    public void testSendMessage() throws IOException, URISyntaxException {
        outgoingWebSocketService = new OutgoingWebSocketService();


        outgoingWebSocketService.sendMessage(testPayload,
                "wss://in6xuk1co5.execute-api.ca-central-1.amazonaws.com/staging/",
                "eWBI8coYYosCHrw=",
                super.credentialsProvider );
    }

}
