package crunch.ski.cli.services;

public class CliUtils {


    public static String calcTableName(String tableType, String environment, String projectName) {
        return new StringBuilder()
                .append(environment).append("-")
                .append(projectName).append("-")
                .append(tableType)
                .toString();
    }

    public static String calcBucketName(String bucketType, String environment, String projectName) {
        return new StringBuilder()
                .append(environment).append("-")
                .append(bucketType).append("-")
                .append(projectName)
                .toString();
    }
}
