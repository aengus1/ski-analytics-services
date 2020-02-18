package ski.crunch.cloudformation.rockset.model;

import com.amazonaws.services.identitymanagement.model.Tag;
import ski.crunch.cloudformation.ResourceProperties;
import ski.crunch.utils.MissingRequiredParameterException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Data structure for cloudformation template parameters
 */
public class RocksetIntegrationResourceProperties extends ResourceProperties {

    private String name;
    private String region;
    private String apiKeySSM;
    private RocksetIntegrationType integrationType;
    private List<String> accessibleResources;
    private String apiServer;
    private Optional<String> externalId;
    private Optional<String> rocksetAwsAccount;
    private Optional<List<Tag>> tags;
    private String awsAccountId;

    public static final String DEFAULT_ROCKSET_API_SERVER = "api.rs2.usw2.rockset.com";


    public RocksetIntegrationResourceProperties(Map<String, Object> input) {

        List<String> requiredParameters = Stream.of(
                "Name",
                "Region",
                "ApiKeySSM",
                "AccessibleResources",
                "RocksetAccountId",
                "IntegrationType",
                "ExternalId"
        ).collect(Collectors.toList());
        this.awsAccountId = System.getenv("awsAccountId");
        checkRequiredParameters(input, requiredParameters);
        try {
            this.name = (String) input.get("Name");
            this.region = (String) input.get("Region");
            this.apiKeySSM = (String) input.get("ApiKeySSM");
            this.accessibleResources = (List) (input.get("AccessibleResources"));
            this.apiServer = (String) input.getOrDefault("ApiServer", DEFAULT_ROCKSET_API_SERVER);
            this.rocksetAwsAccount = Optional.of((String) input.getOrDefault("RocksetAccountId", Optional.empty()));
            this.externalId = Optional.of((String) input.getOrDefault("ExternalId", Optional.empty()));
            this.integrationType = RocksetIntegrationType.valueOf((String) input.get("IntegrationType"));
            if (input.containsKey("Tags")) {
                tags = Optional.of(new ArrayList<>());
                List<LinkedHashMap<String, String>> tagMap = (List) input.get("Tags");
                for (Map<String, String> m : tagMap) {
                    Tag t = new Tag();
                    t.setKey(m.get("Key"));
                    t.setValue(m.get("Value"));
                    tags.get().add(t);
                }
            } else {
                tags = Optional.empty();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new MissingRequiredParameterException("Parameter missing in resourceProperties", ex);
        }
    }


    public List<String> getAccessibleResources() {
        return this.accessibleResources;
    }

    public String getName() {
        return name;
    }

    public String getRegion() {
        return region;
    }

    public Optional<List<Tag>> getTags() {
        return this.tags;
    }

    public String getApiKeySSM() {
        return apiKeySSM;
    }

    public RocksetIntegrationType getIntegrationType() {
        return integrationType;
    }


    public String getApiServer() {
        return apiServer;
    }

    public Optional<String> getExternalId() {
        return externalId;
    }

    public Optional<String> getRocksetAwsAccount() {
        return rocksetAwsAccount;
    }

    public String getAwsAccountId() {
        return this.awsAccountId;
    }

    public void setAwsAccountId(String awsAccountId){
        this.awsAccountId = awsAccountId;
    }


}