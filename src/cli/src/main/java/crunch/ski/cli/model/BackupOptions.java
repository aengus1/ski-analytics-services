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
    private int nThreads = 2;
    private String usersString;
    private String encryptionType;
    private boolean uncompressed;
    private String environment;
    private String destination;
    private long startTs;
    private long endTs;
    private boolean verbose;



    public BackupOptions() {

    }


    public Map<String, String> getConfigMap() {
        return configMap;
    }

    public void setConfigMap(Map<String, String> configMap) {
        this.configMap = configMap;
    }

    public String getBackupId() {
        return backupId;
    }

    public void setBackupId(String backupId) {
        this.backupId = backupId;
    }

    public boolean isS3Destination() {
        return isS3Destination;
    }

    public void setS3Destination(boolean s3Destination) {
        isS3Destination = s3Destination;
    }

    public LocalDateTime getBackupDateTime() {
        return backupDateTime;
    }

    public void setBackupDateTime(LocalDateTime backupDateTime) {
        this.backupDateTime = backupDateTime;
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

    public File getDestDir() {
        return destDir;
    }

    public void setDestDir(File destDir) {
        this.destDir = destDir;
    }

    public String getDestBucket() {
        return destBucket;
    }

    public void setDestBucket(String destBucket) {
        this.destBucket = destBucket;
    }

    public String getDestKey() {
        return destKey;
    }

    public void setDestKey(String destKey) {
        this.destKey = destKey;
    }

    public boolean isTransferAcceleration() {
        return transferAcceleration;
    }

    public void setTransferAcceleration(boolean transferAcceleration) {
        this.transferAcceleration = transferAcceleration;
    }

    public int getnThreads() {
        return nThreads;
    }

    public void setnThreads(int nThreads) {
        this.nThreads = nThreads;
    }

    public String getUsersString() {
        return usersString;
    }

    public void setUsersString(String usersString) {
        this.usersString = usersString;
    }

    public String getEncryptionType() {
        return encryptionType;
    }

    public void setEncryptionType(String encryptionType) {
        this.encryptionType = encryptionType;
    }

    public boolean isUncompressed() {
        return uncompressed;
    }

    public void setUncompressed(boolean uncompressed) {
        this.uncompressed = uncompressed;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

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
        return this.verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
}
