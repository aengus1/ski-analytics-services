package ski.crunch.activity.service;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Created by aengusmccullough on 2018-09-14.
 */
public class S3Service {

    private AmazonS3 s3Client;
    private static final Logger LOG = Logger.getLogger(S3Service.class);

    public S3Service(String region) {
        this.s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(region)
                //.withCredentials(new ProfileCredentialsProvider())
                .build();
    }

    public boolean doesObjectExist(String bucket, String key){
        return this.s3Client.doesObjectExist(bucket, key);
    }

    public byte[] getObject(String bucket, String key) throws IOException {

        S3Object object = this.s3Client.getObject(new GetObjectRequest(bucket, key));

        return IOUtils.toByteArray(object.getObjectContent());
    }

    public InputStream getObjectAsInputStream(String bucket, String key) throws IOException {
        return this.s3Client.getObject(new GetObjectRequest(bucket, key)).getObjectContent();
    }

    public void putObject(String bucket, String key, InputStream is) throws IOException{
        File scratchFile = File.createTempFile("activity", "tempProto");
        try {
            FileUtils.copyInputStreamToFile(is, scratchFile);
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, key, scratchFile);
            PutObjectResult putObjectResult = this.s3Client.putObject(putObjectRequest);

        } finally {
            if(scratchFile.exists()) {
                scratchFile.delete();
            }
        }

    }

    public void putObject(String bucket, String key, File f) throws IOException{
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, key, f);
            PutObjectResult putObjectResult = this.s3Client.putObject(putObjectRequest);
    }
    public void deleteObject(String bucket, String key) throws IOException {
         this.s3Client.deleteObject(bucket, key);
    }

    public void putObject(InputStream is, String bucketName, String objectKey, long length, String metaTitle) throws IOException{
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("application/x-binary");
        metadata.addUserMetadata("x-amz-meta-title",metaTitle);
        metadata.setContentLength(length);
        PutObjectRequest por = new PutObjectRequest(bucketName, objectKey, is, metadata);
        try {
            PutObjectResult result = s3Client.putObject(por);

        }catch(SdkClientException ex ){
            LOG.error(ex);
            throw new IOException(ex);
        }
    }



}
