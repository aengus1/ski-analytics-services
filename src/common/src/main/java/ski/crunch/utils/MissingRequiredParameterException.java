package ski.crunch.utils;

public class MissingRequiredParameterException extends RuntimeException {

    public MissingRequiredParameterException(String errorMessage) {
        super(errorMessage);
    }

    public MissingRequiredParameterException(String errorMessage, Exception ex) {
        super(errorMessage, ex);
    }
}
