package ski.crunch.testhelpers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class TerraformPropertiesReader {

    public enum TerraformStack {
        SHARED(false),
        DATA(true),
        APPLICATION(false),
        ADMIN(false),
        GLOBAL(true);

        private boolean isJson = false;

        private TerraformStack(boolean isJson) {
            this.isJson = isJson;
        }
    }

    public static String getTerraformVariable(String variableName, TerraformStack stack) throws IOException {
        return getTerraformVariable(variableName, stack, "dev");
    }

    public static String getTerraformVariable(String variableName, TerraformStack stack, String stage) throws IOException {
        File srcDir = TestUtils.getSrcDirPath();
        File rootDir = srcDir.getParentFile();
        File infraDir = new File(rootDir, "infra");
        String variableFilePath = null;
        switch (stack) {
            case GLOBAL: {
                variableFilePath = "stacks/global.tfvars.json";
                break;
            }
            case SHARED: {
                variableFilePath = "stacks/shared/terraform.tfvars";
                break;
            }
            case DATA: {
                variableFilePath = "stacks/data/" + stage + ".tfvars.json";
                break;
            }
            case APPLICATION: {
                variableFilePath = "stacks/application/" + stage + ".terraform.tfvars";
                break;
            }
            case ADMIN: {
                variableFilePath = "stacks/admin/terraform.tfvars";
                break;
            }

        }
        File globalVars = new File(infraDir, variableFilePath);
        FileReader fr = new FileReader(globalVars);

        Properties properties = new Properties();
        if (stack.isJson) {
            BufferedReader br = new BufferedReader(fr);
            String line = "";
            while ((line = br.readLine()) != null) {
                if (line.length() < 3) {
                    continue;
                }
                line = line.replaceAll("\"","").replaceAll(" ","");
                String[] prop = line.split(":");
                properties.put(prop[0], prop[1].replace(",", ""));
            }
        } else {
            properties.load(fr);
        }

        return properties.containsKey(variableName) ? properties.get(variableName).toString() : "";
    }

    public static void main(String[] args) throws IOException {
        System.out.println(getTerraformVariable("stage", TerraformStack.GLOBAL));
        System.out.println(getTerraformVariable("primary_region", TerraformStack.GLOBAL));
    }
}
