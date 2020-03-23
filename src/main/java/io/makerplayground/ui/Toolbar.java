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

import io.makerplayground.device.actual.Platform;
import io.makerplayground.generator.upload.*;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectConfigurationStatus;
import io.makerplayground.ui.dialog.UploadDialogView;
import io.makerplayground.util.OSInfo;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.*;
import javafx.collections.ListChangeListener;
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
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class Toolbar extends AnchorPane {

    private final ObjectProperty<Project> project;
    private final UploadManager uploadManager;
    private final UploadTargetScanner uploadTargetScanner;

    @FXML private MenuItem newMenuItem;
    @FXML private MenuItem openMenuItem;
    @FXML private MenuItem saveMenuItem;
    @FXML private MenuItem saveAsMenuItem;
    @FXML private MenuItem exportMenuItem;
    @FXML private MenuItem uploadMenuItem;
    @FXML private MenuItem uploadStatusMenuItem;
    @FXML private MenuItem closeMenuItem;

    @FXML private RadioButton diagramEditorButton;
    @FXML private RadioButton deviceConfigButton;
    private ImageView deviceHasProblemImageView;
    private ImageView diagramHasProblemImageView;
    @FXML private RadioButton deviceMonitorButton;
    @FXML private Label statusLabel;
    @FXML private Label portLabel;
    @FXML private ComboBox<UploadTarget> portComboBox;
    @FXML private Button interactiveButton;
    @FXML private Button uploadButton;
    @FXML private Separator separator;
    @FXML private Button uploadStatusButton;

    private ImageView interactiveStartImageView;
    private ImageView uploadStartImageView;
    private ImageView uploadStopImageView;
    private Timeline hideUploadStatus;

    public Toolbar(ObjectProperty<Project> project) {
        this.project = project;
        this.uploadManager = new UploadManager(project);
        this.uploadTargetScanner = new UploadTargetScanner(project);

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

        ToggleGroup toggleGroup = new ToggleGroup();
        toggleGroup.getToggles().addAll(diagramEditorButton, deviceConfigButton, deviceMonitorButton);

        deviceConfigButton.setSelected(true);

        deviceHasProblemImageView = new ImageView(new Image(getClass().getResourceAsStream("/css/warning.png")));
        deviceHasProblemImageView.setFitWidth(15);
        deviceHasProblemImageView.setFitHeight(15);

        diagramHasProblemImageView = new ImageView(new Image(getClass().getResourceAsStream("/css/warning.png")));
        diagramHasProblemImageView.setFitWidth(15);
        diagramHasProblemImageView.setFitHeight(15);

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

    public BooleanProperty deviceMonitorSelectProperty() {
        return deviceMonitorButton.selectedProperty();
    }

    public ReadOnlyObjectProperty<UploadTarget> selectingSerialPortProperty() {
        return portComboBox.getSelectionModel().selectedItemProperty();
    }

    public void setStatusMessage(String message) {
        statusLabel.setText(message);
    }

    private ListChangeListener<? super UploadTarget> uploadConnectionListChangeListener;

    private void initUI() {
        deviceConfigButton.graphicProperty().bind(Bindings.when(project.get().getProjectConfiguration().statusProperty().isEqualTo(ProjectConfigurationStatus.ERROR))
                .then(deviceHasProblemImageView).otherwise((ImageView) null));
        diagramEditorButton.graphicProperty().bind(Bindings.when(project.get().diagramErrorProperty()).then(diagramHasProblemImageView).otherwise((ImageView) null));

        BooleanBinding uploading = uploadManager.uploadStatusProperty().isEqualTo(UploadStatus.UPLOADING);
        BooleanBinding startingInteractiveMode = uploadManager.uploadStatusProperty().isEqualTo(UploadStatus.STARTING_INTERACTIVE);
        ReadOnlyBooleanProperty interactiveModeInitialize = project.get().getInteractiveModel().startedProperty();
        BooleanBinding portNotSelected = portComboBox.getSelectionModel().selectedItemProperty().isNull();
        BooleanProperty deviceMonitorShowing = deviceMonitorButton.selectedProperty();  // we disable other controls whether it is successfully initialized or not as a precaution

        portLabel.disableProperty().bind(portComboBox.disableProperty());

        project.get().platformProperty().addListener((observable, oldValue, newValue) -> {
            portComboBox.getSelectionModel().clearSelection();
            uploadTargetScanner.scan();
            if (!portComboBox.getItems().isEmpty()) {
                portComboBox.getSelectionModel().selectFirst();
            }
        });

        portComboBox.setCellFactory(getListViewListCellCallback());
        portComboBox.setButtonCell(getListViewListCellCallback().call(null));
        uploadConnectionListChangeListener = c -> {
            List<UploadMode> supportUploadModes = project.get().getSelectedPlatform().getSupportUploadModes();
            if (supportUploadModes.contains(UploadMode.SERIAL_PORT)) {
                while(c.next()) {
                    if (c.wasRemoved() && c.getList().size() > 0) {
                        portComboBox.getSelectionModel().select(c.getList().get(0));
                    }
                    if (c.wasAdded()) {
                        portComboBox.getSelectionModel().select(c.getAddedSubList().get(0));
                    }
                }
            } else if (supportUploadModes.contains(UploadMode.RPI_ON_NETWORK)) {
                if (c.getList().size() >= 1 && portComboBox.getSelectionModel().isEmpty()) {
                    portComboBox.getSelectionModel().select(c.getList().get(0));
                }
                else if (c.getList().size() == 0) {
                    portComboBox.getSelectionModel().clearSelection();
                }
            }
        };
        portComboBox.itemsProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue != null) {
                portComboBox.getSelectionModel().clearSelection();
                oldValue.removeListener(uploadConnectionListChangeListener);
            }
            if (newValue != null) {
                newValue.addListener(uploadConnectionListChangeListener);
            }
        });
        portComboBox.itemsProperty().bind(uploadTargetScanner.uploadTargetListProperty());
        portComboBox.setOnShowing(event -> {
            portComboBox.getSelectionModel().clearSelection();
        });
        portComboBox.disableProperty().bind(uploading.or(startingInteractiveMode).or(interactiveModeInitialize).or(deviceMonitorShowing));
        uploadTargetScanner.scan();

        // TODO: add case when uploading
        deviceMonitorButton.disableProperty().bind(uploading.or(startingInteractiveMode).or(interactiveModeInitialize).or(portNotSelected));

        interactiveButton.graphicProperty().bind(Bindings.when(startingInteractiveMode.or(interactiveModeInitialize))
                .then(uploadStopImageView).otherwise(interactiveStartImageView));
        Tooltip interactiveButtonTooltip = new Tooltip();
        interactiveButtonTooltip.setShowDelay(Duration.millis(250));
        interactiveButtonTooltip.textProperty().bind(Bindings.when(startingInteractiveMode.or(interactiveModeInitialize))
                .then("Stop interactive mode").otherwise("Start interactive mode"));
        interactiveButton.setTooltip(interactiveButtonTooltip);

        ReadOnlyBooleanProperty useHwSerialProperty = project.get().getProjectConfiguration().useHwSerialProperty();
        BooleanBinding projectNotOk = project.get().getProjectConfiguration().statusProperty().isNotEqualTo(ProjectConfigurationStatus.OK);

        interactiveButton.disableProperty().bind(interactiveModeInitialize.not().and(portNotSelected.or(uploading).or(useHwSerialProperty).or(projectNotOk).or(deviceMonitorShowing)));

        uploadButton.graphicProperty().bind(Bindings.when(uploading).then(uploadStopImageView).otherwise(uploadStartImageView));
        Tooltip uploadButtonTooltip = new Tooltip();
        uploadButtonTooltip.setShowDelay(Duration.millis(250));
        uploadButtonTooltip.textProperty().bind(Bindings.when(uploading).then("Stop uploading").otherwise("Upload to board"));
        uploadButton.setTooltip(uploadButtonTooltip);
        uploadButton.disableProperty().bind(portNotSelected.or(startingInteractiveMode).or(interactiveModeInitialize).or(projectNotOk).or(deviceMonitorShowing));

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

        interactiveButton.setOnAction(event -> onInteractiveButtonPressed());
        uploadButton.setOnAction(event -> onUploadButtonPressed());
        uploadStatusButton.setOnAction(event -> {
            showUploadDialog();
            if (uploadManager.getUploadStatus() != UploadStatus.UPLOADING && uploadManager.getUploadStatus() != UploadStatus.STARTING_INTERACTIVE) {
                hideUploadStatus.playFromStart();
            }
        });
    }

    private Callback<ListView<UploadTarget>, ListCell<UploadTarget>> getListViewListCellCallback() {
        return param -> new ListCell<>() {
            @Override
            protected void updateItem(UploadTarget item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    if (item.getUploadMode().equals(UploadMode.SERIAL_PORT)) {
                        setText(item.getSerialPort().getDescriptivePortName());
                    }
                    else if (item.getUploadMode().equals(UploadMode.RPI_ON_NETWORK)) {
                        setText("Raspberry Pi on Network (" + item.getRpiHostName() + ")");
                    }
                    else {
                        setText("Not Supported Yet");
                    }
                }
            }
        };
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
}
