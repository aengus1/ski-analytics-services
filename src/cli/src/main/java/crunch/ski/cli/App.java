package crunch.ski.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

@Command(
subcommands = {
   Backup.class
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

    @Option(names = {"-v", "--verbose"})
    private boolean verbose;
    @Option(names = { "--stack-trace" } )
    private boolean stackTrace;

    public static void main(String[] args) {
       int exitCode = new CommandLine(new App()).execute(args);
       System.exit(exitCode);
    }

    @Override
    public Integer call() {
        System.out.printf("Please choose one of the subcommands: backup, restore, wipe");
        return 0;
    }

}
