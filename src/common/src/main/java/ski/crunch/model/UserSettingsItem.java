package ski.crunch.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@DynamoDBTable(tableName = "UserSettings")  //override this on call
public class UserSettingsItem implements Jsonable {

    private String id;
    private String gender;
    private int height;
    private int weight;
    private List<Integer> hrZones;
    private String connectionId;

    private Set<String> activityTypes;
    private Set<String> tags;
    private Set<String> devices;


    @DynamoDBHashKey(attributeName = "id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    @DynamoDBAttribute(attributeName = "height")
    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @DynamoDBAttribute(attributeName = "weight")
    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }


    @DynamoDBAttribute(attributeName = "gender")
    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }


    @DynamoDBAttribute(attributeName = "hrZones")
    public List<Integer> getHrZones() {
        return hrZones;
    }

    @DynamoDBAttribute(attributeName = "connectionId")
    public String getConnectionId() {
        return connectionId;
    }

    public void setHrZones(List<Integer> hrZones) {
        this.hrZones = hrZones;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    @DynamoDBAttribute(attributeName = "activityTypes")
    public Set<String> getActivityTypes() {
        return activityTypes;
    }


    public void setActivityTypes(Set<String> activityTypes) {
        this.activityTypes = activityTypes;
    }

    @DynamoDBAttribute(attributeName = "tags")
    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    @DynamoDBAttribute(attributeName = "devices")
    public Set<String> getDevices() {
        return devices;
    }

    public void setDevices(Set<String> devices) {
        this.devices = devices;
    }

    public void addDevice(String device) {
        if (this.devices == null) {
            this.devices = new HashSet<String>();
        }
        this.devices.add(device);
    }

    public String toJsonString() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(this);
    }


}

