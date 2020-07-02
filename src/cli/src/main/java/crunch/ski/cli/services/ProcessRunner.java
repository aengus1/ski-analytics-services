package crunch.ski.cli.services;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ProcessRunner {
    private InputStream inputStream;
    private InputStream errorStream;


    public int startProcess(String[] cmdArray, File directory) {
        String output = "";
        String error = "";
        Process process;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(cmdArray);
            processBuilder.environment().put("CLI_BUILD", "TRUE");
            processBuilder.directory(directory);
            //processBuilder.inheritIO();
            process = processBuilder.start();
            process.waitFor();
            this.inputStream = process.getInputStream();
            this.errorStream = process.getErrorStream();
//            output = StreamUtils.convertStreamToString(is);
//            error = StreamUtils.convertStreamToString(es);
            return process.exitValue();
        } catch (IOException e) {
            e.printStackTrace();
            return 1;
        } catch (InterruptedException ex) {
            ex.printStackTrace();
            return 1;
//        } finally {
//            System.out.println(output);
//            System.err.println(error);
        }
    }

    public InputStream getInputStream() {
        return this.inputStream;
    }

    public InputStream getErrorStream() {
        return this.errorStream;
    }
}
