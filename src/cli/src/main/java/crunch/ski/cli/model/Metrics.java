package crunch.ski.cli.model;

public class Metrics {
    private String[] errors;
    private String dataVolumeRaw;
    private String dataVolumeCompressed;
    private String transferElapsed;

    public String[] getErrors() {
        return errors;
    }

    public void setErrors(String[] errors) {
        this.errors = errors;
    }

    public String getDataVolumeRaw() {
        return dataVolumeRaw;
    }

    public void setDataVolumeRaw(String dataVolumeRaw) {
        this.dataVolumeRaw = dataVolumeRaw;
    }

    public String getDataVolumeCompressed() {
        return dataVolumeCompressed;
    }

    public void setDataVolumeCompressed(String dataVolumeCompressed) {
        this.dataVolumeCompressed = dataVolumeCompressed;
    }

    public String getTransferElapsed() {
        return transferElapsed;
    }

    public void setTransferElapsed(String transferElapsed) {
        this.transferElapsed = transferElapsed;
    }


}
