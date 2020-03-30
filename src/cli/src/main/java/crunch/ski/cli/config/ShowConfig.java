package crunch.ski.cli.config;

import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "show", description = "print configuration")
public class ShowConfig implements Callable<Integer> {

    private String homeDir = System.getProperty("user.home");

    @Override
    public Integer call() throws Exception {

        try {
            // set up hidden directory
            File storageDir = new File(homeDir, ".crunch");
            if (!storageDir.exists()) {
                System.out.println("No configuration detected");
                return 0;
            }

            File configFile = new File(storageDir, "config");
            if (!configFile.exists()) {
                System.out.println("No configuration detected");
                return 0;
            }

            try (FileReader fileReader = new FileReader(configFile)) {
                try (BufferedReader bufferedReader = new BufferedReader(fileReader)) {
                    String line;

                    // update configuration
                    while ((line = bufferedReader.readLine()) != null) {
                        String[] kvp = line.split("=");
                        //String key = kvp[0].substring(0, kvp[0].length() - 1);
                        System.out.println(kvp[0] + ":    " + kvp[1]);
                    }
                }
            }
            return 0;
        } catch (Exception ex) {
            ex.printStackTrace();
            return 1;
        }
    }
}