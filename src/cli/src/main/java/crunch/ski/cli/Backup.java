package crunch.ski.cli;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import crunch.ski.cli.config.Config;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;
import ski.crunch.aws.S3Facade;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;


@Command(name = "backup",
        aliases = {"bck"},
        mixinStandardHelpOptions = true,
        version = "1.0.0",
        description = "Creates a backup dump of user data from a live environment to file or another S3 bucket")
public class Backup implements Callable<Integer> {


    //private static Logger logger = LogManager.getLogger(Backup.class);


    @ParentCommand
    private App parent;

    @Option(names = {"-e", "--env"}, description = "Name of the environment to backup.  E.g. dev, ci, prod")
    private String environment;

    @Option(names = {"-f", "--dest-dir"}, description = "")
    private File destinationFile;

    private Config config = new Config();

    @Override
    public Integer call() throws Exception {
        System.out.println("Backing up data....");
        // pre backup checks:  //connectivity, free space on destination, permissions,
        // write backup metadata to dest -> ts, user, params, config, etc
        // individual users or full ?
        // if full....
        //  backup raw-activity bucket
        // backup processed-activity bucket
        // backup dynamodb tables
        // backup cognito data
        // checksums...
        backupS3BucketToFile("dev-activity-crunch-ski", "/tmp/");


        return 0;
    }


    public void backupS3BucketToFile(String bucketName, String destDir) throws Exception {
        Map<String, String> configMap = config.readConfiguration();
        ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider(configMap.get("PROFILE_NAME"));
        S3Facade s3Facade = new S3Facade(configMap.get("DATA_REGION"), credentialsProvider);
        List<String> objectKeys = s3Facade.listObjects(bucketName);
        String tmpDirKey = String.valueOf(System.currentTimeMillis());
        File tmpDir = new File(System.getProperty("java.io.tmpdir"), tmpDirKey);
        tmpDir.mkdir();

        for (String objectKey : objectKeys) {

            S3Object o = s3Facade.getS3Client().getObject(bucketName, objectKey);
            try (S3ObjectInputStream s3is = o.getObjectContent()) {
                try (FileOutputStream fos = new FileOutputStream(new File(tmpDir, objectKey))) {
                    byte[] read_buf = new byte[1024];
                    int read_len = 0;
                    while ((read_len = s3is.read(read_buf)) > 0) {
                        fos.write(read_buf, 0, read_len);
                    }
                }
            }
        }
    }



}
