package ski.crunch.cloudformation.services;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.rockset.client.RocksetClient;
import com.rockset.client.model.AwsKeyIntegration;
import com.rockset.client.model.CreateIntegrationRequest;
import com.rockset.client.model.CreateIntegrationResponse;

/**
 * Rockset client docs are here -> https://docs.rockset.com/java/
 */
public class RocksetService {

    private RocksetClient client;
    private AWSCredentialsProvider rocksetCredentialsProvider;
    private static final String API_SERVER = "api.rs2.usw2.rockset.com";

    public RocksetService(String rocksetApiKey, AWSCredentialsProvider rocksetCredentialsProvider) {
        this.client = new RocksetClient(rocksetApiKey, API_SERVER);
        this.rocksetCredentialsProvider = rocksetCredentialsProvider;

    }

    public void createIntegration(String integrationName) throws Exception {
        CreateIntegrationRequest request = new CreateIntegrationRequest()
                .name("my-first-integration")
                .aws(new AwsKeyIntegration()
                        .awsAccessKeyId(rocksetCredentialsProvider.getCredentials().getAWSAccessKeyId())
                        .awsSecretAccessKey(rocksetCredentialsProvider.getCredentials().getAWSSecretKey()));
        CreateIntegrationResponse response = client.createIntegration(request);
    }
}
