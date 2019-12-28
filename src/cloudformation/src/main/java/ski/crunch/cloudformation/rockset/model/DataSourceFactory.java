package ski.crunch.cloudformation.rockset.model;

import ski.crunch.utils.NotFoundException;

public class DataSourceFactory {

    public static DataSource getInstance(RocksetIntegrationType type) {
        switch(type) {
            case s3: {
                return new S3DataSource();
            }
            case dynamodb: {
                return new DynamoDbDataSource();
            }
            case kinesis: {
                return new KinesisDataSource();
            }
            default: {
                throw new NotFoundException("Data source "  + type.getDataSourceName() + " not configured");
            }
        }
    }
}
