package io.makerplayground.upload;

import io.makerplayground.project.Project;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.concurrent.WorkerStateEvent;

import java.util.Timer;
import java.util.TimerTask;

public class UploadManager {

    private final ObjectProperty<Project> project;
    private final ReadOnlyObjectWrapper<UploadStatus> uploadStatus = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyStringWrapper uploadLog = new ReadOnlyStringWrapper();
    private final ReadOnlyDoubleWrapper uploadProgress = new ReadOnlyDoubleWrapper();

    private UploadTaskBase uploadTask;

    public UploadManager(ObjectProperty<Project> project) {
        this.project = project;
        // cancel pending upload task when user load or create a new project
        project.addListener((observable, oldValue, newValue) -> cancelUpload());
    }

    public void startInteractiveMode(UploadTarget uploadTarget) {
        uploadTask = new UploadTaskBase.Builder()
                .setProject(project.get())
                .setUploadTarget(uploadTarget)
                .isInteractive(true)
                .build();
        bindUploadTaskPropertyAndEvent();
        new Thread(uploadTask).start();
    }

    public void startUploadProject(UploadTarget uploadTarget) {
        uploadTask = new UploadTaskBase.Builder()
                .setProject(project.get())
                .setUploadTarget(uploadTarget)
                .isInteractive(false)
                .build();
        bindUploadTaskPropertyAndEvent();
        new Thread(uploadTask).start();
    }

    private void bindUploadTaskPropertyAndEvent() {
        uploadStatus.bind(uploadTask.uploadStatusProperty());
        uploadProgress.bind(uploadTask.progressProperty());
        uploadLog.bind(uploadTask.fullLogProperty());
        if (uploadTask.isInteractiveUpload()) {
            uploadTask.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, event1 -> {
                // On platform with internal USB host such as ATSAMD21 and ATSAMD51, the serial port may disappear for
                // a little moment after upload new code so we wait for 2 second before trying to connect to the device
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> {
                            if (uploadTask.getValue() == UploadResult.OK) {
                                project.get().getInteractiveModel().start(uploadTask.getUploadTarget());
                            }
                        });
                    }
                }, 2000);
            });
        }
    }

    public void cancelUpload() {
        if (uploadTask != null) {
            uploadTask.cancel();
        }
    }

    public UploadTaskBase getUploadTask() {
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
