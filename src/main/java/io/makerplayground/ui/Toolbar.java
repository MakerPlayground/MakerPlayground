/*
 * Copyright (c) 2019. The Maker Playground Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.makerplayground.ui;

import com.fazecast.jSerialComm.SerialPort;
import io.makerplayground.generator.upload.*;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectConfigurationStatus;
import io.makerplayground.ui.dialog.DeviceMonitor;
import io.makerplayground.ui.dialog.UploadDialogView;
import io.makerplayground.util.OSInfo;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Optional;

public class Toolbar extends AnchorPane {

    private final ObjectProperty<Project> project;
    private final UploadManager uploadManager;

    @FXML private MenuItem newMenuItem;
    @FXML private MenuItem openMenuItem;
    @FXML private MenuItem saveMenuItem;
    @FXML private MenuItem saveAsMenuItem;
    @FXML private MenuItem exportMenuItem;
    @FXML private MenuItem uploadMenuItem;
    @FXML private MenuItem uploadStatusMenuItem;
    @FXML private MenuItem deviceMonitorMenuItem;
    @FXML private MenuItem closeMenuItem;

    @FXML private RadioButton diagramEditorButton;
    @FXML private RadioButton deviceConfigButton;
    private ImageView deviceConfigProblemImageView;

    @FXML private Label statusLabel;
    @FXML private Label portLabel;
    @FXML private ComboBox<SerialPort> portComboBox;
    @FXML private Button deviceMonitorMenuButton;
    @FXML private Button interactiveButton;
    @FXML private Button uploadButton;
    @FXML private Separator separator;
    @FXML private Button uploadStatusButton;

    private ImageView interactiveStartImageView;
    private ImageView uploadStartImageView;
    private ImageView uploadStopImageView;
    private Timeline hideUploadStatus;

    public Toolbar(ObjectProperty<Project> project, UploadManager uploadManager) {
        this.project = project;
        this.uploadManager = uploadManager;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/ToolBar.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // macos uses Command+Q to close the program rather than Alt+F4 and there isn't any platform independent way to handle this in javafx
        if (OSInfo.getOs() == OSInfo.OS.MAC) {
            closeMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.SHORTCUT_DOWN));
        } else {
            closeMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.F4, KeyCombination.ALT_DOWN));
        }

        Tooltip deviceMonitorButtonTooltip = new Tooltip("Open a device monitor");
        deviceMonitorButtonTooltip.setShowDelay(Duration.millis(250));
        deviceMonitorMenuButton.setTooltip(deviceMonitorButtonTooltip);

        ToggleGroup toggleGroup = new ToggleGroup();
        toggleGroup.getToggles().addAll(diagramEditorButton, deviceConfigButton);

        deviceConfigButton.setSelected(true);

        deviceConfigProblemImageView = new ImageView(new Image(getClass().getResourceAsStream("/css/warning.png")));
        deviceConfigProblemImageView.setFitWidth(15);
        deviceConfigProblemImageView.setFitHeight(15);

        interactiveStartImageView = new ImageView(new Image(getClass().getResourceAsStream("/css/interactive-start.png")));
        interactiveStartImageView.setFitWidth(20);
        interactiveStartImageView.setFitHeight(20);

        uploadStartImageView = new ImageView(new Image(getClass().getResourceAsStream("/css/upload-start.png")));
        uploadStartImageView.setFitWidth(20);
        uploadStartImageView.setFitHeight(20);

        uploadStopImageView = new ImageView(new Image(getClass().getResourceAsStream("/css/upload-stop.png")));
        uploadStopImageView.setFitWidth(20);
        uploadStopImageView.setFitHeight(20);

        hideUploadStatus = new Timeline(new KeyFrame(Duration.seconds(3), event -> {
            uploadStatusButton.setVisible(false);
        }));

        project.addListener((observable, oldValue, newValue) -> initUI());
        initUI();
    }

    public void setOnNewButtonPressed(EventHandler<ActionEvent> event) {
        newMenuItem.setOnAction(event);
    }

    public void setOnLoadButtonPressed(EventHandler<ActionEvent> event) {
        openMenuItem.setOnAction(event);
    }

    public void setOnSaveButtonPressed(EventHandler<ActionEvent> event) {
        saveMenuItem.setOnAction(event);
    }

    public void setOnSaveAsButtonPressed(EventHandler<ActionEvent> event) {
        saveAsMenuItem.setOnAction(event);
    }

    public void setOnExportButtonPressed(EventHandler<ActionEvent> event) {
        exportMenuItem.setOnAction(event);
    }

    public void setOnCloseButtonPressed(EventHandler<ActionEvent> event) {
        closeMenuItem.setOnAction(event);
    }

    public BooleanProperty diagramEditorSelectProperty() {
        return diagramEditorButton.selectedProperty();
    }

    public BooleanProperty deviceConfigSelectProperty() {
        return deviceConfigButton.selectedProperty();
    }

    public void setStatusMessage(String message) {
        statusLabel.setText(message);
    }

    private void initUI() {

        deviceConfigButton.graphicProperty().bind(Bindings.when(project.get().getProjectConfiguration().statusProperty().isEqualTo(ProjectConfigurationStatus.ERROR))
                .then(deviceConfigProblemImageView).otherwise((ImageView) null));

        BooleanBinding uploading = uploadManager.uploadStatusProperty().isEqualTo(UploadStatus.UPLOADING);
        BooleanBinding startingInteractiveMode = uploadManager.uploadStatusProperty().isEqualTo(UploadStatus.STARTING_INTERACTIVE);
        ReadOnlyBooleanProperty interactiveModeInitialize = project.get().getInteractiveModel().startedProperty();
        BooleanBinding portNotSelected = portComboBox.getSelectionModel().selectedItemProperty().isNull();

        portLabel.disableProperty().bind(portComboBox.disableProperty());

        portComboBox.getItems().setAll(SerialPort.getCommPorts());
        portComboBox.setOnShowing(event -> {
            SerialPort currentSelectedItem = portComboBox.getSelectionModel().getSelectedItem();
            portComboBox.getItems().setAll(SerialPort.getCommPorts());
            // find the same port in the updated port list (SerialPort's equals method hasn't been override so we do it manually)
            if (currentSelectedItem != null) {
                portComboBox.getItems().stream()
                        .filter(serialPort -> serialPort.getDescriptivePortName().equals(currentSelectedItem.getDescriptivePortName()))
                        .findFirst()
                        .ifPresent(serialPort -> portComboBox.getSelectionModel().select(serialPort));
            }
        });
        portComboBox.disableProperty().bind(uploading.or(startingInteractiveMode).or(interactiveModeInitialize));

        deviceMonitorMenuButton.disableProperty().bind(uploading.or(startingInteractiveMode).or(interactiveModeInitialize).or(portNotSelected));

        interactiveButton.graphicProperty().bind(Bindings.when(startingInteractiveMode.or(interactiveModeInitialize))
                .then(uploadStopImageView).otherwise(interactiveStartImageView));
        Tooltip interactiveButtonTooltip = new Tooltip();
        interactiveButtonTooltip.setShowDelay(Duration.millis(250));
        interactiveButtonTooltip.textProperty().bind(Bindings.when(startingInteractiveMode.or(interactiveModeInitialize))
                .then("Stop interactive mode").otherwise("Start interactive mode"));
        interactiveButton.setTooltip(interactiveButtonTooltip);

        ReadOnlyBooleanProperty useHwSerialProperty = project.get().getProjectConfiguration().useHwSerialProperty();
        BooleanBinding projectNotOk = project.get().getProjectConfiguration().statusProperty().isNotEqualTo(ProjectConfigurationStatus.OK);

        interactiveButton.disableProperty().bind(portNotSelected.or(uploading).or(useHwSerialProperty).or(projectNotOk));

        uploadButton.graphicProperty().bind(Bindings.when(uploading).then(uploadStopImageView).otherwise(uploadStartImageView));
        Tooltip uploadButtonTooltip = new Tooltip();
        uploadButtonTooltip.setShowDelay(Duration.millis(250));
        uploadButtonTooltip.textProperty().bind(Bindings.when(uploading).then("Stop uploading").otherwise("Upload to board"));
        uploadButton.setTooltip(uploadButtonTooltip);
        uploadButton.disableProperty().bind(portNotSelected.or(startingInteractiveMode).or(interactiveModeInitialize).or(projectNotOk));

        uploadManager.uploadStatusProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == UploadStatus.UPLOADING || newValue == UploadStatus.STARTING_INTERACTIVE) {
                uploadStatusButton.setText("Uploading...");
                uploadStatusButton.setVisible(true);
            } else if (newValue == UploadStatus.UPLOAD_DONE) {
                uploadStatusButton.setText("Done");
                hideUploadStatus.playFromStart();
            } else if (newValue == UploadStatus.UPLOAD_FAILED) {
                uploadStatusButton.setText("Failed");
            }
        });
        uploadManager.uploadProgressProperty().addListener((observable, oldValue, newValue) -> {
            if (Double.compare(newValue.doubleValue(), 1.0) == 0) {
                uploadStatusButton.setText("Done");
            } else {
                uploadStatusButton.setText("Uploading (" + (int) (newValue.doubleValue() * 100) + "%)");
            }
        });
        uploadStatusButton.setVisible(false);
        uploadStatusButton.managedProperty().bind(uploadStatusButton.visibleProperty());

        separator.visibleProperty().bind(uploadStatusButton.visibleProperty());
        separator.managedProperty().bind(separator.visibleProperty());

        uploadMenuItem.setOnAction(event -> onUploadButtonPressed());
        uploadMenuItem.disableProperty().bind(uploadButton.disableProperty());
        uploadStatusMenuItem.setOnAction(event -> showUploadDialog());
        uploadStatusMenuItem.disableProperty().bind(Bindings.not(uploadStatusButton.visibleProperty()));
        deviceMonitorMenuItem.setOnAction(event -> onDeviceMonitorPressed());
        deviceMonitorMenuItem.disableProperty().bind(deviceMonitorMenuButton.disableProperty());

        deviceMonitorMenuButton.setOnAction(event -> onDeviceMonitorPressed());
        interactiveButton.setOnAction(event -> onInteractiveButtonPressed());
        uploadButton.setOnAction(event -> onUploadButtonPressed());
        uploadStatusButton.setOnAction(event -> {
            showUploadDialog();
            if (uploadManager.getUploadStatus() != UploadStatus.UPLOADING && uploadManager.getUploadStatus() != UploadStatus.STARTING_INTERACTIVE) {
                hideUploadStatus.playFromStart();
            }
        });
    }

    private void showUploadDialog() {
        UploadDialogView uploadDialogView = new UploadDialogView(getScene().getWindow(), uploadManager.getUploadTask());
        uploadDialogView.progressProperty().bind(uploadManager.getUploadTask().progressProperty());
        uploadDialogView.descriptionProperty().bind(uploadManager.getUploadTask().messageProperty());
        uploadDialogView.logProperty().bind(uploadManager.uploadLogProperty());
        uploadDialogView.show();
    }

    private void onInteractiveButtonPressed() {
        if (project.get().getInteractiveModel().isStarted()) {
            project.get().getInteractiveModel().stop();
        } else if (uploadManager.getUploadStatus() != UploadStatus.STARTING_INTERACTIVE) {
            // stop the auto hide transition that may have been scheduled to run in a few second
            hideUploadStatus.stop();
            uploadManager.startInteractiveMode(portComboBox.getSelectionModel().getSelectedItem());
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to cancel upload?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() != ButtonType.OK) {
                return;
            }
            uploadManager.cancelUpload();
        }
    }

    private void onUploadButtonPressed() {
        if (uploadManager.getUploadStatus() != UploadStatus.UPLOADING) {
            // stop the auto hide transition that may have been scheduled to run in a few second
            hideUploadStatus.stop();
            uploadManager.startUploadProject(portComboBox.getSelectionModel().getSelectedItem());
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to cancel upload?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() != ButtonType.OK) {
                return;
            }
            uploadManager.cancelUpload();
        }
    }

    private void onDeviceMonitorPressed() {
        DeviceMonitor deviceMonitor = new DeviceMonitor(portComboBox.getSelectionModel().getSelectedItem());
        deviceMonitor.showAndWait();
    }
}
