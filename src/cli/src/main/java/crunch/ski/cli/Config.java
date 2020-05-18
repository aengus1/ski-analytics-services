package crunch.ski.cli;

import picocli.CommandLine;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Callable;

/**
 * CLI Command for setting configuration.  Config is stored locally in user's home dir in hidden file .crunch
 * Config will be referenced by other commands on execution to avoid user having to input common variables on each
 * invokation.
 */
@CommandLine.Command(name = "config",
        description = "Configure CLI",
        subcommands = {
                ShowConfig.class
        })
public class Config implements Callable<Integer> {

    private String homeDir = System.getProperty("user.home");
    private File storageDir = new File(homeDir, ".crunch");
    private File configFile = new File(storageDir, "config");
    private Scanner scanner = new Scanner(System.in);

    private enum Variable {
        PROJECT_NAME,
        PROFILE_NAME,
        DATA_REGION
    }

    private Map<Variable, String> values = new HashMap<>();

    @Override
    public Integer call()  {
        try {
            createConfigIfNotExists();

            if (configFile.exists()) {
                updateVariables();
            } else {
                setVariables();
            }
            writeConfig();
        } catch (Exception ex) {
            ex.printStackTrace();
            return 1;
        }
        return 0;
    }

    private void updateVariables() throws IOException {
        try (FileReader fileReader = new FileReader(configFile)) {
            try (BufferedReader bufferedReader = new BufferedReader(fileReader)) {
                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    String[] kvp = line.split("=");
                    String key = kvp[0];
                    for (Variable variable : Variable.values()) {
                        if (variable.name().equalsIgnoreCase(key)) {
                            values.put(variable, kvp[1]);
                            System.out.println(variable.name() + ":    " + kvp[1] + "  [Enter to accept]:");
                            String input = scanner.nextLine();
                            if (!"".equals(input)) {
                                values.put(variable, input);
                            }
                        }
                    }
                }
            }
        }
    }

    private void setVariables() {
        for (Variable variable : Variable.values()) {
            setVariable(variable);
        }
    }

    private void setVariable(Variable variable) {
        System.out.println(variable + " [Enter a value to continue]:");
        String input = scanner.nextLine();
        if (input.isEmpty()) {
            setVariable(variable);
        } else {
            values.put(variable, input);
        }
    }

    private void createConfigIfNotExists() throws IOException {

        if (!storageDir.exists()) {
            storageDir.mkdir();
            System.out.println("No configuration detected");
        }

        File configFile = new File(storageDir, "config");
        if (!configFile.exists()) {
            System.out.println("No configuration detected");
        }
        try (FileReader fr = new FileReader(configFile)) {
            try (BufferedReader br = new BufferedReader(fr)) {
                if (br.read() == -1) {
                    configFile.delete();
                }
            }
        }
    }


    private void writeConfig() throws IOException {
        FileWriter fileWriter = new FileWriter(configFile);
        try {
            for (Variable variables : values.keySet()) {
                fileWriter.write(variables.name() + "=" + values.get(variables) + System.lineSeparator());
            }
        } finally {
            fileWriter.flush();
            fileWriter.close();
        }
    }

    public Map<String, String> readConfiguration() throws Exception {

        Map<String, String> result = new HashMap<>();
        try (FileReader fileReader = new FileReader(configFile)) {
            try (BufferedReader bufferedReader = new BufferedReader(fileReader)) {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    String[] kvp = line.split("=");
                    result.put(kvp[0], kvp[1]);
                }
            }
        }
        return result;
    }
}
