package crunch.ski.cli.services;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ProcessRunner {
    private InputStream inputStream;
    private InputStream errorStream;


    public int startProcess(String[] cmdArray, File directory, boolean isInheritIO) {
        Process process;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(cmdArray);
            processBuilder.environment().put("CLI_BUILD", "TRUE");
            processBuilder.directory(directory);
            if(isInheritIO) {
             processBuilder.inheritIO();
            }
            process = processBuilder.start();
            process.waitFor();
            this.inputStream = process.getInputStream();
            this.errorStream = process.getErrorStream();
            return process.exitValue();
        } catch (IOException e) {
            e.printStackTrace();
            return 1;
        } catch (InterruptedException ex) {
            ex.printStackTrace();
            return 1;
        }
    }

    public InputStream getInputStream() {
        return this.inputStream;
    }

    public InputStream getErrorStream() {
        return this.errorStream;
    }
}
