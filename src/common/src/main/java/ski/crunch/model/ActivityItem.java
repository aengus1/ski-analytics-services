package ski.crunch.model;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import ski.crunch.utils.Jsonable;

import java.util.Date;
import java.util.Set;

/**
 * Created by aengusmccullough on 2018-09-17.
 */

@DynamoDBTable(tableName="ActivityTable")  //override this on call
public class ActivityItem implements Jsonable {

    private String id;
    private Date dateOfUpload;
    private S3Link rawActivity;
    private S3Link processedActivity;
    private String sourceIp;

    private String rawFileType;
    private String userAgent;
    private String userId;
    private String cognitoId;
    //possible states: PENDING, PROCESSED, ERROR
    private Status status;
    private String activityType;
    private String activitySubType;
    private Date activityDate;
    private Integer timeOfDay;
    private String device;
    private Double distance;
    private Double duration;
    private Integer avHr;
    private Integer maxHr;
    private Double avSpeed;
    private Double maxSpeed;
    private Double ascent;
    private Double descent;
    private String notes;
    private Set<String> tags;
    private Date lastUpdateTimestamp;

    public enum Status { PENDING, PROCESSED, ERROR, COMPLETE}

    @DynamoDBRangeKey(attributeName="id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @DynamoDBHashKey(attributeName = "cognitoId")
    public String getCognitoId() {
        return cognitoId;
    }

    public void setCognitoId(String cognitoId) {
        this.cognitoId = cognitoId;
    }

    @DynamoDBAttribute(attributeName = "date")
    public Date getDateOfUpload() {
        return dateOfUpload;
    }

    public void setDateOfUpload(Date dateOfUpload) {
        this.dateOfUpload = dateOfUpload;
    }

    @DynamoDBAttribute(attributeName = "rawActivity")
    public S3Link getRawActivity() {
        return rawActivity;
    }

    public void setRawActivity(S3Link activity) {
        this.rawActivity = activity;
    }

    @DynamoDBAttribute(attributeName = "processedActivity")
    public S3Link getProcessedActivity() {
        return processedActivity;
    }

    public void setProcessedActivity(S3Link activity) {
        this.processedActivity = activity;
    }

    @DynamoDBAttribute(attributeName = "sourceIp")
    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    @DynamoDBAttribute(attributeName = "userAgent")
    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }


    @DynamoDBAttribute(attributeName = "userId")
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


    @DynamoDBTypeConvertedEnum
    @DynamoDBAttribute(attributeName = "status")
    public Status getStatus() {
        return status;
    }

    @DynamoDBTypeConvertedEnum
    public void setStatus(Status status) {
        this.status = status;
    }

    @DynamoDBAttribute(attributeName = "rawFileType")
    public String getRawFileType() {
        return rawFileType;
    }

    public void setRawFileType(String rawFileType) {
        this.rawFileType = rawFileType;
    }

    // additional fields for search

    @DynamoDBAttribute(attributeName = "timeOfDay")
    public Integer getTimeOfDay() {
        return timeOfDay;
    }

    public void setTimeOfDay(Integer timeOfDay) {
        this.timeOfDay = timeOfDay;
    }

    @DynamoDBAttribute(attributeName = "activityType")
    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    @DynamoDBAttribute(attributeName = "activitySubType")
    public String getActivitySubType() {
        return activitySubType;
    }

    public void setActivitySubType(String activitySubType) {
        this.activitySubType = activitySubType;
    }

    @DynamoDBAttribute(attributeName = "activityDate")
    public Date getActivityDate() {
        return activityDate;
    }

    public void setActivityDate(Date activityDate) {
        this.activityDate = activityDate;
    }

    @DynamoDBAttribute(attributeName = "device")
    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    @DynamoDBAttribute(attributeName = "distance")
    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    @DynamoDBAttribute(attributeName = "duration")
    public Double getDuration() {
        return duration;
    }

    public void setDuration(Double duration) {
        this.duration = duration;
    }

    @DynamoDBAttribute(attributeName = "avHr")
    public Integer getAvHr() {
        return avHr;
    }

    public void setAvHr(Integer avHr) {
        this.avHr = avHr;
    }

    @DynamoDBAttribute(attributeName = "maxHr")
    public Integer getMaxHr() {
        return maxHr;
    }

    public void setMaxHr(Integer maxHr) {
        this.maxHr = maxHr;
    }

    @DynamoDBAttribute(attributeName = "avSpeed")
    public Double getAvSpeed() {
        return avSpeed;
    }

    public void setAvSpeed(Double avSpeed) {
        this.avSpeed = avSpeed;
    }

    @DynamoDBAttribute(attributeName = "maxSpeed")
    public Double getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(Double maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    @DynamoDBAttribute(attributeName = "ascent")
    public Double getAscent() {
        return ascent;
    }

    public void setAscent(Double ascent) {
        this.ascent = ascent;
    }

    @DynamoDBAttribute(attributeName = "descent")
    public Double getDescent() {
        return descent;
    }

    public void setDescent(Double descent) {
        this.descent = descent;
    }

    // user editable fields
    @DynamoDBAttribute(attributeName = "notes")
    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @DynamoDBAttribute(attributeName = "tags")
    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    @DynamoDBAttribute(attributeName = "lastUpdateTimestamp")
    public Date getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    public void setLastUpdateTimestamp(Date lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }


}
