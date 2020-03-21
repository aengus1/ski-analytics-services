package ski.crunch.aws;


import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class S3FacadeTest {

    private S3Facade service;
    public static final String REGION = "ca-central-1"; // this is fragile
    private static final String AWS_PROFILE="backend_dev";

    @BeforeEach()
    public void setup(){
        ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider(AWS_PROFILE);
        this.service = new S3Facade(REGION, credentialsProvider);
    }

    @Test
    void testListBuckets(){
        this.service.getS3Client().listBuckets().stream().map(Bucket::getName).forEach(System.out::println);
    }

    @Test
     void listActivityBucket(){
        ObjectListing listing = this.service.getS3Client().listObjects("activity-staging.crunch.ski");
        listing.getObjectSummaries().stream().map(S3ObjectSummary::getKey).forEach(System.out::println);
    }

    @Test
     public void doesObjectExist(){

        boolean exists = this.service.getS3Client().doesObjectExist("activity-staging.crunch.ski", "b9a90594-b8aa-4440-8ab0-c786924e1d7.pbf");
        assertTrue(exists);

    }
}
