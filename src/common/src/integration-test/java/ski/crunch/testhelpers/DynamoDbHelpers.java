package ski.crunch.testhelpers;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.*;
import ski.crunch.aws.DynamoFacade;

import java.util.ArrayList;
import java.util.List;

public class DynamoDbHelpers {



    public static void addGsi(Table table, CreateGlobalSecondaryIndexAction action,
                                              AttributeDefinition hash, AttributeDefinition range) throws Exception{
        if(range != null) {
            table.createGSI(action, hash, range);
        } else {
            table.createGSI(action, hash);
        }
        table.waitForActive();

    }
    public static Table createTable(String region, String profile, String tableName, long readCapacityUnits, long writeCapacityUnits,
                                    String partitionKeyName, String partitionKeyType, String sortKeyName, String sortKeyType) {
        Table table = null;
        DynamoFacade facade = new DynamoFacade(region, tableName);
        try {
            System.out.println("Creating table " + tableName);

            List<KeySchemaElement> keySchema = new ArrayList<KeySchemaElement>();
            keySchema.add(new KeySchemaElement().withAttributeName(partitionKeyName).withKeyType(KeyType.HASH)); // Partition

            // key

            List<AttributeDefinition> attributeDefinitions = new ArrayList<AttributeDefinition>();
            attributeDefinitions
                    .add(new AttributeDefinition().withAttributeName(partitionKeyName).withAttributeType(partitionKeyType));

            if (sortKeyName != null) {
                keySchema.add(new KeySchemaElement().withAttributeName(sortKeyName).withKeyType(KeyType.RANGE)); // Sort
                // key
                attributeDefinitions
                        .add(new AttributeDefinition().withAttributeName(sortKeyName).withAttributeType(sortKeyType));
            }

            table = facade.getClient().createTable(tableName, keySchema, attributeDefinitions, new ProvisionedThroughput()
                    .withReadCapacityUnits(readCapacityUnits).withWriteCapacityUnits(writeCapacityUnits));
            System.out.println("Waiting for " + tableName + " to be created...this may take a while...");
            table.waitForActive();

        }
        catch (Exception e) {
            System.err.println("Failed to create table " + tableName);
            e.printStackTrace(System.err);
            return null;
        }
        return table;
    }




    public static void deleteTable(DynamoFacade facade, String tableName) {
        try {

            Table table = facade.getClient().getTable(tableName);
            table.delete();
            System.out.println("Waiting for " + tableName + " to be deleted...this may take a while...");
            table.waitForDelete();

        }
        catch (Exception e) {
            System.err.println("Failed to delete table " + tableName);
            e.printStackTrace(System.err);
        }
    }


    public static void insertItems(DynamoFacade facade, String tableName, List<Item> items) {

        Table table = facade.getClient().getTable(tableName);

        try {
            for (Item item : items) {
                table.putItem(item);
            }

        } catch (Exception e) {
            System.err.println("Failed to create item  in " + tableName);
            System.err.println(e.getMessage());
        }
    }
}
