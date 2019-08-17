package io.makerplayground.ui;

import com.fazecast.jSerialComm.SerialPort;
import io.makerplayground.device.generic.GenericDevice;
import io.makerplayground.generator.DeviceMapper;
import io.makerplayground.generator.DeviceMapperResult;
import io.makerplayground.generator.source.SourceCodeResult;
import io.makerplayground.generator.upload.ArduinoUploadTask;
import io.makerplayground.generator.upload.UploadResult;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectDevice;
import io.makerplayground.ui.dialog.UploadDialogView;
import io.makerplayground.ui.dialog.configdevice.ConfigActualDeviceView;
import io.makerplayground.ui.dialog.configdevice.ConfigActualDeviceViewModel;
import io.makerplayground.ui.explorer.DeviceExplorerPanel;
import io.makerplayground.ui.explorer.InteractiveCodeGenerator;
import io.makerplayground.ui.explorer.InteractiveDiagramView;
import io.makerplayground.ui.explorer.InteractiveModel;
import javafx.application.HostServices;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.WorkerStateEvent;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DeviceExplorerTab extends SplitPane {

    private final Project currentProject;
    private final HostServices hostServices;
    private final DeviceExplorerPanel deviceExplorer;
    private final SplitPane leftSplitPane;
    private final StackPane rightView;

    private InteractiveModel interactiveModel;
    private InteractiveDiagramView interactiveDiagramView;
    private SerialPort lastSelectedSerialPort;

    public DeviceExplorerTab(Project currentProject, HostServices hostServices) {
        this.currentProject = currentProject;
        this.hostServices = hostServices;

        deviceExplorer = new DeviceExplorerPanel(currentProject.getController(), hostServices);
        deviceExplorer.setOnAddButtonPressed(actualDevice -> {
            List<ProjectDevice> projectDevices = new ArrayList<>();
            for (GenericDevice genericDevice : actualDevice.getSupportedGenericDevice()) {
                projectDevices.add(currentProject.addDevice(genericDevice));
            }

            // sort by name so that the top most device will be the parent device for the other project device
            projectDevices.sort(Comparator.comparing(ProjectDevice::getName));

            ProjectDevice parentDevice = projectDevices.get(0);
            parentDevice.setActualDevice(actualDevice);
            for (int i=1; i<projectDevices.size(); i++) {
                projectDevices.get(i).setParentDevice(parentDevice);
            }

            refreshView();
        });

        leftSplitPane = new SplitPane();
        leftSplitPane.setDividerPositions(0.5);
        leftSplitPane.setOrientation(Orientation.VERTICAL);

        rightView = new StackPane();

        setDividerPositions(0.5);
        setOrientation(Orientation.HORIZONTAL);
        getItems().addAll(leftSplitPane, rightView);

        refreshView();
    }

    public void refreshView() {
        rightView.getChildren().clear();

        // close current connection and clear reference to the old model and view
        if (interactiveDiagramView != null && interactiveDiagramView.getSelectedSerialPort() != null) {
            lastSelectedSerialPort = interactiveDiagramView.getSelectedSerialPort();
            lastSelectedSerialPort.closePort();
            interactiveModel.setSerialPort(null);
            interactiveModel.setInitialized(false);
            interactiveModel = null;
            interactiveDiagramView = null;
        }

        DeviceMapperResult mappingResult = DeviceMapper.validateDeviceAssignment(currentProject);
        if (mappingResult != DeviceMapperResult.OK) {
            // overlay the generate view with a warning icon and an error message
            ImageView warningIcon = new ImageView(new Image(DeviceExplorerTab.this.getClass().getResourceAsStream("/css/dialog/warning.png")));
            Label warningMessage = new Label(mappingResult.getErrorMessage());
            warningMessage.setTextAlignment(TextAlignment.CENTER);
            warningMessage.setWrapText(true);
            VBox errorPane = new VBox();
            errorPane.setPadding(new Insets(20, 20, 20, 20));
            errorPane.setAlignment(Pos.CENTER);
            errorPane.getChildren().addAll(warningIcon, warningMessage);
            rightView.getChildren().add(errorPane);
        } else {
            interactiveModel = new InteractiveModel(currentProject);
            interactiveDiagramView = new InteractiveDiagramView(currentProject, interactiveModel);
            if (lastSelectedSerialPort != null) {
                interactiveDiagramView.setSelectedSerialPort(lastSelectedSerialPort);
            }
            interactiveDiagramView.setOnInteractiveControlButtonPressed(event -> {
                if (!interactiveModel.isInitialized()) {
                    StringBuilder log = new StringBuilder();
                    SimpleStringProperty logProperty = new SimpleStringProperty();

                    SourceCodeResult code = InteractiveCodeGenerator.generateCode(interactiveModel, currentProject);
//                    System.out.println(code.getCode());
                    ArduinoUploadTask uploadTask = new ArduinoUploadTask(code, currentProject, true);
                    uploadTask.logProperty().addListener((observable, oldValue, newValue) -> {
                        log.append(newValue);
                        logProperty.set(log.toString());
                    });
                    uploadTask.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, event1 -> {
                        if (uploadTask.getValue() == UploadResult.OK) {
                            SerialPort serialPort = interactiveDiagramView.getSelectedSerialPort();
                            serialPort.setComPortParameters(115200, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
                            serialPort.addDataListener(interactiveModel);
                            if (serialPort.openPort()) {
                                interactiveModel.setSerialPort(serialPort);
                                interactiveModel.setInitialized(true);
                            }
                        }
                    });
                    new Thread(uploadTask).start();

                    UploadDialogView uploadDialogView = new UploadDialogView(getScene().getWindow(), uploadTask, true);
                    uploadDialogView.progressProperty().bind(uploadTask.progressProperty());
                    uploadDialogView.descriptionProperty().bind(uploadTask.messageProperty());
                    uploadDialogView.logProperty().bind(logProperty);
                    uploadDialogView.show();
                } else {
                    interactiveDiagramView.getSelectedSerialPort().closePort();
                    interactiveModel.setSerialPort(null);
                    interactiveModel.setInitialized(false);
                }
            });
            rightView.getChildren().add(interactiveDiagramView);
        }

        // device config
        ConfigActualDeviceViewModel configActualDeviceViewModel = new ConfigActualDeviceViewModel(currentProject);
        configActualDeviceViewModel.setConfigChangedCallback(this::refreshView);
        ConfigActualDeviceView configActualDeviceView = new ConfigActualDeviceView(configActualDeviceViewModel, false);

        deviceExplorer.setController(currentProject.getController());

        leftSplitPane.getItems().setAll(configActualDeviceView, deviceExplorer);
    }

}
