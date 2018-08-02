package io.makerplayground.ui;

import io.makerplayground.generator.DeviceMapper;
import io.makerplayground.generator.Sourcecode;
import io.makerplayground.generator.UploadTask;
import io.makerplayground.helper.SingletonUploadClick;
import io.makerplayground.helper.SingletonWiringDiagram;
import io.makerplayground.project.Project;
import io.makerplayground.ui.dialog.configdevice.ConfigActualDeviceView;
import io.makerplayground.ui.dialog.configdevice.ConfigActualDeviceViewModel;
import io.makerplayground.ui.dialog.devicepane.devicepanel.DevicePanelView;
import io.makerplayground.ui.dialog.devicepane.devicepanel.DevicePanelViewModel;
import io.makerplayground.ui.dialog.ErrorDialogView;
import io.makerplayground.ui.dialog.generate.GenerateView;
import io.makerplayground.ui.dialog.generate.GenerateViewModel;
import io.makerplayground.ui.dialog.UploadDialogView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

/**
 * Created by Mai.Manju on 12-Jun-17.
 */
public class RightPanel extends VBox {

    private final Project project;

    public RightPanel(Project project) {
        this.project = project;
        initView();
    }

    private void initView() {
        DevicePanelViewModel devicePanelViewModel = new DevicePanelViewModel(project);
        DevicePanelView devicePanelView = new DevicePanelView(devicePanelViewModel);
        devicePanelView.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(devicePanelView, Priority.ALWAYS);

        Button configureBtn = new Button("Configure Device");
        configureBtn.setOnAction(this::handleConfigureBtn);
        configureBtn.setMaxWidth(Double.MAX_VALUE);
        Button generateBtn = new Button("Generate Project");
        generateBtn.setOnAction(this::handleGenerateBtn);
        generateBtn.setMaxWidth(Double.MAX_VALUE);
        Button uploadBtn = new Button("Upload");
        uploadBtn.setOnAction(this::handleUploadBtn);
        uploadBtn.setMaxWidth(Double.MAX_VALUE);

        VBox vBox = new VBox(2);
        vBox.setPadding(new Insets(20, 20, 20, 20));
        vBox.getChildren().addAll(configureBtn, generateBtn, uploadBtn);
        VBox.setVgrow(vBox, Priority.NEVER);
        vBox.getStylesheets().add(getClass().getResource("/css/RightPanel.css").toExternalForm());

        getChildren().addAll(devicePanelView, vBox);
        setStyle("-fx-background-color : #313644;");
        setMaxWidth(330);
    }

    private void handleConfigureBtn(ActionEvent event) {
        ConfigActualDeviceViewModel configActualDeviceViewModel = new ConfigActualDeviceViewModel(project);
        ConfigActualDeviceView configActualDeviceView = new ConfigActualDeviceView(configActualDeviceViewModel);
        configActualDeviceView.showAndWait();
    }

    private void handleGenerateBtn(ActionEvent event) {
        DeviceMapper.DeviceMapperResult mappingResult = DeviceMapper.autoAssignDevices(project);
        if (mappingResult == DeviceMapper.DeviceMapperResult.NO_MCU_SELECTED) {
            ErrorDialogView errorDialogView = new ErrorDialogView("Controller hasn't been selected");
            errorDialogView.showAndWait();
            return;
        } else if (mappingResult == DeviceMapper.DeviceMapperResult.NOT_ENOUGH_PORT) {
            ErrorDialogView errorDialogView = new ErrorDialogView("Not enough port");
            errorDialogView.showAndWait();
            return;
        } else if (mappingResult == DeviceMapper.DeviceMapperResult.NO_SUPPORT_DEVICE) {
            ErrorDialogView errorDialogView = new ErrorDialogView("Can't find any support device");
            errorDialogView.showAndWait();
            return;
        } else if (mappingResult != DeviceMapper.DeviceMapperResult.OK) {
            throw new IllegalStateException("Found unknown error!!!");
        }

        Sourcecode code = Sourcecode.generateCode(project, false);
        if (code.getError() != null) {
            ErrorDialogView errorDialogView = new ErrorDialogView(code.getError().getDescription());
            errorDialogView.showAndWait();
        } else {
            SingletonWiringDiagram.getInstance().setOpenTime();
            GenerateViewModel generateViewModel = new GenerateViewModel(project, code);
            GenerateView generateView = new GenerateView(generateViewModel);
            generateView.showAndWait();
        }
    }

    private void handleUploadBtn(ActionEvent event) {
        SingletonUploadClick.getInstance().click();
        UploadTask uploadTask = new UploadTask(project);

        UploadDialogView uploadDialogView = new UploadDialogView(uploadTask);
        uploadDialogView.progressProperty().bind(uploadTask.progressProperty());
        uploadDialogView.descriptionProperty().bind(uploadTask.messageProperty());
        uploadDialogView.logProperty().bind(uploadTask.logProperty());
        uploadDialogView.show();

        new Thread(uploadTask).start();
    }

}
