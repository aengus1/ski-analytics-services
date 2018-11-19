package ski.crunch.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import ski.crunch.activity.ActivityService;

public class ErrorResponse {

    private int status;
    private String developerMessage;
    private String userMessage;
    private String errorCode;

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger LOG  = Logger.getLogger(ActivityService.class);

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
            LOG.error("JSON exception serializing error response");
            return "";
        }
    }
}
