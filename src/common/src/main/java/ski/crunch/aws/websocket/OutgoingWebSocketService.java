package ski.crunch.aws.websocket;

import com.amazonaws.*;
import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.http.*;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;


//import ski.crunch.utils.HttpClientUtil;

public class OutgoingWebSocketService {

    private static final Logger log = Logger.getLogger(OutgoingWebSocketService.class);

//    private HttpClientUtil httpClient;

    /**
     * @param message             String The message to send
     * @param wssEndpoint         String The web socket endpoint: e.g.  https://c4at2w51lg.execute-api.us-west-2.amazonaws.com/staging/@connections/
     * @param connectionId        String The id of the connected socket to send to
     * @param credentialsProvider AWSCredentialsProvider
     * @return int statusCode
     * @throws IOException
     * @throws URISyntaxException
     */
    public int sendMessage(String message, String wssEndpoint, String connectionId, AWSCredentialsProvider credentialsProvider) throws IOException {
        Request<Void> request = createRequest(message, wssEndpoint, connectionId);
        AWS4Signer signer = new AWS4Signer();
        signer.setServiceName(request.getServiceName());
        signer.sign(request, credentialsProvider.getCredentials());

        AmazonHttpClient.RequestExecutionBuilder builder = new AmazonHttpClient(new ClientConfiguration())
                .requestExecutionBuilder()
                .executionContext(new ExecutionContext(true))
                .request(request)
                .errorResponseHandler(new HttpResponseHandler<AmazonClientException>() {
                    @Override
                    public AmazonClientException handle(HttpResponse rsp) throws Exception {
                        return new AmazonClientException(
                                new StringBuilder()
                                        .append(" error sending message ")
                                        .append(wssEndpoint)
                                        .append(rsp.getStatusCode())
                                        .append(rsp.getStatusText())
                                        .append(readError(rsp.getContent())).toString());
                    }

                    @Override
                    public boolean needsConnectionLeftOpen() {
                        return false;
                    }
                });

        Response<HttpResponse> response = builder.execute(new HttpResponseHandler<HttpResponse>() {
            @Override
            public HttpResponse handle(HttpResponse response) {
                return response;
            }

            @Override
            public boolean needsConnectionLeftOpen() {
                return false;
            }
        });

        log.info("status: + " + response.getHttpResponse().getStatusCode());
        log.info("text: + " + response.getHttpResponse().getStatusText());

        return response.getHttpResponse().getStatusCode();

    }


    private Request<Void> createRequest(String message, String wssEndpoint, String connectionId) {

        wssEndpoint = wssEndpoint.endsWith("/") ? wssEndpoint : wssEndpoint + "/";

        String targetUrl = new StringBuilder()
                .append(wssEndpoint.replace("wss:", "https:"))
                .append("@connections/")
                .append(connectionId)
                .toString();

        log.info(" target = " + targetUrl);

        Request<Void> request = new DefaultRequest<>("execute-api");
        request.setHttpMethod(HttpMethodName.POST);
        request.setEndpoint(URI.create(targetUrl));
        request.setContent(new ByteArrayInputStream(message.getBytes(StandardCharsets.UTF_8)));
        return request;
    }


    private static String readError(InputStream inputStream) throws IOException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            bos.write(buffer, 0, length);
        }
        return bos.toString(StandardCharsets.UTF_8.name());
    }
}
