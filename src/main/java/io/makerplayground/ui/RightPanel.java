package io.makerplayground.ui;

import io.makerplayground.generator.DeviceMapper;
import io.makerplayground.generator.Sourcecode;
import io.makerplayground.generator.SourcecodeGenerator;
import io.makerplayground.generator.UploadTask;
import io.makerplayground.helper.SingletonUploadClick;
import io.makerplayground.helper.SingletonWiringDiagram;
import io.makerplayground.project.Project;
import io.makerplayground.ui.dialog.ErrorDialogView;
import io.makerplayground.ui.dialog.UploadDialogView;
import io.makerplayground.ui.dialog.configdevice.ConfigActualDeviceView;
import io.makerplayground.ui.dialog.configdevice.ConfigActualDeviceViewModel;
import io.makerplayground.ui.dialog.devicepane.devicepanel.DevicePanelView;
import io.makerplayground.ui.dialog.devicepane.devicepanel.DevicePanelViewModel;
import io.makerplayground.ui.dialog.generate.GenerateView;
import io.makerplayground.ui.dialog.generate.GenerateViewModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.IOException;

/**
 * Created by Mai.Manju on 12-Jun-17.
 */
class RightPanel extends VBox {

    private final Project project;

    RightPanel(Project project) {
        this.project = project;
        initView();
    }

    private void initView() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/RightPanel.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        DevicePanelViewModel devicePanelViewModel = new DevicePanelViewModel(project);
        DevicePanelView devicePanelView = new DevicePanelView(devicePanelViewModel);
        devicePanelView.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(devicePanelView, Priority.ALWAYS);

        getChildren().add(0, devicePanelView);
    }

    @FXML
    private void handleConfigureBtn(ActionEvent event) {
        ConfigActualDeviceViewModel configActualDeviceViewModel = new ConfigActualDeviceViewModel(project);
        ConfigActualDeviceView configActualDeviceView = new ConfigActualDeviceView(getScene().getWindow(), configActualDeviceViewModel);
        configActualDeviceView.show();
    }

    @FXML
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

        Sourcecode code = SourcecodeGenerator.generateCode(project, true);
        if (code.getError() != null) {
            ErrorDialogView errorDialogView = new ErrorDialogView(code.getError().getDescription());
            errorDialogView.showAndWait();
        } else {
            SingletonWiringDiagram.getInstance().setOpenTime();
            GenerateViewModel generateViewModel = new GenerateViewModel(project, code);
            GenerateView generateView = new GenerateView(getScene().getWindow(), generateViewModel);
            generateView.show();
        }
    }

    @FXML
    private void handleUploadBtn(ActionEvent event) {
        SingletonUploadClick.getInstance().click();
        UploadTask uploadTask = new UploadTask(project);

        UploadDialogView uploadDialogView = new UploadDialogView(getScene().getWindow(), uploadTask);
        uploadDialogView.progressProperty().bind(uploadTask.progressProperty());
        uploadDialogView.descriptionProperty().bind(uploadTask.messageProperty());
        uploadDialogView.logProperty().bind(uploadTask.logProperty());
        uploadDialogView.show();

        new Thread(uploadTask).start();
    }

}
