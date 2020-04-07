package ski.crunch.utils;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.*;
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

    public static void createTarGzFile(File source) throws  IOException {
        TarArchiveOutputStream tarOs;
        // Using input name to create output name
        try (FileOutputStream fos = new FileOutputStream(source.getAbsolutePath().concat(".tar.gz"))) {
            try (GZIPOutputStream gos = new GZIPOutputStream(new BufferedOutputStream(fos))) {
                tarOs = new TarArchiveOutputStream(gos);
                addFilesToTarGZ(source, "", tarOs);
            }
        }
    }

    private static void addFilesToTarGZ (File file, String parent, TarArchiveOutputStream tarArchive) throws
            IOException {
        // Create entry name relative to parent file path
        String entryName = parent + file.getName();
        // add tar ArchiveEntry
        tarArchive.putArchiveEntry(new TarArchiveEntry(file, entryName));
        if (file.isFile()) {
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);
            // Write file content to archive
            IOUtils.copy(bis, tarArchive);
            tarArchive.closeArchiveEntry();
            bis.close();
        } else if (file.isDirectory()) {
            // no need to copy any content since it is
            // a directory, just close the outputstream
            tarArchive.closeArchiveEntry();
            // for files in the directories

            for (File f : file.listFiles()) {
                // recursively call the method for all the subdirectories
                addFilesToTarGZ(f, entryName + File.separator, tarArchive);
            }
        }
    }
}

