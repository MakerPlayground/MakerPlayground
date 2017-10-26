package io.makerplayground.generator;

import io.makerplayground.helper.UploadResult;
import io.makerplayground.project.Project;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import org.apache.commons.io.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class UploadTask extends Task<UploadResult> {

    private Project project;
    private StringProperty log;

    public UploadTask(Project project) {
        this.project = project;
        this.log = new SimpleStringProperty();
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
        List<String> library = null;
        String platform = project.getController().getPlatform().getPlatformioId();
        String code = sourcecode.getCode();
        library = project.getAllDeviceUsed().stream()
                .map(projectDevice -> projectDevice.getActualDevice().getLibraryName())
                .flatMap(Collection::stream).collect(Collectors.toList());
        log.set("List of library used \n");
        for (String libName : library) {
            log.set(" - " + libName + "\n");
        }
        //System.out.println(code);
        //System.out.println(library);
        Path currentRelativePath = Paths.get("");
        String path = currentRelativePath.toAbsolutePath().toString();
        //System.out.println("Current relative path is: " + path);
        try {
            FileUtils.deleteDirectory(new File(path + File.separator + "upload" + File.separator + "project"));
            FileUtils.forceMkdir(new File(path + File.separator + "upload" + File.separator + "project"));

            currentRelativePath = Paths.get("");
            path = currentRelativePath.toAbsolutePath().toString();
            //System.out.println("Current relative path is: " + path);

            ProcessBuilder builder = new ProcessBuilder("pio", "init", "--board", platform);
            builder.directory(new File("upload" + File.separator + "project").getAbsoluteFile()); // this is where you set the root folder for the executable to run with
            builder.redirectErrorStream(true);
            Process p = builder.start();
            Scanner s = new Scanner(p.getInputStream());
            while (s.hasNextLine()) {
                log.set(s.nextLine() + "\n");
            }
            s.close();
            try {
                int result = p.waitFor();
            } catch (InterruptedException e) {
                return UploadResult.UNKNOWN_ERROR;
            }

            //Runtime.getRuntime().exec("pio init --board "+platform);
            //System.out.println(platform);
        } catch (IOException e) {
            updateMessage("Error: Can't find platformio");
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
                //File sourcecpp = new File(path + File.separator + "lib" + File.separator + x + ".cpp");
                File sourcecpp = new File(getClass().getResource("/library/arduino/src/" + x + ".cpp").toExternalForm());
                File destcpp = new File(path + File.separator + "upload" + File.separator + "project" + File.separator + "lib" + File.separator + x + File.separator + x + ".cpp");
                //File sourceh = new File(path + File.separator + "lib" + File.separator + x + ".h");
                File sourceh = new File(getClass().getResource("/library/arduino/src/" + x + ".h").toExternalForm());
                File desth = new File(path + File.separator + "upload" + File.separator + "project" + File.separator + "lib" + File.separator + x + File.separator + x + ".h");
                Files.copy(sourcecpp.toPath(), destcpp.toPath());
                Files.copy(sourceh.toPath(), desth.toPath());
            }
        } catch (IOException e) {
            updateMessage("Error: Missing some libraries");
            return UploadResult.UNKNOWN_ERROR;
        }

        updateProgress(0.75, 1);
        updateMessage("Uploading to board");
        try {
            ProcessBuilder builder = new ProcessBuilder("platformio", "run", "--target", "upload");
            builder.directory(new File("upload" + File.separator + "project").getAbsoluteFile()); // this is where you set the root folder for the executable to run with
            builder.redirectErrorStream(true);
            Process p = builder.start();
            Scanner s = new Scanner(p.getInputStream());
            while (s.hasNextLine()) {
                log.set(s.nextLine() + "\n");
            }
            s.close();
            try {
                int result = p.waitFor();
                if (result == 1) {
                    updateMessage("Error: Can't find board. Please check connection.");
                    return UploadResult.CANT_FIND_BOARD;
                }
            } catch (InterruptedException e) {
                return UploadResult.UNKNOWN_ERROR;
            }
        } catch (IOException e) {
            return UploadResult.CANT_FIND_PIO;
        }

        updateProgress(1, 1);
        updateMessage("Done");

        return UploadResult.OK;
    }

    public String getLog() {
        return log.get();
    }

    public StringProperty logProperty() {
        return log;
    }
}
