package crunch.ski.cli.model;

import com.amazonaws.util.json.Jackson;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class Metrics {
    private List<String> errors = new ArrayList<>();
    private String dataVolumeRaw;
    private String dataVolumeCompressed;
    private String transferElapsed;
    private String backupArchiveName;
    private String backupId;
    private String restoreId;


    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
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

    public void printMetrics(OutputStream os) throws IOException {
        String output = Jackson.toJsonPrettyString(this);
        try {
            os.write(output.getBytes());
        }finally{
            os.flush();
            os.close();
        }
    }

    public String getBackupArchiveName() {
        return backupArchiveName;
    }

    public void setBackupArchiveName(String backupArchiveName) {
        this.backupArchiveName = backupArchiveName;
    }

    public String getBackupId() {
        return backupId;
    }

    public void setBackupId(String backupId) {
        this.backupId = backupId;
    }

    public String getRestoreId() {
        return restoreId;
    }

    public void setRestoreId(String restoreId) {
        this.restoreId = restoreId;
    }
}
