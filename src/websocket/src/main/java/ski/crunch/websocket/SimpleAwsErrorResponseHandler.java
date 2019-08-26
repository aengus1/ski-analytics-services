package ski.crunch.websocket;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.http.HttpResponse;
import com.amazonaws.http.HttpResponseHandler;

/**
 * Simple exception handler that returns an {@link AmazonServiceException}
 * containing the HTTP status code and status text.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 */
public class SimpleAwsErrorResponseHandler implements HttpResponseHandler<AmazonServiceException> {

    /**
     * See {@link HttpResponseHandler}, method needsConnectionLeftOpen()
     */
    private boolean needsConnectionLeftOpen;

    /**
     * Ctor.
     * @param connectionLeftOpen Should the connection be closed immediately or not?
     */
    public SimpleAwsErrorResponseHandler(boolean connectionLeftOpen) {
        this.needsConnectionLeftOpen = connectionLeftOpen;
    }

    @Override
    public AmazonServiceException handle(HttpResponse response) {
        AmazonServiceException ase = new AmazonServiceException(response.getStatusText());
        ase.setStatusCode(response.getStatusCode());
        return ase;
    }

    @Override
    public boolean needsConnectionLeftOpen() {
        return this.needsConnectionLeftOpen;
    }

}