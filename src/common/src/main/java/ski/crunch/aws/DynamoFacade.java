package ski.crunch.aws;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;

/**
 * Created by aengusmccullough on 2018-09-19.
 */
public class DynamoFacade {

    private AmazonDynamoDB client;
    private DynamoDB dynamo;
    private DynamoDBMapper mapper;
    private DynamoDBMapperConfig config;
    private AWSCredentialsProvider credentialsProvider;

    public DynamoFacade(String region, String tableName) {
        this.client = AmazonDynamoDBClientBuilder.standard().withRegion(region).build();
        config = new DynamoDBMapperConfig.Builder().withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableName))
                .build();
        this.dynamo = new DynamoDB(client);
        this.mapper = new DynamoDBMapper(client, config);

    }


    /**
     * Use this constructor if you need to use the S3link
     * @param region String aws region
     * @param tableName String name of table
     * @param credentialsProvider AWSCredentialsProvider
     */
    public DynamoFacade(String region, String tableName, AWSCredentialsProvider credentialsProvider) {
        this.client = AmazonDynamoDBClientBuilder.standard().withRegion(region).build();
        config = new DynamoDBMapperConfig.Builder().withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableName))
                .build();

        this.mapper = new DynamoDBMapper(client, config, credentialsProvider);
        this.dynamo = new DynamoDB(client);
        this.credentialsProvider = credentialsProvider;
    }

    public void updateTableName(String tableName) {
        config = new DynamoDBMapperConfig.Builder().withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableName))
                .build();

        this.mapper = credentialsProvider!=null ? new DynamoDBMapper(client, config, credentialsProvider) : new DynamoDBMapper(client, config);
    }


    public DynamoDBMapper getMapper(){
        return this.mapper;
    }

    public Table getTable(String tableName){
        return this.dynamo.getTable(tableName);
    }




    public DynamoDBMapperConfig getConfig() {
        return this.config;
    }
}
