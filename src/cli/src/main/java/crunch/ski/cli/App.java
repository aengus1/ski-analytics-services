package crunch.ski.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

@Command(
        subcommands = {
                Backup.class,
                Restore.class,
                Config.class,
                Wipe.class
        },
        name = "crunch",
        description = "Administrative tool for loading / restoring crunch.ski data",
        version = "1.0.0",
        mixinStandardHelpOptions = true
)
public class App implements Callable<Integer> {

    @Option(names = {"-p", "--profile"},
            paramLabel = "PROFILE",
            description = "the AWS credentials profile"
    )
    private String awsProfile;


    @Option(names = {"-n", "--project-name"},
            paramLabel = "PROJECT_NAME",
            description = "the name of project"
    )
    private String projectName;

    @Option(names = {"-r", "--data-region"},
            paramLabel = "DATA_REGION",
            description = "the aws region of data stack"
    )
    private String dataRegion;

    @Option(names = {"-v", "--verbose"})
    private boolean verbose;
    @Option(names = {"--stack-trace"})
    private boolean stackTrace;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new App()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        System.out.printf("Please choose one of the subcommands: backup, restore, wipe, configure");
        return 0;
    }


    public String getAwsProfile() {
        return awsProfile;
    }
    public String getProjectName() {
        return projectName;
    }
    public String getDataRegion() {
        return dataRegion;
    }
    void setAwsProfile(String profile) {
        this.awsProfile = profile;
    }
    void setProjectName(String projectName) {
        this.projectName = projectName;
    }
    void setDataRegion(String dataRegion) {
        this.dataRegion = dataRegion;
    }
    public boolean isVerbose() {
        return verbose;
    }

    public boolean isStackTrace() {
        return stackTrace;
    }
}
