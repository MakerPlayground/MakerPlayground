package io.makerplayground.ui;

import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.device.actual.ActualDevice;
import io.makerplayground.device.actual.CloudPlatform;
import io.makerplayground.generator.devicemapping.ProjectLogic;
import io.makerplayground.generator.devicemapping.ProjectMappingResult;
import io.makerplayground.generator.source.SourceCodeResult;
import io.makerplayground.generator.upload.UploadResult;
import io.makerplayground.project.Project;
import io.makerplayground.util.OSInfo;
import io.makerplayground.util.PathUtility;
import io.makerplayground.util.ZipArchiver;
import io.makerplayground.util.ZipResourceExtractor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class ArduinoExportTask extends ProjectExportTask {
    ArduinoExportTask(Project project, SourceCodeResult sourcecode, String zipFilePath) {
        super(project, sourcecode, zipFilePath);
    }

    @Override
    protected ExportResult call() throws Exception {
        updateProgress(0, 1);
        updateMessage("Checking project");

        ProjectMappingResult mappingResult = ProjectLogic.validateDeviceAssignment(project);
        if (mappingResult != ProjectMappingResult.OK) {
            updateMessage(mappingResult.getErrorMessage());
            exportResult = ExportResult.DEVICE_OR_PORT_MISSING;
            throw new IllegalStateException("");
        }

        if (sourcecode.getError() != null) {
            updateMessage("Error: " + sourcecode.getError().getDescription());
            exportResult = ExportResult.CANT_GENERATE_CODE;
            throw new IllegalStateException("");
        }

        updateProgress(0.2, 1);
        updateMessage("Checking libraries");

        List<ActualDevice> actualDevicesUsed = project.getAllDeviceUsed().stream()
                .filter(projectDevice -> configuration.getActualDevice(projectDevice).isPresent())
                .map(configuration::getActualDevice)
                .map(Optional::get)
                .collect(Collectors.toList());

        Set<String> mpLibraries = actualDevicesUsed.stream()
                .map(actualDevice -> actualDevice.getMpLibrary(project.getSelectedPlatform()))
                .collect(Collectors.toSet());
        mpLibraries.add("MakerPlayground");
        mpLibraries.add("MP_DEVICE");

        Set<String> externalLibraries = actualDevicesUsed.stream()
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

        // SPECIAL CASE: apply fixed for atmega328pb used in MakerPlayground Baseboard
        if (project.getSelectedController().getPioBoardId().equals("ATmega328PB")) {
            externalLibraries.add("Wire");
            externalLibraries.add("SPI");
        }

        updateProgress(0.4, 1);
        updateMessage("Generating project code");

        String projectPath = PathUtility.MP_WORKSPACE + File.separator + "upload";
        try {
            FileUtils.deleteDirectory(new File(projectPath));
            FileUtils.forceMkdir(new File(projectPath));
        } catch (IOException e) {
            updateMessage("Error: can't create project directory (permission denied)");
            exportResult = ExportResult.CANT_CREATE_PROJECT;
            throw new IllegalStateException("");
        }

        Optional<List<String>> pioCommand = PathUtility.getPlatformIOCommand();
        Optional<String> pioHomeDirPath = PathUtility.getIntegratedPIOHomeDirectory();

        UploadResult result = runPlatformIOCommand(pioCommand.get(), projectPath, pioHomeDirPath
                , List.of("init", "--board", project.getSelectedController().getPioBoardId())
                , "Error: Can't create project directory (permission denied)", UploadResult.CANT_CREATE_PROJECT);
        if (result != UploadResult.OK) {
            updateMessage("Error: can't create project directory (permission denied)");
            exportResult = ExportResult.CANT_CREATE_PROJECT;
            throw new IllegalStateException("");
        }

        updateProgress(0.6, 1);
        updateMessage("Generating PlatformIO project files and extracting the libraries");

        try {
            FileUtils.forceMkdir(new File(projectPath + File.separator + "src"));
            FileUtils.forceMkdir(new File(projectPath + File.separator + "lib"));

            // generate source file
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(projectPath + File.separator + "src" + File.separator + "main.cpp"))){
                bw.write(sourcecode.getCode());
            }
        } catch (IOException | NullPointerException e) {
            updateMessage("Error: Cannot write code to project directory");
            exportResult = ExportResult.CANT_WRITE_CODE;
            throw new IllegalStateException("");
        }

        // get path to the library directory
        Optional<String> libraryPath = DeviceLibrary.getLibraryPath();
        if (libraryPath.isEmpty()) {
            updateMessage("Error: Missing library directory");
            exportResult = ExportResult.MISSING_LIBRARY_DIR;
            throw new IllegalStateException("");
        }

        // copy mp library
        for (String libName: mpLibraries) {
            File source = Paths.get(libraryPath.get(), "lib", project.getSelectedPlatform().getLibFolderName(), libName).toFile();
            File destination = Paths.get(projectPath, "lib", libName).toFile();
            try {
                FileUtils.copyDirectory(source, destination);
            } catch (IOException e) {
                updateMessage("Error: Missing some libraries");
                exportResult = ExportResult.CANT_FIND_LIBRARY;
                throw new IllegalStateException("CANT_FIND_LIBRARY");
            }
        }


        //copy and extract external Libraries
        for (String libName : externalLibraries) {
            Path sourcePath = Paths.get(libraryPath.get(),"lib_ext", libName + ".zip");
            String destinationPath = projectPath + File.separator + "lib";
            ZipResourceExtractor.ExtractResult extractResult = ZipResourceExtractor.extract(sourcePath, destinationPath);
            if (extractResult != ZipResourceExtractor.ExtractResult.SUCCESS) {
                updateMessage("Error: Failed to extract libraries");
                exportResult = ExportResult.CANT_FIND_LIBRARY;
                throw new IllegalStateException("CANT_FIND_LIBRARY");
            }
        }

        updateProgress(0.8, 1);
        updateMessage("Archiving the project");

        if (ZipArchiver.archiveDirectory(projectPath, zipFilePath, FilenameUtils.getBaseName(zipFilePath)) == ZipArchiver.ArchiveResult.FAIL) {
            exportResult = ExportResult.FAIL_TO_CREATE_ARCHIVE;
            throw new IllegalStateException("FAIL_TO_CREATE_ARCHIVE");
        }

        updateProgress(1, 1);
        updateMessage("Done");

        return ExportResult.OK;
    }

    protected UploadResult runPlatformIOCommand(List<String> pioCommand, String projectPath, Optional<String> pioHomeDirPath, List<String> args
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
                    s.nextLine();
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
