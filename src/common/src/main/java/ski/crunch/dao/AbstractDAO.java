package ski.crunch.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ski.crunch.aws.DynamoFacade;

public abstract class AbstractDAO {

    DynamoFacade dynamoDBService;
    protected String tableName;

    protected static final Logger logger = LoggerFactory.getLogger(AbstractDAO.class);

    public AbstractDAO(DynamoFacade dynamo, String tableName) {

        this.dynamoDBService = dynamo;
        this.tableName = tableName;
    }
}
