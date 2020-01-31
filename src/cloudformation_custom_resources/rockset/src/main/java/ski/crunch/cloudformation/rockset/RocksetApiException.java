package ski.crunch.cloudformation.rockset;

public class RocksetApiException extends RuntimeException {

    public RocksetApiException(String errorMessage, Exception ex) {
        super(errorMessage, ex);
    }

    public RocksetApiException(String errorMessage) {
        super(errorMessage);
    }
}
