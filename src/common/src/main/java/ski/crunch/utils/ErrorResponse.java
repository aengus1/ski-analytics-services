package ski.crunch.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ErrorResponse {

    private int status;
    private String developerMessage;
    private String userMessage;
    private String errorCode;

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(ErrorResponse.class);
    public ErrorResponse(int status, String developerMessage, String userMessage, String errorCode){
        this.status = status;
        this.developerMessage = developerMessage;
        this.userMessage  = userMessage;
        this.errorCode = errorCode;
    }
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getDeveloperMessage() {
        return developerMessage;
    }

    public void setDeveloperMessage(String developerMessage) {
        this.developerMessage = developerMessage;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String toJSON() {
        try {
            return objectMapper.writeValueAsString(this);
        }catch(JsonProcessingException e){
            logger.error("JSON exception serializing error response");
            return "";
        }
    }

    public static String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
