package io.makerplayground.generator.upload;

import com.fazecast.jSerialComm.SerialPort;
import io.makerplayground.device.actual.Platform;
import io.makerplayground.generator.source.InteractiveSourceCodeGenerator;
import io.makerplayground.generator.source.SourceCodeGenerator;
import io.makerplayground.generator.source.SourceCodeResult;
import io.makerplayground.project.Project;
import io.makerplayground.util.MakerPlaygroundRpiScanner;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.scene.control.TextInputDialog;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UploadManager implements Runnable {
    private final ObjectProperty<Project> project;

    private final ReadOnlyObjectWrapper<UploadStatus> uploadStatus = new ReadOnlyObjectWrapper<>(UploadStatus.IDLE);
    private final ReadOnlyStringWrapper uploadLog = new ReadOnlyStringWrapper();
    private final ReadOnlyDoubleWrapper uploadProgress = new ReadOnlyDoubleWrapper();
    private final MakerPlaygroundRpiScanner rpiScanner = new MakerPlaygroundRpiScanner();

    private UploadTask uploadTask;

    ObservableList<UploadConnection> resultSerialList = FXCollections.observableArrayList();

    public UploadManager(ObjectProperty<Project> project) {
        this.project = project;
        // cancel pending upload task when user load or create a new project
        project.addListener((observable, oldValue, newValue) -> {
            cancelUpload();
        });
    }

    @Override
    public void run() {
        List<UploadConnection.Type> supportTypes = project.get().getSelectedPlatform().getSupportUploadConnectionTypes();
        if (supportTypes.contains(UploadConnection.Type.SERIALPORT)) {
            resultSerialList.clear();
            resultSerialList.addAll(Arrays.stream(SerialPort.getCommPorts()).map(UploadConnection::new).collect(Collectors.toList()));
        }
        if (supportTypes.contains(UploadConnection.Type.RPI)) {
            rpiScanner.run();
        }
    }

    public ObservableList<UploadConnection> getUploadMethod() {
        ObservableList<UploadConnection> resultList = FXCollections.observableArrayList();
        List<UploadConnection.Type> supportTypes = project.get().getSelectedPlatform().getSupportUploadConnectionTypes();
        if (supportTypes.contains(UploadConnection.Type.SERIALPORT)) {
            merge(resultList, resultSerialList);
        }
        if (supportTypes.contains(UploadConnection.Type.RPI)) {
            merge(resultList, rpiScanner.getHostList());
        }
        return resultList;
    }

    private void merge(ObservableList<UploadConnection> result, ObservableList<UploadConnection>... items) {
        for (ObservableList<UploadConnection> obj: items) {
            result.addAll(obj);
            obj.addListener((ListChangeListener<? super UploadConnection>) c -> {
                while(c.next()) {
                    if (c.wasAdded()) {
                        result.addAll(c.getAddedSubList());
                    }
                    if (c.wasRemoved()) {
                        result.removeAll(c.getRemoved());
                    }
                }
            });
        }
    }

    public boolean startInteractiveMode(UploadConnection uploadConnection) {
        if (uploadConnection.getType().equals(UploadConnection.Type.SERIALPORT)) {
            return startInteractiveMode(uploadConnection.getSerialPort());
        } else {
            throw new IllegalStateException("Unsupported Interactive Mode");
        }
    }

    private boolean startInteractiveMode(SerialPort serialPort) {
        Project clonedProject = Project.newInstance(project.get());
        SourceCodeResult sourceCode = InteractiveSourceCodeGenerator.generate(clonedProject);
        if (createUploadTask(clonedProject, sourceCode, serialPort)) {
            project.get().getInteractiveModel().initialize();
            new Thread(uploadTask).start();
            uploadTask.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, event1 -> {
                if (uploadTask.getValue() == UploadResult.OK) {
                    project.get().getInteractiveModel().start(serialPort);
                }
            });
            uploadStatus.set(UploadStatus.STARTING_INTERACTIVE);
            uploadLog.set("");
            uploadProgress.set(0);
            return true;
        }
        return false;
    }

    public boolean startUploadProject(UploadConnection uploadConnection) {
        if (uploadConnection.getType().equals(UploadConnection.Type.SERIALPORT)) {
            return startUploadProject(uploadConnection.getSerialPort());
        } else {
            throw new IllegalStateException("Unsupport Upload");
        }
    }

    private boolean startUploadProject(SerialPort serialPort) {
        Project clonedProject = Project.newInstance(project.get());
        SourceCodeResult sourceCode = SourceCodeGenerator.generate(clonedProject);
        if (createUploadTask(clonedProject, sourceCode, serialPort)) {
            new Thread(uploadTask).start();
            uploadStatus.set(UploadStatus.UPLOADING);
            uploadLog.set("");
            uploadProgress.set(0);
            return true;
        }
        return false;
    }

    private boolean createUploadTask(Project clonedProject, SourceCodeResult sourceCode, SerialPort serialPort) {
        StringBuilder log = new StringBuilder();

        // create the upload task
        switch (clonedProject.getSelectedPlatform()) {
            case ARDUINO_AVR8:
            case ARDUINO_ESP32:
            case ARDUINO_ESP8266:
                uploadTask = new ArduinoUploadTask(clonedProject, sourceCode, serialPort);
                break;
            case RASPBERRYPI:
                String initialIpValue = "192.168.1.100";
                TextInputDialog textInputDialog = new TextInputDialog(initialIpValue);
                textInputDialog.setTitle("Connect Raspberry Pi");
                textInputDialog.setHeaderText("Connect Raspberry Pi in Network: ");
                textInputDialog.setContentText("IP Address:");
                Optional<String> ip = textInputDialog.showAndWait();
                if (ip.isPresent()) {
                    uploadTask = new RaspberryPiUploadTask(clonedProject, sourceCode, serialPort, ip.get());
                } else {
                    return false;
                }
                break;
            default:
                throw new IllegalStateException("No upload method for current platform");
        }

        uploadTask.progressProperty().addListener((observable, oldValue, newValue) -> {
            uploadProgress.set(newValue.doubleValue());
        });
        uploadTask.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, event1 -> {
            if (uploadTask.getValue() == UploadResult.OK) {
                uploadStatus.set(UploadStatus.UPLOAD_DONE);
            } else {
                uploadStatus.set(UploadStatus.UPLOAD_FAILED);
            }
        });
        uploadTask.addEventHandler(WorkerStateEvent.WORKER_STATE_CANCELLED, event1 -> {
            uploadStatus.set(UploadStatus.UPLOAD_FAILED);
        });
        uploadTask.logProperty().addListener((observable, oldValue, newValue) -> {
            log.append(newValue);
            uploadLog.set(log.toString());
        });

        return true;
    }

    public void cancelUpload() {
        if (uploadTask != null) {
            uploadTask.cancel();
        }
    }

    public UploadTask getUploadTask() {
        return uploadTask;
    }

    public UploadStatus getUploadStatus() {
        return uploadStatus.get();
    }

    public ReadOnlyObjectProperty<UploadStatus> uploadStatusProperty() {
        return uploadStatus.getReadOnlyProperty();
    }

    public String getUploadLog() {
        return uploadLog.get();
    }

    public ReadOnlyStringProperty uploadLogProperty() {
        return uploadLog.getReadOnlyProperty();
    }

    public double getUploadProgress() {
        return uploadProgress.get();
    }

    public ReadOnlyDoubleProperty uploadProgressProperty() {
        return uploadProgress.getReadOnlyProperty();
    }
}
