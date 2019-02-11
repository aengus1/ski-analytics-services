package ski.crunch.utils;

import java.io.*;
import java.util.Base64;
import java.util.UUID;

public class FileUtils {

    public static java.io.File writeBase64ToFile(String base64) throws IOException {

        byte[] data = Base64.getDecoder().decode(base64);
        String name = UUID.randomUUID().toString();
        try (OutputStream stream = new FileOutputStream("/tmp/" + name)) {
            stream.write(data);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return new java.io.File("/tmp/" + name);
    }


    public static byte[] toByteArray(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        buffer.flush();

        return buffer.toByteArray();
    }

    public static void deleteIfExists(File file) {
        if(file.exists()) {
            file.delete();
        }
    }

}
