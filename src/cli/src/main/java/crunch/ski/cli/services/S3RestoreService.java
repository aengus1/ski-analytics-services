package crunch.ski.cli.services;

import crunch.ski.cli.model.RestoreOptions;

public class S3RestoreService implements BackupRestoreService {

    public S3RestoreService(RestoreOptions options) {

    }
    @Override
    public int apply() {
        return 0;
    }
}
