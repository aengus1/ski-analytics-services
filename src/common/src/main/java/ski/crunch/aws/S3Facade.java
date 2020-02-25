package ski.crunch.aws;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;


/**
 * Created by aengusmccullough on 2018-09-14.
 */
public class S3Facade {

    private AmazonS3 s3Client;
    private static final Logger LOG = Logger.getLogger(S3Facade.class);

    public S3Facade(String region) {
        LOG.info("creating S3 service in " + region);
        this.s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(region)
                //.withCredentials(new ProfileCredentialsProvider())
                .build();
    }

    public S3Facade(String region, AWSCredentialsProvider credentialsProvider) {
        this.s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .withCredentials(credentialsProvider)
                .build();
    }

    public boolean doesObjectExist(String bucket, String key) {
        boolean exists = this.s3Client.doesObjectExist(bucket, key);
        LOG.info("confirmed that " +  key + " in "  + bucket + " exists " + exists);
        return exists;
    }

    public byte[] getObject(String bucket, String key) throws IOException {

        S3Object object = this.s3Client.getObject(new GetObjectRequest(bucket, key));

        return IOUtils.toByteArray(object.getObjectContent());
    }

    public InputStream getObjectAsInputStream(String bucket, String key) {
        return this.s3Client.getObject(new GetObjectRequest(bucket, key)).getObjectContent();
    }

    public void saveObjectToTmpDir(String bucket, String key) throws IOException {

            S3Object o = this.s3Client.getObject(bucket, key);
            try(S3ObjectInputStream s3is = o.getObjectContent()) {
                try (FileOutputStream fos = new FileOutputStream(new File("/tmp", key))) {
                    byte[] read_buf = new byte[1024];
                    int read_len = 0;
                    while ((read_len = s3is.read(read_buf)) > 0) {
                        fos.write(read_buf, 0, read_len);
                    }
                }
            }
    }

    public void putObject(String bucket, String key, InputStream is) throws IOException {
        File scratchFile = File.createTempFile("activity", "tempProto");
        try {
            FileUtils.copyInputStreamToFile(is, scratchFile);
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, key, scratchFile);
            PutObjectResult putObjectResult = this.s3Client.putObject(putObjectRequest);

        } finally {
            if (scratchFile.exists()) {
                scratchFile.delete();
            }
        }

    }

    public void putObject(String bucket, String key, File f) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, key, f);
        PutObjectResult putObjectResult = this.s3Client.putObject(putObjectRequest);
    }

    public void setBucketNotificationConfiguration(String bucket, BucketNotificationConfiguration configuration) {
        this.s3Client.setBucketNotificationConfiguration(bucket, configuration);
    }

    public void deleteBucketNotificationConfiguration(String bucket) {
        BucketNotificationConfiguration configuration = this.s3Client.getBucketNotificationConfiguration(bucket);
        Map<String, NotificationConfiguration> configs = configuration.getConfigurations();
        for (String s : configs.keySet()) {
            configuration.removeConfiguration(s);
        }
        this.s3Client.setBucketNotificationConfiguration(bucket, configuration);
    }

    public void deleteObject(String bucket, String key) throws IOException {
        this.s3Client.deleteObject(bucket, key);
    }

    public AmazonS3 getS3Client() {
        return s3Client;
    }

    public void putObject(InputStream is, String bucketName, String objectKey, long length, String metaTitle) throws IOException {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("application/x-binary");
        metadata.addUserMetadata("x-amz-meta-title", metaTitle);
        metadata.setContentLength(length);
        PutObjectRequest por = new PutObjectRequest(bucketName, objectKey, is, metadata);
        try {
            PutObjectResult result = s3Client.putObject(por);

        } catch (SdkClientException ex) {
            LOG.error(ex);
            throw new IOException(ex);
        }
    }


}
