package ski.crunch.cloudformation.rockset.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ski.crunch.cloudformation.ResourceProperties;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class S3DataSource extends ResourceProperties implements DataSource {

    private String s3Prefix;
    private String s3Pattern;
    private String s3Bucket;

    public String getS3Prefix() {
        return s3Prefix;
    }

    public String getS3Pattern() {
        return s3Pattern;
    }

    public String getS3Bucket() {
        return s3Bucket;
    }

    @Override
    public JsonNode toJson(ObjectMapper objectMapper) {
        ObjectNode properties = objectMapper.createObjectNode();
        properties.put("prefix", s3Prefix);
        properties.put("pattern", s3Pattern);
        properties.put("bucket", s3Bucket);
        return properties;
    }

    @Override
    public void parse(Map<String, Object> input ) {
        List<String> requiredParams = Stream.of(
                "S3Prefix",
                "S3Pattern",
                "S3Bucket"
        ).collect(Collectors.toList());
        checkRequiredParameters(input, requiredParams);
        s3Prefix = (String) input.get("S3Prefix");
        s3Pattern = (String) input.get("S3Pattern");
        s3Bucket = (String) input.get("S3Bucket");
    }


    @Override
    public RocksetIntegrationType getIntegrationType() {
        return RocksetIntegrationType.s3;
    }
}
