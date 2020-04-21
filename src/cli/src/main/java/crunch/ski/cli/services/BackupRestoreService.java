package crunch.ski.cli.services;

import crunch.ski.cli.model.*;
import ski.crunch.aws.DynamoFacade;
import ski.crunch.aws.S3Facade;

import java.net.UnknownHostException;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Locale;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.temporal.ChronoField.*;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;

public interface BackupRestoreService {

    String PROCESSED_ACTIVITY_FOLDER = "processed_activities";
    String RAW_ACTIVITY_FOLDER = "raw_activities";
    String USER_TABLE_IDENTIFIER = "userTable";
    String ACTIVITY_TABLE_IDENTIFIER = "Activity";
    String METADATA_FILENAME = ".metadata.json";
    String ACTIVITY_BUCKET = "activity";
    String RAW_ACTIVITY_BUCKET = "raw-activity";
    String USER_FILENAME = "users.json";
    String ACTIVITY_FILENAME = "activities.json";
    String SSM_FILENAME = "ssm.json";
    DateTimeFormatter ISO_LOCAL_DATE_TIME_FILE = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(ISO_LOCAL_DATE)
            .appendLiteral('T')
            .appendValue(HOUR_OF_DAY, 2)
            .appendLiteral('-')
            .appendValue(MINUTE_OF_HOUR, 2)
            .appendLiteral('-')
            .appendValue(SECOND_OF_MINUTE, 2)
            .toFormatter(Locale.ENGLISH);

    public static final DateTimeFormatter ISO_LOCAL_DATE_TIME_NO_NANO = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(ISO_LOCAL_DATE)
            .appendLiteral('T')
            .appendValue(HOUR_OF_DAY, 2)
            .appendLiteral(':')
            .appendValue(MINUTE_OF_HOUR, 2)
            .appendLiteral(':')
            .appendValue(SECOND_OF_MINUTE, 2)
            .toFormatter(Locale.ENGLISH);

    int apply();


    default String calcTableName(String tableType, RestoreOptions options) {
        return new StringBuilder()
                .append(options.getEnvironment()).append("-")
                .append(options.getConfigMap().get("PROJECT_NAME")).append("-")
                .append(tableType)
                .toString();
    }

    default String calcTableName(String tableType, BackupOptions options) {
        return new StringBuilder()
                .append(options.getEnvironment()).append("-")
                .append(options.getConfigMap().get("PROJECT_NAME")).append("-")
                .append(tableType)
                .toString();
    }

    default String calcBucketName(String bucketType, BackupOptions options) {
        return new StringBuilder()
                .append(options.getEnvironment()).append("-")
                .append(bucketType).append("-")
                .append(options.getConfigMap().get("PROJECT_NAME"))
                .toString();
    }

    default String calcBucketName(String bucketType, RestoreOptions options) {
        return new StringBuilder()
                .append(options.getEnvironment()).append("-")
                .append(bucketType).append("-")
                .append(options.getConfigMap().get("PROJECT_NAME"))
                .toString();
    }

    /**
     * Build metadata file for output
     *
     * @return MetadataBuilder builder
     */
    default MetadataBuilder buildMetadata(BackupOptions options) {
        MetadataBuilder metadataBuilder = new MetadataBuilder();
        metadataBuilder.setProjectName(options.getConfigMap().get("PROJECT_NAME"))
                .setProfile(options.getConfigMap().get("PROFILE_NAME"))
                .setDataRegion(options.getConfigMap().get("DATA_REGION"))
                .setBackupId(options.getBackupId())
                .setBackupType(options.getUsers() == null ? BackupType.FULL : BackupType.USER)
                .setTransferAcceleration(options.isTransferAcceleration())
                .setTimestamp(options.getBackupDateTime().format(ISO_LOCAL_DATE_TIME_NO_NANO))
                .setEnvironment(options.getEnvironment())
                .setExportUsers(options.getUsers())
                .setUser(System.getProperty("user.name"))
                .setCompressionType(options.isUncompressed() ? CompressionType.NONE : CompressionType.GZIP);
        try {
            metadataBuilder.setHost(java.net.InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException ex) {
            metadataBuilder.setHost("unknown");
        }
        metadataBuilder.setDestination(options.getDestination())
                .setDestinationType(options.getDestination().startsWith("s3://") ? DestinationType.S3 : DestinationType.LOCAL)
                .setEncryptionType(options.getEncryptionKey() == null || options.getEncryptionKey().equalsIgnoreCase("NONE")
                        ? EncryptionType.NONE : EncryptionType.AES_256)
                .setCompressionType(options.isUncompressed() ? CompressionType.NONE :
                        CompressionType.GZIP);
        return metadataBuilder;
    }

    S3Facade getS3();
    DynamoFacade getDynamo();

}
