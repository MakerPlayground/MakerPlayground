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

package io.makerplayground.upload;

import com.fazecast.jSerialComm.SerialPort;
import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.device.actual.ActualDevice;
import io.makerplayground.device.actual.CloudPlatform;
import io.makerplayground.generator.devicemapping.ProjectLogic;
import io.makerplayground.generator.devicemapping.ProjectMappingResult;
import io.makerplayground.generator.source.MicroPythonUploadCode;
import io.makerplayground.generator.source.SourceCodeResult;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectDevice;
import io.makerplayground.util.OSInfo;
import io.makerplayground.util.PathUtility;
import io.makerplayground.util.ZipResourceExtractor;
import javafx.application.Platform;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MicroPythonUploadTask extends UploadTask {

    protected MicroPythonUploadTask(Project project, UploadTarget uploadTarget, boolean isInteractiveUpload) {
        super(project, uploadTarget, isInteractiveUpload);
    }

    @Override
    protected UploadResult doUpload() {
        updateProgress(0, 1);
        updateMessage("Checking project");

        // wait for 500ms so that when the upload failed very early, user can see that the upload has started (progress is at 0%)
        // for a short period of time before seeing the error message
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            updateMessage("Upload has been canceled");
            return UploadResult.USER_CANCEL;
        }

        ProjectMappingResult mappingResult = ProjectLogic.validateDeviceAssignment(project);
        if (mappingResult != ProjectMappingResult.OK) {
            updateMessage(mappingResult.getErrorMessage());
            return UploadResult.DEVICE_OR_PORT_MISSING;
        }

        // TODO: add support for the interactive mode
        SourceCodeResult sourcecode = MicroPythonUploadCode.generateCode(project);  // interactiveUpload ? ArduinoInteractiveCode.generateCode(project) : ArduinoUploadCode.generateCode(project);
        if (sourcecode.getError() != null) {
            updateMessage("Error: " + sourcecode.getError().getDescription());
            return UploadResult.CANT_GENERATE_CODE;
        }

        updateProgress(0.10, 1);
        updateMessage("Checking required dependencies");

        Platform.runLater(() -> log.set("Workspace is at " + PathUtility.MP_WORKSPACE + "\n"));

        // check ampy installation
        Optional<List<String>> ampyCommand = PathUtility.getAmpyCommand();
        if (ampyCommand.isEmpty()) {
            updateMessage("Error: Can't find valid ampy installation see: https://learn.adafruit.com/micropython-basics-load-files-and-run-code/install-ampy");
            return UploadResult.CANT_FIND_PIO;
        }
        Platform.runLater(() -> log.set("Execute ampy by " + ampyCommand.get() + "\n"));

        updateProgress(0.20, 1);
        updateMessage("Preparing to generate project");

        Collection<ProjectDevice> projectDeviceList = interactiveUpload ? project.getUnmodifiableProjectDevice() : project.getAllDeviceUsed();
        Set<ActualDevice> allActualDevices = projectDeviceList.stream()
                .flatMap(projectDevice -> configuration.getActualDeviceOrActualDeviceOfIdenticalDevice(projectDevice).stream())
                .collect(Collectors.toSet());
        Platform.runLater(() -> log.set("List of actual device used \n"));
        for (ActualDevice actualDevice : allActualDevices) {
            Platform.runLater(() -> log.set(" - " + actualDevice.getId() + "\n"));
        }

        Set<String> mpLibraries = allActualDevices.stream()
                .map(actualDevice -> actualDevice.getMpLibrary(project.getSelectedPlatform()))
                .collect(Collectors.toSet());
        mpLibraries.add("MakerPlayground");
//        mpLibraries.add("MP_DEVICE");

        Set<String> externalLibraries = allActualDevices.stream()
                .map(actualDevice -> actualDevice.getExternalLibrary(project.getSelectedPlatform()))
                .flatMap(Collection::stream).collect(Collectors.toSet());

        // Add Cloud Platform libraries
        for(CloudPlatform cloudPlatform: project.getAllCloudPlatforms()) {
            // add abstract .h library for the cloudPlatform.
            mpLibraries.add(cloudPlatform.getLibName());

            // add controller-specific library when using cloudPlatform.
            mpLibraries.add(project.getSelectedController().getCloudPlatformLibraryName(cloudPlatform));

            // add controller-specific external dependency when using cloudPlatform.
            externalLibraries.addAll(project.getSelectedController().getCloudPlatformLibraryDependency(cloudPlatform));
        }

        Platform.runLater(() -> log.set("List of library used \n"));
        for (String libName : mpLibraries) {
            Platform.runLater(() -> log.set(" - " + libName + "\n"));
        }
        for (String libName : externalLibraries) {
            Platform.runLater(() -> log.set(" - " + libName + "\n"));
        }

        updateMessage("Generating project");
        String projectPath = PathUtility.MP_WORKSPACE + File.separator + "upload";
        Platform.runLater(() -> log.set("Generating project at " + projectPath + "\n"));
        try {
            FileUtils.deleteQuietly(new File(projectPath));
            FileUtils.forceMkdir(new File(projectPath));
        } catch (IOException|IllegalArgumentException e) {
            updateMessage("Error: can't create project directory (permission denied)");
            return UploadResult.CANT_CREATE_PROJECT;
        }

        updateProgress(0.4, 1);
        updateMessage("Generating source files and libraries");
        try {
            // generate source file
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(projectPath + File.separator + "main.py", false))){
                bw.write(sourcecode.getCode());
            }
        } catch (IOException | NullPointerException e) {
            updateMessage("Error: Cannot write code to project directory");
            return UploadResult.CANT_WRITE_CODE;
        }

        // get path to the library directory
        Optional<String> libraryPath = DeviceLibrary.INSTANCE.getLibraryPath();
        if (libraryPath.isEmpty()) {
            updateMessage("Error: Missing library directory");
            return UploadResult.MISSING_LIBRARY_DIR;
        }
        Platform.runLater(() -> log.set("Using libraries stored at " + libraryPath.get() + "\n"));

        // copy board specific files
        File codeDir = Paths.get(libraryPath.get(), "devices", project.getProjectConfiguration().getController().getId(), "code").toFile();
        Collection<File> boardSpecificFiles = FileUtils.listFiles(codeDir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
        Platform.runLater(() -> log.set("Board specific files found : " + boardSpecificFiles + "\n"));
        try {
            FileUtils.copyToDirectory(boardSpecificFiles, new File(projectPath));
        } catch (IOException e) {
            updateMessage("Error: Cannot write code to project directory");
            return UploadResult.CANT_WRITE_CODE;
        }

        // copy mp library
        for (String libName: mpLibraries) {
            File source = Paths.get(libraryPath.get(), "lib", project.getSelectedPlatform().getLibFolderName(), libName).toFile();
            File destination = new File(projectPath);
            try {
                FileUtils.copyToDirectory(FileUtils.listFiles(source, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE), destination);
            } catch (IOException e) {
                Platform.runLater(() -> log.set("Error: Missing some libraries (" + libName + ")\n"));
                updateMessage("Error: Missing some libraries");
                return UploadResult.CANT_FIND_LIBRARY;
            }
        }

        //copy and extract external Libraries
        for (String libName : externalLibraries) {
            Path sourcePath = Paths.get(libraryPath.get(),"lib_ext", libName + ".zip");
            String destinationPath = projectPath;
            ZipResourceExtractor.ExtractResult extractResult = ZipResourceExtractor.extract(sourcePath, destinationPath);
            if (extractResult != ZipResourceExtractor.ExtractResult.SUCCESS) {
                Platform.runLater(() -> log.set("Error: Failed to extract libraries (" + sourcePath + ")\n"));
                updateMessage("Error: Failed to extract libraries");
                return UploadResult.CANT_FIND_LIBRARY;
            }
        }

        updateProgress(0.6, 1);
        updateMessage("Uploading to board");

        SerialPort serialPort = uploadTarget.getSerialPort();
        String serialPortName = OSInfo.getOs() == OSInfo.OS.WINDOWS ? serialPort.getSystemPortName() : "/dev/" + serialPort.getSystemPortName();

        List<String> fileList;
        UploadResult result;

        // get flash directory prefix (None for ESP32, /flash for K210 etc.)
        String prefix = "";
        fileList = new ArrayList<>();
        result = runAmpyCommand(ampyCommand.get(), projectPath, List.of("-p", serialPortName, "ls")
                , false, "Error: Can't list file/directory on the board", UploadResult.CANT_FIND_BOARD, fileList);
        if (result != UploadResult.OK) {
            return result;
        }
        if (fileList.contains("/sd")) {
            prefix = "/sd";
        } else if (fileList.contains("/flash")) {
            prefix = "/flash";
        }

        try {
            Path uploadingDir = Path.of(projectPath);
            for (Path path : Files.list(uploadingDir).collect(Collectors.toList())) {
                String sourcePath = uploadingDir.relativize(path).toString();
                String destPath = prefix.isEmpty() ? sourcePath : prefix + "/" + sourcePath;
                result = runAmpyCommand(ampyCommand.get(), projectPath, List.of("-p", serialPortName, "put", sourcePath, destPath)
                        , false, "Error: Can't upload file/directory to the board", UploadResult.CANT_FIND_BOARD, null);
                if (result != UploadResult.OK) {
                    return result;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        updateProgress(0.9, 1);
        updateMessage("Reset the board");
        result = runAmpyCommand(ampyCommand.get(), projectPath, List.of("-p", serialPortName, "reset", "--hard")
                , false, "Error: Can't reset the board", UploadResult.CANT_RESET_BOARD, null);
        if (result != UploadResult.OK) {
            return result;
        }

        updateProgress(1, 1);
        updateMessage("Done");

        return UploadResult.OK;
    }

    private UploadResult runAmpyCommand(List<String> ampyCommand, String projectPath, List<String> args, boolean allowError, String errorMessage, UploadResult error, List<String> commandOutput) {
        Process p = null;
        try {
            // create argument list
            List<String> arguments = new ArrayList<>(ampyCommand);
            arguments.addAll(args);
            Platform.runLater(() -> log.set("Executing " + arguments + "\n"));
            // create process to invoke ampy
            ProcessBuilder builder = new ProcessBuilder(arguments);
            builder.directory(new File(projectPath).getAbsoluteFile()); // this is where you set the root folder for the executable to run with
            builder.redirectErrorStream(true);
            p = builder.start();
            // wait for at most 30 seconds for the process to finish before returning error
            if (!p.waitFor(30, TimeUnit.SECONDS)) {
                killProcess(p);
                updateMessage(errorMessage);
                return error;
            }
            try (Scanner s = new Scanner(p.getInputStream())) {
                while (s.hasNextLine()) {
                    if (isCancelled()) {
                        throw new InterruptedException();
                    }
                    String line = s.nextLine();
                    if (commandOutput != null) {
                        commandOutput.add(line);
                    }
                    Platform.runLater(() -> log.set(line + "\n"));
                }
            }
            int result = p.exitValue();
            if (!allowError && result != 0) {
                updateMessage(errorMessage);
                return error;
            }
        } catch (InterruptedException e) {
            if (isCancelled()) {
                updateMessage("Canceling upload...");
                killProcess(p);
                updateMessage("Upload has been canceled");
                return UploadResult.USER_CANCEL;
            }
            updateMessage("Unknown error has occurred. Please try again.");
            return UploadResult.UNKNOWN_ERROR;
        } catch (IOException e) {
            updateMessage("Unknown error (I/O) has occurred. Please try again.");
            return UploadResult.UNKNOWN_ERROR;
        }
        return UploadResult.OK;
    }

    private void killProcess(Process p) {
        try {
            if (OSInfo.getOs() == OSInfo.OS.WINDOWS) {
                Runtime.getRuntime().exec("taskkill /f /t /pid " + p.pid());
            } else {
                p.destroy();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
