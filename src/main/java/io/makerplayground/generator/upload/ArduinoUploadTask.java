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

package io.makerplayground.generator.upload;

import com.fazecast.jSerialComm.SerialPort;
import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.device.actual.ActualDevice;
import io.makerplayground.device.actual.CloudPlatform;
import io.makerplayground.device.actual.DeviceType;
import io.makerplayground.generator.devicemapping.ProjectLogic;
import io.makerplayground.generator.devicemapping.ProjectMappingResult;
import io.makerplayground.generator.source.ArduinoUploadCode;
import io.makerplayground.generator.source.ArduinoInteractiveCode;
import io.makerplayground.generator.source.SourceCodeResult;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectDevice;
import io.makerplayground.util.OSInfo;
import io.makerplayground.util.PathUtility;
import io.makerplayground.util.ZipResourceExtractor;
import javafx.application.Platform;
import org.apache.commons.io.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class ArduinoUploadTask extends UploadTaskBase {

    protected ArduinoUploadTask(Project project, UploadTarget uploadTarget, boolean isInteractiveUpload) {
        super(project, uploadTarget, isInteractiveUpload);
    }

    @Override
    protected UploadResult doUpload() {
        SerialPort serialPort = uploadTarget.getSerialPort();
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

        SourceCodeResult sourcecode = interactiveUpload ? ArduinoInteractiveCode.generateCode(project) : ArduinoUploadCode.generateCode(project);
        if (sourcecode.getError() != null) {
            updateMessage("Error: " + sourcecode.getError().getDescription());
            return UploadResult.CANT_GENERATE_CODE;
        }

        updateProgress(0.10, 1);
        updateMessage("Checking required dependencies");

        Platform.runLater(() -> log.set("Workspace is at " + PathUtility.MP_WORKSPACE + "\n"));

        // check platformio installation
        Optional<List<String>> pioCommand = PathUtility.getPlatformIOCommand();
        if (pioCommand.isEmpty()) {
            updateMessage("Error: Can't find valid platformio installation see: http://docs.platformio.org/en/latest/installation.html");
            return UploadResult.CANT_FIND_PIO;
        }
        Platform.runLater(() -> log.set("Execute platform by " + pioCommand.get() + "\n"));

        // check platformio home directory
        Optional<String> pioHomeDirPath = PathUtility.getIntegratedPIOHomeDirectory();
        if (pioHomeDirPath.isPresent()) {
            Platform.runLater(() -> log.set("Using integrated platformio dependencies at " + pioHomeDirPath.get() + "\n"));
        } else {
            Platform.runLater(() -> log.set("Using default platformio dependencies folder (~/.platformio) \n"));
        }

        updateProgress(0.20, 1);

        updateMessage("Preparing to generate project");
        Collection<ProjectDevice> projectDeviceList = interactiveUpload ? project.getUnmodifiableProjectDevice() : project.getAllDeviceUsed();
        List<ActualDevice> allActualDevices = projectDeviceList.stream()
                .filter(projectDevice -> configuration.getActualDevice(projectDevice).isPresent())
                .map(configuration::getActualDevice)
                .map(Optional::get)
                .collect(Collectors.toList());
        Platform.runLater(() -> log.set("List of actual device used \n"));
        for (String actualDeviceId :
                allActualDevices.stream().map(ActualDevice::getId).collect(Collectors.toList())) {
            Platform.runLater(() -> log.set(" - " + actualDeviceId + "\n"));
        }

        Set<String> mpLibraries = allActualDevices.stream()
                .map(actualDevice -> actualDevice.getMpLibrary(project.getSelectedPlatform()))
                .collect(Collectors.toSet());
        mpLibraries.add("MakerPlayground");
        mpLibraries.add("MP_DEVICE");

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

        // SPECIAL CASE: apply fixed for atmega328pb used in MakerPlayground Baseboard
        if (project.getSelectedController().getPioBoardId().equals("atmega328pb")) {
            externalLibraries.add("Wire");
            externalLibraries.add("SPI");
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
        String iniFilePath = projectPath + File.separator + "platformio.ini";
        Platform.runLater(() -> log.set("Generating project at " + projectPath + "\n"));
        try {
            FileUtils.forceMkdir(new File(projectPath));
            List<String> params = new ArrayList<>();
            params.add("init");
            DeviceLibrary.INSTANCE.getActualDevice(DeviceType.CONTROLLER).stream().map(ActualDevice::getPioBoardId).filter(s -> !s.isBlank()).distinct().forEach(s -> {
                params.add("--board");
                params.add(s);
            });
            UploadResult result = runPlatformIOCommand(pioCommand.get(), projectPath, pioHomeDirPath
                    , params
                    , "Error: Can't create project directory (permission denied)", UploadResult.CANT_CREATE_PROJECT);
            if (result != UploadResult.OK) {
                return result;
            }
        } catch (IOException e) {
            updateMessage("Error: can't create project directory (permission denied)");
            return UploadResult.CANT_CREATE_PROJECT;
        }

        updateProgress(0.4, 1);
        updateMessage("Generating source files and libraries");
        try {
            FileUtils.forceMkdir(new File(projectPath + File.separator + "src"));
            FileUtils.forceMkdir(new File(projectPath + File.separator + "lib"));

            // generate source file
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(projectPath + File.separator + "src" + File.separator + "main.cpp", false))){
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

        try {
            FileUtils.cleanDirectory(Paths.get(projectPath, "lib").toFile());
        } catch (IOException e) {
            // Do nothing
        }
        // copy mp library
        for (String libName: mpLibraries) {
            File source = Paths.get(libraryPath.get(), "lib", project.getSelectedPlatform().getLibFolderName(), libName).toFile();
            File destination = Paths.get(projectPath, "lib", libName).toFile();
            try {
                FileUtils.copyDirectory(source, destination);
            } catch (IOException e) {
                Platform.runLater(() -> log.set("Error: Missing some libraries (" + libName + ")\n"));
                updateMessage("Error: Missing some libraries");
                return UploadResult.CANT_FIND_LIBRARY;
            }
        }

        //copy and extract external Libraries
        for (String libName : externalLibraries) {
            Path sourcePath = Paths.get(libraryPath.get(),"lib_ext", libName + ".zip");
            String destinationPath = projectPath + File.separator + "lib";
            ZipResourceExtractor.ExtractResult extractResult = ZipResourceExtractor.extract(sourcePath, destinationPath);
            if (extractResult != ZipResourceExtractor.ExtractResult.SUCCESS) {
                Platform.runLater(() -> log.set("Error: Failed to extract libraries (" + sourcePath + ")\n"));
                updateMessage("Error: Failed to extract libraries");
                return UploadResult.CANT_FIND_LIBRARY;
            }
        }

        updateProgress(0.6, 1);
        updateMessage("Building project");
        UploadResult result = runPlatformIOCommand(pioCommand.get(), projectPath, pioHomeDirPath, List.of("run", "-e", project.getSelectedController().getPioBoardId()),
                "Error: Can't build the generated sourcecode. Please contact the development team.", UploadResult.CODE_ERROR);
        if (result != UploadResult.OK) {
            return result;
        }

        updateProgress(0.8, 1);
        updateMessage("Uploading to board");
        result = runPlatformIOCommand(pioCommand.get(), projectPath, pioHomeDirPath, List.of("run", "-e", project.getSelectedController().getPioBoardId(), "-t", "upload"),
                "Error: Can't find board. Please check connection.", UploadResult.CANT_FIND_BOARD);
        if (result != UploadResult.OK) {
            return result;
        }

        updateProgress(1, 1);
        updateMessage("Done");

        return UploadResult.OK;
    }

    private UploadResult runPlatformIOCommand(List<String> pioCommand, String projectPath, Optional<String> pioHomeDirPath, List<String> args
            , String errorMessage, UploadResult error) {
        Process p = null;
        try {
            // create argument list
            List<String> arguments = new ArrayList<>(pioCommand);
            arguments.addAll(args);
            // create process to invoke platformio
            ProcessBuilder builder = new ProcessBuilder(arguments);
            builder.directory(new File(projectPath).getAbsoluteFile()); // this is where you set the root folder for the executable to run with
            pioHomeDirPath.ifPresent(s -> builder.environment().put("PLATFORMIO_HOME_DIR", s));
            builder.redirectErrorStream(true);
            p = builder.start();
            try (Scanner s = new Scanner(p.getInputStream())) {
                while (s.hasNextLine()) {
                    if (isCancelled()) {
                        throw new InterruptedException();
                    }
                    String line = s.nextLine();
                    Platform.runLater(() -> log.set(line + "\n"));
                }
            }
            int result = p.waitFor();
            if (result != 0) {
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
