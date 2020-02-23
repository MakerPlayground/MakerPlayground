package io.makerplayground.viewmodel;

import com.fazecast.jSerialComm.SerialPort;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.ViewModel;
import io.makerplayground.generator.upload.UploadStatus;
import io.makerplayground.project.ProjectConfigurationStatus;
import io.makerplayground.scope.AppScope;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.*;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

public class ToolbarViewModel implements ViewModel {
    @InjectScope
    AppScope appScope;

    private ObjectProperty<SerialPort> serialPort = new SimpleObjectProperty<>();
    private StringProperty uploadStatusText = new SimpleStringProperty();
    private BooleanProperty uploadStatusVisible = new SimpleBooleanProperty();

    private Timeline timeLineUploadStatusInvisible = new Timeline(new KeyFrame(Duration.seconds(3), event -> uploadStatusVisible.set(false)));

    public void initialize() {
        uploadStatusProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == UploadStatus.UPLOADING || newValue == UploadStatus.STARTING_INTERACTIVE) {
                uploadStatusText.set("Uploading...");
                uploadStatusVisible.set(true);
            } else if (newValue == UploadStatus.UPLOAD_DONE) {
                uploadStatusText.set("Done");
                timeLineUploadStatusInvisible.playFromStart();
            } else if (newValue == UploadStatus.UPLOAD_FAILED) {
                uploadStatusText.set("Failed");
            }
        });

        uploadProgressProperty().addListener((observable, oldValue, newValue) -> {
            if (Double.compare(newValue.doubleValue(), 1.0) == 0) {
                uploadStatusText.set("Done");
            } else {
                uploadStatusText.set("Uploading (" + (int) (newValue.doubleValue() * 100) + "%)");
            }
        });
    }

    public StringProperty statusMessageProperty() {
        return appScope.toolbarStatusMessageProperty();
    }

    public ReadOnlyObjectProperty<ProjectConfigurationStatus> projectConfigurationStatusProperty() {
        return appScope.getCurrentProject().getProjectConfiguration().statusProperty();
    }

    public ReadOnlyBooleanProperty diagramErrorProperty() {
        return appScope.getCurrentProject().diagramErrorProperty();
    }

    public ReadOnlyBooleanProperty useHwSerialProperty() {
        return appScope.getCurrentProject().getProjectConfiguration().useHwSerialProperty();
    }

    public ReadOnlyBooleanProperty interactiveStartedProperty() {
        return appScope.getCurrentProject().getInteractiveModel().startedProperty();
    }

    public ReadOnlyObjectProperty<UploadStatus> uploadStatusProperty() {
        return appScope.getUploadManager().uploadStatusProperty();
    }

    public ReadOnlyDoubleProperty uploadProgressProperty() {
        return appScope.getUploadManager().uploadProgressProperty();
    }

    public StringProperty uploadStatusTextProperty() {
        return uploadStatusText;
    }

    public BooleanProperty uploadStatusVisibleProperty() {
        return uploadStatusVisible;
    }

    public ObjectProperty<SerialPort> serialPortProperty() {
        return serialPort;
    }

    public ReadOnlyStringProperty uploadMessageProperty() {
        return appScope.getUploadManager().getUploadTask().messageProperty();
    }

    public ReadOnlyStringProperty uploadLogProperty() {
        return appScope.getUploadManager().getUploadTask().logProperty();
    }

    public void startHideUploadTimeLine() {
        timeLineUploadStatusInvisible.playFromStart();
    }

    public void cancelUpload() {
        appScope.getUploadManager().cancelUpload();
    }

    public void toggleInteractive() {
        if (interactiveStartedProperty().get()) {
            appScope.getCurrentProject().getInteractiveModel().stop();
        } else if (uploadStatusProperty().get() != UploadStatus.STARTING_INTERACTIVE) {
            // stop the auto hide transition that may have been scheduled to run in a few second
            timeLineUploadStatusInvisible.stop();
            appScope.getUploadManager().startInteractiveMode(serialPort.get());
        }
    }

    public void startUploadProject() {
        // stop the auto hide transition that may have been scheduled to run in a few second
        timeLineUploadStatusInvisible.stop();
        appScope.getUploadManager().startUploadProject(serialPort.get());
    }

    public void requestNewProject() {
        appScope.requestNewProject();
    }

    public void requestLoadProject() {
        appScope.requestLoadProject();
    }

    public void saveProject() {
        appScope.saveProject();
    }

    public void saveProjectAs() {
        appScope.saveProjectAs();
    }

    public void requestClose() {
        appScope.getRootStage().fireEvent(new WindowEvent(appScope.getRootStage(), WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    public void exportProject() {
        appScope.exportProject();
    }
}
