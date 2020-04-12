package crunch.ski.cli;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.dynamodbv2.document.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import ski.crunch.aws.CredentialsProviderFactory;
import ski.crunch.aws.CredentialsProviderType;
import ski.crunch.aws.DynamoFacade;
import ski.crunch.testhelpers.DynamoDbHelpers;
import ski.crunch.testhelpers.IntegrationTestPropertiesReader;

import java.io.IOException;
import java.util.*;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class DynamoUserBackupTest {

    private DynamoBackup dynamoBackup;
    private DynamoFacade dynamoFacade;
    private String TABLE_NAME;
    private String PROFILE_NAME;
    private String REGION;


        @BeforeEach
        public void setUp() throws IOException {
            TABLE_NAME = IntegrationTestPropertiesReader.get("test-table");
            PROFILE_NAME = IntegrationTestPropertiesReader.get("profile");
            REGION = IntegrationTestPropertiesReader.get("region");
            CredentialsProviderFactory credentialsProviderFactory = CredentialsProviderFactory.getInstance();
            AWSCredentialsProvider provider = credentialsProviderFactory.newCredentialsProvider(CredentialsProviderType.PROFILE, Optional.of(PROFILE_NAME));
            dynamoFacade = new DynamoFacade(REGION, TABLE_NAME);
            dynamoBackup = new DynamoBackup(REGION, provider);
            DynamoDbHelpers.createTable(REGION, PROFILE_NAME, TABLE_NAME,
                    1,1,"id", "S", null, null);

            Item item = new Item().withPrimaryKey("id", "123")
                    .withString("Title", "Book " + 2 + " Title").withString("ISBN", "111-1111111111")
                    .withStringSet("Authors", new HashSet<>(Arrays.asList("Author1"))).withNumber("Price", 2)
                    .withString("Dimensions", "8.5 x 11.0 x 0.5").withNumber("PageCount", 500)
                    .withBoolean("InPublication", true).withString("ProductCategory", "Book");

            Item item2 = new Item().withPrimaryKey("id", "12345")
                    .withString("Title", "Book " + 2 + " Title").withString("ISBN", "111-1111111111")
                    .withStringSet("Authors", new HashSet<>(Arrays.asList("Author1"))).withNumber("Price", 2)
                    .withString("Dimensions", "8.5 x 11.0 x 0.5").withNumber("PageCount", 500)
                    .withBoolean("InPublication", true).withString("ProductCategory", "Book");

            Item item3 = new Item().withPrimaryKey("id", "123456")
                    .withString("Title", "Book " + 2 + " Title").withString("ISBN", "111-1111111111")
                    .withStringSet("Authors", new HashSet<>(Arrays.asList("Author1"))).withNumber("Price", 2)
                    .withString("Dimensions", "8.5 x 11.0 x 0.5").withNumber("PageCount", 500)
                    .withBoolean("InPublication", true).withString("ProductCategory", "Book");

            List<Item> items = new ArrayList<>();
            items.add(item);
            items.add(item2);
            items.add(item3);
            DynamoDbHelpers.insertItems(dynamoFacade, TABLE_NAME,items);

        }
}
