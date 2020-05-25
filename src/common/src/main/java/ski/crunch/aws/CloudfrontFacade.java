package ski.crunch.aws;

import com.amazonaws.services.cloudfront.AmazonCloudFront;
import com.amazonaws.services.cloudfront.AmazonCloudFrontClient;
import com.amazonaws.services.cloudfront.model.GetDistributionRequest;
import com.amazonaws.services.cloudfront.model.NoSuchDistributionException;

public class CloudfrontFacade {

    private AmazonCloudFront cloudfront;

    public CloudfrontFacade(String region) {
        cloudfront = AmazonCloudFrontClient.builder().withRegion(region).build();
    }
    public boolean cfDistroExists(String cfId) {
        GetDistributionRequest request = new GetDistributionRequest();
        request.setId(cfId);

        try {
            cloudfront.getDistribution(request);
            return true;
        }catch (NoSuchDistributionException ex) {
            return false;
        }
    }
}
