package com.serverless;

import com.amazonaws.services.dynamodbv2.datamodeling.*;

import java.util.Date;

/**
 * Created by aengusmccullough on 2018-09-17.
 */

@DynamoDBTable(tableName="ActivityTable")  //override this on call
public class ActivityItem {

    private String id;
    private Date date;
    private String user_id;
    private S3Link activity;

    @DynamoDBHashKey(attributeName="id")
    public String getId() {
        return id;
    }

    @DynamoDBRangeKey(attributeName="date")
    public Date getDate() {
        return date;
    }

    @DynamoDBAttribute(attributeName="user_id")
    public String getUser_id() {
        return user_id;
    }

    public S3Link getActivity() {
        return activity;
    }




}
