package ski.crunch.aws.websocket;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import ski.crunch.aws.AbstractAwsTest;

import java.io.IOException;
import java.net.URISyntaxException;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class OutgoingWebSocketServiceTest extends AbstractAwsTest {

    private OutgoingWebSocketService outgoingWebSocketService;

    private static final String testPayload = "{\n" +
            "\t\"message\": \"successful connection\",\n" +
            "\t\"status\": \"OK\"\n" +
            "}";

    //TODO -> make this an integration test that first opens a socket and tests sending / receiving
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
