package io.makerplayground.generator;

import io.makerplayground.device.Device;
import io.makerplayground.helper.UploadResult;
import io.makerplayground.project.Project;
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

    private final String MP_WORKSPACE = System.getProperty("user.home") + File.separator + ".makerplayground";

    public UploadTask(Project project) {
        this.project = project;
        this.log = new ReadOnlyStringWrapper();
    }

    @Override
    protected UploadResult call() {
        updateProgress(0, 1);
        updateMessage("Checking project");

        /* Device Mapping */
        DeviceMapper.DeviceMapperResult mappingResult = DeviceMapper.autoAssignDevices(project);
        if (mappingResult == DeviceMapper.DeviceMapperResult.NOT_ENOUGH_PORT) {
            updateMessage("Error: not enough port available");
            return UploadResult.NOT_ENOUGH_PORT;
        } else if (mappingResult == DeviceMapper.DeviceMapperResult.NO_SUPPORT_DEVICE) {
            updateMessage("Error: can't find support device");
            return UploadResult.NO_SUPPORT_DEVICE;
        } else if (mappingResult == DeviceMapper.DeviceMapperResult.NO_MCU_SELECTED) {
            updateMessage("Error: please select a mcu");
            return UploadResult.NO_MCU_SELECTED;
        } else if (mappingResult != DeviceMapper.DeviceMapperResult.OK) {
            updateMessage("Error: found unknown error");
            return UploadResult.UNKNOWN_ERROR;
        }

        /* Code Generating */
        Sourcecode sourcecode = SourcecodeGenerator.generateCode(project, true);
        if (sourcecode.getError() != null) {
            updateMessage("Error: " + sourcecode.getError().getDescription());
            return UploadResult.CANT_GENERATE_CODE;
        }

        updateProgress(0.15, 1);

        /* check platformio installation */
        if (OSInfo.OS.WINDOWS == OSInfo.getOs() && !checkPlatformio().isPresent()) {
            updateMessage("Installing Python");
            Platform.runLater(() -> log.set("extracting python"));
            extractPythonFromJar();
            Platform.runLater(() -> log.set("successfully extract python"));
        }
        Optional<String> pythonPath = checkPlatformio();
        if (!pythonPath.isPresent()) {
            updateMessage("Error: Can't find platformio see: http://docs.platformio.org/en/latest/installation.html");
            return UploadResult.CANT_FIND_PIO;
        }
        Platform.runLater(() -> log.set("Using python at " + pythonPath.get() + "\n"));

        /* Library List Preparing */
        updateProgress(0.25, 1);
        updateMessage("Preparing to generate project");
        String platform = project.getPlatform().getPlatformioId();
        String code = sourcecode.getCode();

        List<Device> actualDevicesUsed = project.getAllDeviceUsed().stream()
                .map(projectDevice -> projectDevice.getActualDevice())
                .collect(Collectors.toList());
        Platform.runLater(() -> log.set("List of actual device used \n"));
        for (String actualDeviceId :
                actualDevicesUsed.stream().map(device -> device.getId()).collect(Collectors.toList())) {
            Platform.runLater(() -> log.set(" - " + actualDeviceId + "\n"));
        }

        Set<String> mpLibraries = actualDevicesUsed.stream()
                .map(device -> device.getMpLibrary())
                .collect(Collectors.toSet());
        Set<String> externalLibraries = actualDevicesUsed.stream()
                .map(device -> device.getExternalLibrary())
                .flatMap(Collection::stream).collect(Collectors.toSet());
        Platform.runLater(() -> log.set("List of library used \n"));
        for (String libName : mpLibraries) {
            Platform.runLater(() -> log.set(" - " + libName + "\n"));
        }
        for (String libName : externalLibraries) {
            Platform.runLater(() -> log.set(" - " + libName + "\n"));
        }

        /* Source Files Preparing */
        updateProgress(0.3, 1);
        updateMessage("Preparing platformio project");
        String projectPath = MP_WORKSPACE + File.separator + "upload";
        Platform.runLater(() -> log.set("Generating project at " + projectPath + "\n"));
        try {
            FileUtils.deleteDirectory(new File(projectPath));
            FileUtils.forceMkdir(new File(projectPath));

            ProcessBuilder builder = new ProcessBuilder(pythonPath.get(), "-m", "platformio", "init", "--board", platform);
            builder.directory(new File(projectPath).getAbsoluteFile()); // this is where you set the root folder for the executable to run with
            builder.redirectErrorStream(true);
            Process p = builder.start();
            try (Scanner s = new Scanner(p.getInputStream())) {
                while (s.hasNextLine()) {
                    if (isCancelled()) {
                        updateMessage("Canceling upload...");
                        p.destroy();
                        break;
                    }
                    String line = s.nextLine();
                    Platform.runLater(() -> log.set(line + "\n"));
                }
            }
            if (p.waitFor() != 0) {
                updateMessage("Error: pio init failed");
                return UploadResult.UNKNOWN_ERROR;
            }
        } catch (InterruptedException e) {
            if (isCancelled()) {
                updateMessage("Upload has been canceled");
                return UploadResult.USER_CANCEL;
            }
            updateMessage("Unknown error has occurred. Please try again.");
            return UploadResult.UNKNOWN_ERROR;
        } catch (IOException e) {
            updateMessage("Error: can't create project directory (permission denied)");
            return UploadResult.NO_PERMISSION;
        }

        updateProgress(0.5, 1);
        updateMessage("Generate source files and libraries");
        try {
            FileUtils.forceMkdir(new File(projectPath + File.separator + "src"));
            FileUtils.forceMkdir(new File(projectPath + File.separator + "lib"));

            // generate source file
            FileWriter fw = new FileWriter(projectPath + File.separator + "src" + File.separator + "main.cpp");
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(code);
            bw.close();
            fw.close();
        } catch (IOException | NullPointerException e) {
            updateMessage("Error: Cannot write code to project directory");
            return UploadResult.CANT_WRITE_CODE;
        }

        // copy mp library
        for (String x: mpLibraries){
            String destinationPath = projectPath + File.separator + "lib";
            Path libraryPath = Paths.get("library/lib",x);
            if(Files.exists(libraryPath)&&Files.isDirectory(libraryPath)){
                try {
                    FileUtils.copyDirectory(libraryPath.toFile(),new File(destinationPath+File.separator+x));
                } catch (IOException e) {
                    e.printStackTrace();
                    updateMessage("Error: Missing some libraries");
                    return UploadResult.CANT_FIND_LIBRARY;
                }
            }
            else {
                updateMessage("Error: Missing some libraries");
                return UploadResult.CANT_FIND_LIBRARY;
            }
        }

        //copy and extract external Libraries
        for (String x : externalLibraries) {
            String destinationPath = projectPath + File.separator + "lib";
            Path libraryPath = Paths.get("library/lib_ext",x+".zip");
            if(Files.exists(libraryPath)){
                ZipResourceExtractor.ExtractResult extractResult = ZipResourceExtractor.extract(libraryPath,destinationPath);
                if(extractResult != ZipResourceExtractor.ExtractResult.SUCCESS){
                    updateMessage("Error: Failed to extract libraries.");
                    return UploadResult.CANT_FIND_LIBRARY;
                }
            }
            else {
                updateMessage("Error: Missing some libraries");
                return UploadResult.CANT_FIND_LIBRARY;
            }
        }

        updateProgress(0.75, 1);
        updateMessage("Uploading to board");
        try {
            ProcessBuilder builder = new ProcessBuilder(pythonPath.get(), "-m", "platformio", "run", "--target", "upload");
            builder.directory(new File(projectPath).getAbsoluteFile()); // this is where you set the root folder for the executable to run with
            builder.redirectErrorStream(true);
            Process p = builder.start();
            try (Scanner s = new Scanner(p.getInputStream())) {
                while (s.hasNextLine()) {
                    if (isCancelled()) {
                        updateMessage("Canceling upload...");
                        p.destroy();
                        break;
                    }
                    String line = s.nextLine();
                    Platform.runLater(() -> log.set(line + "\n"));
                }
            }
            int result = p.waitFor();
            if (result != 0) {
                updateMessage("Error: Can't find board. Please check connection.");
                return UploadResult.CANT_FIND_BOARD;
            }
        } catch (InterruptedException e) {
            if (isCancelled()) {
                updateMessage("Upload has been canceled");
                return UploadResult.USER_CANCEL;
            }
            updateMessage("Unknown error has occurred. Please try again.");
            return UploadResult.UNKNOWN_ERROR;
        } catch (IOException e) {
            updateMessage("Error: can't find project directory (permission denied)");
            return UploadResult.NO_PERMISSION;
        }

        updateProgress(1, 1);
        updateMessage("Done");

        return UploadResult.OK;
    }

    private void addSourcesFromDirectory(Path sourcePath,String destinationPath) throws IOException {
        //List<Path> possiblePath;
        int sourcePathStringLength = sourcePath.toString().length();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(sourcePath,"*.{cpp,h}");){
            for(Path p: directoryStream){
                String filename = p.toString().substring(sourcePathStringLength);
                Path targetPath = Paths.get(destinationPath,filename);
                Files.copy(p,Paths.get(destinationPath,filename),StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    private void extractPythonFromJar() {
        String zipFilePath = "/python/python-2.7.13.zip";
        String destinationPath = MP_WORKSPACE;
        ZipResourceExtractor.extract(getClass(), zipFilePath, destinationPath);
    }

    public ReadOnlyStringProperty logProperty() {
        return log.getReadOnlyProperty();
    }

    /**
     * check whether platformio has been install in this system
     * @return path to valid python or Optional.empty()
     */
    private Optional<String> checkPlatformio() {
        List<String> path = List.of(MP_WORKSPACE + File.separator + "python-2.7.13" + File.separator + "python"      // integrated python for windows version
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
}
