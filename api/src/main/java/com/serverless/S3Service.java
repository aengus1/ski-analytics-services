package com.serverless;
import com.amazonaws.services.s3.AmazonS3;


/**
 * Created by aengusmccullough on 2018-09-14.
 */
public class S3Service {

    private AmazonS3 s3Client;

    public S3Service() {
        this.s3Client = AmazonS3ClientBuilder.standard();
    }


    public byte[] getObject(String region, String bucket, String key) {
return null;
    }

}
