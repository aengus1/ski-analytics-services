package ski.crunch.build;

import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.*;
import org.gradle.work.Incremental;

import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class UnDeployServerless extends Exec {

    public static final String DEFAULT_ENVIRONMENT = "dev";

    @Incremental()
    @PathSensitive(PathSensitivity.RELATIVE)
    FileCollection sourceFiles;
    private File outputDir;
    private File serverlessFile;
    //private Logger logger = LoggerFactory.getLogger("deployTaskLogger");



    @OutputDirectory()
    public File getOutputDir(){
        return this.outputDir;
    }

    @InputFiles()
    public FileCollection getSourceFiles() {
        return this.sourceFiles;
    }

    @InputFile()
    public File getServerlessFile() {
        return this.serverlessFile;
    }

    public void setSourceFiles(FileCollection files) {
        this.sourceFiles = files;
    }

    public void setServerlessFile(File file) {
        this.serverlessFile = file;
    }

    @Input
    public String getStage() {
        return System.getProperty("stage") != null ? System.getProperty("stage") : DEFAULT_ENVIRONMENT;
    }


    @Inject()
    public UnDeployServerless() {
        super.setDescription("Calls the Serverless executable to remove a module. To set the stage pass a system" +
                " property e.g.  -Dstage=\"dev\"");
        super.setGroup("deploy");
        super.setExecutable("/usr/local/bin/serverless");
        this.outputDir = new File(getProject().getBuildDir(), "deploy");

        setServerlessFile(new File(getWorkingDir() + "/serverless.yml"));

        FileCollection files = getProject().fileTree("src");
        setSourceFiles(files);

    }

    @TaskAction
    public void run() {

        List<String> args = new ArrayList<>();
        args.add("remove");
        args.add("--stage");
        args.add(getStage());
        args.add("-v");
        super.setArgs(args);
        super.exec();

    }
}
