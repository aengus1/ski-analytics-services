package crunch.ski.cli.services;

public class CliUtils {


    public static String calcTableName(String tableType, String environment, String projectName) {
        return environment + "-" +
                projectName + "-" +
                tableType;
    }

    public static String calcBucketName(String bucketType, String environment, String projectName) {
        return environment + "-" +
                bucketType + "-" +
                projectName;
    }

    public static String calcSSMParameterName(String parameterName, String environment) {
        return environment + "-" +
                parameterName + "-" +
                "api-key";
    }

    public static String calcStackName(String stack, String environment, String projectName) {
        return environment + "-" +
                projectName + "-" +
                stack;
    }
}
