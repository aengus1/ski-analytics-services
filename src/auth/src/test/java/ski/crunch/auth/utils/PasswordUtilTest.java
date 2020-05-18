package ski.crunch.auth.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class PasswordUtilTest {

    private static final String PASSWORD = "my*asec!passowrd780";
    private static final String BAD_PASSWORD = "my*asec!!pass0w2kdf!";

    @Test
    public void testPasswordHash() {
        String hash = PasswordUtil.hashPassword(PASSWORD);

        assertTrue(PasswordUtil.verifyPassword(hash, PASSWORD ));
        assertFalse(PasswordUtil.verifyPassword(hash, BAD_PASSWORD));
    }

    @Test
    public void testSalt() {
        String hash = PasswordUtil.hashPassword(PASSWORD);
        String hash2 = PasswordUtil.hashPassword(PASSWORD);
        assertNotEquals(hash, hash2);
    }
}
