package ski.crunch.model;

import com.amazonaws.services.dynamodbv2.datamodeling.S3Link;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;

public class ActivityItemSerializer extends StdSerializer<ActivityItem> {

    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    public static final Logger logger = LoggerFactory.getLogger(ActivityItemSerializer.class);

    public ActivityItemSerializer() {
        this(null);
    }

    public ActivityItemSerializer(Class<?> vc) {
        super((Class<ActivityItem>) vc);

    }

    @Override
    public void serialize(ActivityItem value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("id", value.getId());
        gen.writeStringField("cognitoId", value.getCognitoId());
        gen.writeStringField("date", value.getDateOfUpload() == null ? "" : sdf.format(value.getDateOfUpload()));
        gen.writeObjectField("rawActivity", buildS3Link(value.getRawActivity()));
        gen.writeObjectField("processedActivity", buildS3Link(value.getProcessedActivity()));
        gen.writeStringField("sourceIp", value.getSourceIp() == null ? "" : value.getSourceIp());
        gen.writeStringField("userAgent", value.getUserAgent() == null ? "" : value.getUserAgent());
        gen.writeStringField("userId", value.getUserId() == null ? "" : value.getUserId());
        gen.writeStringField("status", value.getStatus() == null ? "" : value.getStatus().name());
        gen.writeStringField("rawFileType", value.getRawFileType() == null ? "" : value.getRawFileType());
        gen.writeStringField("timeOfDay", value.getTimeOfDay() == null ? "" : sdf.format(value.getTimeOfDay()));
        gen.writeStringField("activityType", value.getActivityType() == null ? "" : value.getActivityType());
        gen.writeStringField("activitySubType", value.getActivitySubType() == null ? "" : value.getActivitySubType());
        gen.writeStringField("activityDate", value.getActivityDate() == null ? "" : sdf.format(value.getActivityDate()));
        gen.writeStringField("device", value.getDevice() == null ? "" : value.getDevice());
        gen.writeNumberField("distance", value.getDistance() == null ? -998 : value.getDistance());
        gen.writeNumberField("duration", value.getDuration() == null ? -998 : value.getDuration());
        gen.writeNumberField("avHr", value.getAvHr() == null ? -998 : value.getAvHr());
        gen.writeNumberField("maxHr", value.getMaxHr() == null ? -998 : value.getMaxHr());
        gen.writeNumberField("avSpeed", value.getAvSpeed() == null ? -998 : value.getAvSpeed());
        gen.writeNumberField("maxSpeed", value.getMaxSpeed() == null ? -998 : value.getMaxSpeed());
        gen.writeNumberField("ascent", value.getAscent() == null ? -998 : value.getAscent());
        gen.writeNumberField("descent", value.getDescent() == null ? -998 : value.getDescent());
        gen.writeStringField("notes", value.getNotes() == null ? "" : value.getNotes());
        if (value.getTags() != null) {
            gen.writeArrayFieldStart("tags");
            for (String s : value.getTags()) {
                gen.writeString(s);
            }
            gen.writeEndArray();
        }
        gen.writeStringField("lastUpdateTimestamp", value.getLastUpdateTimestamp() == null ? "" : sdf.format(value.getLastUpdateTimestamp()));
        gen.writeEndObject();
    }

    private JsonNode buildS3Link(S3Link s3Link) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.createObjectNode();
        JsonNode childNode1 = mapper.createObjectNode();
        ((ObjectNode) childNode1).put("bucket", s3Link == null ? "null" : s3Link.getBucketName());
        ((ObjectNode) childNode1).put("key", s3Link == null ? "null" : s3Link.getKey());
        ((ObjectNode) rootNode).set("s3", childNode1);
        return rootNode;
    }
}
