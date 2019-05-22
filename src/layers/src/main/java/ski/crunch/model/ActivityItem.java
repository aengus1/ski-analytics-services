package ski.crunch.model;

import com.amazonaws.services.dynamodbv2.datamodeling.*;

import java.util.Date;

/**
 * Created by aengusmccullough on 2018-09-17.
 */

@DynamoDBTable(tableName="ActivityTable")  //override this on call
public class ActivityItem {

    private String id;
    private Date dateOfUpload;
    private S3Link rawActivity;
    private String sourceIp;

    private String rawFileType;
    private String userAgent;
    private String userId;
    //possible states: PENDING, PROCESSED, ERROR
    private String status;  //TODO convert to enum when have time to figure out dynamo mapping



    @DynamoDBHashKey(attributeName="id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @DynamoDBRangeKey(attributeName="date")
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

    @DynamoDBAttribute(attributeName = "status")
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @DynamoDBAttribute(attributeName = "rawFileType")
    public String getRawFileType() {
        return rawFileType;
    }

    public void setRawFileType(String rawFileType) {
        this.rawFileType = rawFileType;
    }

}
