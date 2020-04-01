package crunch.ski.cli;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import ski.crunch.model.UserSettingsItem;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DynamoBackupTest {

    private DynamoBackup dynamoBackup;
    @Test
    public void testDynamoScan() throws Exception{
        ProfileCredentialsProvider profileCredentialsProvider = new ProfileCredentialsProvider("backend_dev");
        dynamoBackup = new DynamoBackup("ca-central-1", profileCredentialsProvider);
        dynamoBackup.fullTableBackup(UserSettingsItem.class, "123", "dev-crunch-ski-Activity", 2);
    }
}
