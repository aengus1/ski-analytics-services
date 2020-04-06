package ski.crunch.utils;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class FileUtils {

    public static File writeBase64ToFile(String base64) {

        byte[] data = Base64.getDecoder().decode(base64);
        String name = UUID.randomUUID().toString();
        try (OutputStream stream = new FileOutputStream("/tmp/" + name)) {
            stream.write(data);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return new File("/tmp/" + name);
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

    public static void writeStringToFile(String content, File file) throws IOException {
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(content);
            fileWriter.flush();
        }
    }

    public static void deleteIfExists(File file) {
        if (file.exists()) {
            file.delete();
        }
    }

    public static long getFolderSizeBytes(Path folder) throws IOException {
        AtomicLong size = new AtomicLong(0);

        Files.walkFileTree(folder, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException {
                size.addAndGet(attrs.size());
                return FileVisitResult.CONTINUE;
            }
        });
        return size.longValue();
    }
}
