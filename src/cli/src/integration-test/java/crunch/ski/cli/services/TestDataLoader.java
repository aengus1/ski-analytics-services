package crunch.ski.cli.services;

import ski.crunch.aws.DynamoFacade;
import ski.crunch.aws.S3Facade;
import ski.crunch.dao.UserDAO;
import ski.crunch.model.ActivityItem;
import ski.crunch.model.UserSettingsItem;
import ski.crunch.testhelpers.DynamoDbHelpers;
import ski.crunch.testhelpers.IntegrationTestPropertiesReader;
import ski.crunch.utils.FileUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;

public class TestDataLoader {


public static final String USER1_ACT1 = "user1_activity1";
public static final String USER1_ACT2 = "user1_activity2";
public static final String USER2_ACT1 = "user2_activity1";

public static final String PASSWORD="my*asec!passowrd780";

    public void loadUserData() throws IOException {

        DynamoDbHelpers.createTable(
                IntegrationTestPropertiesReader.get("region"),
                IntegrationTestPropertiesReader.get("profile"),
                IntegrationTestPropertiesReader.get("test-table-user"),
                1, 1,
                "id", "S", null, null);

        UserSettingsItem user1 = new UserSettingsItem();
        user1.setId("123");
        user1.setEmail("integration-test-user@crunch.ski");
        user1.setLastName("Crunch");
        user1.setFirstName("Ski");
        user1.setPwhash("246172676F6E32696424763D3139246D3D3132382C743D32302C703D31246D46315A4D6B6B38666E7351444C35594958437A476724514A786B482F64584E67766344723158444268304351494D39695449713164386249474C376750613850490000000000000000000000000000000000000000000000000000000000000000");
        user1.setConfirmed(true);
        user1.setHeight(190);
        user1.setWeight(40);
        user1.setGender("M");
        user1.setHrZones(Arrays.asList(new Integer[]{60,80,100,120,140}));
        user1.setTags(Set.of("dog", "long", "slow"));
        user1.setDevices(Set.of("garmin forerunner", "suunto x", "apple watch"));

        UserSettingsItem user2 = new UserSettingsItem();
        user2.setId("456");
        user2.setEmail("integration-test-user2@crunch.ski");
        user2.setLastName("Cruncher");
        user2.setFirstName("Skier");
        user2.setPwhash("246172676F6E32696424763D3139246D3D3132382C743D32302C703D31246D46315A4D6B6B38666E7351444C35594958437A476724514A786B482F64584E67766344723158444268304351494D39695449713164386249474C376750613850490000000000000000000000000000000000000000000000000000000000000000");
        user2.setConfirmed(true);
        user2.setHeight(200);
        user2.setWeight(50);
        user2.setGender("F");
        user2.setHrZones(Arrays.asList(new Integer[]{60,80,100,120,140}));
        user2.setTags(Set.of("dog", "long", "slow"));
        user2.setDevices(Set.of("garmin forerunner", "suunto x", "apple watch"));

        DynamoFacade dynamoFacade = new DynamoFacade(IntegrationTestPropertiesReader.get("region"), IntegrationTestPropertiesReader.get("test-table-user"));
        UserDAO userDAO = new UserDAO(dynamoFacade, IntegrationTestPropertiesReader.get("test-table-user"));
        userDAO.updateUser(user1);
        userDAO.updateUser(user2);
    }


    public void loadActivityData() throws IOException {

        DynamoDbHelpers.createTable(
                IntegrationTestPropertiesReader.get("region"),
                IntegrationTestPropertiesReader.get("profile"),
                IntegrationTestPropertiesReader.get("test-table-act"),
                1, 1,
                "userId", "S", "id", "S");

        ActivityItem activityItem1 = new ActivityItem();
        activityItem1.setId("act1");
        activityItem1.setUserId("integration-test-user@crunch.ski");
        activityItem1.setActivityDate(new Date());
        activityItem1.setAscent(100.0);
        activityItem1.setDescent(30.0);

        //TODO -> FINISH populating activity item
        // todo -> add remaining activities

        DynamoFacade dynamoFacade = new DynamoFacade(IntegrationTestPropertiesReader.get("region"), IntegrationTestPropertiesReader.get("test-table-act"));
        dynamoFacade.getMapper().save(activityItem1);
//        dynamoFacade.getMapper().save(activityItem2);
//        dynamoFacade.getMapper().save(activityItem3);

    }

    public void loadRawFiles() throws IOException {

        S3Facade s3Facade = new S3Facade(IntegrationTestPropertiesReader.get("region"));
        s3Facade.getS3Client().createBucket(IntegrationTestPropertiesReader.get("test-raw-bucket"));
        s3Facade.putObject(
                IntegrationTestPropertiesReader.get("test-raw-bucket"),
                USER1_ACT1,
                FileUtils.readFromResourcesDirectory(TestDataLoader.class, USER1_ACT1+".fit")
        );
        s3Facade.putObject(
                IntegrationTestPropertiesReader.get("test-raw-bucket"),
                USER1_ACT2,
                FileUtils.readFromResourcesDirectory(TestDataLoader.class, USER1_ACT2+".fit")
        );
        s3Facade.putObject(
                IntegrationTestPropertiesReader.get("test-raw-bucket"),
                USER2_ACT1,
                FileUtils.readFromResourcesDirectory(TestDataLoader.class, USER2_ACT1+".fit")
        );

    }


    // should I just let the s3 trigger create these?
    public void loadActFiles() throws IOException {

        S3Facade s3Facade = new S3Facade(IntegrationTestPropertiesReader.get("region"));
        s3Facade.getS3Client().createBucket(IntegrationTestPropertiesReader.get("test-act-bucket"));
        s3Facade.putObject(
                IntegrationTestPropertiesReader.get("test-act-bucket"),
                USER1_ACT1,
                FileUtils.readFromResourcesDirectory(TestDataLoader.class, USER1_ACT1+".pbf")
        );
        s3Facade.putObject(
                IntegrationTestPropertiesReader.get("test-act-bucket"),
                USER1_ACT2,
                FileUtils.readFromResourcesDirectory(TestDataLoader.class, USER1_ACT2+".pbf")
        );
        s3Facade.putObject(
                IntegrationTestPropertiesReader.get("test-act-bucket"),
                USER2_ACT1,
                FileUtils.readFromResourcesDirectory(TestDataLoader.class, USER2_ACT1+".pbf")
        );
    }
}
