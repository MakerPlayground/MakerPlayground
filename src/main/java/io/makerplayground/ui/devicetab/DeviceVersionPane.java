package io.makerplayground.ui.devicetab;

import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.util.PathUtility;
import io.makerplayground.version.DeviceLibraryVersion;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.util.Duration;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

public class DeviceVersionPane extends HBox {
    private static final DateFormat df = new SimpleDateFormat("d MMM yyyy");
    private final Label versionLabel;
    private final Label statusLabel;
    private final ProgressBar downloadProgress;
    private final Button downloadButton;
    private final Button installButton;
    private final Button checkUpdateButton;
    private final Timeline statusHider;

    public DeviceVersionPane() {
        versionLabel = new Label();

        Pane pane = new Pane();
        HBox.setHgrow(pane, Priority.ALWAYS);

        statusLabel = new Label();

        downloadProgress = new ProgressBar(0.0);
        downloadProgress.managedProperty().bind(downloadProgress.visibleProperty());

        downloadButton = new Button("Download & Install");
        downloadButton.managedProperty().bind(downloadButton.visibleProperty());
        downloadButton.setOnAction((event) -> downloadUpdate());

        installButton = new Button("Install");
        installButton.managedProperty().bind(installButton.visibleProperty());

        checkUpdateButton = new Button("Check Update");
        checkUpdateButton.managedProperty().bind(checkUpdateButton.visibleProperty());
        checkUpdateButton.setOnAction((event) -> checkUpdate());

        getChildren().addAll(versionLabel, pane, statusLabel, downloadProgress, downloadButton, installButton, checkUpdateButton);
        getStyleClass().add("hbox");
        getStylesheets().add(getClass().getResource("/css/DeviceVersionPane.css").toExternalForm());
        setSpacing(5);
        setPadding(new Insets(4, 8, 6, 8));
        setAlignment(Pos.CENTER_LEFT);

        statusHider = new Timeline();
        statusHider.getKeyFrames().add(new KeyFrame(Duration.seconds(3), (e) -> statusLabel.setText("")));

        checkUpdate();
    }

    private void checkUpdate() {
        if (DeviceLibraryVersion.getCurrentVersion().isEmpty()) {
            versionLabel.setText("Version: -");
        } else {
            DeviceLibraryVersion current = DeviceLibraryVersion.getCurrentVersion().get();
            versionLabel.setText("Version: " + current.getVersion() + " (" + df.format(current.getReleaseDate()) + ")");
        }
        statusLabel.setText("Checking for update...");
        downloadProgress.setVisible(false);
        downloadButton.setVisible(false);
        installButton.setVisible(false);
        checkUpdateButton.setVisible(true);
        checkUpdateButton.setDisable(true);
        DeviceLibraryVersion.fetchLatestCompatibleVersion(() -> {
            checkUpdateButton.setDisable(false);
            updateState();
        });
    }

    private void downloadUpdate() {
        Optional<DeviceLibraryVersion> latestVersion = DeviceLibraryVersion.getLatestCompatibleVersion();
        if (latestVersion.isPresent()) {
            statusLabel.setText("Downloading...");
            downloadButton.setDisable(true);
            Task<Void> downloadTask = DeviceLibraryVersion.launchDownloadUpdateFileTask();
            downloadTask.setOnSucceeded((value) -> {
                statusLabel.setText("");
                downloadProgress.setVisible(false);
                downloadButton.setDisable(false);
                updateState();
            });
            downloadProgress.progressProperty().unbind();
            downloadProgress.progressProperty().bind(downloadTask.progressProperty());
            downloadProgress.setVisible(true);
        } else {
            updateState();
        }
    }

    private void updateState() {
        statusHider.stop();

        if (DeviceLibraryVersion.getCurrentVersion().isEmpty()) {   // no library at all so we should download newest version
            versionLabel.setText("Version: -");
            statusLabel.setText("Can't locate the device library");
            downloadProgress.setVisible(false);
            if (DeviceLibraryVersion.isUpdateFileAvailable()) {
                downloadButton.setVisible(false);
                installButton.setVisible(true);
            } else {
                downloadButton.setVisible(true);
                installButton.setVisible(false);
            }
            checkUpdateButton.setVisible(false);
        } else if (DeviceLibraryVersion.getLatestCompatibleVersion().isPresent()) {
            DeviceLibraryVersion current = DeviceLibraryVersion.getCurrentVersion().get();
            DeviceLibraryVersion latest = DeviceLibraryVersion.getLatestCompatibleVersion().get();
            if (current.getVersion().equals(latest.getVersion())) {     // already up-to-date, do nothing
                versionLabel.setText("Version: " + current.getVersion() + " (" + df.format(current.getReleaseDate()) + ")");
                statusLabel.setText("Library is up to date");
                statusHider.playFromStart();
                downloadProgress.setVisible(false);
                downloadButton.setVisible(false);
                installButton.setVisible(false);
                checkUpdateButton.setVisible(true);
            } else {    // newer version is available
                versionLabel.setText("Version: " + current.getVersion() + " (" + df.format(current.getReleaseDate()) + ")");
                statusLabel.setText("Newer version is available");
                downloadProgress.setVisible(false);
                if (DeviceLibraryVersion.isUpdateFileAvailable()) {
                    downloadButton.setVisible(false);
                    installButton.setVisible(true);
                } else {
                    downloadButton.setVisible(true);
                    installButton.setVisible(false);
                }
                checkUpdateButton.setVisible(false);
            }
        } else {    // check internet connection
            DeviceLibraryVersion current = DeviceLibraryVersion.getCurrentVersion().get();
            versionLabel.setText("Version: " + current.getVersion() + " (" + df.format(current.getReleaseDate()) + ")");
            statusLabel.setText("Can't check for update");
            downloadProgress.setVisible(false);
            downloadButton.setVisible(false);
            installButton.setVisible(false);
            checkUpdateButton.setVisible(true);
        }
    }

    public void setOnLibraryUpdateButtonPressed(EventHandler<ActionEvent> eventHandler) {
        installButton.setOnAction(eventHandler);
    }
}
