package io.makerplayground.generator.upload;

import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.device.actual.ActualDevice;
import io.makerplayground.device.actual.CloudPlatform;
import io.makerplayground.generator.DeviceMapper;
import io.makerplayground.generator.DeviceMapperResult;
import io.makerplayground.generator.source.SourceCodeGenerator;
import io.makerplayground.generator.source.SourceCodeResult;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectDevice;
import io.makerplayground.util.OSInfo;
import io.makerplayground.util.ZipResourceExtractor;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.concurrent.Task;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class UploadTask extends Task<UploadResult> {

    private final Project project;
    private final ReadOnlyStringWrapper log;

    // workspace directory for storing generated project folder
    private final String MP_WORKSPACE = System.getProperty("user.home") + File.separator + ".makerplayground";
    // program installation directory
    private final String MP_INSTALLDIR = new File("").getAbsoluteFile().getPath();

    public UploadTask(Project project) {
        this.project = project;
        this.log = new ReadOnlyStringWrapper();
    }

    @Override
    protected UploadResult call() {
        updateProgress(0, 1);
        updateMessage("Checking project");

        DeviceMapperResult mappingResult = DeviceMapper.autoAssignDevices(project);
        if (mappingResult == DeviceMapperResult.NOT_ENOUGH_PORT) {
            updateMessage("Error: not enough port available");
            return UploadResult.NOT_ENOUGH_PORT;
        } else if (mappingResult == DeviceMapperResult.NO_SUPPORT_DEVICE) {
            updateMessage("Error: can't find support device");
            return UploadResult.NO_SUPPORT_DEVICE;
        } else if (mappingResult == DeviceMapperResult.NO_MCU_SELECTED) {
            updateMessage("Error: please select a mcu");
            return UploadResult.NO_MCU_SELECTED;
        } else if (mappingResult != DeviceMapperResult.OK) {
            updateMessage("Error: found unknown error");
            return UploadResult.UNKNOWN_ERROR;
        }

        SourceCodeResult sourcecode = SourceCodeGenerator.generateCode(project, true);
        if (sourcecode.getError() != null) {
            updateMessage("Error: " + sourcecode.getError().getDescription());
            return UploadResult.CANT_GENERATE_CODE;
        }

        updateProgress(0.10, 1);
        updateMessage("Checking required dependencies");

        Platform.runLater(() -> log.set("Install directory is at " + MP_INSTALLDIR + "\n"));

        // check platformio installation
        Optional<String> pythonPath = getPythonPath();
        if (!pythonPath.isPresent()) {
            updateMessage("Error: Can't find python with valid platformio installation see: http://docs.platformio.org/en/latest/installation.html");
            return UploadResult.CANT_FIND_PIO;
        }
        Platform.runLater(() -> log.set("Using python at " + pythonPath.get() + "\n"));

        // check platformio home directory
        Optional<String> pioHomeDirPath = getIntegratedPIOHomeDirectory();
        if (pioHomeDirPath.isPresent()) {
            Platform.runLater(() -> log.set("Using integrated platformio dependencies at " + pioHomeDirPath.get() + "\n"));
        } else {
            Platform.runLater(() -> log.set("Using default platformio dependencies folder (~/.platformio) \n"));
        }

        updateProgress(0.20, 1);
        updateMessage("Preparing to generate project");

        List<ActualDevice> actualDevicesUsed = project.getAllDeviceUsed().stream()
                .map(ProjectDevice::getActualDevice)
                .collect(Collectors.toList());
        Platform.runLater(() -> log.set("List of actual device used \n"));
        for (String actualDeviceId :
                actualDevicesUsed.stream().map(ActualDevice::getId).collect(Collectors.toList())) {
            Platform.runLater(() -> log.set(" - " + actualDeviceId + "\n"));
        }

        Set<String> mpLibraries = actualDevicesUsed.stream()
                .map(ActualDevice::getMpLibrary)
                .collect(Collectors.toSet());
        mpLibraries.add("MakerPlayground");
        mpLibraries.add("MP_DEVICE");

        Set<String> externalLibraries = actualDevicesUsed.stream()
                .map(ActualDevice::getExternalLibrary)
                .flatMap(Collection::stream).collect(Collectors.toSet());

        // Add Cloud Platform libraries
        for(CloudPlatform cloudPlatform: project.getCloudPlatformUsed()) {
            // add abstract .h library for the cloudPlatform.
            mpLibraries.add(cloudPlatform.getLibName());

            // add controller-specific library when using cloudPlatform.
            mpLibraries.add(project.getController().getCloudPlatformLibraryName(cloudPlatform));

            // add controller-specific external dependency when using cloudPlatform.
            externalLibraries.addAll(project.getController().getCloudPlatformLibraryDependency(cloudPlatform));
        }

        // SPECIAL CASE: apply fixed for atmega328pb used in MakerPlayground Baseboard
        if (project.getController().getPlatformIOBoardId().equals("atmega328pb")) {
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
        String projectPath = MP_WORKSPACE + File.separator + "upload";
        Platform.runLater(() -> log.set("Generating project at " + projectPath + "\n"));
        try {
            FileUtils.deleteDirectory(new File(projectPath));
            FileUtils.forceMkdir(new File(projectPath));
        } catch (IOException e) {
            updateMessage("Error: can't create project directory (permission denied)");
            return UploadResult.CANT_CREATE_PROJECT;
        }
        UploadResult result = runPlatformIOCommand(pythonPath.get(), projectPath, pioHomeDirPath
                , List.of("init", "--board", project.getController().getPlatformIOBoardId())
                , "Error: Can't create project directory (permission denied)", UploadResult.CANT_CREATE_PROJECT);
        if (result != UploadResult.OK) {
            return result;
        }

        updateProgress(0.4, 1);
        updateMessage("Generating source files and libraries");
        try {
            FileUtils.forceMkdir(new File(projectPath + File.separator + "src"));
            FileUtils.forceMkdir(new File(projectPath + File.separator + "lib"));

            // generate source file
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(projectPath + File.separator + "src" + File.separator + "main.cpp"))){
                bw.write(sourcecode.getCode());
            }
        } catch (IOException | NullPointerException e) {
            updateMessage("Error: Cannot write code to project directory");
            return UploadResult.CANT_WRITE_CODE;
        }

        // get path to the library directory
        Optional<String> libraryPath = DeviceLibrary.INSTANCE.getLibraryPath();
        if (!libraryPath.isPresent()) {
            updateMessage("Error: Missing library directory");
            return UploadResult.MISSING_LIBRARY_DIR;
        }
        Platform.runLater(() -> log.set("Using libraries stored at " + libraryPath.get() + "\n"));

        // copy mp library
        for (String libName: mpLibraries) {
            File source = Paths.get(libraryPath.get(), "lib", project.getPlatform().getLibraryFolderName(), libName).toFile();
            File destination = Paths.get(projectPath, "lib", libName).toFile();
            try {
                FileUtils.copyDirectory(source, destination);
            } catch (IOException e) {
                Platform.runLater(() -> log.set("Error: Missing some libraries (" + libraryPath + ")\n"));
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
                Platform.runLater(() -> log.set("Error: Failed to extract libraries (" + libraryPath + ")\n"));
                updateMessage("Error: Failed to extract libraries");
                return UploadResult.CANT_FIND_LIBRARY;
            }
        }

        updateProgress(0.6, 1);
        updateMessage("Building project");
        result = runPlatformIOCommand(pythonPath.get(), projectPath, pioHomeDirPath, List.of("run"),
                "Error: Can't build the generated sourcecode. Please contact the development team.", UploadResult.CODE_ERROR);
        if (result != UploadResult.OK) {
            return result;
        }

        updateProgress(0.8, 1);
        updateMessage("Uploading to board");
        result = runPlatformIOCommand(pythonPath.get(), projectPath, pioHomeDirPath, List.of("run", "--target", "upload"),
                "Error: Can't find board. Please check connection.", UploadResult.CANT_FIND_BOARD);
        if (result != UploadResult.OK) {
            return result;
        }

        updateProgress(1, 1);
        updateMessage("Done");

        return UploadResult.OK;
    }

    private UploadResult runPlatformIOCommand(String pythonPath, String projectPath, Optional<String> pioHomeDirPath, List<String> args
            , String errorMessage, UploadResult error) {
        Process p = null;
        try {
            // create argument list
            List<String> arguments = new ArrayList<>(List.of(pythonPath, "-m", "platformio"));
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

    public ReadOnlyStringProperty logProperty() {
        return log.getReadOnlyProperty();
    }

    /**
     * Get path to python with usable platformio installation
     * @return path to valid python installation or Optional.empty()
     */
    private Optional<String> getPythonPath() {
        List<String> path = List.of(MP_INSTALLDIR + File.separator + "python-2.7.13" + File.separator + "python"      // integrated python for windows version
                                    , "python"                  // python in user's system path
                                    , "/usr/bin/python");       // internal python of macOS and Linux which is used by platformio installation script

        for (String s : path) {
            try {
                Process p = new ProcessBuilder(s, "-m", "platformio").redirectErrorStream(true).start();
                // read from an input stream to prevent the child process from stalling
                try (BufferedReader processOutputReader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    String readLine;
                    while ((readLine = processOutputReader.readLine()) != null) {
//                        System.out.println(readLine);
                    }
                }
                if (p.waitFor(5, TimeUnit.SECONDS) && (p.exitValue() == 0)) {
                    return Optional.of(s);
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
    private Optional<String> getIntegratedPIOHomeDirectory() {
        List<String> path = List.of(MP_INSTALLDIR + File.separator + "platformio"   // default path for Windows installer and when running from the IDE
                , "/Library/Application Support/MakerPlayground/platformio");       // default path for macOS installer
        return path.stream().filter(s -> new File(s).exists()).findFirst();
    }
}
