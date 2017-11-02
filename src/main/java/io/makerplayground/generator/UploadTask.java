package io.makerplayground.generator;

import io.makerplayground.helper.UploadResult;
import io.makerplayground.project.Project;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.concurrent.Task;
import org.apache.commons.io.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class UploadTask extends Task<UploadResult> {

    private final Project project;
    private final ReadOnlyStringWrapper log;

    public UploadTask(Project project) {
        this.project = project;
        this.log = new ReadOnlyStringWrapper();
    }

    @Override
    protected UploadResult call() throws Exception {
        updateProgress(0, 1);
        updateMessage("Checking project");
        if (!DeviceMapper.autoAssignDevices(project)) {
            updateMessage("Error: not enough port available");
            return UploadResult.NOT_ENOUGH_PORT;
        }
        Sourcecode sourcecode = Sourcecode.generateCode(project, true);
        if (sourcecode.getError() != null) {
            updateMessage("Error: " + sourcecode.getError().getDescription());
            return UploadResult.CANT_GENERATE_CODE;
        }

        updateProgress(0.25, 1);
        updateMessage("Preparing to generate project");
        String platform = project.getController().getPlatform().getPlatformioId();
        String code = sourcecode.getCode();
        List<String> library = project.getAllDeviceUsed().stream()
                .map(projectDevice -> projectDevice.getActualDevice().getLibraryName())
                .flatMap(Collection::stream).collect(Collectors.toList());
        Platform.runLater(() -> log.set("List of library used \n"));
        for (String libName : library) {
            Platform.runLater(() -> log.set(" - " + libName + "\n"));
        }

        Path currentRelativePath = Paths.get("");
        String path = currentRelativePath.toAbsolutePath().toString();
        try {
            FileUtils.deleteDirectory(new File(path + File.separator + "upload" + File.separator + "project"));
            FileUtils.forceMkdir(new File(path + File.separator + "upload" + File.separator + "project"));

            ProcessBuilder builder = new ProcessBuilder("pio", "init", "--board", platform);
            builder.directory(new File("upload" + File.separator + "project").getAbsoluteFile()); // this is where you set the root folder for the executable to run with
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
            updateMessage("Error: Can't find platformio see: http://docs.platformio.org/en/latest/installation.html");
            return UploadResult.CANT_FIND_PIO;
        }

        updateProgress(0.5, 1);
        updateMessage("Generate source files and libraries");
        try {
            FileUtils.forceMkdir(new File(path + File.separator + "upload" + File.separator + "project" + File.separator + "src"));
            FileUtils.forceMkdir(new File(path + File.separator + "upload" + File.separator + "project" + File.separator + "lib"));

            // generate source file
            FileWriter fw = new FileWriter(path + File.separator + "upload" + File.separator + "project" + File.separator + "src" + File.separator + "main.cpp");
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(code);
            bw.close();
            fw.close();

            // copy libraries
            for (String x : library) {
                FileUtils.forceMkdir(new File(path + File.separator + "upload" + File.separator + "project" + File.separator + "lib" + File.separator + x));

                URL sourcecpp = getClass().getResource("/library/arduino/src/" + x + ".cpp");
                File destcpp = new File(path + File.separator + "upload" + File.separator + "project" + File.separator + "lib" + File.separator + x + File.separator + x + ".cpp");

                URL sourceh = getClass().getResource("/library/arduino/src/" + x + ".h");
                File desth = new File(path + File.separator + "upload" + File.separator + "project" + File.separator + "lib" + File.separator + x + File.separator + x + ".h");

                FileUtils.copyURLToFile(sourcecpp, destcpp, 1, 1);
                FileUtils.copyURLToFile(sourceh, desth, 1, 1);
            }
        } catch (IOException | NullPointerException e) {
            updateMessage("Error: Missing some libraries");
            return UploadResult.CANT_FIND_LIBRARY;
        }

        updateProgress(0.75, 1);
        updateMessage("Uploading to board");
        try {
            ProcessBuilder builder = new ProcessBuilder("platformio", "run", "--target", "upload");
            builder.directory(new File("upload" + File.separator + "project").getAbsoluteFile()); // this is where you set the root folder for the executable to run with
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
            updateMessage("Error: Can't find platformio see: http://docs.platformio.org/en/latest/installation.html");
            return UploadResult.CANT_FIND_PIO;
        }

        updateProgress(1, 1);
        updateMessage("Done");

        return UploadResult.OK;
    }

    public ReadOnlyStringProperty logProperty() {
        return log.getReadOnlyProperty();
    }
}
