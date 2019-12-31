package ski.crunch.cloudformation.rockset.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public interface DataSource {

    void parse(Map<String, Object> input);
    JsonNode toJson(ObjectMapper objectMapper);

    RocksetIntegrationType getIntegrationType();
}
