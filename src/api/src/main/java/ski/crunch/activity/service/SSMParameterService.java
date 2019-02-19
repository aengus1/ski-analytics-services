package ski.crunch.activity.service;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterRequest;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterResult;

public class SSMParameterService {
    AWSSimpleSystemsManagement ssmClient;

    public SSMParameterService(String region, AWSCredentialsProvider credentialsProvider) {
        this.ssmClient = AWSSimpleSystemsManagementClientBuilder.standard().withRegion(region).withCredentials(credentialsProvider).build();
    }


    public String getParameter( String name) {

        GetParameterRequest request= new GetParameterRequest();
        request.withName(name)
                .setWithDecryption(true);

        GetParameterResult result = ssmClient.getParameter(request);
        return result.getParameter().getValue();
    }
}
