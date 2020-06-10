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

import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.device.actual.ActualDevice;
import io.makerplayground.device.actual.CloudPlatform;
import io.makerplayground.generator.devicemapping.ProjectLogic;
import io.makerplayground.generator.devicemapping.ProjectMappingResult;
import io.makerplayground.generator.source.RpiPythonInteractiveCode;
import io.makerplayground.generator.source.RpiPythonUploadCode;
import io.makerplayground.generator.source.SourceCodeResult;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectDevice;
import io.makerplayground.util.*;
import javafx.application.Platform;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class RaspberryPiUploadTask extends UploadTaskBase {

    protected RaspberryPiUploadTask(Project project, UploadTarget uploadTarget, boolean isInteractiveUpload) {
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
            Platform.runLater(()->updateMessage("Upload has been canceled"));
            return UploadResult.USER_CANCEL;
        }

        ProjectMappingResult mappingResult = ProjectLogic.validateDeviceAssignment(project);
        if (mappingResult != ProjectMappingResult.OK) {
            Platform.runLater(()->updateMessage(mappingResult.getErrorMessage()));
            return UploadResult.DEVICE_OR_PORT_MISSING;
        }

        SourceCodeResult sourcecode = interactiveUpload ? RpiPythonInteractiveCode.generateCode(project) : RpiPythonUploadCode.generateCode(project);
        if (sourcecode.getError() != null) {
            Platform.runLater(()->updateMessage("Error: " + sourcecode.getError().getDescription()));
            return UploadResult.CANT_GENERATE_CODE;
        }

        updateProgress(0.10, 1);
        updateMessage("Checking required dependencies");

        String ip = uploadTarget.getRpiHostName();
        String urlStr = "http://" + ip + ":" + RpiServiceChecker.PORT;

        // Test ping to device and check if it has makerplayground runtime.
        Platform.runLater(() -> log.set("Workspace is at " + PathUtility.MP_WORKSPACE + "\n"));
        try {
            URL url = new URL(urlStr);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            int status = con.getResponseCode();
            Reader streamReader;
            if (status > 299) {
                streamReader = new InputStreamReader(con.getErrorStream());
            } else {
                streamReader = new InputStreamReader(con.getInputStream());
            }
            BufferedReader bufferedReader = new BufferedReader(streamReader);
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = bufferedReader.readLine()) != null) {
                content.append(inputLine);
            }
            streamReader.close();
            con.disconnect();
            if (!"makerplayground".contentEquals(content)) {
                Platform.runLater(()->updateMessage("Could not connect to the Raspberry Pi on " + ip));
                return UploadResult.CANT_FIND_BOARD;
            }
        } catch (IOException e) {
            Platform.runLater(()->updateMessage("Could not connect to the Raspberry Pi on " + ip));
            return UploadResult.CANT_FIND_BOARD;
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

        Set<String> externalLibraries = allActualDevices.stream()
                .map(actualDevice -> actualDevice.getExternalLibrary(project.getSelectedPlatform()))
                .flatMap(Collection::stream).collect(Collectors.toSet());

        // Add Cloud Platform libraries
        for(CloudPlatform cloudPlatform: project.getCloudPlatformUsed()) {
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

        String projectPath = PathUtility.MP_WORKSPACE + File.separator + "script";
        Platform.runLater(() -> log.set("Generating project at " + projectPath + "\n"));
        try {
            FileUtils.deleteDirectory(new File(projectPath));
            FileUtils.forceMkdir(new File(projectPath));
        } catch (IOException e) {
            updateMessage("Error: can't create project directory (permission denied)");
            return UploadResult.CANT_CREATE_PROJECT;
        }

        String projectZipPath = PathUtility.MP_WORKSPACE + File.separator + "script.zip";
        File zipFile = new File(projectZipPath);
        try {
            if (zipFile.exists()) {
                FileUtils.forceDelete(zipFile);
            }
        } catch (IOException e) {
            updateMessage("Error: can't create project zip file (permission denied)");
            return UploadResult.CANT_CREATE_PROJECT;
        }

        updateProgress(0.4, 1);
        updateMessage("Generating source files and libraries");

        // get path to the library directory
        Optional<String> libraryPath = DeviceLibrary.getLibraryPath();
        if (libraryPath.isEmpty()) {
            Platform.runLater(()->updateMessage("Error: Missing library directory"));
            return UploadResult.MISSING_LIBRARY_DIR;
        }
        Platform.runLater(() -> log.set("Using libraries stored at " + libraryPath.get() + "\n"));

        // generate source file
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(projectPath + File.separator + "main.py"))){
            bw.write(sourcecode.getCode());
        } catch (IOException e) {
            Platform.runLater(()->updateMessage("Error: Cannot write code to project directory"));
            return UploadResult.CANT_WRITE_CODE;
        }

        // copy mp library
        for (String libName: mpLibraries) {
            File source = Paths.get(libraryPath.get(), "lib", project.getSelectedPlatform().getLibFolderName(), libName).toFile();
            File destination = Paths.get(projectPath, libName).toFile();
            try {
                FileUtils.copyDirectory(source, destination);
            } catch (IOException e) {
                Platform.runLater(() -> log.set("Error: Missing some libraries (" + libName + ")\n"));
                Platform.runLater(()->updateMessage("Error: Missing some libraries"));
                return UploadResult.CANT_FIND_LIBRARY;
            }
        }

        //copy and extract external Libraries
        for (String libName : externalLibraries) {
            Path sourcePath = Paths.get(libraryPath.get(),"lib_ext", libName + ".zip");
            ZipResourceExtractor.ExtractResult extractResult = ZipResourceExtractor.extract(sourcePath, projectPath);
            if (extractResult != ZipResourceExtractor.ExtractResult.SUCCESS) {
                Platform.runLater(() -> log.set("Error: Failed to extract libraries (" + sourcePath + ")\n"));
                Platform.runLater(()->updateMessage("Error: Failed to extract libraries"));
                return UploadResult.CANT_FIND_LIBRARY;
            }
        }
        updateProgress(0.6, 1);
        updateMessage("Finalizing project");
        ZipArchiver.archiveDirectory(projectPath, projectZipPath);

        updateProgress(0.8, 1);
        updateMessage("Uploading to board");

        // upload code
        try {
            MultipartUtility multipart = new MultipartUtility(urlStr + "/upload");
            multipart.addFilePart("script", zipFile);
            multipart.finish();
        } catch (IOException e) {
            e.printStackTrace();
            Platform.runLater(()->updateMessage("Cannot upload project to Raspberry Pi. Please try again"));
            return UploadResult.CANT_WRITE_CODE;
        }

        updateProgress(1, 1);
        updateMessage("Done");
        return UploadResult.OK;
    }
}
