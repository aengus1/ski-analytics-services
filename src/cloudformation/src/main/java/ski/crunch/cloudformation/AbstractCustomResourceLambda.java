package ski.crunch.cloudformation;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.common.annotations.VisibleForTesting;
import org.apache.log4j.Logger;
import ski.crunch.utils.StackTraceUtil;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.*;


/**
 * Abstract super class for custom resource lambda functions.  Ensures timeout is respected and handles returning
 * response.
 */
public abstract class AbstractCustomResourceLambda implements RequestHandler<Map<String, Object>, Context> {

    private static final Logger LOG = Logger.getLogger(AbstractCustomResourceLambda.class);
    protected AWSCredentialsProvider credentialsProvider;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();


    public AbstractCustomResourceLambda() {
        try {
            this.credentialsProvider = DefaultAWSCredentialsProviderChain.getInstance();
            credentialsProvider.getCredentials();
            LOG.debug("Obtained default aws credentials");
        } catch (AmazonClientException e) {
            LOG.error("Unable to obtain default aws credentials", e);
        }
    }

    public Context execute(Map<String, Object> input, Context context) {
        LOG.info("Input: " + input);
        if (executorService.isShutdown()) {
            executorService = Executors.newSingleThreadExecutor();
        }
        CloudformationRequest request = new CloudformationRequest(input);

        try {
            Runnable r = () -> {
                handle(request, input, context);
            };
            LOG.debug("Submitting job to executor service");

            Future<?> f = executorService.submit(r);
            f.get(context.getRemainingTimeInMillis() - 1000, TimeUnit.MILLISECONDS);

        } catch (final TimeoutException | InterruptedException
                | ExecutionException e) {
            e.printStackTrace();
            LOG.info("FAILURE!", e);
            CloudformationResponse errorResponse = CloudformationResponse.errorResponse(request);
            errorResponse.getData().put("Message", "Execution exception occurred. " + StackTraceUtil.getStackTrace(e));
            sendResponse(errorResponse, context, input);
            // Took too long!
        } finally {
            executorService.shutdown();
        }
        return null;
    }

    @VisibleForTesting
    boolean handle(CloudformationRequest request, Map<String, Object> input, Context context) {
        LOG.info("Executing");
        CloudformationResponse response = null;
        try {
            switch (request.getRequestType()) {
                case CREATE: {
                    LOG.info("Create:");
                    response = doCreate(request);
                    LOG.info("Resource creation complete ");
                    break;
                }
                case UPDATE: {
                    LOG.info("Update:");
                    response = doUpdate(request);
                    LOG.info("Resource update complete");
                    break;
                }
                case DELETE: {
                    LOG.info("Delete:");
                    response = doDelete(request);
                    LOG.info("Resource delete request complete");
                    break;
                }
                default: {
                    LOG.info("Failure: Request type " + request.getRequestType() + " not supported. Use CREATE, UPDATE or DELETE");
                    response = CloudformationResponse.errorResponse(request);
                    response.getData().put("Message", "FAILURE! Request type " + request.getRequestType() + " not supported. Use CREATE, UPDATE or DELETE");
                    break;
                }
            }
        } catch (Exception ex) {
            LOG.info("Failure: exception occurred", ex);
            response = CloudformationResponse.errorResponse(request);
            response.getData().put("Message", "FAILURE! Request type " + request.getRequestType() + " not supported. Use CREATE, UPDATE or DELETE");

        } finally {
            sendResponse(response, context, input);
        }
        return true;
    }

    /**
     * Send a response to CloudFormation regarding progress in creating resource.
     */
    protected Object sendResponse(CloudformationResponse response, Context context, Map<String, Object> input) {

        String responseUrl = (String) input.get("ResponseURL");

        try {

            String output = response.build();
            LOG.info("Response: " + output);

            URL url = new URL(responseUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("PUT");

            try (OutputStreamWriter responseWriter = new OutputStreamWriter(connection.getOutputStream())) {
                responseWriter.write(output);
            } catch(Exception ex){
                ex.printStackTrace();
            }

            context.getLogger().log("Response Code: " + connection.getResponseCode());

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Context handleRequest(Map<String, Object> input, Context context) {
        return execute(input, context);
    }


    public abstract CloudformationResponse doCreate(CloudformationRequest request) throws Exception;

    public abstract CloudformationResponse doUpdate(CloudformationRequest request) throws Exception;

    public abstract CloudformationResponse doDelete(CloudformationRequest request) throws Exception;


}
