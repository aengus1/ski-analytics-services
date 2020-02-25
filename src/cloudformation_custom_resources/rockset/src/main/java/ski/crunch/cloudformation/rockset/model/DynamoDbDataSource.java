package ski.crunch.cloudformation.rockset.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ski.crunch.cloudformation.ResourceProperties;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DynamoDbDataSource extends ResourceProperties implements DataSource {

    private String dynamoDbAwsRegion;
    private String dynamoDbTableName;
    private long dynamoDbRcu;



    @Override
    public JsonNode toJson(ObjectMapper objectMapper) {
        ObjectNode properties = objectMapper.createObjectNode();
        properties.put("aws_region", dynamoDbAwsRegion);
        properties.put("table_name", dynamoDbTableName);
        properties.put("rcu", dynamoDbRcu);
        return properties;
    }

    @Override
    public void parse(Map<String, Object> input ) {
        List<String> requiredParams = Stream.of(
                "DynamoDbAwsRegion",
                "DynamoDbTableName",
                "DynamoDbRcu"
        ).collect(Collectors.toList());
        checkRequiredParameters(input, requiredParams);
        dynamoDbAwsRegion = (String) input.get("DynamoDbAwsRegion");
        dynamoDbTableName = (String) input.get("DynamoDbTableName");
        dynamoDbRcu =  Long.parseLong((String) input.get("DynamoDbRcu"));
    }

    @Override
    public RocksetIntegrationType getIntegrationType() {
        return RocksetIntegrationType.dynamodb;
    }
}
