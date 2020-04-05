package ski.crunch.aws;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import ski.crunch.utils.MissingRequiredParameterException;

import java.util.Optional;

public class CredentialsProviderFactory {

    private static final CredentialsProviderFactory instance = new CredentialsProviderFactory();

    private CredentialsProviderFactory() {
    }

    public static AWSCredentialsProvider getDefaultCredentialsProvider() {
        return DefaultAWSCredentialsProviderChain.getInstance();
    }

    public static CredentialsProviderFactory getInstance() {
        return instance;
    }

    public AWSCredentialsProvider newCredentialsProvider(CredentialsProviderType type) throws MissingRequiredParameterException{
       return this.newCredentialsProvider(type, Optional.empty());
    }

    public AWSCredentialsProvider newCredentialsProvider(CredentialsProviderType type, Optional<String> profileName) throws MissingRequiredParameterException{
        switch(type) {
            case DEFAULT:
                return DefaultAWSCredentialsProviderChain.getInstance();
            case PROFILE:
                return new ProfileCredentialsProvider(profileName.orElseThrow(() -> new MissingRequiredParameterException("Profile name is required " +
                        "to instantiate a new profile credentials provder")));
        }
        return null;
    }
}
