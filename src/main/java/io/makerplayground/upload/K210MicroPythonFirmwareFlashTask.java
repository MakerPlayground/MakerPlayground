package io.makerplayground.upload;

import com.fazecast.jSerialComm.SerialPort;
import io.makerplayground.project.Project;
import io.makerplayground.util.OSInfo;
import io.makerplayground.util.PathUtility;
import javafx.application.Platform;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class K210MicroPythonFirmwareFlashTask extends UploadTask {
    public K210MicroPythonFirmwareFlashTask(Project project, UploadTarget uploadTarget) {
        super(project, uploadTarget, false);
    }

    @Override
    protected UploadResult doUpload() {
        if (!project.getSelectedPlatform().isMicroPython()) {
            throw new IllegalStateException("Error: the current platform is not based on MicroPython");
        }

        SerialPort serialPort = uploadTarget.getSerialPort();
        String serialPortName = OSInfo.getOs() == OSInfo.OS.WINDOWS ? serialPort.getSystemPortName() : "/dev/" + serialPort.getSystemPortName();

        updateProgress(0, 1);
        updateMessage("Preparing to flash new firmware");

        // wait for 500ms so that when the upload failed very early, user can see that the upload has started (progress is at 0%)
        // for a short period of time before seeing the error message
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            updateMessage("Upload has been canceled");
            return UploadResult.USER_CANCEL;
        }

        // check kflash
        Optional<List<String>> kflashCommand = PathUtility.getKflashCommand();
        if (kflashCommand.isEmpty()) {
            updateMessage("Error: Can't find valid kflash installation see: https://github.com/sipeed/kflash.py");
            return UploadResult.CANT_FIND_PIO;
        }
        Platform.runLater(() -> log.set("Execute kflash by " + kflashCommand.get() + "\n"));

        UploadResult result;

        updateProgress(0.5, 1);
        updateMessage("Flashing firmware");

        List<String> firmwarePath = project.getSelectedController().getFirmwarePath().get(project.getSelectedPlatform());
        if (firmwarePath.size() != 1) {
            throw new IllegalStateException("Error: MicroPythonFirmwareFlashTask only support 1 firmware file");
        }
        result = runKflashCommand(kflashCommand.get(), List.of("-p", serialPortName, PathUtility.getDeviceFirmwarePath() + File.separator + firmwarePath.get(0))
                , "Error: Can't flash the firmware", UploadResult.CANT_WRITE_CODE);
        if (result != UploadResult.OK) {
            return result;
        }

        updateProgress(1, 1);
        updateMessage("Done");

        return UploadResult.OK;
    }

    private UploadResult runKflashCommand(List<String> command, List<String> args, String errorMessage, UploadResult error) {
        Process p = null;
        try {
            // create argument list
            List<String> arguments = new ArrayList<>(command);
            arguments.addAll(args);
            Platform.runLater(() -> log.set("Executing " + arguments + "\n"));
            // create process to invoke esptool
            ProcessBuilder builder = new ProcessBuilder(arguments);
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
