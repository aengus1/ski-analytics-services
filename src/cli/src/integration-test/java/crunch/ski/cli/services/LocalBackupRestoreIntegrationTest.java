package crunch.ski.cli.services;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

/**
 * loads data into environment
 * perform backup to local env
 * assert data is intact
 * perform restore to previous env
 * assert data equality with initial
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LocalBackupRestoreIntegrationTest {

    @BeforeAll
    public void setup() {

    }



}
