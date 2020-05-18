package ski.crunch.utils;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GZipUtils {

    public static int BUFFER_SIZE = 1096;
    public static final Logger logger = LoggerFactory.getLogger(GZipUtils.class);

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

    public static void createTarGzFile(File source) throws IOException {
        TarArchiveOutputStream tarOs;
        // Using input name to create output name
        try (FileOutputStream fos = new FileOutputStream(source.getAbsolutePath().concat(".tar.gz"))) {
            try (GZIPOutputStream gos = new GZIPOutputStream(new BufferedOutputStream(fos))) {
                tarOs = new TarArchiveOutputStream(gos);
                addFilesToTarGZ(source, "", tarOs);
            }
        }
    }

    public static void extractTarGZ(File archive, File destDir) throws IOException {

        if (!destDir.exists() || !destDir.isDirectory()) {
            destDir.mkdir();
        }

        try(FileInputStream fileInputStream = new FileInputStream(archive)) {
            try (GzipCompressorInputStream gzipIn = new GzipCompressorInputStream(fileInputStream)) {
                try (TarArchiveInputStream tarIn = new TarArchiveInputStream(gzipIn)) {
                    TarArchiveEntry entry;

                    while ((entry = (TarArchiveEntry) tarIn.getNextEntry()) != null) {
                        /** If the entry is a directory, create the directory. **/
                        if (entry.isDirectory()) {
                            File f = new File(destDir, entry.getName());
                            boolean created = f.mkdir();
                            if (!created) {
                                logger.error("Unable to create directory '%s', during extraction of archive contents.\n",
                                        f.getAbsolutePath());
                                throw new IOException("unable to create directory " + f.getAbsolutePath());
                            }
                        } else {
                            int count;
                            byte data[] = new byte[BUFFER_SIZE];
                            FileOutputStream fos = new FileOutputStream(new File(destDir, entry.getName()), false);
                            try (BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER_SIZE)) {
                                while ((count = tarIn.read(data, 0, BUFFER_SIZE)) != -1) {
                                    dest.write(data, 0, count);
                                }
                            }
                        }
                    }

                    System.out.println("Untar completed successfully!");
                }
            }
        }
    }

        private static void addFilesToTarGZ (File file, String parent, TarArchiveOutputStream tarArchive) throws
        IOException {
            tarArchive.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
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

