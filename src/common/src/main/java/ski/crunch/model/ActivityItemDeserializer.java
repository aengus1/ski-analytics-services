package ski.crunch.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.S3Link;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ActivityItemDeserializer extends StdDeserializer<ActivityItem> {

    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    public static final Logger logger = LoggerFactory.getLogger(ActivityItemDeserializer.class);

    public ActivityItemDeserializer() {
        this(null);
    }

    public ActivityItemDeserializer(Class<?> vc) {
        super(vc);

    }

    @Override
    public ActivityItem deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        DynamoDBMapper mapper = (DynamoDBMapper) ctxt.findInjectableValue("mapper", null, null);
        String region = (String) ctxt.findInjectableValue("region", null, null);
        String rawBucket = (String) ctxt.findInjectableValue("raw_bucket", null, null);
        String procBucket = (String) ctxt.findInjectableValue("proc_bucket", null, null);
        if (mapper == null || region == null || rawBucket == null || procBucket == null) {
            logger.warn("One or more injectable values missing.  This may affect deserialization of S3Links");
        }

        ActivityItem activityItem = new ActivityItem();
        try {
            JsonNode node = p.getCodec().readTree(p);
            if (node.get("id") == null || node.get("cognitoId") == null) {
                throw new IOException("Missing key");
            }
            String id = node.get("id").textValue();
            activityItem.setId(id);

            String cognitoId = node.get("cognitoId").textValue();
            activityItem.setCognitoId(cognitoId);

            if (node.get("date") != null) {
                Date date = sdf.parse(node.get("date").asText());
                activityItem.setDateOfUpload(date);
            }

            S3Link rawLink = createS3Link("rawActivity", node, ctxt, region, mapper, activityItem);

            if (rawLink == null) {
                logger.warn("s3 link to raw activity not set");
            } else {
                activityItem.setRawActivity(rawLink);
            }

            S3Link procLink = createS3Link("processedActivity", node, ctxt, region, mapper, activityItem);

            if (procLink == null) {
                logger.warn("s3 link to processed activity not set");
            } else {
                activityItem.setProcessedActivity(procLink);
            }

            if (node.get("sourceIp") != null) {
                String sourceIp = node.get("sourceIp").asText();
                activityItem.setSourceIp(sourceIp);
            }
            if (node.get("userAgent") != null) {
                String userAgent = node.get("userAgent").asText();
                activityItem.setUserAgent(userAgent);
            }
            if (node.get("userId") != null) {
                String userId = node.get("userId").asText();
                activityItem.setUserId(userId);
            }
            if (node.get("status") != null) {
                ActivityItem.Status status = ActivityItem.Status.valueOf(node.get("status").asText());
                activityItem.setStatus(status);
            }
            if (node.get("rawFileType") != null) {
                String rawFileType = node.get("rawFileType").asText();
                activityItem.setRawFileType(rawFileType);
            }
            if (node.get("timeOfDay") != null) {
                Integer timeOfDay = node.get("timeOfDay").asInt();
                activityItem.setTimeOfDay(timeOfDay);
            }
            if (node.get("activityType") != null) {
                String activityType = node.get("activityType").asText();
                activityItem.setActivityType(activityType);
            }
            if (node.get("activitySubType") != null) {
                String activitySubType = node.get("activitySubType").asText();
                activityItem.setActivitySubType(activitySubType);
            }
            if (node.get("activityDate") != null) {
                Date activityDate = sdf.parse(node.get("activityDate").asText());
                activityItem.setActivityDate(activityDate);
            }
            if (node.get("device") != null) {
                String device = node.get("device").asText();
                activityItem.setDevice(device);
            }
            if (node.get("distance") != null) {
                Double distance = node.get("distance").asDouble();
                activityItem.setDistance(distance);
            }
            if (node.get("duration") != null) {
                Double duration = node.get("duration").asDouble();
                activityItem.setDuration(duration);
            }
            if (node.get("avHr") != null) {
                Integer avHr = node.get("avHr").asInt();
                activityItem.setAvHr(avHr);
            }
            if (node.get("maxHr") != null) {
                Integer maxHr = node.get("maxHr").asInt();
                activityItem.setMaxHr(maxHr);
            }
            if (node.get("avSpeed") != null) {
                Double avSpeed = node.get("avSpeed").asDouble();
                activityItem.setAvSpeed(avSpeed);
            }
            if (node.get("maxSpeed") != null) {
                Double maxSpeed = node.get("maxSpeed").asDouble();
                activityItem.setMaxSpeed(maxSpeed);
            }
            if (node.get("ascent") != null) {
                Double ascent = node.get("ascent").asDouble();
                activityItem.setAscent(ascent);
            }
            if (node.get("descent") != null) {
                Double descent = node.get("descent").asDouble();
                activityItem.setDescent(descent);
            }
            if (node.get("notes") != null) {
                String notes = node.get("notes").asText();
                activityItem.setNotes(notes);
            }
            if (node.get("tags") != null) {
                final List<String> tags = new ArrayList<>();
                ArrayNode arrayNode = (ArrayNode) node.get("tags");
                arrayNode.iterator().forEachRemaining(t -> {
                    tags.add(t.textValue());
                });

                activityItem.setTags(tags);
            }
            if (node.get("lastUpdateTimestamp") != null) {
                Date lastUpdateTs = sdf.parse(node.get("lastUpdateTimestamp").asText());
                activityItem.setLastUpdateTimestamp(lastUpdateTs);
            }

            return activityItem;
        } catch (ParseException ex) {
            ex.printStackTrace();
            logger.error("deserialization exception", ex);
            return null;
        }
    }

    private S3Link createS3Link(String nodeName, JsonNode node, DeserializationContext ctxt, String region,
                                DynamoDBMapper mapper, ActivityItem activityItem) throws IOException {
        if (node.get(nodeName) != null && ctxt.findInjectableValue("mapper", null, null) != null) {
            String processedActivityLink = node.get(nodeName).asText().replaceAll("\\\\\"", "\"");

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(processedActivityLink);
            //System.out.println("key = " + jsonNode.get("s3").get("key").asText());
            //System.out.println("bucket = " + jsonNode.get("s3").get("bucket").asText());
            return mapper.createS3Link(region, jsonNode.get("s3").get("bucket").asText(), jsonNode.get("s3").get("key").asText());
        }
        return null;
    }
}
