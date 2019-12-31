package ski.crunch.cloudformation.rockset.model;

public enum RocksetIntegrationType {
    dynamodb("DynamoDbDataSource"),
    s3("S3DataSource"),
    kinesis("KinesisDataSource");


    private String dataSourceName;

    public String getDataSourceName(){
        return this.dataSourceName;
    }

    RocksetIntegrationType(String dataSourceName){
        this.dataSourceName = dataSourceName;
    }
}
