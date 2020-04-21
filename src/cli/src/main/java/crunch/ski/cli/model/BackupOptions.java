package crunch.ski.cli.model;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class BackupOptions {


    private Map<String, String> configMap;
    private String backupId;
    private boolean isS3Destination = false;
    private LocalDateTime backupDateTime;
    private List<String> users;
    private File destDir;
    private String destBucket;
    private String destKey;
    private boolean transferAcceleration = false;
    private String encryptionKey;
    private boolean uncompressed;
    private String environment;
    private String destination;
    private long startTs;
    private long endTs;
    private boolean verbose;



    public BackupOptions() {

    }


    /**
     * ConfigMap is a map of CLI configuration options.
     * DATA_REGION the AWS region in which the data layer being backed up resides
     * PROJECT_NAME the configured name of the project. used to determine AWS resource names
     * PROFILE_NAME the name of the AWS profile to use
     * @return Map<String, String> config
     */
    public Map<String, String> getConfigMap() {
        return configMap;
    }

    public void setConfigMap(Map<String, String> configMap) {
        this.configMap = configMap;
    }

    /**
     * @return the UUID of this backup
     */
    public String getBackupId() {
        return backupId;
    }

    public void setBackupId(String backupId) {
        this.backupId = backupId;
    }

    /**
     *
     * @return  boolean true if the configured destination is an S3 bucket
     */
    public boolean isS3Destination() {
        return isS3Destination;
    }

    public void setS3Destination(boolean s3Destination) {
        isS3Destination = s3Destination;
    }

    /**
     *
     * @return LocalDateTime the timestamp this backup occurred
     */
    public LocalDateTime getBackupDateTime() {
        return backupDateTime;
    }

    public void setBackupDateTime(LocalDateTime backupDateTime) {
        this.backupDateTime = backupDateTime;
    }

    /**
     * If a list of users is present then only data from these users will be backed up.  Results in a different
     * backup archive format than a full backup
     * @return List<String> user id's or email addresses
     */
    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

    /**
     * The output destination when backing up to local file system
     * @return File output destination
     */
    public File getDestDir() {
        return destDir;
    }

    public void setDestDir(File destDir) {
        this.destDir = destDir;
    }

    /**
     *  The output destination bucket when backing up to S3
     * @return String bucket name
     */
    public String getDestBucket() {
        return destBucket;
    }

    public void setDestBucket(String destBucket) {
        this.destBucket = destBucket;
    }

    /**
     * The output destination key when backing up to S3
     * @return String destinationKey
     */
    public String getDestKey() {
        return destKey;
    }

    public void setDestKey(String destKey) {
        this.destKey = destKey;
    }

    /**
     * Use S3 transfer acceleration
     * @return boolean S3TransferAcceleration
     */
    public boolean isTransferAcceleration() {
        return transferAcceleration;
    }

    public void setTransferAcceleration(boolean transferAcceleration) {
        this.transferAcceleration = transferAcceleration;
    }

    /**
     * Encryption key used in backup.  (optional)
     * @return String encryption key.  Leave null for unencrypted backup
     */
    public String getEncryptionKey() {
        return encryptionKey;
    }

    public void setEncryptionKey(String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    /**
     * If this flag is set the backup will not be compressed
     * @return boolean isUncompressed
     */
    public boolean isUncompressed() {
        return uncompressed;
    }

    public void setUncompressed(boolean uncompressed) {
        this.uncompressed = uncompressed;
    }

    /**
     * The name of the AWS environment to backup
     * @return
     */
    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    /**
     * The raw destination string (file path or S3 path)
     * @return String destination
     */
    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    /**
     * timestamp (ms) of backup start time
     * @return long startTs
     */
    public long getStartTs() {
        return startTs;
    }

    public void setStartTs(long startTs) {
        this.startTs = startTs;
    }

    /**
     * timestamp (ms) of backup end time
     * @return long endTs
     */
    public long getEndTs() {
        return endTs;
    }

    public void setEndTs(long endTs) {
        this.endTs = endTs;
    }

    /**
     * Verbose output
     * @return boolean isVerbose
     */
    public boolean isVerbose() {
        return this.verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
}
