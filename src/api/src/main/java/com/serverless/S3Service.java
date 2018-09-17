package com.serverless;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;

import java.io.IOException;


/**
 * Created by aengusmccullough on 2018-09-14.
 */
public class S3Service {

    private AmazonS3 s3Client;

    public S3Service(String region) {
        this.s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(region)
                //.withCredentials(new ProfileCredentialsProvider())
                .build();
    }


    public byte[] getObject(String bucket, String key) throws IOException {

        S3Object object = this.s3Client.getObject(new GetObjectRequest(bucket, key));

        return IOUtils.toByteArray(object.getObjectContent());
    }

}
