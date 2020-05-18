package ski.crunch.dao;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.dynamodbv2.document.Item;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import ski.crunch.aws.CredentialsProviderFactory;
import ski.crunch.aws.DynamoFacade;
import ski.crunch.testhelpers.DynamoDbHelpers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class SerialScannerTest {


    private DynamoFacade dynamoFacade;
    public static final String TABLE_NAME = "backup-test-table";
    public static final String PROFILE_NAME = "default";
    public static final String REGION = "ca-central-1";

    @BeforeEach
    public void setUp() {
        AWSCredentialsProvider provider = CredentialsProviderFactory.getDefaultCredentialsProvider();
        dynamoFacade = new DynamoFacade(REGION, TABLE_NAME);

        DynamoDbHelpers.createTable(REGION, PROFILE_NAME, TABLE_NAME,
                1, 1, "id", "S", null, null);

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
        Item item4 = new Item().withPrimaryKey("id", "1234567")
                .withString("Title", "Book " + 2 + " Title").withString("ISBN", "111-1111111111")
                .withStringSet("Authors", new HashSet<>(Arrays.asList("Author1"))).withNumber("Price", 2)
                .withString("Dimensions", "8.5 x 11.0 x 0.5").withNumber("PageCount", 500)
                .withBoolean("InPublication", true).withString("ProductCategory", "Book");
        Item item5 = new Item().withPrimaryKey("id", "1234568")
                .withString("Title", "Book " + 2 + " Title").withString("ISBN", "111-1111111111")
                .withStringSet("Authors", new HashSet<>(Arrays.asList("Author1"))).withNumber("Price", 2)
                .withString("Dimensions", "8.5 x 11.0 x 0.5").withNumber("PageCount", 500)
                .withBoolean("InPublication", true).withString("ProductCategory", "Book");
        Item item6 = new Item().withPrimaryKey("id", "1234569")
                .withString("Title", "Book " + 2 + " Title").withString("ISBN", "111-1111111111")
                .withStringSet("Authors", new HashSet<>(Arrays.asList("Author1"))).withNumber("Price", 2)
                .withString("Dimensions", "8.5 x 11.0 x 0.5").withNumber("PageCount", 500)
                .withBoolean("InPublication", true).withString("ProductCategory", "Book");

        List<Item> items = new ArrayList<>();
        items.add(item);
        items.add(item2);
        items.add(item3);
        items.add(item4);
        items.add(item5);
        items.add(item6);
        DynamoDbHelpers.insertItems(dynamoFacade, TABLE_NAME, items);

    }

    @Test
    public void testFullTableScan() throws Exception {
        File outputDir = new File(System.getProperty("java.io.tmpdir"));
        String outputFilename =  TABLE_NAME + "-123";
        File outputFile = new File(outputDir, outputFilename);

        if(outputFile.exists()) {
            outputFile.delete();
        }

        dynamoFacade.fullTableBackup(BookItem.class, TABLE_NAME,outputDir,outputFilename,null);


            int count = 0;
            try (FileReader fileReader = new FileReader(new File(outputDir, outputFilename))) {
                try (BufferedReader bufferedReader = new BufferedReader(fileReader)) {
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        System.out.println(line);
                        if (line != null && !line.isEmpty()) {
                            count++;
                        }
                    }
                }
            }
            assertEquals(8, count);
        }

        @AfterEach
        public void tearDown () {
            DynamoDbHelpers.deleteTable(dynamoFacade, TABLE_NAME);
        }


    }
