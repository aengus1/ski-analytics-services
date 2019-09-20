package ski.crunch.cloudformation;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import ski.crunch.cloudformation.services.RocksetService;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.*;

public class RocksetIntegrationHandler implements RequestHandler<Map<String, Object>, Context> {

    private static final Logger LOG = Logger.getLogger(RocksetIntegrationHandler.class);

    private RocksetService rocksetService;
    private AWSCredentialsProvider credentialsProvider;


    public RocksetIntegrationHandler() {
        try {
            this.credentialsProvider = DefaultAWSCredentialsProviderChain.getInstance();
            credentialsProvider.getCredentials();
            LOG.debug("Obtained default aws credentials");
        } catch (AmazonClientException e) {
            LOG.error("Unable to obtain default aws credentials", e);
        }
        //this.rocksetService = new RocksetService(credentialsProvider);

    }

    @Override
    public Context handleRequest(Map<String, Object> input, Context context) {

        LOG.info("Input: " + input);

        final String requestType = (String) input.get("RequestType");
        Map<String,String> parameters = (Map<String,String>) input.get("ResourceProperties");
        for (String s : parameters.keySet()) {
            System.out.println("key: " + s);
        }

        for (String s : input.keySet()) {
            System.out.println("key: " + s);
        }

        ExecutorService service = Executors.newSingleThreadExecutor();
        JSONObject responseData = new JSONObject();
        try {
            if (requestType == null) {
                throw new RuntimeException();
            }

            Runnable r = new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(10000);
                    } catch (final InterruptedException e) {
                        // empty block
                    }
                    if (requestType.equalsIgnoreCase("Create")) {
                        LOG.info("CREATE!");
                        // Put your custom create logic here
                        responseData.put("Message", "Resource creation successful!");
                        sendResponse(input, context, "SUCCESS", responseData);
                    } else if (requestType.equalsIgnoreCase("Update")) {
                        LOG.info("UDPATE!");
                        // Put your custom update logic here
                        responseData.put("Message", "Resource update successful!");
                        sendResponse(input, context, "SUCCESS", responseData);
                    } else if (requestType.equalsIgnoreCase("Delete")) {
                        LOG.info("DELETE!");
                        // Put your custom delete logic here
                        responseData.put("Message", "Resource deletion successful!");
                        sendResponse(input, context, "SUCCESS", responseData);
                    } else {
                        LOG.info("FAILURE!");
                        sendResponse(input, context, "FAILED", responseData);
                    }
                }
            };
            Future<?> f = service.submit(r);
            f.get(context.getRemainingTimeInMillis() - 1000, TimeUnit.MILLISECONDS);
        } catch (final TimeoutException | InterruptedException
                | ExecutionException e) {
            LOG.info("FAILURE!");
            sendResponse(input, context, "FAILED", responseData);
            // Took too long!
        } finally {
            service.shutdown();
        }
        return null;
    }

    /**
     * Send a response to CloudFormation regarding progress in creating resource.
     */
    public final Object sendResponse(
            final Map<String, Object> input,
            final Context context,
            final String responseStatus,
            JSONObject responseData) {

        String responseUrl = (String) input.get("ResponseURL");
        context.getLogger().log("ResponseURL: " + responseUrl);

        URL url;
        try {
            url = new URL(responseUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("PUT");

            JSONObject responseBody = new JSONObject();
            responseBody.put("Status", responseStatus);
            responseBody.put("PhysicalResourceId", context.getLogStreamName());
            responseBody.put("StackId", input.get("StackId"));
            responseBody.put("RequestId", input.get("RequestId"));
            responseBody.put("LogicalResourceId", input.get("LogicalResourceId"));
            responseBody.put("Data", responseData);

            OutputStreamWriter response = new OutputStreamWriter(connection.getOutputStream());
            response.write(responseBody.toString());
            response.close();
            context.getLogger().log("Response Code: " + connection.getResponseCode());

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

}

