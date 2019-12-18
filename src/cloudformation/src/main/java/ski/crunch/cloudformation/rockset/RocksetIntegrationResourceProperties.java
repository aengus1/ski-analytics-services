package ski.crunch.cloudformation.rockset;

import com.amazonaws.services.identitymanagement.model.Tag;
import ski.crunch.utils.MissingRequiredParameterException;

import java.util.*;

/**
 * Data structure for cloudformation template parameters
 */
public class RocksetIntegrationResourceProperties {

    private String name;
    private String region;
    private String apiKeySSM;
    private RocksetIntegrationType integrationType;
    private List<String> accessibleResources;
    private String apiServer;
    private Optional<String> externalId;
    private Optional<String> rocksetAwsAccount;
    private Optional<List<Tag>> tags;

    private static final String DEFAULT_ROCKSET_API_SERVER = "api.rs2.usw2.rockset.com";


    public RocksetIntegrationResourceProperties(Map<String, Object> input) {

        checkRequiredParameters(input);
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


    private void checkRequiredParameters(Map<String, Object> input) throws MissingRequiredParameterException {
        if (input == null) {
            throw new MissingRequiredParameterException("No ResourceProperties found");
        }
        checkParameter("Name", input);
        checkParameter("Region", input);
        checkParameter("ApiKeySSM", input);
        checkParameter("AccessibleResources", input);
        checkParameter("ExternalId", input);
        checkParameter("RocksetAccountId", input);
        checkParameter("IntegrationType", input);
    }


    private void checkParameter(String parameter, Map<String, Object> input) throws MissingRequiredParameterException {
        if (!input.containsKey(parameter)) {
            throw new MissingRequiredParameterException("Parameter " + parameter + " not supplied");
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


}

