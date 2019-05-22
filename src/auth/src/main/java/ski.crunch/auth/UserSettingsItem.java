package ski.crunch.auth;

import com.amazonaws.services.dynamodbv2.datamodeling.*;

import java.util.List;

@DynamoDBTable(tableName = "UserSettings")  //override this on call
public class UserSettingsItem {

    private String id;
    private String gender;
    private int height;
    private int weight;
    private List<Integer> hrZones;


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

    public void setHrZones(List<Integer> hrZones) {
        this.hrZones = hrZones;
    }


}

