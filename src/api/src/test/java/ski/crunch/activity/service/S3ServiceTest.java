package ski.crunch.activity.service;


import com.amazonaws.services.s3.model.ObjectListing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class S3ServiceTest extends  AbstractIntegrationTest {

    private S3Service service;

    @BeforeEach()
    public void setup(){
        super.setup();
        this.service = new S3Service(REGION, super.credentialsProvider);
    }

    @Test
    public void testListBucket(){
        this.service.getS3Client().listBuckets().stream().map(x -> x.getName()).forEach(System.out::println);
    }

    @Test
    public void listActivityBucket(){
        ObjectListing listing = this.service.getS3Client().listObjects("activity-staging.crunch.ski");
        listing.getObjectSummaries().stream().map(x -> x.getKey()).forEach(System.out::println);
    }

    @Test
    public void doesObjectExist(){

        boolean exists = this.service.getS3Client().doesObjectExist("activity-staging.crunch.ski", "b9a90594-b8aa-4440-8ab0-c786924e1d7.pbf");
        assert(exists==true);

    }
}