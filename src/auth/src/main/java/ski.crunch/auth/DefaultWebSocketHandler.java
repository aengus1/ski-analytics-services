package ski.crunch.auth;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DefaultWebSocketHandler implements RequestStreamHandler {
    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        System.out.println("hit default handler");
        int letter;
        String eventObject = "";

        while ((letter = inputStream.read()) > -1) {
            char inputChar= (char) letter;
            eventObject += inputChar;
        }

        //Passing a custom response as the output string
        String response = "{\n" +
                "    \"statusCode\": 200,\n" +
                "    \"headers\": {\"Content-Type\": \"application/json\"},\n" +
                "    \"body\": \"plain text response\"\n" +
                "}";
        outputStream.write(response.getBytes());

        System.out.println("Input-Event: " + eventObject);
    }
}
