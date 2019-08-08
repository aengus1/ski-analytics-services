package ski.crunch.aws;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterRequest;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterResult;
import org.apache.log4j.Logger;

public class SSMParameterService {
    AWSSimpleSystemsManagement ssmClient;
    String region;
    private static final Logger LOG = Logger.getLogger(SSMParameterService.class);

    public SSMParameterService(String region, AWSCredentialsProvider credentialsProvider) {
        this.region = region;
        this.ssmClient = AWSSimpleSystemsManagementClientBuilder.standard().withRegion(region).withCredentials(credentialsProvider).build();
    }


    public String getParameter( String name) {
        LOG.info("fetching SSM parameter: " + name);
        GetParameterRequest request= new GetParameterRequest();
        request.withName(name)
                .setWithDecryption(false);

        GetParameterResult result = ssmClient.getParameter(request);
        return result.getParameter().getValue();
    }
}
