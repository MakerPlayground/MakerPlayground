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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.device.actual.ActualDevice;
import io.makerplayground.device.actual.CloudPlatform;
import io.makerplayground.device.actual.IntegratedActualDevice;
import io.makerplayground.device.generic.GenericDevice;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class PathUtility {
    // workspace directory for storing generated project folder
    static public final String MP_WORKSPACE = System.getProperty("user.home") + File.separator + ".makerplayground";
    // program installation directory
    static public final String MP_INSTALLDIR = new File("").getAbsoluteFile().getPath();

    private static Optional<List<String>> tryCommand(List<List<String>> command, List<String> args) {
        for (List<String> c : command) {
            try {
                // append command with additional argument use to check for tool availability e.g. 'esptool' return 1 so we need to invoke 'esptool.py version' instead
                List<String> fullCommand = new ArrayList<>(c);
                fullCommand.addAll(args);

                Process p = new ProcessBuilder(fullCommand).redirectErrorStream(true).start();
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
        return tryCommand(command, Collections.emptyList());
    }

    /**
     * Get command for executing Adafruit MicroPython Tool (ampy)
     * @return command for executing ampy on the current platform or Optional.empty()
     */
    public static Optional<List<String>> getAmpyCommand() {
        List<List<String>> command = List.of(
                List.of(MP_INSTALLDIR + File.separator + "dependencies" + File.separator + "python-3.7.7" + File.separator + "python", "-m", "ampy.cli"),     // integrated python of our windows installer
                List.of(System.getProperty("user.home") + "/.platformio/penv/bin/ampy"),   // virtualenv created by official platformio installation script
                List.of("python", "-m", "ampy.cli"),          // default python in user's system path
                List.of("/usr/bin/python", "-m", "ampy.cli")  // internal python of macOS and Linux
        );
        return tryCommand(command, Collections.emptyList());
    }

    /**
     * Get command for executing esptool
     * @return command for executing esptool on the current platform or Optional.empty()
     */
    public static Optional<List<String>> getEsptoolCommand() {
        List<List<String>> command = List.of(
                List.of(MP_INSTALLDIR + File.separator + "dependencies" + File.separator + "python-3.7.7" + File.separator + "python", "-m", "esptool"),     // integrated python of our windows installer
                List.of(System.getProperty("user.home") + "/.platformio/penv/bin/esptool.py"),   // virtualenv created by official platformio installation script
                List.of("python", "-m", "esptool"),          // default python in user's system path
                List.of("/usr/bin/python", "-m", "esptool")  // internal python of macOS and Linux
        );
        return tryCommand(command, List.of("version"));
    }

    /**
     * Get command for executing kflash
     * @return command for executing kflash on the current platform or Optional.empty()
     */
    public static Optional<List<String>> getKflashCommand() {
        List<List<String>> command = List.of(
                List.of(MP_INSTALLDIR + File.separator + "dependencies" + File.separator + "python-3.7.7" + File.separator + "python", "-m", "kflash"),     // integrated python of our windows installer
                List.of(System.getProperty("user.home") + "/.platformio/penv/bin/kflash"),   // virtualenv created by official platformio installation script
                List.of("python", "-m", "kflash"),          // default python in user's system path
                List.of("/usr/bin/python", "-m", "kflash")  // internal python of macOS and Linux
        );
        return tryCommand(command, List.of("-v"));
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

    public static String getDeviceFirmwarePath() {
        if (DeviceLibrary.INSTANCE.getLibraryPath().isEmpty()) {
            throw new IllegalStateException("Library Path is missing");
        }
        return DeviceLibrary.INSTANCE.getLibraryPath().get() + File.separator + "firmware";
    }

    // cache list of path to model to avoid listing and reading model's yaml file every time
    private static Map<ActualDevice, List> modelPath = new HashMap<>();

    public static <T> List<T> getDeviceModelPath(ActualDevice actualDevice, Class<T> tClass) {
        if (modelPath.containsKey(actualDevice)) {
            return modelPath.get(actualDevice);
        } else {
            Path modelDir = Path.of(PathUtility.getDeviceDirectoryPath(), actualDevice.getId(), "model");
            try (Stream<Path> paths = Files.list(modelDir)) {
                ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
                List<T> modelList = new ArrayList<>();
                paths.filter(path -> path.toString().endsWith(".yaml")).forEach(path -> {
                    try {
                        T detectionModel = mapper.readValue(path.toFile(), tClass);
                        modelList.add(detectionModel);
                    } catch (IOException e) {
                        System.err.println("Can't read " + path);
                        e.printStackTrace();
                    }
                });
                modelPath.put(actualDevice, Collections.unmodifiableList(modelList));
            } catch (Exception e) {
                e.printStackTrace();
                modelPath.put(actualDevice, Collections.emptyList());
            }
            return modelPath.get(actualDevice);
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
