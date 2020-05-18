package crunch.ski.cli.services;

import crunch.ski.cli.model.RestoreOptions;
import ski.crunch.aws.DynamoFacade;
import ski.crunch.aws.S3Facade;

public class S3RestoreService implements BackupRestoreService {

    public S3RestoreService(RestoreOptions options) {

    }
    @Override
    public int apply() {
        return 0;
    }

    @Override
    public S3Facade getS3() {
        return null;
    }

    @Override
    public DynamoFacade getDynamo() {
        return null;
    }
}
