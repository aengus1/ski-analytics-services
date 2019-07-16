package ski.crunch.activity.service;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import ski.crunch.activity.model.ActivityItem;

/**
 * Created by aengusmccullough on 2018-09-19.
 */
public class DynamoDBService {

    AmazonDynamoDB client;
    DynamoDB dynamo;
    DynamoDBMapper mapper;
    ActivityItem item;
    DynamoDBMapperConfig config;

    public DynamoDBService(String region, String tableName) {
        this.client = AmazonDynamoDBClientBuilder.standard().withRegion(region).build();
        config = new DynamoDBMapperConfig.Builder().withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableName))
                .build();
        this.dynamo = new DynamoDB(client);

        this.mapper = new DynamoDBMapper(client, config);
    }


    /**
     * Use this constructor if you need to use the S3link
     * @param region
     * @param tableName
     * @param credentialsProvider
     */
    public DynamoDBService(String region, String tableName, AWSCredentialsProvider credentialsProvider) {
        this.client = AmazonDynamoDBClientBuilder.standard().withRegion(region).build();
        config = new DynamoDBMapperConfig.Builder().withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableName))
                .build();

        this.mapper = new DynamoDBMapper(client, config, credentialsProvider);
        this.dynamo = new DynamoDB(client);
    }


    public DynamoDBMapper getMapper(){
        return this.mapper;
    }

    public Table getTable(String tableName){
        return this.dynamo.getTable(tableName);
    }


}
