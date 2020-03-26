package crunch.ski.cli;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

import java.io.File;
import java.util.concurrent.Callable;


@Command(name = "backup",
        aliases = {"bck"},
        mixinStandardHelpOptions = true,
        version = "1.0.0",
        description = "Creates a backup dump of user data from a live environment to file or another S3 bucket")
public class Backup implements Callable<Integer> {


    //private static Logger logger = LogManager.getLogger(Backup.class);


    @ParentCommand
    private App parent;

    @Option(names = {"-e", "--env"}, description = "Name of the environment to backup.  E.g. dev, ci, prod")
    private String environment;

    @Option(names = {"-f", "--dest-dir"}, description = "")
    private File destinationFile;


    @Override
    public Integer call() throws Exception {
        System.out.println("Backing up data....");
//        logger.info("log test");
//        logger.error("yep");
        return 0;
    }
}
