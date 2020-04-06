package crunch.ski.cli.model;

import java.time.LocalDateTime;
import java.util.List;

public class MetadataBuilder {
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
    private String destination;
    private DestinationType destinationType;
    private List<String> exportUsers;
    private EncryptionType encryptionType;
    private CompressionType compressionType;
    private Metrics metrics;

    public MetadataBuilder setBackupId(String backupId) {
        this.backupId = backupId;
        return this;
    }

    public MetadataBuilder setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public MetadataBuilder setEnvironment(String environment) {
        this.environment = environment;
        return this;
    }

    public MetadataBuilder setBackupType(BackupType backupType) {
        this.backupType = backupType;
        return this;
    }

    public MetadataBuilder setUser(String user) {
        this.user = user;
        return this;
    }

    public MetadataBuilder setHost(String host) {
        this.host = host;
        return this;
    }

    public MetadataBuilder setProfile(String profile) {
        this.profile = profile;
        return this;
    }

    public MetadataBuilder setProjectName(String projectName) {
        this.projectName = projectName;
        return this;
    }

    public MetadataBuilder setDataRegion(String dataRegion) {
        this.dataRegion = dataRegion;
        return this;
    }

    public MetadataBuilder setTransferAcceleration(boolean transferAcceleration) {
        this.transferAcceleration = transferAcceleration;
        return this;
    }

    public MetadataBuilder setThreads(int threads) {
        this.threads = threads;
        return this;
    }

    public MetadataBuilder setDestination(String destination) {
        this.destination = destination;
        return this;
    }

    public MetadataBuilder setDestinationType(DestinationType destinationType) {
        this.destinationType = destinationType;
        return this;
    }

    public MetadataBuilder setEncryptionType(EncryptionType encryptionType) {
        this.encryptionType = encryptionType;
        return this;
    }

    public MetadataBuilder setCompressionType(CompressionType compressionType) {
        this.compressionType = compressionType;
        return this;
    }

    public MetadataBuilder setExportUsers(List<String> exportUsers) {
        this.exportUsers = exportUsers;
        return this;
    }
    public MetadataBuilder setMetrics(Metrics metrics) {
        this.metrics = metrics;
        return this;
    }

    public Metadata createMetadata() {

        return new Metadata(backupId, timestamp, environment, backupType, user, host, profile, projectName, dataRegion,
                transferAcceleration, threads, destination, destinationType, exportUsers, encryptionType, compressionType, metrics);
    }
}