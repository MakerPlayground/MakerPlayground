package io.makerplayground.ui;

import io.makerplayground.generator.DeviceMapper;
import io.makerplayground.generator.Sourcecode;
import io.makerplayground.generator.UploadTask;
import io.makerplayground.helper.SingletonUploadClick;
import io.makerplayground.helper.SingletonWiringDiagram;
import io.makerplayground.project.Project;
import io.makerplayground.ui.devicepanel.ConfigActualDeviceView;
import io.makerplayground.ui.devicepanel.ConfigActualDeviceViewModel;
import io.makerplayground.ui.devicepanel.DevicePanelView;
import io.makerplayground.ui.devicepanel.DevicePanelViewModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

/**
 * Created by Mai.Manju on 12-Jun-17.
 */
public class RightPanel extends AnchorPane {

    @FXML
    private VBox projectButton;
    @FXML
    private Button configureBtn;
    @FXML
    private Button generateBtn;
    @FXML
    private Button uploadBtn;

    private final Project project;

    public RightPanel(Project project) {
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
            e.printStackTrace();
        }

        configureBtn.prefWidthProperty().bind(projectButton.widthProperty());
        generateBtn.prefWidthProperty().bind(projectButton.widthProperty());
        uploadBtn.prefWidthProperty().bind(projectButton.widthProperty());

        DevicePanelViewModel devicePanelViewModel = new DevicePanelViewModel(project);
        DevicePanelView devicePanelView = new DevicePanelView(devicePanelViewModel);
        AnchorPane.setLeftAnchor(devicePanelView,0.0);
        AnchorPane.setRightAnchor(devicePanelView,0.0);
        AnchorPane.setTopAnchor(devicePanelView,0.0);
        AnchorPane.setBottomAnchor(devicePanelView,120.0);

        getChildren().add(0, devicePanelView);
    }


    @FXML
    private void handleConfigureBtn(ActionEvent event) {
        ConfigActualDeviceViewModel configActualDeviceViewModel = new ConfigActualDeviceViewModel(project);
        ConfigActualDeviceView configActualDeviceView = new ConfigActualDeviceView(configActualDeviceViewModel);
        configActualDeviceView.showAndWait();
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

    @FXML
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

    public static class ProgressForm {
        private final Stage dialogStage;
        private final ProgressIndicator pin = new ProgressIndicator();

        public ProgressForm() {
            dialogStage = new Stage();
            dialogStage.initStyle(StageStyle.UTILITY);
            dialogStage.setResizable(false);
            dialogStage.initModality(Modality.APPLICATION_MODAL);

            pin.setProgress(-1F);

            final HBox hb = new HBox();
            hb.setSpacing(5);
            hb.setAlignment(Pos.CENTER);
            hb.getChildren().addAll(pin);

            Scene scene = new Scene(hb);
            dialogStage.setScene(scene);
        }

        public Stage getDialogStage() {
            return dialogStage;
        }
    }

}
