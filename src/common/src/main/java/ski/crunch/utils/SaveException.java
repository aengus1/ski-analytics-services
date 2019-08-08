package ski.crunch.utils;

public class SaveException extends Exception {

    public SaveException(String message){
        super(message);
    }

    public SaveException(String message, Exception ex){
        super(message, ex);
    }
}
