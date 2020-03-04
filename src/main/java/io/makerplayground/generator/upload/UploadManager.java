package io.makerplayground.generator.upload;

import io.makerplayground.generator.source.InteractiveSourceCodeGenerator;
import io.makerplayground.generator.source.SourceCodeGenerator;
import io.makerplayground.generator.source.SourceCodeResult;
import io.makerplayground.project.Project;
import io.makerplayground.util.*;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;

import java.util.List;

public class UploadManager {
    private final ObjectProperty<Project> project;

    private final ReadOnlyObjectWrapper<UploadStatus> uploadStatus = new ReadOnlyObjectWrapper<>(UploadStatus.IDLE);
    private final ReadOnlyStringWrapper uploadLog = new ReadOnlyStringWrapper();
    private final ReadOnlyDoubleWrapper uploadProgress = new ReadOnlyDoubleWrapper();

    private UploadTask uploadTask;

    private final ObservableList<UploadTarget> serialPortList = FXCollections.observableArrayList();
    private final ObservableList<UploadTarget> rpiHostList = FXCollections.observableArrayList();

    private ObjectProperty<ObservableList<UploadTarget>> uploadInfoList = new SimpleObjectProperty<>();

    public UploadManager(ObjectProperty<Project> project) {
        this.project = project;
        // cancel pending upload task when user load or create a new project
        project.addListener((observable, oldValue, newValue) -> cancelUpload());
    }

    public ObjectProperty<ObservableList<UploadTarget>> uploadInfoListProperty() {
        return uploadInfoList;
    }

//    SerialPortDiscoveryThread serialPortDiscoveryThread = null;
    RpiDiscoverer rpiDiscoverer = new RpiDiscoverer(rpiHostList);
    RpiKeepAliveChecker rpiKeepAliveChecker = new RpiKeepAliveChecker(rpiHostList);

    SerialPortDiscoverer serialPortDiscoverer = new SerialPortDiscoverer(serialPortList);

//    RpiDiscoveryThread rpiDiscoveryThread;
//    RpiKeepAliveThread rpiKeepAliveThread;

    public void startScanUploadConnection() {
        List<UploadMode> supportMethods = project.get().getSelectedPlatform().getSupportUploadModes();
        if (supportMethods.contains(UploadMode.SERIAL_PORT)) {
            uploadInfoList.set(serialPortList);
            if (!serialPortDiscoverer.isRunning()) {
                serialPortDiscoverer.startScan();
            }
            if (rpiDiscoverer.isRunning()) {
                rpiDiscoverer.stopScan();
            }
//            if (rpiKeepAliveChecker.isRunning()) {
//                rpiKeepAliveChecker.stopScan();
//            }
        }
        if (supportMethods.contains(UploadMode.RPI_ON_NETWORK)) {
            uploadInfoList.set(rpiHostList);
            if (serialPortDiscoverer.isRunning()) {
                serialPortDiscoverer.stopScan();
            }
            if (!rpiDiscoverer.isRunning()) {
                rpiDiscoverer.startScan();
            }
//            if (!rpiKeepAliveChecker.isRunning()) {
//                rpiKeepAliveChecker.startScan();
//            }
        }
    }

    public boolean startInteractiveMode(UploadTarget uploadTarget) {
        Project clonedProject = Project.newInstance(project.get());
        SourceCodeResult sourceCode = InteractiveSourceCodeGenerator.generate(clonedProject);
        if (createUploadTask(clonedProject, sourceCode, uploadTarget)) {
            project.get().getInteractiveModel().initialize();
            new Thread(uploadTask).start();
            uploadTask.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, event1 -> {
                if (uploadTask.getValue() == UploadResult.OK) {
                    project.get().getInteractiveModel().start(uploadTarget);
                }
            });
            uploadStatus.set(UploadStatus.STARTING_INTERACTIVE);
            uploadLog.set("");
            uploadProgress.set(0);
            return true;
        }
        return false;
    }

    public boolean startUploadProject(UploadTarget uploadTarget) {
        Project clonedProject = Project.newInstance(project.get());
        SourceCodeResult sourceCode = SourceCodeGenerator.generate(clonedProject);
        if (createUploadTask(clonedProject, sourceCode, uploadTarget)) {
            new Thread(uploadTask).start();
            uploadStatus.set(UploadStatus.UPLOADING);
            uploadLog.set("");
            uploadProgress.set(0);
            return true;
        }
        return false;
    }

    private boolean createUploadTask(Project clonedProject, SourceCodeResult sourceCode, UploadTarget uploadTarget) {
        StringBuilder log = new StringBuilder();
        uploadTask = UploadTask.create(clonedProject, sourceCode, uploadTarget);

        uploadTask.progressProperty().addListener((observable, oldValue, newValue) -> uploadProgress.set(newValue.doubleValue()));

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
