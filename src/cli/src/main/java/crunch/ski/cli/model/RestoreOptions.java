package crunch.ski.cli.model;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class RestoreOptions {


    private Map<String, String> configMap;
    private String restoreId;
    private boolean isS3Source = false;
    private LocalDateTime restoreDateTime;
    private List<String> users;
    private File sourceDir;
    private String backupArchive;
    private String fullyQualifiedArchive;
    private String sourceBucket;
    private String sourceKey;
    private boolean transferAcceleration = false;
    private String decryptKey;
    private String environment;
    private boolean overwrite;
    private long startTs;
    private long endTs;

    public Map<String, String> getConfigMap() {
        return configMap;
    }

    public void setConfigMap(Map<String, String> configMap) {
        this.configMap = configMap;
    }

    public String getRestoreId() {
        return restoreId;
    }

    public void setRestoreId(String backupId) {
        this.restoreId = backupId;
    }

    public boolean isS3Source() {
        return isS3Source;
    }

    public void setS3Source(boolean s3Source) {
        isS3Source = s3Source;
    }

    public LocalDateTime getRestoreDateTime() {
        return restoreDateTime;
    }

    public void setRestoreDateTime(LocalDateTime restoreDateTime) {
        this.restoreDateTime = restoreDateTime;
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

    public File getSourceDir() {
        return sourceDir;
    }

    public void setSourceDir(File sourceDir) {
        this.sourceDir = sourceDir;
    }

    public String getSourceBucket() {
        return sourceBucket;
    }

    public void setSourceBucket(String sourceBucket) {
        this.sourceBucket = sourceBucket;
    }

    public String getSourceKey() {
        return sourceKey;
    }

    public void setSourceKey(String sourceKey) {
        this.sourceKey = sourceKey;
    }

    public boolean isTransferAcceleration() {
        return transferAcceleration;
    }

    public void setTransferAcceleration(boolean transferAcceleration) {
        this.transferAcceleration = transferAcceleration;
    }

    public String getDecryptKey() {
        return decryptKey;
    }

    public void setDecryptKey(String decryptKey) {
        this.decryptKey = decryptKey;
    }

//    public boolean isUncompressed() {
//        return uncompressed;
//    }
//
//    public void setUncompressed(boolean uncompressed) {
//        this.uncompressed = uncompressed;
//    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

//    public String getDestination() {
//        return destination;
//    }
//
//    public void setDestination(String destination) {
//        this.destination = destination;
//    }

    public long getStartTs() {
        return startTs;
    }

    public void setStartTs(long startTs) {
        this.startTs = startTs;
    }

    public long getEndTs() {
        return endTs;
    }

    public void setEndTs(long endTs) {
        this.endTs = endTs;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    private boolean verbose;



    public RestoreOptions() {

    }


    public boolean isOverwrite() {
        return overwrite;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    public String getBackupArchive() {
        return backupArchive;
    }

    public void setBackupArchive(String backupArchive) {
        this.backupArchive = backupArchive;
    }

    public String getFullyQualifiedArchive() {
        return fullyQualifiedArchive;
    }

    public void setFullyQualifiedArchive(String fullyQualifiedArchive) {
        this.fullyQualifiedArchive = fullyQualifiedArchive;
    }
}
