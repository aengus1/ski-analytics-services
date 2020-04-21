package crunch.ski.cli.services;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;
import crunch.ski.cli.model.Metadata;
import crunch.ski.cli.model.RestoreOptions;
import org.apache.commons.io.FileUtils;
import ski.crunch.aws.CredentialsProviderFactory;
import ski.crunch.aws.DynamoFacade;
import ski.crunch.aws.S3Facade;
import ski.crunch.model.ActivityItem;
import ski.crunch.model.UserSettingsItem;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class LocalRestoreService implements BackupRestoreService {

    private RestoreOptions options;
    private AWSCredentialsProvider credentialsProvider;
    private String userTableName;
    private String activityTableName;
    private S3Facade s3Facade;
    private DynamoFacade dynamoFacade;
    private BackupRestoreService restoreService;


    public LocalRestoreService(RestoreOptions options) {
        this.options = options;
        this.credentialsProvider = CredentialsProviderFactory.getDefaultCredentialsProvider();
        this.userTableName = calcTableName(USER_TABLE_IDENTIFIER, options);
        this.activityTableName = calcTableName(ACTIVITY_TABLE_IDENTIFIER, options);
        this.s3Facade = new S3Facade(options.getConfigMap().get("DATA_REGION"), credentialsProvider, options.isTransferAcceleration());
        this.dynamoFacade = new DynamoFacade(options.getConfigMap().get("DATA_REGION"), userTableName, credentialsProvider);
    }

    @Override
    public int apply() {
        return 0;
    }


    private InjectableValues newActivityDeserializerInjectables() {
        DynamoFacade dynamoFacade = new DynamoFacade(options.getConfigMap().get("DATA_REGION"), userTableName, credentialsProvider);
        return new InjectableValues.Std()
                .addValue("mapper", dynamoFacade.getMapper())
                .addValue("region", options.getConfigMap().get("DATA_REGION"))
                .addValue("proc_bucket", calcBucketName(ACTIVITY_BUCKET, options))
                .addValue("raw_bucket", calcBucketName(RAW_ACTIVITY_BUCKET, options));
    }

    /**
     * Performs full backup to local file system
     *
     * @throws Exception on error
     */
    private void fullLocalRestore(Metadata metadata) throws Exception {
        String usersJson = FileUtils.readFileToString(new File(options.getSourceDir(), USER_FILENAME), StandardCharsets.UTF_8);
        String activitiesJson = FileUtils.readFileToString(new File(options.getSourceDir(), ACTIVITY_FILENAME), StandardCharsets.UTF_8);
        ObjectMapper mapper = new ObjectMapper();
        List<UserSettingsItem> users = Arrays.asList(mapper.readValue(usersJson, UserSettingsItem[].class));
        List<ActivityItem> activityItems = Arrays.asList(mapper.reader(newActivityDeserializerInjectables()).forType(ActivityItem[].class).readValue(activitiesJson));

        // save to dynamo
        DynamoFacade dynamoFacade = new DynamoFacade(options.getConfigMap().get("DATA_REGION"), userTableName,
                options.isOverwrite() ? DynamoDBMapperConfig.SaveBehavior.CLOBBER : DynamoDBMapperConfig.SaveBehavior.UPDATE);

        dynamoFacade.getMapper().batchSave(users);
        dynamoFacade.updateTableName(activityTableName);
        dynamoFacade.getMapper().batchSave(activityItems);

        // restore s3
        File rawDir = new File(options.getSourceDir(), RAW_ACTIVITY_FOLDER);
        File procDir = new File(options.getSourceDir(), PROCESSED_ACTIVITY_FOLDER);
        for (ActivityItem activityItem : activityItems) {
            if (activityItem.getRawActivity() != null) {
                System.out.println("uploading " + activityItem.getRawActivity().getKey());
                activityItem.getRawActivity().uploadFrom(new File(rawDir, activityItem.getRawActivity().getKey()));
            }
            if (activityItem.getProcessedActivity() != null) {
                System.out.println("uploading " + activityItem.getProcessedActivity().getKey());
                activityItem.getProcessedActivity().uploadFrom(new File(procDir, activityItem.getProcessedActivity().getKey()));
            }
        }
    }

    public S3Facade getS3() {
        return s3Facade;
    }

    public DynamoFacade getDynamo() {
        return dynamoFacade;
    }
}
