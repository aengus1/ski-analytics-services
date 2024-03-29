package ski.crunch.dao;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import ski.crunch.aws.DynamoFacade;
import ski.crunch.model.UserSettingsItem;
import ski.crunch.utils.NotFoundException;

import java.util.*;

public class UserDAO extends AbstractDAO {


    public UserDAO(DynamoFacade dynamo, String tableName) {
        super(dynamo, tableName);
    }

    public Optional<UserSettingsItem> getUserSettings(String id) {
        dynamoDBService.updateTableName(tableName);
        logger.info("id: " + id);
        if (id == null || id.isEmpty()) {
            logger.error("id null or empty");
            return Optional.empty();
        }

        Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
        eav.put(":val1", new AttributeValue().withS(id));

        DynamoDBQueryExpression<UserSettingsItem> queryExpression = new DynamoDBQueryExpression<UserSettingsItem>()
                .withKeyConditionExpression("id = :val1")
                .withExpressionAttributeValues(eav);
        List<UserSettingsItem> result = dynamoDBService.getMapper().query(UserSettingsItem.class, queryExpression);

        if (result.isEmpty()) {
            logger.error("User not found for id " + id);
            return Optional.empty();

        }
        return Optional.of(result.get(0));
    }

    public void addDeviceAndActivityType(String id, String device, Set<String> activityTypes) {
        Optional<UserSettingsItem> userToUpdate = getUserSettings(id);
        DynamoDBMapperConfig dynamoDBMapperConfig = new DynamoDBMapperConfig.Builder()
                .withConsistentReads(DynamoDBMapperConfig.ConsistentReads.CONSISTENT)
                .withSaveBehavior(DynamoDBMapperConfig.SaveBehavior.UPDATE)
                .build();


        if (userToUpdate.isPresent()) {
            UserSettingsItem user = userToUpdate.get();
            logger.info("updating user with id = " + user.getId());
            logger.info("user id = " + user.getId());
            user.addDevice(device);
            user.setActivityTypes(activityTypes);
            try {
                dynamoDBService.getMapper().save(user, dynamoDBMapperConfig);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void initializeUserSettings(String username) {
        try {
            UserSettingsItem userSettings = dynamoDBService.getMapper().load(UserSettingsItem.class, username);
            logger.debug("usersettings pwhash = " + userSettings.getPwhash());
            userSettings.setGender("");
            userSettings.setConfirmed(true);
            userSettings.setHeight(0);
            userSettings.setWeight(0);
            List<Integer> zones = new ArrayList<>();
            Collections.addAll(zones, 60, 130, 145, 150, 171, 190);
            userSettings.setHrZones(zones);
            dynamoDBService.getMapper().save(userSettings);
        } catch (Exception e) {
            logger.error("Error writing user settings", e);

        }
    }

    public void storeUserPwHash(String username, String hash, String email, String firstName, String lastName) {
        try {
            UserSettingsItem userSettings = new UserSettingsItem();
            userSettings.setEmail(email);
            userSettings.setFirstName(firstName);
            userSettings.setLastName(lastName);
            userSettings.setId(username);
            userSettings.setConfirmed(false);
            userSettings.setPwhash(hash);
            dynamoDBService.getMapper().save(userSettings);
        } catch (Exception e) {
            logger.error("Error saving password hash", e);

        }
    }

    public void updateUser(UserSettingsItem user) {
        dynamoDBService.getMapper().save(user);
    }

    public void deleteUser(UserSettingsItem user) {
        dynamoDBService.getMapper().delete(user);
    }


    /**
     * Fetch User item from dynamodb
     *
     * @param user String user (email or id)*
     * @return UserSettingsItem result
     * @throws NotFoundException on user not found
     */
    public UserSettingsItem lookupUser(String user) throws NotFoundException {
        UserSettingsItem userItem;

        if (user.contains("@")) {
            Optional<UserSettingsItem> userSettingsItem = getUserByEmailAddress(user);

            if (userSettingsItem.isPresent()) {
                return userSettingsItem.get();
            } else {
                throw new NotFoundException("user " + user + " not found via email");
            }

        } else {
            userItem = super.dynamoDBService.getMapper().load(UserSettingsItem.class, user);
        }
        return userItem;
    }


    public Optional<UserSettingsItem> getUserByEmailAddress(String email) {
        UserSettingsItem userItem;

        //query email via gsi
        final UserSettingsItem userSettingsItem = new UserSettingsItem();
        userSettingsItem.setEmail(email);
        final DynamoDBQueryExpression<UserSettingsItem> queryExpression = new DynamoDBQueryExpression<>();
        queryExpression.setHashKeyValues(userSettingsItem);
        queryExpression.setIndexName("email-index");
        queryExpression.setConsistentRead(false);


        final PaginatedQueryList<UserSettingsItem> results = super.dynamoDBService.getMapper().query(UserSettingsItem.class, queryExpression);
        if (results.size() < 1) {
            return Optional.empty();
        }
        userItem = results.get(0);

        return Optional.of(userItem);
    }
}
