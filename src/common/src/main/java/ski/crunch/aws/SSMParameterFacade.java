package ski.crunch.aws;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import com.amazonaws.services.simplesystemsmanagement.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class SSMParameterFacade {
    AWSSimpleSystemsManagement ssmClient;
    String region;
    private static final Logger logger = LoggerFactory.getLogger(SSMParameterFacade.class);

    public SSMParameterFacade(String region, AWSCredentialsProvider credentialsProvider) {
        this.region = region;
        this.ssmClient = AWSSimpleSystemsManagementClientBuilder.standard().withRegion(region).withCredentials(credentialsProvider).build();
    }


    public String getParameter( String name) {
        logger.info("fetching SSM parameter: " + name);
        GetParameterRequest request= new GetParameterRequest();
        request.withName(name)
                .setWithDecryption(false);

        GetParameterResult result = ssmClient.getParameter(request);
        return result.getParameter().getValue();
    }

    public PutParameterResult putParameter( String name, String value, String description, Optional<List<Tag>> tags ) {
        logger.info("putting SSM parameter: {}", name);
        PutParameterRequest request  = new PutParameterRequest();
        request.withName(name)
                .withValue(value)
                .withType(ParameterType.String)
                .withDescription(description);
                if(tags != null && tags.isPresent()) {
                    request.withTags(tags.get());
                }
        PutParameterResult result = ssmClient.putParameter(request);
                return result;
    }

    public DeleteParameterResult deleteParameter(String name) {
        logger.info("deleting parameter {}", name);
        DeleteParameterRequest request = new DeleteParameterRequest();
        request.withName(name);
        return ssmClient.deleteParameter(request);
    }
}
