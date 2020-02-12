package ski.crunch.testhelpers;

import ski.crunch.utils.NotFoundException;

import java.io.File;

public class TestUtils {

    public static File getSrcDirPath() {
        String buildPath = IntegrationTestHelper.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        int i = 0;
        File srcDirFile = new File(IntegrationTestHelper.class.getResource("../").getFile());

        while (srcDirFile.getParent() != null && !srcDirFile.getParent().endsWith("/src") && i < 20) {
            i++;
            srcDirFile = srcDirFile.getParentFile();
        }
        if (i == 20 || srcDirFile.getPath().equals("/")) {
            throw new NotFoundException("Error locating module source directory");
        }

        return srcDirFile.getParentFile();
    }

}
