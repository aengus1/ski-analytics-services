package ski.crunch.auth;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

//public class DefaultWebSocketHandler implements RequestHandler<Map<String, Object>, String> {
//    @Override
//    public String handleRequest(Map<String, Object> input, Context context) {
//        String connId = null;
//        for (String s : input.keySet()) {
//            System.out.println("key = " + s);
//
//            if(s.equals("requestContext")){
//                Map res = (Map) input.get("requestContext");
//                for (Object o : res.keySet()) {
//                    System.out.println("r = " + o);
//                    if(o.equals("connectionId")){
//                        connId = (String) res.get("connectionId");
//                    }
//                }
//            }
//        }
//        System.out.println("connection id = " + connId);
//        return null;
//    }

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
