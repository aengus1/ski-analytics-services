package ski.crunch.utils;

import com.amazonaws.util.json.Jackson;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.GeneralSecurityException;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CryptoInputOutputStreamTest {

    private static final String secretKey = "mysecretKey!";
    private static final File destFile = new File(System.getProperty("java.io.tmpdir"), "encryption_result.json");
    private static final File sourceFile = new File(System.getProperty("java.io.tmpdir"), "encryption_source.json");
    private static final String jsonToWrite = "[ {\n" +
            "  \"cognitoId\" : \"778\",\n" +
            "  \"whtae\" : \" no\",\n" +
            "  \"id\" : \"4566\",\n" +
            "  \"rawActivity\" : \"linux_kernel.pdf\",\n" +
            "  \"processedActivity\" : \"behavioural_answers.docx\"\n" +
            "}\n" +
            ",{\n" +
            "  \"cognitoId\" : \"123\",\n" +
            "  \"activitydata\" : \"yes\",\n" +
            "  \"id\" : \"123435\"\n" +
            "} ]";

    @BeforeAll
    public void setup() throws IOException, GeneralSecurityException {

        // write encrypted
        try (CryptoFileOutputStream cryptoFileOutputStream = new CryptoFileOutputStream(destFile, secretKey)) {
            cryptoFileOutputStream.write(jsonToWrite.getBytes(StandardCharsets.UTF_8));
        }
    }


    @Test
    @Order(1)
    public void testEncryption() throws IOException {
        StringBuilder sb = new StringBuilder();
        try (FileReader fileReader = new FileReader(destFile)) {
            try (BufferedReader bufferedReader = new BufferedReader(fileReader)) {
                String encrypted = null;
                while ((encrypted = bufferedReader.readLine()) != null) {
                    sb.append(encrypted);
                }
            }
        }
        System.out.println("encrypted = " + sb.toString());
        assertNotEquals(jsonToWrite, sb.toString());
    }

    @Test
    @Order(2)
    public void testDecryption() throws IOException, GeneralSecurityException {
        StringBuilder sb = new StringBuilder();
        try (CryptoFileInputStream cryptoFileInputStream = new CryptoFileInputStream(destFile, secretKey)) {
            try (InputStreamReader inputStreamReader = new InputStreamReader(cryptoFileInputStream)) {
                try (BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

                    String line = null;
                    while ((line = bufferedReader.readLine()) != null) {
                        System.out.println(line);
                        sb.append(line);
                    }
                }
            }
        }
        System.out.println("decrypted = " + sb.toString());
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        String json = Jackson.jsonNodeOf(sb.toString()).toPrettyString();

        assertEquals(Jackson.jsonNodeOf(jsonToWrite).toPrettyString(), json);
    }


    @Test
    @Order(3)
    public void testDecryptionWithWrongKey() throws IOException, GeneralSecurityException {
        StringBuilder sb = new StringBuilder();
        try (CryptoFileInputStream cryptoFileInputStream = new CryptoFileInputStream(destFile, "notTheCorrectSec5ret")) {
            try (InputStreamReader inputStreamReader = new InputStreamReader(cryptoFileInputStream)) {
                try (BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

                    String line = null;
                    while ((line = bufferedReader.readLine()) != null) {
                        System.out.println(line);
                        sb.append(line);
                    }
                }
            }
        }
        System.out.println("decrypted = " + sb.toString());
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        assertThrows(com.amazonaws.SdkClientException.class, () -> {
            Jackson.jsonNodeOf(sb.toString()).toPrettyString();
        });
    }

    @AfterAll
    public void tearDown() throws IOException {
        Files.delete(destFile.toPath());
        Files.delete(sourceFile.toPath());
    }
}
