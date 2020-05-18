package ski.crunch.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.GeneralSecurityException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class EncryptionUtilsTest {

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
    private static final File sourceFile = new File(System.getProperty("java.io.tmpdir"), "src.json");
    private static final String secretKey = "mysecretKey!";

    @BeforeEach
    public void setup() throws IOException{
        //write plain
        FileUtils.writeStringToFile(jsonToWrite, sourceFile);
    }

    @Test
    public void testEncryptDecrypt() {
        String encrypted = EncryptionUtils.encrypt(jsonToWrite.getBytes(StandardCharsets.UTF_8), secretKey);
        assertNotEquals(jsonToWrite, encrypted);

        String decrypted = EncryptionUtils.decrypt(encrypted, secretKey);

        assertEquals(jsonToWrite, decrypted);
    }
    @Test
    public void testCopyEncrypt() throws IOException, GeneralSecurityException {

        File encryptedDest = new File(System.getProperty("java.io.tmpdir"), "encrypted_dest.json");

        File decryptedDest = new File(System.getProperty("java.io.tmpdir"), "decrypted.json");
        EncryptionUtils.copyEncrypt(sourceFile, encryptedDest, secretKey);
        String encrypted = FileUtils.readFileToString(encryptedDest);

        // ensure copied file is encrypted
        assertNotEquals(jsonToWrite, encrypted);

        EncryptionUtils.copyDecrypt(encryptedDest, decryptedDest, secretKey);
        String expected = FileUtils.readFileToString(decryptedDest);

        //ensure decrypted text matches original
        assertEquals(expected.replaceAll(" ", "").replaceAll(System.lineSeparator(), ""),
                jsonToWrite.replaceAll(" ", "").replaceAll(System.lineSeparator(), ""));

        Files.delete(new File(System.getProperty("java.io.tmpdir"), "encrypted_dest.json").toPath());
        Files.delete(new File(System.getProperty("java.io.tmpdir"), "decrypted.json").toPath());
    }

    @AfterEach
    public void tearDown() throws IOException {
        Files.delete(sourceFile.toPath());
    }
}
