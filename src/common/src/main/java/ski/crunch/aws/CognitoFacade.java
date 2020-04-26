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

    public DescribeUserPoolResult describeUserPool(DescribeUserPoolRequest describeUserPoolRequest){
        return cognitoIdpClient.describeUserPool(describeUserPoolRequest);
    }

    public GetUserResult getUser(GetUserRequest getUserRequest) {
        return cognitoIdpClient.getUser(getUserRequest);
    }

    public AdminGetUserResult adminGetUser(AdminGetUserRequest getUserRequest) throws UserNotFoundException {
        return cognitoIdpClient.adminGetUser(getUserRequest);
    }
}
