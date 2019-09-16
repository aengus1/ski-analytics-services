package ski.crunch.dao;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import ski.crunch.aws.DynamoDBService;
import ski.crunch.model.UserSettingsItem;

import java.util.*;

public class UserDAO extends AbstractDAO {

    public UserDAO(DynamoDBService dynamo, String tableName) {
        super(dynamo, tableName);
    }

    public Optional<UserSettingsItem> getUserSettings(String cognitoId) {
        dynamoDBService.updateTableName(tableName);
        LOG.info("cognitoId: " + cognitoId);
        if (cognitoId == null || cognitoId.isEmpty()) {
            LOG.error("cognito id null or empty" );
            return Optional.empty();
        }

//        DynamoDBQueryExpression<UserSettingsItem> userQueryExpression = new DynamoDBQueryExpression<>();
//        UserSettingsItem userToQuery = new UserSettingsItem();
//        userToQuery.setId(cognitoId);


        Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
        eav.put(":val1", new AttributeValue().withS(cognitoId));

        DynamoDBQueryExpression<UserSettingsItem> queryExpression = new DynamoDBQueryExpression<UserSettingsItem>()
                .withKeyConditionExpression("id = :val1")
                .withExpressionAttributeValues(eav);
        List<UserSettingsItem> result = dynamoDBService.getMapper().query(UserSettingsItem.class, queryExpression);

        if (result.isEmpty()) {
            LOG.error("User not found for cognitoId " + cognitoId );
            return Optional.empty();

        }
        return Optional.of(result.get(0));
    }

    public void addDeviceAndActivityType(String cognitoId, String device, Set<String> activityTypes) {
        Optional<UserSettingsItem> userToUpdate = getUserSettings(cognitoId);
        DynamoDBMapperConfig dynamoDBMapperConfig = new DynamoDBMapperConfig.Builder()
                .withConsistentReads(DynamoDBMapperConfig.ConsistentReads.CONSISTENT)
                .withSaveBehavior(DynamoDBMapperConfig.SaveBehavior.UPDATE)
                .build();


        if (userToUpdate.isPresent()) {
            UserSettingsItem user = userToUpdate.get();
            LOG.info("updating user with id = " + user.getId());
            LOG.info("user id = " + user.getId());
            user.addDevice(device);
            user.setActivityTypes(activityTypes);
            try {
                dynamoDBService.getMapper().save(user, dynamoDBMapperConfig);
            }catch(Exception ex ) {
                ex.printStackTrace();
            }
        }
    }


}
