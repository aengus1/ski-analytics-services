package ski.crunch.aws;


import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import ski.crunch.aws.testhelpers.AbstractAwsTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class S3ServiceTest extends AbstractAwsTest {

    private S3Service service;

    @BeforeEach()
    public void setup(){
        super.setup();
        this.service = new S3Service(REGION, super.credentialsProvider);
    }

    @Test
    void testListBucket(){
        this.service.getS3Client().listBuckets().stream().map(Bucket::getName).forEach(System.out::println);
    }

    @Test
     void listActivityBucket(){
        ObjectListing listing = this.service.getS3Client().listObjects("activity-staging.crunch.ski");
        listing.getObjectSummaries().stream().map(S3ObjectSummary::getKey).forEach(System.out::println);
    }

    @Disabled
    @Test
     public void doesObjectExist(){

        boolean exists = this.service.getS3Client().doesObjectExist("activity-staging.crunch.ski", "b9a90594-b8aa-4440-8ab0-c786924e1d7.pbf");
        assertTrue(exists);

    }
}
