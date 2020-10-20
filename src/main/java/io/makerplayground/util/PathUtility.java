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

import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.device.actual.ActualDevice;
import io.makerplayground.device.actual.CloudPlatform;
import io.makerplayground.device.actual.IntegratedActualDevice;
import io.makerplayground.device.generic.GenericDevice;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class PathUtility {
    // workspace directory for storing generated project folder
    static public final String MP_WORKSPACE = System.getProperty("user.home") + File.separator + ".makerplayground";
    // program installation directory
    static public final String MP_INSTALLDIR = new File("").getAbsoluteFile().getPath();

    /**
     * Get command for executing platformio
     * @return command for executing platformio on the current platform or Optional.empty()
     */
    public static Optional<List<String>> getPlatformIOCommand() {
        List<List<String>> command = List.of(
                List.of(MP_INSTALLDIR + File.separator + "dependencies" + File.separator + "python-3.7.7" + File.separator + "python", "-m", "platformio"),     // integrated python of our windows installer
                List.of(System.getProperty("user.home") + "/.platformio/penv/bin/platformio"),   // virtualenv created by official platformio installation script
                List.of("python", "-m", "platformio"),          // default python in user's system path
                List.of("/usr/bin/python", "-m", "platformio")  // internal python of macOS and Linux
        );

        for (List<String> c : command) {
            try {
                Process p = new ProcessBuilder(c).redirectErrorStream(true).start();
                // read from an input stream to prevent the child process from stalling
                try (BufferedReader processOutputReader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    String readLine;
                    while ((readLine = processOutputReader.readLine()) != null) {
//                        System.out.println(readLine);
                    }
                }
                if (p.waitFor(5, TimeUnit.SECONDS) && (p.exitValue() == 0)) {
                    return Optional.of(c);
                }
            } catch (IOException | InterruptedException e) {
                // do nothing as we expected the code to throw exception
            }
        }

        return Optional.empty();
    }

    /**
     * Get path to an integrated platformio home directory which is used for storing compilers and tools for each platform
     * @return path to the integrated platformio home directory or Optional.empty()
     */
    public static Optional<String> getIntegratedPIOHomeDirectory() {
        List<String> path = List.of(MP_INSTALLDIR + File.separator + "dependencies" + File.separator + "platformio"   // default path for Windows installer
                , "/Library/Application Support/MakerPlayground/platformio");       // default path for macOS installer
        return path.stream().filter(s -> new File(s).exists()).findFirst();
    }

    public static String getDeviceDirectoryPath() {
        if (DeviceLibrary.INSTANCE.getLibraryPath().isEmpty()) {
            throw new IllegalStateException("Library Path is missing");
        }
        return DeviceLibrary.INSTANCE.getLibraryPath().get() + File.separator + "devices";
    }

    public static Path getDeviceImagePath(ActualDevice actualDevice) {
        String id;
        if (actualDevice instanceof IntegratedActualDevice) {
            id = ((IntegratedActualDevice) actualDevice).getParent().getId();
        } else {
            id = actualDevice.getId();
        }
        // TODO: Should we handle case that the image is missing or let the caller check for path existence?
        return Path.of(getDeviceDirectoryPath(), id, "asset", "device.png");
    }

    public static Path getDeviceThumbnailPath(ActualDevice actualDevice) {
        String id;
        if (actualDevice instanceof IntegratedActualDevice) {
            id = ((IntegratedActualDevice) actualDevice).getParent().getId();
        } else {
            id = actualDevice.getId();
        }
        Path thumbnailPath = Path.of(getDeviceDirectoryPath(), id, "asset", "device_thumbnail.png");
        if (Files.exists(thumbnailPath)) {
            return thumbnailPath;
        } else {
            return getDeviceImagePath(actualDevice);
        }
    }

    private static InputStream getIconAsStream(String name) {
        Optional<String> libraryPath = DeviceLibrary.INSTANCE.getLibraryPath();
        if (libraryPath.isPresent()) {
            Path filePath = Path.of(libraryPath.get(), "icons", name + ".png");
            if (Files.exists(filePath)) {
                try {
                    return Files.newInputStream(filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new IllegalStateException("Error while loading icon name: " + name);
                }
            }
        }
        return DeviceLibrary.class.getResourceAsStream("/icons/generic_missing_icon.png");
    }

    public static InputStream getGenericDeviceIconAsStream(GenericDevice genericDevice) {
        return getIconAsStream(genericDevice.getName());
    }

    public static InputStream getCloudPlatformIconAsStream(CloudPlatform cloudPlatform) {
        return getIconAsStream(cloudPlatform.getDisplayName());
    }

    public static String getUserLibraryPath() {
        return MP_WORKSPACE + File.separator + "library";
    }
}
