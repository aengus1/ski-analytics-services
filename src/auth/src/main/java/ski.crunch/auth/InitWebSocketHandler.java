package ski.crunch.auth;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class InitWebSocketHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {

        String connId = "";
        for (String s : input.keySet()) {
            System.out.println("key = " + s);

            if(s.equals("requestContext")){
                Map res = (Map) input.get("requestContext");
                for (Object o : res.keySet()) {
                    System.out.println("r = " + o);
                    if(o.equals("connectionId")){
                        connId = (String) res.get("connectionId");
                    }
                }
            }
        }
        System.out.println("connection id = " + connId);
//        LinkedHashMap<String, Object> requestContext = (LinkedHashMap<String, Object>) input.get("requestContext");
//String connId = "OK";
//        for (String s : input.keySet()) {
//            System.out.println(requestContext + ":" );
//            try {
//                System.out.println((String) input.get(s));
//                //connId = (String) requestContext.get("connectionId");
//            }catch (Exception ex){
//                // ex.printStackTrace();
//            }
//        }

        // TODO -> insert connection id into user table
        Map<String, String> res = new HashMap<>();

        return new ApiGatewayResponse(200, connId,res, false);



    }
}
