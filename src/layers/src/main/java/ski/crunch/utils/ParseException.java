package ski.crunch.utils;

public class ParseException extends  Exception{

    public ParseException(String message){
        super(message);
    }

    public ParseException(String message, Exception ex){
        super(message, ex);
    }


}
