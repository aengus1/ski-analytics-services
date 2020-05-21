package crunch.ski.cli.model;

import java.util.Map;

public class WipeOptions {

    private Map<String, String> configMap;
    private String region;
    private String environment;
    private boolean skipBackup;
    private boolean forDeletionOnly;
    private boolean deploymentBucketOnly;
    private boolean remoteStateOnly;
    private boolean autoApprove;
    private String backupLocation;

    public boolean isSkipBackup() {
        return skipBackup;
    }

    public void setSkipBackup(boolean skipBackup) {
        this.skipBackup = skipBackup;
    }

    public String getBackupLocation() {
        return backupLocation;
    }

    public void setBackupLocation(String backupLocation) {
        this.backupLocation = backupLocation;
    }

    public Map<String, String> getConfigMap() {
        return configMap;
    }

    public void setConfigMap(Map<String, String> configMap) {
        this.configMap = configMap;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public boolean isForDeletionOnly() {
        return forDeletionOnly;
    }

    public void setForDeletionOnly(boolean forDeletionOnly) {
        this.forDeletionOnly = forDeletionOnly;
    }

    public boolean isAutoApprove() {
        return autoApprove;
    }

    public void setAutoApprove(boolean autoApprove) {
        this.autoApprove = autoApprove;
    }

    public boolean isDeploymentBucketOnly() {
        return deploymentBucketOnly;
    }

    public void setDeploymentBucketOnly(boolean deploymentBucketOnly) {
        this.deploymentBucketOnly = deploymentBucketOnly;
    }

    public boolean isRemoteStateOnly() {
        return remoteStateOnly;
    }

    public void setRemoteStateOnly(boolean remoteStateOnly) {
        this.remoteStateOnly = remoteStateOnly;
    }
}
