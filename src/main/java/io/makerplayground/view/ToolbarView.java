package io.makerplayground.view;

import com.fazecast.jSerialComm.SerialPort;
import de.saxsys.mvvmfx.FxmlPath;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import io.makerplayground.generator.upload.UploadStatus;
import io.makerplayground.project.ProjectConfigurationStatus;
import io.makerplayground.util.OSInfo;
import io.makerplayground.viewmodel.ToolbarViewModel;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

@FxmlPath("/io/makerplayground/view/ToolbarView.fxml")
public class ToolbarView implements FxmlView<ToolbarViewModel>, Initializable {

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
    @FXML private RadioButton deviceMonitorButton;
    @FXML private Label statusLabel;
    @FXML private Label portLabel;
    @FXML private ComboBox<SerialPort> portComboBox;
    @FXML private Button interactiveButton;
    @FXML private Button uploadButton;
    @FXML private Separator separator;
    @FXML private Button uploadStatusButton;

    @FXML private Tooltip interactiveButtonTooltip;
    @FXML private Tooltip uploadButtonTooltip;

    private ImageView deviceHasProblemImageView = new ImageView(new Image(ToolbarView.class.getResourceAsStream("/io/makerplayground/icon/warning.png")));
    private ImageView diagramHasProblemImageView = new ImageView(new Image(ToolbarView.class.getResourceAsStream("/io/makerplayground/icon/warning.png")));
    private ImageView interactiveStartImageView = new ImageView(new Image(ToolbarView.class.getResourceAsStream("/io/makerplayground/icon/interactive-start.png")));
    private ImageView uploadStartImageView = new ImageView(new Image(ToolbarView.class.getResourceAsStream("/io/makerplayground/icon/upload-start.png")));
    private ImageView uploadStopImageView = new ImageView(new Image(ToolbarView.class.getResourceAsStream("/io/makerplayground/icon/upload-stop.png")));

    @InjectViewModel
    private ToolbarViewModel viewModel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        statusLabel.textProperty().bind(viewModel.statusMessageProperty());
        if (OSInfo.getOs() == OSInfo.OS.MAC) {
            closeMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.SHORTCUT_DOWN));
        } else {
            closeMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.F4, KeyCombination.ALT_DOWN));
        }

        ToggleGroup toggleGroup = new ToggleGroup();
        toggleGroup.getToggles().addAll(diagramEditorButton, deviceConfigButton, deviceMonitorButton);

        newMenuItem.setOnAction(event -> viewModel.requestNewProject());
        openMenuItem.setOnAction(event -> viewModel.requestLoadProject());
        saveMenuItem.setOnAction(event -> viewModel.saveProject());
        saveAsMenuItem.setOnAction(event -> viewModel.saveProjectAs());
        exportMenuItem.setOnAction(event -> viewModel.exportProject());
        closeMenuItem.setOnAction(event -> viewModel.requestClose());

        deviceConfigButton.setSelected(true);

        deviceHasProblemImageView.setFitWidth(15);
        deviceHasProblemImageView.setFitHeight(15);

        diagramHasProblemImageView.setFitWidth(15);
        diagramHasProblemImageView.setFitHeight(15);

        interactiveStartImageView.setFitWidth(20);
        interactiveStartImageView.setFitHeight(20);

        uploadStartImageView.setFitWidth(20);
        uploadStartImageView.setFitHeight(20);

        uploadStopImageView.setFitWidth(20);
        uploadStopImageView.setFitHeight(20);

        initUI();
    }

    private void initUI() {
        BooleanBinding projectConfigNotOk = viewModel.projectConfigurationStatusProperty().isNotEqualTo(ProjectConfigurationStatus.OK);
        BooleanBinding uploading = viewModel.uploadStatusProperty().isEqualTo(UploadStatus.UPLOADING);
        BooleanBinding startingInteractiveMode = viewModel.uploadStatusProperty().isEqualTo(UploadStatus.STARTING_INTERACTIVE);
        ReadOnlyBooleanProperty interactiveModeInitialize = viewModel.interactiveStartedProperty();
        BooleanProperty deviceMonitorShowing = deviceMonitorButton.selectedProperty();  // we disable other controls whether it is successfully initialized or not as a precaution

        BooleanBinding portNotSelected = portComboBox.getSelectionModel().selectedItemProperty().isNull();
        BooleanBinding disablePortUI = uploading.or(startingInteractiveMode).or(interactiveModeInitialize).or(deviceMonitorShowing);
        BooleanBinding disableDeviceMonitorUI = uploading.or(startingInteractiveMode).or(interactiveModeInitialize).or(portNotSelected);
        BooleanBinding disableInteractiveButton = interactiveModeInitialize.not().and(portNotSelected.or(uploading).or(viewModel.useHwSerialProperty()).or(projectConfigNotOk).or(deviceMonitorShowing));
        BooleanBinding disableUploadButton = portNotSelected.or(startingInteractiveMode).or(interactiveModeInitialize).or(projectConfigNotOk).or(deviceMonitorShowing);
        StringBinding interactiveTooltipText = Bindings.when(startingInteractiveMode.or(interactiveModeInitialize)).then("Stop interactive mode").otherwise("Start interactive mode");

        /* Warning Image at Device Button (Tab) */
        ObjectBinding<ImageView> deviceConfigWarningImage = Bindings.when(projectConfigNotOk).then(deviceHasProblemImageView).otherwise((ImageView) null);
        deviceConfigButton.graphicProperty().bind(deviceConfigWarningImage);

        /* Warning Image at Diagram Button (Tab) */
        ObjectBinding<ImageView> diagramEditorWarningImage = Bindings.when(viewModel.diagramErrorProperty()).then(diagramHasProblemImageView).otherwise((ImageView) null);
        diagramEditorButton.graphicProperty().bind(diagramEditorWarningImage);

        /* Device Monitor Button (Tab) */
        deviceMonitorButton.disableProperty().bind(disableDeviceMonitorUI);
        // TODO: add case when uploading

        /* Port ComboBox and Label */
        viewModel.serialPortProperty().bind(portComboBox.getSelectionModel().selectedItemProperty());
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
        portComboBox.disableProperty().bind(disablePortUI);
        portLabel.disableProperty().bind(disablePortUI);

        /* Interactive Start/Stop Button */
        ObjectBinding<ImageView> interactiveButtonImage = Bindings.when(startingInteractiveMode.or(interactiveModeInitialize))
                .then(uploadStopImageView).otherwise(interactiveStartImageView);
        interactiveButton.graphicProperty().bind(interactiveButtonImage);
        interactiveButton.disableProperty().bind(disableInteractiveButton);
        interactiveButtonTooltip.textProperty().bind(interactiveTooltipText);

        /* Upload Button */
        ObjectBinding<ImageView> uploadButtonImage = Bindings.when(uploading).then(uploadStopImageView).otherwise(uploadStartImageView);
        uploadButton.graphicProperty().bind(uploadButtonImage);
        uploadButton.disableProperty().bind(disableUploadButton);
        uploadButtonTooltip.textProperty().bind(Bindings.when(uploading).then("Stop uploading").otherwise("Upload to board"));

        uploadStatusButton.textProperty().bind(viewModel.uploadStatusTextProperty());
        uploadStatusButton.visibleProperty().bind(viewModel.uploadStatusVisibleProperty());
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
            if (viewModel.uploadStatusProperty().get() != UploadStatus.UPLOADING && viewModel.uploadStatusProperty().get() != UploadStatus.STARTING_INTERACTIVE) {
                viewModel.startHideUploadTimeLine();
            }
        });
    }

    private void showUploadDialog() {
//        UploadDialogView uploadDialogView = new UploadDialogView(pane.getScene().getWindow(), uploadManager.getUploadTask());
//        uploadDialogView.progressProperty().bind(viewModel.uploadProgressProperty());
//        uploadDialogView.descriptionProperty().bind(viewModel.uploadMessageProperty());
//        uploadDialogView.logProperty().bind(viewModel.uploadLogProperty());
//        uploadDialogView.show();
    }

    private void onInteractiveButtonPressed() {
        if (!viewModel.interactiveStartedProperty().get() && viewModel.uploadStatusProperty().get() == UploadStatus.STARTING_INTERACTIVE) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to cancel upload?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() != ButtonType.OK) {
                return;
            }
            viewModel.cancelUpload();
        } else {
            viewModel.toggleInteractive();
        }
    }

    private void onUploadButtonPressed() {
        if (viewModel.uploadStatusProperty().get() == UploadStatus.UPLOADING) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to cancel upload?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() != ButtonType.OK) {
                return;
            }
            viewModel.cancelUpload();
        } else {
            viewModel.startUploadProject();
        }
    }
}
