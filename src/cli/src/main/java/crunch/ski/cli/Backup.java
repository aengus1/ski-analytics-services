package crunch.ski.cli;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;
import ski.crunch.aws.CredentialsProviderFactory;
import ski.crunch.aws.CredentialsProviderType;
import ski.crunch.aws.S3Facade;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;


@Command(name = "backup",
        aliases = {"bck"},
        mixinStandardHelpOptions = true,
        version = "1.0.0",
        description = "Creates a backup dump of user data from a live environment to file or another S3 bucket")
public class Backup implements Callable<Integer> {

    public static final DateTimeFormatter dtf = DateTimeFormatter.ISO_DATE_TIME;
    private static Logger logger = LoggerFactory.getLogger(Backup.class);


    @ParentCommand
    private App parent;

    @Option(names = {"-e", "--env"}, required = true, description = "Name of the environment to backup.  E.g. dev, ci, prod")
    private String environment;

    @Option(names = {"-f", "--dest-dir"}, required = true, description = "Local directory to save backup to")
    private File destDir;

    @Option(names = {"-ta", "--transfer-acceleration"}, description = "Enable S3 Transfer Acceleration")
    private boolean transferAcceleration = false;

    private CredentialsProviderFactory credentialsProviderFactory;
    private AWSCredentialsProvider credentialsProvider;
    private S3Facade s3Facade;
    private Map<String, String> configMap;
    private String backupId;

    public Backup(){

    }


    public Backup(App parent, CredentialsProviderFactory credentialsProviderFactory,
                  Map<String, String> configMap, String environment, File destDir, boolean transferAcceleration) {
        this.parent = parent;
        this.credentialsProviderFactory = credentialsProviderFactory;
        this.configMap = configMap;
        this.environment = environment;
        this.destDir = destDir;
        this.transferAcceleration = transferAcceleration;
    }

    @Override
    public Integer call() throws Exception {
        initialize();
        System.out.println("Backing up data....");
        // pre backup checks:  //connectivity, free space on destination, permissions,
        // write backup metadata to dest -> ts, user, params, config, etc
        // individual users or full ?
        // if full....
        //  backup raw-activity bucket
        // backup processed-activity bucket
        // backup dynamodb tables
        // backup cognito data
        backupS3BucketToFile(configMap, backupId, calcBucketName("activity"));
        backupS3BucketToFile(configMap, backupId, calcBucketName("raw-activity"));

        return 0;
    }


    public String calcBucketName(String bucketType) {
        return new StringBuilder()
                .append(environment).append("-")
                .append(bucketType).append("-")
                .append(configMap.get("PROJECT_NAME"))
                .toString();
    }

    public void backupS3BucketToFile(Map<String, String> configMap, String backupId, String bucketName) throws Exception {
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
                throw new Exception("md5 checksum failed for " + objectKey);
            }
        }
    }


    public String getBackupId() {
        return this.backupId;
    }

    S3Facade getS3Facade() {
        return this.s3Facade;
    }

    void initialize() {
        try {
            Config config = new Config();
            configMap = config.readConfiguration();
            if(credentialsProviderFactory ==null) {
                credentialsProviderFactory = CredentialsProviderFactory.getInstance();
            }
            if(parent.getProjectName()!=null) {
                configMap.put("PROJECT_NAME", parent.getProjectName());
            }
            if(parent.getDataRegion()!=null){
                configMap.put("DATA_REGION", parent.getDataRegion());
            }
            if(parent.getAwsProfile()!=null) {
                configMap.put("PROFILE_NAME", parent.getAwsProfile());
            }
            credentialsProvider =
                    credentialsProviderFactory.newCredentialsProvider(CredentialsProviderType.PROFILE, Optional.of(configMap.get("PROFILE_NAME")));
            s3Facade = new S3Facade(configMap.get("DATA_REGION"), credentialsProvider, transferAcceleration);
            backupId = environment+"-"+dtf.format(LocalDateTime.now());
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("Initialization Error.  Ensure you have run crunch config", ex);
        }
    }


}
