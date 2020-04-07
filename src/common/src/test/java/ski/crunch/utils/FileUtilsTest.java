package ski.crunch.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class FileUtilsTest {


    @Test
    public void deleteDirectoryTest()
            throws IOException {

        // set up -> create a directory structure in tmp dir
        File TEMP_DIR = new File(System.getProperty("java.io.tmpdir"));
        File DIR_NAME = new File(TEMP_DIR, "deletedirtest");
        File dirToDelete = new File(System.getProperty("java.io.tmpdir"), "deletedirtest");
        DIR_NAME.mkdir();
        File subDirToDelete = new File(DIR_NAME, "subdir");
        subDirToDelete.mkdir();
        File fileInSubDir = new File(subDirToDelete, "file1.json");
        File fileInSubDir2 = new File(subDirToDelete, "file2.json");
        File fileInRoot = new File(DIR_NAME, "fileroot.json");
        FileUtils.writeStringToFile("test string", fileInRoot);
        FileUtils.writeStringToFile("test string", fileInSubDir);
        FileUtils.writeStringToFile("test string", fileInSubDir2);

        boolean result = FileUtils.deleteDirectory(DIR_NAME);
        Path dirPath = DIR_NAME.toPath();
        assertTrue(result);
        assertFalse(
                Files.exists(dirPath),
                "Directory still exists");

    }
}