package ski.crunch.aws;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import com.amazonaws.services.cognitoidp.model.*;

public class CognitoFacade {

    private AWSCognitoIdentityProvider cognitoIdpClient;
    private String region;

    public CognitoFacade(String region) {
        this.region = region;
        this.cognitoIdpClient = AWSCognitoIdentityProviderClientBuilder.standard().withRegion(region).build();

    }

    public UpdateUserPoolResult updateUserPool(UpdateUserPoolRequest updateUserPoolRequest) {

        return cognitoIdpClient.updateUserPool(updateUserPoolRequest);
    }

    public DescribeUserPoolResult describeUserPool(DescribeUserPoolRequest describeUserPoolRequest) {
        return cognitoIdpClient.describeUserPool(describeUserPoolRequest);
    }

    public GetUserResult getUser(GetUserRequest getUserRequest) {
        return cognitoIdpClient.getUser(getUserRequest);
    }

    public AdminGetUserResult adminGetUser(AdminGetUserRequest getUserRequest) throws UserNotFoundException {
        return cognitoIdpClient.adminGetUser(getUserRequest);
    }

    public boolean userPoolExists(String userpoolId) {
        DescribeUserPoolRequest request = new DescribeUserPoolRequest();
        request.setUserPoolId(userpoolId);
        request.setRequestCredentialsProvider(CredentialsProviderFactory.getDefaultCredentialsProvider());
        try {
            describeUserPool(request);
            return true;
        } catch (ResourceNotFoundException ex) {
            return false;
        }
    }

    public boolean userPoolHasDomainNameConfigured(String userPoolId) {
        DescribeUserPoolRequest request = new DescribeUserPoolRequest();
        request.setUserPoolId(userPoolId);
        request.setRequestCredentialsProvider(CredentialsProviderFactory.getDefaultCredentialsProvider());
        try {
            DescribeUserPoolResult result = describeUserPool(request);
            String domain = result.getUserPool().getDomain();
            System.out.println("userpool domain = " + domain);
            return true;
        }catch(ResourceNotFoundException ex) {
            return false;
        }
    }
}
