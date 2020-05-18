package crunch.ski.cli.services;

import com.amazonaws.services.dynamodbv2.datamodeling.S3Link;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.*;
import ski.crunch.aws.CredentialsProviderFactory;
import ski.crunch.aws.DynamoFacade;
import ski.crunch.aws.S3Facade;
import ski.crunch.aws.SSMParameterFacade;
import ski.crunch.dao.ActivityDAO;
import ski.crunch.dao.UserDAO;
import ski.crunch.model.ActivityItem;
import ski.crunch.model.UserSettingsItem;
import ski.crunch.testhelpers.DynamoDbHelpers;
import ski.crunch.testhelpers.IntegrationTestPropertiesReader;
import ski.crunch.utils.FileUtils;

import java.io.IOException;
import java.util.*;

public class TestDataLoader {


    public static final String USER1_ACT1 = "user1_activity1";
    public static final String USER1_ACT2 = "user1_activity2";
    public static final String USER2_ACT1 = "user2_activity1";

    public static final String PASSWORD = "my*asec!passowrd780";


    public void createUserTable() throws Exception {
        Table table = DynamoDbHelpers.createTable(
                IntegrationTestPropertiesReader.get("region"),
                IntegrationTestPropertiesReader.get("profile"),
                IntegrationTestPropertiesReader.get("test-table-user"),
                1, 1,
                "id", "S", null, null);
        if (table != null) {
            addGSI(table);
        }
    }

    public void dropUserData() throws Exception {
        DynamoFacade dynamoFacade = new DynamoFacade(IntegrationTestPropertiesReader.get("region"), IntegrationTestPropertiesReader.get("test-table-user"));
        UserDAO userDAO = new UserDAO(dynamoFacade, IntegrationTestPropertiesReader.get("test-table-user"));

        UserSettingsItem user = userDAO.getUserSettings("123").get();
        UserSettingsItem user2 = userDAO.getUserSettings("456").get();
        userDAO.deleteUser(user);
        userDAO.deleteUser(user2);

    }

    public void loadUserData() throws Exception {

        //createUserTable();

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
        user1.setHrZones(Arrays.asList(new Integer[]{60, 80, 100, 120, 140}));
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
        user2.setHrZones(Arrays.asList(new Integer[]{60, 80, 100, 120, 140}));
        user2.setTags(Set.of("dog", "long", "slow"));
        user2.setDevices(Set.of("garmin forerunner", "suunto x", "apple watch"));

        DynamoFacade dynamoFacade = new DynamoFacade(IntegrationTestPropertiesReader.get("region"), IntegrationTestPropertiesReader.get("test-table-user"));
        UserDAO userDAO = new UserDAO(dynamoFacade, IntegrationTestPropertiesReader.get("test-table-user"));
        userDAO.updateUser(user1);
        userDAO.updateUser(user2);
    }


    public void createActivityTable() throws IOException {
        DynamoDbHelpers.createTable(
                IntegrationTestPropertiesReader.get("region"),
                IntegrationTestPropertiesReader.get("profile"),
                IntegrationTestPropertiesReader.get("test-table-act"),
                1, 1,
                "userId", "S", "id", "S");
    }

    public void dropActivityData() throws IOException {

        DynamoFacade dynamoFacade = new DynamoFacade(IntegrationTestPropertiesReader.get("region"),
                IntegrationTestPropertiesReader.get("test-table-act"),
                CredentialsProviderFactory.getDefaultCredentialsProvider());

        ActivityDAO activityDAO = new ActivityDAO(dynamoFacade, IntegrationTestPropertiesReader.get("test-table-act"));

        activityDAO.deleteActivityItemById("act1", "integration-test-user@crunch.ski");
        activityDAO.deleteActivityItemById("act2", "integration-test-user@crunch.ski");
        activityDAO.deleteActivityItemById("act3", "integration-test-user2@crunch.ski");
    }

    public void loadActivityData() throws IOException {


        DynamoFacade dynamoFacade = new DynamoFacade(IntegrationTestPropertiesReader.get("region"),
                IntegrationTestPropertiesReader.get("test-table-act"),
                CredentialsProviderFactory.getDefaultCredentialsProvider());

        ActivityItem activityItem1 = new ActivityItem();
        activityItem1.setId("act1");
        activityItem1.setUserId("integration-test-user@crunch.ski");
        activityItem1.setActivityDate(new Date());
        activityItem1.setAscent(100.0);
        activityItem1.setDescent(30.0);
        activityItem1.setActivitySubType("trail");
        activityItem1.setActivityType("Running");
        activityItem1.setDateOfUpload(new Date());
        activityItem1.setNotes("my long notes");
        activityItem1.setDistance(1000.12);
        activityItem1.setLastUpdateTimestamp(new Date());
        activityItem1.setTimeOfDay(12);
        activityItem1.setStatus(ActivityItem.Status.COMPLETE);
        activityItem1.setRawFileType(".fit");

        S3Link rawLink = dynamoFacade.getMapper().createS3Link(IntegrationTestPropertiesReader.get("test-raw-bucket"),
                "integration-test-user@crunch.ski/act1.fit");
        S3Link procLink = dynamoFacade.getMapper().createS3Link(IntegrationTestPropertiesReader.get("test-act-bucket"),
                "integration-test-user@crunch.ski/act1.pbf");
        activityItem1.setRawActivity(rawLink);
        activityItem1.setProcessedActivity(procLink);


        ActivityItem activityItem2 = new ActivityItem();
        activityItem2.setId("act2");
        activityItem2.setUserId("integration-test-user@crunch.ski");
        activityItem2.setActivityDate(new Date());
        activityItem2.setAscent(200.0);
        activityItem2.setDescent(60.0);
        activityItem2.setActivitySubType("skate");
        activityItem2.setActivityType("XC Ski");
        activityItem2.setDateOfUpload(new Date());
        activityItem2.setNotes("my long notes again");
        activityItem2.setDistance(12000.12);
        activityItem2.setLastUpdateTimestamp(new Date());
        activityItem2.setTimeOfDay(4);
        activityItem2.setStatus(ActivityItem.Status.COMPLETE);
        activityItem2.setRawFileType(".fit");

        String rawJson2 = "{\"s3\":{\"bucket\":\"" + IntegrationTestPropertiesReader.get("test-raw-bucket")
                + "\",\"key\":\"integration-test-user@crunch.ski/act2.fit\",\"region\":\"ca-central-1\"}}";

        String procJson2 = "{\"s3\":{\"bucket\":\"" + IntegrationTestPropertiesReader.get("test-act-bucket")
                + "\",\"key\":\"integration-test-user@crunch.ski/act2.pbf\",\"region\":\"ca-central-1\"}}";

        activityItem2.setRawActivity(S3Link.fromJson(dynamoFacade.getMapper().getS3ClientCache(), rawJson2));
        activityItem2.setProcessedActivity(S3Link.fromJson(dynamoFacade.getMapper().getS3ClientCache(), procJson2));


        ActivityItem activityItem3 = new ActivityItem();
        activityItem3.setId("act3");
        activityItem3.setUserId("integration-test-user2@crunch.ski");
        activityItem3.setActivityDate(new Date());
        activityItem3.setAscent(204.0);
        activityItem3.setDescent(64.0);
        activityItem3.setActivitySubType("classic");
        activityItem3.setActivityType("XC Ski");
        activityItem3.setDateOfUpload(new Date());
        activityItem3.setNotes("my long notes again");
        activityItem3.setDistance(24000.12);
        activityItem3.setLastUpdateTimestamp(new Date());
        activityItem3.setTimeOfDay(8);
        activityItem3.setStatus(ActivityItem.Status.COMPLETE);
        activityItem3.setRawFileType(".fit");

        String rawJson3 = "{\"s3\":{\"bucket\":\"" + IntegrationTestPropertiesReader.get("test-raw-bucket")
                + "\",\"key\":\"integration-test-user2@crunch.ski/act3.fit\",\"region\":\"ca-central-1\"}}";

        String procJson3 = "{\"s3\":{\"bucket\":\"" + IntegrationTestPropertiesReader.get("test-act-bucket")
                + "\",\"key\":\"integration-test-user2@crunch.ski/act3.pbf\",\"region\":\"ca-central-1\"}}";

        activityItem3.setRawActivity(S3Link.fromJson(dynamoFacade.getMapper().getS3ClientCache(), rawJson3));
        activityItem3.setProcessedActivity(S3Link.fromJson(dynamoFacade.getMapper().getS3ClientCache(), procJson3));

        dynamoFacade.getMapper().save(activityItem1);
        dynamoFacade.getMapper().save(activityItem2);
        dynamoFacade.getMapper().save(activityItem3);

    }

    public void createS3Buckets()  throws IOException{
        S3Facade s3Facade = new S3Facade(IntegrationTestPropertiesReader.get("region"));
        try {
            s3Facade.getS3Client().createBucket(IntegrationTestPropertiesReader.get("test-act-bucket"));
        }catch (Exception ignored) {
            ignored.printStackTrace();
        }
        try {
            s3Facade.getS3Client().createBucket(IntegrationTestPropertiesReader.get("test-raw-bucket"));
        }catch(Exception ignored ) {
            ignored.printStackTrace();
        }
    }

    public void dropRawFiles() throws IOException {
        S3Facade s3Facade = new S3Facade(IntegrationTestPropertiesReader.get("region"));

        s3Facade.getS3Client().deleteObject(IntegrationTestPropertiesReader.get("test-act-bucket"), "integration-test-user@crunch.ski/act1.fit");
        s3Facade.getS3Client().deleteObject(IntegrationTestPropertiesReader.get("test-act-bucket"), "integration-test-user@crunch.ski/act2.fit");
        s3Facade.getS3Client().deleteObject(IntegrationTestPropertiesReader.get("test-act-bucket"), "integration-test-user2@crunch.ski/act3.fit");
    }

    public void loadRawFiles() throws IOException {

        S3Facade s3Facade = new S3Facade(IntegrationTestPropertiesReader.get("region"));

        s3Facade.putObject(
                IntegrationTestPropertiesReader.get("test-raw-bucket"),
                "integration-test-user@crunch.ski/act1.fit",
                FileUtils.readFromResourcesDirectory(TestDataLoader.class, "/" + USER1_ACT1 + ".fit")
        );
        s3Facade.putObject(
                IntegrationTestPropertiesReader.get("test-raw-bucket"),
                "integration-test-user@crunch.ski/act2.fit",
                FileUtils.readFromResourcesDirectory(TestDataLoader.class, "/" + USER1_ACT2 + ".fit")
        );
        s3Facade.putObject(
                IntegrationTestPropertiesReader.get("test-raw-bucket"),
                "integration-test-user2@crunch.ski/act3.fit",
                FileUtils.readFromResourcesDirectory(TestDataLoader.class, "/" + USER2_ACT1 + ".fit")
        );

    }

    public void dropUserTable() throws IOException {
        DynamoFacade facade = new DynamoFacade(IntegrationTestPropertiesReader.get("region"), IntegrationTestPropertiesReader.get("test-table-user"));
        DynamoDbHelpers.deleteTable(facade, IntegrationTestPropertiesReader.get("test-table-user"));
    }

    public void dropActTable() throws IOException {
        DynamoFacade facade = new DynamoFacade(IntegrationTestPropertiesReader.get("region"), IntegrationTestPropertiesReader.get("test-table-act"));
        DynamoDbHelpers.deleteTable(facade, IntegrationTestPropertiesReader.get("test-table-act"));
    }

    public void dropBucket(String bucketName) throws IOException {
        S3Facade s3Facade = new S3Facade(IntegrationTestPropertiesReader.get("region"));
        s3Facade.listObjects(bucketName).stream().forEach(x -> {
            try {
                s3Facade.deleteObject(bucketName, x);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        s3Facade.getS3Client().deleteBucket(bucketName);
    }

    private void addGSI(Table table) throws Exception {
        CreateGlobalSecondaryIndexAction globalSecondaryIndexAction = new CreateGlobalSecondaryIndexAction();
        ProvisionedThroughput provisionedThroughput = new ProvisionedThroughput();
        provisionedThroughput.setReadCapacityUnits(1l);
        provisionedThroughput.setWriteCapacityUnits(1l);

        globalSecondaryIndexAction.setProvisionedThroughput(provisionedThroughput);

        globalSecondaryIndexAction.setIndexName("email-index");

        KeySchemaElement keySchemaElement = new KeySchemaElement();
        keySchemaElement.setAttributeName("email");
        keySchemaElement.setKeyType(KeyType.HASH);


        globalSecondaryIndexAction.setKeySchema(List.of(keySchemaElement));

        Projection projection = new Projection();
        projection.setProjectionType(ProjectionType.ALL);

        globalSecondaryIndexAction.setProjection(projection);

        AttributeDefinition hashKeyDef = new AttributeDefinition();
        hashKeyDef.setAttributeName("email");
        hashKeyDef.setAttributeType("S");


        DynamoDbHelpers.addGsi(table, globalSecondaryIndexAction, hashKeyDef, null);
    }


    public void createSSMParameters() throws Exception {
        SSMParameterFacade ssmParameterFacade = new SSMParameterFacade(IntegrationTestPropertiesReader.get("region")
                ,CredentialsProviderFactory.getDefaultCredentialsProvider());
        String env = IntegrationTestPropertiesReader.get("test-table").split("-")[0];
        ssmParameterFacade.putParameter(env+"-weather-api-key","weather123", "weather api key", null);
        List<com.amazonaws.services.simplesystemsmanagement.model.Tag> tags = new ArrayList<>();
        com.amazonaws.services.simplesystemsmanagement.model.Tag tag = new com.amazonaws.services.simplesystemsmanagement.model.Tag();
        tag.setKey("module");
        tag.setValue("activity");
        tags.add(tag);
        ssmParameterFacade.putParameter(env+"-location-api-key","location123", "location api key",
                Optional.of(tags));
    }

    public void deleteSSMParameters() throws Exception {
        SSMParameterFacade ssmParameterFacade = new SSMParameterFacade(IntegrationTestPropertiesReader.get("region")
                ,CredentialsProviderFactory.getDefaultCredentialsProvider());
        String env = IntegrationTestPropertiesReader.get("test-table").split("-")[0];
        ssmParameterFacade.deleteParameter(env+"-weather-api-key");
        ssmParameterFacade.deleteParameter(env+"-location-api-key");
    }
}
