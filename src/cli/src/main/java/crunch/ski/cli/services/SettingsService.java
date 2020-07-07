package crunch.ski.cli.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import crunch.ski.cli.model.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ski.crunch.utils.StreamUtils;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class SettingsService {

    private static final Logger logger = LoggerFactory.getLogger(SettingsService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private Options options;

    public SettingsService(Options options) {
        this.options = options;
    }

    public Properties getSettings() throws IOException {
        Properties properties = new Properties();

        //determine infrastructure directory
        String projectSrcDir = options.getConfigMap().get("PROJECT_SOURCE_DIR");
        File infraDir = new File(projectSrcDir + "/infra/envs");
        //logger.info("Infra directory: {}", infraDir.getAbsolutePath());

        //parse data settings
        File envDir = new File(infraDir, options.getEnvironment());
        File dataDir = new File(envDir, "data");
        File dataSettings = new File(dataDir, options.getEnvironment() + ".terraform.tfvars.json");
        JsonNode dataSettingsJson = objectMapper.readTree(dataSettings);
        boolean isProdProfile = dataSettingsJson.get("stage").textValue().equals("prod");
        String authRegion = dataSettingsJson.get("primary_region").textValue();

        //parse global settings
        File globalSettings = new File(infraDir, "global.tfvars.json");
        JsonNode globalSettingsJson = objectMapper.readTree(globalSettings);
        String domainName = globalSettingsJson.get("domain_name").textValue();

        //parse api settings
        File apiDir = new File(envDir, "api");
        File apiSettings = new File(apiDir, options.getEnvironment() + ".terraform.tfvars.json");
        JsonNode apiSettingsJson = objectMapper.readTree(apiSettings);
        String apiDomain = "https://"+apiSettingsJson.get("api_sub_domain").textValue()+"." + domainName +"/"+options.getEnvironment();

        // waiting on terraform issue aws_api_gateway_v2_domain_name before can use custom domain name for ws endpoint
        //  https://github.com/terraform-providers/terraform-provider-aws/pull/9391
        // String wsDomain = apiSettingsJson.get("ws_sub_domain").textValue() + "." + domainName;


        //get terraform data module output
        ProcessRunner processRunner = new ProcessRunner();
        String[] cmdArray = new String[]{"terraform", "show", "-json"};
        processRunner.startProcess(cmdArray, dataDir, false);
        JsonNode terraformShowDataOutput = objectMapper.readTree(processRunner.getInputStream());

        String userPoolId = terraformShowDataOutput.path("values").path("outputs").path("userpool-id").path("value").asText();
        String userPoolClientId = terraformShowDataOutput.path("values").path("outputs").path("userpool-client-id").path("value").textValue();

        //get sls info graphql output
        String graphqlEndpoint = getSlsInfo(projectSrcDir, "graphql", processRunner, "appsync endpoints");

        //get sls info websocket output
        String wsEndpoint = getSlsInfo(projectSrcDir, "websocket", processRunner, "endpoints");


        properties.put("isProdProfile", isProdProfile);
        properties.put("authRegion", authRegion);
        properties.put("domain", domainName);

        properties.put("userPoolId", userPoolId);
        properties.put("userPoolWebClientId", userPoolClientId);

        properties.put("graphQLEndpoint", graphqlEndpoint);
        properties.put("appSyncRegion", authRegion);
        properties.put("apiEndpoint", apiDomain);
        properties.put("wsEndpoint", wsEndpoint);

        return properties;
    }

    private String getSlsInfo(String projectSrcDir, String moduleName, ProcessRunner processRunner, String outputName) throws IOException {
        String slsInfo;
        String[] slsInfoSplit;
        File websocketDir = new File(projectSrcDir, "src/"+moduleName);
        String[] slsCmds = new String[]{"sls", "info", "--stage="+options.getEnvironment()};
        processRunner.startProcess(slsCmds, websocketDir, false);
        slsInfo = StreamUtils.convertStreamToString(processRunner.getInputStream());
        String output = "";
        slsInfoSplit = slsInfo.split(System.lineSeparator());
        for (int i = 0; i< slsInfoSplit.length; i++) {
            if(slsInfoSplit[i].contains(outputName+":")) {
                output = slsInfoSplit[i+1];
                break;
            }
        }
        return output;
    }
}
