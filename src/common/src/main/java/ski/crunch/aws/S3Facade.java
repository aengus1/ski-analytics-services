package ski.crunch.aws;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Created by aengusmccullough on 2018-09-14.
 */
public class S3Facade {

    private AmazonS3 s3Client;
    private boolean transferAcceleration = false;
    private String region;
    private static final Logger logger = LoggerFactory.getLogger(S3Facade.class);

    public S3Facade(String region) {
        this.region = region;
        logger.info("creating S3 service in " + region);
        this.s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(region)
                //.withCredentials(new ProfileCredentialsProvider())
                .build();
    }

    public S3Facade(String region, boolean transferAcceleration) {
        this.region = region;
        logger.info("creating S3 service in " + region);
        this.transferAcceleration = transferAcceleration;
        this.s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(region)
                //.withCredentials(new ProfileCredentialsProvider())
                .withAccelerateModeEnabled(transferAcceleration)
                .build();
    }

    public S3Facade(String region, AWSCredentialsProvider credentialsProvider) {
        this(region, credentialsProvider, false);
    }

    public S3Facade(String region, AWSCredentialsProvider credentialsProvider, boolean transferAcceleration) {
        this.region = region;
        this.transferAcceleration = transferAcceleration;
        this.s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .withCredentials(credentialsProvider)
                .withAccelerateModeEnabled(transferAcceleration)
                .build();

    }

    public boolean doesObjectExist(String bucket, String key) {
        boolean exists = this.s3Client.doesObjectExist(bucket, key);
        logger.info("confirmed that " + key + " in " + bucket + " exists " + exists);
        return exists;
    }

    public byte[] getObject(String bucket, String key) throws IOException {

        setTransferAcceleration(bucket);
        S3Object object = this.s3Client.getObject(new GetObjectRequest(bucket, key));
        return IOUtils.toByteArray(object.getObjectContent());
    }

    public ObjectMetadata getObjectMetadata(String bucket, String key) throws IOException {
        return s3Client.getObjectMetadata(bucket, key);
    }

    public InputStream getObjectAsInputStream(String bucket, String key) {
        setTransferAcceleration(bucket);
        return this.s3Client.getObject(new GetObjectRequest(bucket, key)).getObjectContent();
    }

    public void saveObjectToTmpDir(String bucket, String key) throws IOException {
        setTransferAcceleration(bucket);
        File dest;
        if(key.contains("/")){
            String[] keyS = key.split("/");
            dest = new File(System.getProperty("java.io.tmpdir")+keyS[0]);
            key = keyS[1];
        }else {
            dest = new File(System.getProperty("java.io.tmpdir"));
        }
        S3Object o = this.s3Client.getObject(bucket, key);
        try (S3ObjectInputStream s3is = o.getObjectContent()) {
            try (FileOutputStream fos = new FileOutputStream(new File(dest, key))) {
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
            setTransferAcceleration(bucket);
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, key, scratchFile);
            PutObjectResult putObjectResult = this.s3Client.putObject(putObjectRequest);

        } finally {
            if (scratchFile.exists()) {
                scratchFile.delete();
            }
        }

    }

    public void putObject(String bucket, String key, File f) {
        setTransferAcceleration(bucket);
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
        setTransferAcceleration(bucket);
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
        setTransferAcceleration(bucketName);
        PutObjectRequest por = new PutObjectRequest(bucketName, objectKey, is, metadata);
        try {
            PutObjectResult result = s3Client.putObject(por);

        } catch (SdkClientException ex) {
            logger.error("exception putting object result", ex);
            throw new IOException(ex);
        }
    }

    public List<String> listObjects(String bucket) {
        setTransferAcceleration(bucket);
        ObjectListing objectListing = this.s3Client.listObjects(bucket);
        return objectListing.getObjectSummaries().stream().map(x -> x.getKey()).collect(Collectors.toList());
    }

    public List<String> listObjects(String bucket, String prefix) {
        setTransferAcceleration(bucket);
        ObjectListing objectListing = this.s3Client.listObjects(bucket, prefix);
        return objectListing.getObjectSummaries().stream().map(x -> x.getKey()).collect(Collectors.toList());
    }

    private void setTransferAcceleration(String bucket) {
        if (transferAcceleration) {
            s3Client.setBucketAccelerateConfiguration(
                    new SetBucketAccelerateConfigurationRequest(bucket,
                            new BucketAccelerateConfiguration(
                                    BucketAccelerateStatus.Enabled)));
        } else {
            s3Client.setBucketAccelerateConfiguration(
                    new SetBucketAccelerateConfigurationRequest(bucket,
                            new BucketAccelerateConfiguration(
                                    BucketAccelerateStatus.Suspended)));
        }
    }

    public String getRegion(){
        return this.region;
    }

   public boolean getTransferAcceleration() {
        return transferAcceleration;
    }

}
