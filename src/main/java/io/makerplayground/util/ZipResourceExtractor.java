/*
 * Copyright (c) 2019. The Maker Playground Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.makerplayground.util;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipResourceExtractor {

    public enum ExtractResult {
        SUCCESS,
        FAIL,
    }

    public static ExtractResult extract(Path zipFilePath, String destinationPath){
        // create output directory if it doesn't exist
        File dir = new File(destinationPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFilePath))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File entryFile = new File(destinationPath, entry.getName());
                if (entry.isDirectory()) {
                    // create directories if it isn't existed
                    if (!entryFile.exists()) {
                        entryFile.mkdirs();
                    }
                } else {
                    // copy content to a file
                    try (OutputStream os = new FileOutputStream(entryFile)) {
                        IOUtils.copy(zis, os);
                    }
                }
            }
        } catch (IOException e) {
            return ExtractResult.FAIL;
        }
        return ExtractResult.SUCCESS;
    }
}
