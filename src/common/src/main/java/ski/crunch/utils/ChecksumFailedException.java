package ski.crunch.utils;

public class ChecksumFailedException extends  Exception {

    public ChecksumFailedException() {
        super();
    }

    public ChecksumFailedException(String message) {
        super(message);
    }
}
