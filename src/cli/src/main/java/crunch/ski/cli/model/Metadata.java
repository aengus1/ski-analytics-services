package crunch.ski.cli.model;

import java.time.LocalDateTime;
import java.util.List;

public class Metadata {
    public static final int VERSION = 1;
    private String backupId;
    private LocalDateTime timestamp;
    private String environment;
    private BackupType backupType;
    private String user;
    private String host;
    private String profile;
    private String projectName;
    private String dataRegion;
    private boolean transferAcceleration;
    private int threads;
    private DestinationType destinationType;
    private String destination;
    private List<String> exportUsers;
    private EncryptionType encryptionType;
    private CompressionType compressionType;
    private Metrics metrics;


    public Metadata(String backupId, LocalDateTime timestamp, String environment, BackupType backupType, String user,
                    String host,
                    String profile, String projectName, String dataRegion,
                    boolean transferAcceleration, int threads, String destination, DestinationType destinationType,
                    List<String> exportUsers, EncryptionType encryptionType,
                    CompressionType compressionType, Metrics metrics){
        this.backupId = backupId;
        this.timestamp = timestamp;
        this.environment = environment;
        this.backupType = backupType;
        this.user = user;
        this.host = host;
        this.profile = profile;
        this.projectName = projectName;
        this.dataRegion = dataRegion;
        this.transferAcceleration = transferAcceleration;
        this.threads = threads;
        this.destination = destination;
        this.destinationType = destinationType;
        this.exportUsers = exportUsers;
        this.encryptionType = encryptionType;
        this.compressionType = compressionType;
        this.metrics = metrics;

    }
    public String getBackupId() {
        return backupId;
    }

    public void setBackupId(String backupId) {
        this.backupId = backupId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public BackupType getBackupType() {
        return backupType;
    }

    public void setBackupType(BackupType backupType) {
        this.backupType = backupType;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getDataRegion() {
        return dataRegion;
    }

    public void setDataRegion(String dataRegion) {
        this.dataRegion = dataRegion;
    }

    public boolean isTransferAcceleration() {
        return transferAcceleration;
    }

    public void setTransferAcceleration(boolean transferAcceleration) {
        this.transferAcceleration = transferAcceleration;
    }

    public int getThreads() {
        return threads;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

    public DestinationType getDestinationType() {
        return destinationType;
    }

    public void setDestinationType(DestinationType destinationType) {
        this.destinationType = destinationType;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public EncryptionType getEncryptionType() {
        return encryptionType;
    }

    public void setEncryptionType(EncryptionType encryptionType) {
        this.encryptionType = encryptionType;
    }

    public CompressionType getCompressionType() {
        return compressionType;
    }

    public void setCompressionType(CompressionType compressionType) {
        this.compressionType = compressionType;
    }

    public Metrics getMetrics() {
        return metrics;
    }

    public void setMetrics(Metrics metrics) {
        this.metrics = metrics;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public List<String> getExportUsers() {
        return exportUsers;
    }

    public void setExportUsers(List<String> exportUsers) {
        this.exportUsers = exportUsers;
    }
}
