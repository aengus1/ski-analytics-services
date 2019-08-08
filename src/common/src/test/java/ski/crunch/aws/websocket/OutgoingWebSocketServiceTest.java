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

    @Disabled()
    @Test()
    public void testSendMessage() throws IOException, URISyntaxException {
        outgoingWebSocketService = new OutgoingWebSocketService();


        outgoingWebSocketService.sendMessage(testPayload,
                "wss://c4at2w51lg.execute-api.us-west-2.amazonaws.com/staging/",
                "d7WaZdT-PHcCGMg=",
                super.credentialsProvider );
    }

}
