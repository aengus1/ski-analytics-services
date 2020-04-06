package ski.crunch.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GZipUtils {

    public static void decompressGzipFile(File gzipFile, File newFile) throws IOException {
        try (FileInputStream fis = new FileInputStream(gzipFile)) {
            try (GZIPInputStream gis = new GZIPInputStream(fis)) {
                try (FileOutputStream fos = new FileOutputStream(newFile)) {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = gis.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                    }
                }
            }
        }
    }

    public static void compressGzipFile(File file, File gzipFile) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            try (FileOutputStream fos = new FileOutputStream(gzipFile)) {
                try (GZIPOutputStream gzipOS = new GZIPOutputStream(fos)) {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = fis.read(buffer)) != -1) {
                        gzipOS.write(buffer, 0, len);
                    }
                }
            }
        }
    }
}
