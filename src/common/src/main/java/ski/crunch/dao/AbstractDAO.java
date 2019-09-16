package ski.crunch.dao;

import org.apache.log4j.Logger;
import ski.crunch.aws.DynamoDBService;

public abstract class AbstractDAO {

    DynamoDBService dynamoDBService;
    protected String tableName;

    protected static final Logger LOG = Logger.getLogger(AbstractDAO.class);

    public AbstractDAO(DynamoDBService dynamo, String tableName) {

        this.dynamoDBService = dynamo;
        this.tableName = tableName;
    }
}
