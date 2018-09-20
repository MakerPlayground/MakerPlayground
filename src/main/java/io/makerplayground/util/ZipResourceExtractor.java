package io.makerplayground.util;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipResourceExtractor {

    public enum ExtractResult {
        SUCCESS,
        FAIL,
    }

    public static ExtractResult extract(Class caller_class, String zipResourcePath, String destinationPath) {
        caller_class.getResourceAsStream(zipResourcePath);
        InputStream is = caller_class.getResourceAsStream(zipResourcePath);
        ZipInputStream zis = new ZipInputStream(is);
        return extract(zis,destinationPath);
    }

    public static ExtractResult extract(Path zipFilePath, String destinationPath){
        try {
            InputStream is = Files.newInputStream(zipFilePath);
            ZipInputStream zis = new ZipInputStream(is);
            return extract(zis,destinationPath);
        } catch (IOException e) {
            return ExtractResult.FAIL;
        }
    }

    private static ExtractResult extract(ZipInputStream zis, String destinationPath) {
        ZipEntry entry;
        try {
            while ((entry = zis.getNextEntry()) != null) {

                // Create a file on HDD in the destinationPath directory
                // destinationPath is a "root" folder, where you want to extract your ZIP file
                File entryFile = new File(destinationPath, entry.getName());
                if (entry.isDirectory()) {

                    if (!entryFile.exists()) {
                        entryFile.mkdirs();
                    }

                } else {

                    // Make sure all folders exists (they should, but the safer, the better ;-))
                    if (entryFile.getParentFile() != null && !entryFile.getParentFile().exists()) {
                        entryFile.getParentFile().mkdirs();
                    }

                    // Create file on disk...
                    if (!entryFile.exists()) {
                        entryFile.createNewFile();
                    }

                    // and rewrite data from stream
                    OutputStream os = null;
                    try {
                        os = new FileOutputStream(entryFile);
                        IOUtils.copy(zis, os);
                    } finally {
                        IOUtils.closeQuietly(os);
                    }
                }
            }
            IOUtils.closeQuietly(zis);
            return ExtractResult.SUCCESS;
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        return ExtractResult.FAIL;
    }
}
