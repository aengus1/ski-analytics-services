package ski.crunch.dao;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;
import ski.crunch.aws.DynamoFacade;
import ski.crunch.utils.Jsonable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class SerialScanner<T, R> {


    public static <T extends Jsonable, R> List<R> scan(DynamoFacade facade, Function<Stream<T>, R> function, String tableName, Class<T> clazz) {

        List<R> result = new ArrayList<>();
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        facade.updateTableName(tableName);
        PaginatedScanList<T> scanResult = facade.getMapper().scan(clazz, scanExpression);
        result.add(function.apply(scanResult.stream()));

        return result;
    }
}
