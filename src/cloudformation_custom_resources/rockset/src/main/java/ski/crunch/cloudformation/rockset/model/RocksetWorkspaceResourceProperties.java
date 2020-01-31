package ski.crunch.cloudformation.rockset.model;

import ski.crunch.cloudformation.ResourceProperties;
import ski.crunch.utils.MissingRequiredParameterException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ski.crunch.cloudformation.rockset.model.RocksetIntegrationResourceProperties.DEFAULT_ROCKSET_API_SERVER;

public class RocksetWorkspaceResourceProperties  extends ResourceProperties {
    private String name;
    private String region;
    private String apiKeySSM;
    private String apiServer;
    private Optional<String> description;


    public RocksetWorkspaceResourceProperties(Map<String, Object> input) {
        List<String> requiredParameters = Stream.of(
                "Name",
                "Region",
                "ApiKeySSM"
        ).collect(Collectors.toList());

        checkRequiredParameters(input, requiredParameters);

        try{
            this.name = (String) input.get("Name");
            this.region = (String) input.get("Region");
            this.apiKeySSM = (String) input.get("ApiKeySSM");
            this.apiServer = (String) input.getOrDefault("ApiServer", DEFAULT_ROCKSET_API_SERVER);
            this.description = input.containsKey("Description") ? (Optional.of((String) input.get("Description"))) : Optional.empty();
        }catch(Exception ex) {
            ex.printStackTrace();
            throw new MissingRequiredParameterException("Parameter missing in resourceProperties", ex);
        }
    }

    public String getName() {
        return name;
    }

    public String getRegion() {
        return region;
    }

    public String getApiKeySSM() {
        return apiKeySSM;
    }

    public Optional<String> getDescription() {
        return description;
    }

    public String getApiServer(){
        return this.apiServer;
    }
}
