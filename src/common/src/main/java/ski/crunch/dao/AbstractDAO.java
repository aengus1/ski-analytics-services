package ski.crunch.dao;

import org.apache.log4j.Logger;
import ski.crunch.aws.DynamoFacade;

public abstract class AbstractDAO {

    DynamoFacade dynamoDBService;
    protected String tableName;

    protected static final Logger LOG = Logger.getLogger(AbstractDAO.class);

    public AbstractDAO(DynamoFacade dynamo, String tableName) {

        this.dynamoDBService = dynamo;
        this.tableName = tableName;
    }
}
