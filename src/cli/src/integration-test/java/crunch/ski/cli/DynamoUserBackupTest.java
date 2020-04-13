package crunch.ski.cli;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.junit.jupiter.api.*;
import ski.crunch.aws.CredentialsProviderFactory;
import ski.crunch.aws.CredentialsProviderType;
import ski.crunch.aws.DynamoFacade;
import ski.crunch.model.ActivityItem;
import ski.crunch.model.UserSettingsItem;
import ski.crunch.testhelpers.DynamoDbHelpers;
import ski.crunch.testhelpers.IntegrationTestPropertiesReader;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DynamoUserBackupTest {

    private DynamoBackup dynamoBackup;
    private DynamoFacade dynamoFacade;
    private String ACTIVITY_TABLE_NAME;
    private String USER_TABLE_NAME;
    private String PROFILE_NAME;
    private String REGION;
    private static final String userToExport = "user2";

    @BeforeAll
    public void setUp() throws IOException {
        ACTIVITY_TABLE_NAME = IntegrationTestPropertiesReader.get("test-table");
        USER_TABLE_NAME = IntegrationTestPropertiesReader.get("test-table-user");
        PROFILE_NAME = IntegrationTestPropertiesReader.get("profile");
        REGION = IntegrationTestPropertiesReader.get("region");
        CredentialsProviderFactory credentialsProviderFactory = CredentialsProviderFactory.getInstance();
        AWSCredentialsProvider provider = credentialsProviderFactory.newCredentialsProvider(CredentialsProviderType.PROFILE, Optional.of(PROFILE_NAME));
        dynamoFacade = new DynamoFacade(REGION, ACTIVITY_TABLE_NAME);
        dynamoBackup = new DynamoBackup(REGION, provider);
        DynamoDbHelpers.createTable(REGION, PROFILE_NAME, ACTIVITY_TABLE_NAME,
                1, 1, "cognitoId", "S", "id", "S");

        ActivityItem activityItem = new ActivityItem();
        activityItem.setId("12345");
        activityItem.setUserId("user1@email.com");
        activityItem.setCognitoId("user1");
        activityItem.setLastUpdateTimestamp(new Date());


        ActivityItem activityItem2 = new ActivityItem();
        activityItem2.setId("678910");
        activityItem.setUserId("user2@email.com");
        activityItem2.setCognitoId(userToExport);
        activityItem2.setLastUpdateTimestamp(new Date());


        ActivityItem activityItem3 = new ActivityItem();
        activityItem3.setId("11121314");
        activityItem.setUserId("user2@email.com");
        activityItem3.setCognitoId(userToExport);
        activityItem3.setLastUpdateTimestamp(new Date());

        dynamoFacade.getMapper().save(activityItem);
        dynamoFacade.getMapper().save(activityItem2);
        dynamoFacade.getMapper().save(activityItem3);


        dynamoFacade.updateTableName(USER_TABLE_NAME);
        DynamoDbHelpers.createTable(REGION, PROFILE_NAME, USER_TABLE_NAME,
                1, 1, "id", "S", null, null);

        UserSettingsItem userSettingsItem = new UserSettingsItem();
        userSettingsItem.setId(userToExport);
        userSettingsItem.setEmail("user2@email.com");

        UserSettingsItem userSettingsItem2 = new UserSettingsItem();
        userSettingsItem2.setId("user1");
        userSettingsItem2.setEmail("user@email.com");

        dynamoFacade.getMapper().save(userSettingsItem);
        dynamoFacade.getMapper().save(userSettingsItem2);
    }

    @Test
    public void testUserBackup() throws IOException{
        File destDir = new File(System.getProperty("java.io.tmpdir")+"/userbackuptest");
        destDir.mkdir();
        dynamoBackup.userDataBackup(userToExport, USER_TABLE_NAME, ACTIVITY_TABLE_NAME, destDir);

        // confirm that the archive contains only activities from user2
        File activitiesJson = new File(destDir,"activities.json");

        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode node = (ArrayNode) objectMapper.readTree(activitiesJson);
        int count = 0;
        for (JsonNode jsonNode : node) {
            System.out.println(jsonNode.get("cognitoId").textValue());
            if (jsonNode.get("cognitoId").textValue().equals(userToExport)) {
                count++;
            }
        }
        assertEquals(2, count);


    }
    @AfterAll
    public void tearDown() {
        DynamoDbHelpers.deleteTable(dynamoFacade, ACTIVITY_TABLE_NAME);

        DynamoDbHelpers.deleteTable(dynamoFacade, USER_TABLE_NAME);
    }
}
