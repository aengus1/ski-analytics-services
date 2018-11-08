package com.serverless;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;

/**
 * Created by aengusmccullough on 2018-09-19.
 */
public class DynamoDBService {

    AmazonDynamoDB client;
    DynamoDBMapper mapper;
    ActivityItem item;
    DynamoDBMapperConfig config;

    public DynamoDBService(String region, String tableName) {
        this.client = AmazonDynamoDBClientBuilder.standard().build();
        config = new DynamoDBMapperConfig.Builder().withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableName))
                .build();
        this.mapper = new DynamoDBMapper(client, config);
    }


    public DynamoDBMapper getMapper(){
        return this.mapper;
    }
}
