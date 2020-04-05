package crunch.ski.cli;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ski.crunch.aws.S3Facade;
import ski.crunch.utils.ChecksumFailedException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class S3Backup {

    public static final Logger logger = LoggerFactory.getLogger(S3Backup.class);
    private S3Facade s3Facade;


    public S3Backup(String region, AWSCredentialsProvider credentialsProvider, boolean transferAcceleration) {
        this.s3Facade = new S3Facade(region, credentialsProvider, transferAcceleration);
    }

    /**
     * Copies all contents of a bucket to "java.io.tmpdir"/bucketName-backupId and performs checksum validation
     * @param backupId
     * @param bucketName
     * @throws Exception
     */
    public void backupS3BucketToTempDir(String backupId, String bucketName) throws IOException, ChecksumFailedException {
        List<String> objectKeys = s3Facade.listObjects(bucketName);
        String tmpDirKey = bucketName + "-" + backupId;
        File tmpDir = new File(System.getProperty("java.io.tmpdir"), tmpDirKey);
        tmpDir.mkdir();

        for (String objectKey : objectKeys) {
            File destFile = new File(tmpDir, objectKey);
            S3Object o = s3Facade.getS3Client().getObject(bucketName, objectKey);

            try (S3ObjectInputStream s3is = o.getObjectContent()) {
                try (FileOutputStream fos = new FileOutputStream(destFile)) {
                    byte[] read_buf = new byte[1024];
                    int read_len = 0;
                    while ((read_len = s3is.read(read_buf)) > 0) {
                        fos.write(read_buf, 0, read_len);
                    }
                }
            }
            //checksum validation
            logger.info("checking md5...");
            logger.info("file: " + DigestUtils.md5Hex(new FileInputStream(destFile)));
            logger.info("s3  : " + o.getObjectMetadata().getETag());
            if (!DigestUtils.md5Hex(new FileInputStream(destFile)).equals(o.getObjectMetadata().getETag())) {
                throw new ChecksumFailedException("md5 checksum failed for " + objectKey);
            }
        }
    }

    public S3Facade getS3Facade() {
        return this.s3Facade;
    }
}
