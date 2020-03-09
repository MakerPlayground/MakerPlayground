package io.makerplayground.generator.upload;

import io.makerplayground.project.Project;
import javafx.beans.property.*;
import javafx.concurrent.WorkerStateEvent;

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
                if (uploadTask.getValue() == UploadResult.OK) {
                    project.get().getInteractiveModel().start(uploadTask.getUploadTarget());
                }
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
