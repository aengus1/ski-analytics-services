package crunch.ski.cli.services;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

/**
 * loads data into environment
 * perform backup to local env
 * assert data is intact
 * perform restore to previous env
 * assert data equality with initial
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class S3BackupRestoreIntegrationTest {

    @BeforeAll
    public void setup() {

    }

    @Test
    public void testFullBackupNoEncryption() {

    }

    @Test
    public void testFullBackupEncrypted() {

    }

    @Test
    public void testUserBackupNoEncryption() {

    }

    @Test
    public void testUserBackupEncrypted() {

    }

    @AfterAll
    public void tearDown() {

    }



}
