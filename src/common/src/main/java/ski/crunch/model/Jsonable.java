package ski.crunch.model;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface Jsonable {

    public String toJsonString() throws JsonProcessingException;

    //public JsonNode toJson() throws JsonProcessingException;
}
