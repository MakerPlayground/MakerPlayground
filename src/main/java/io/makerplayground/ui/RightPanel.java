package io.makerplayground.ui;

import com.fazecast.jSerialComm.SerialPort;
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
import javafx.geometry.Insets;
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

/**
 * Created by Mai.Manju on 12-Jun-17.
 */
public class RightPanel extends AnchorPane {


    private final Project project;

    public RightPanel(Project project) {
        this.project = project;
        //File f = new File("src/main/resources/css/RightPanel.css");
        //getStylesheets().add("file:///" + f.getAbsolutePath().replace("\\", "/"));
        getStylesheets().add(RightPanel.class.getResource("/css/RightPanel.css").toExternalForm());
        initView();
    }

    private void initView() {
        setStyle("-fx-background-color : #404040");
        setMaxWidth(330.0);
        DevicePanelViewModel devicePanelViewModel = new DevicePanelViewModel(project);
        DevicePanelView devicePanelView = new DevicePanelView(devicePanelViewModel);
        Button configureBtn = new Button("Configure Device");

        configureBtn.setOnAction(event -> {
            ConfigActualDeviceViewModel configActualDeviceViewModel = new ConfigActualDeviceViewModel(project);
            ConfigActualDeviceView configActualDeviceView = new ConfigActualDeviceView(configActualDeviceViewModel);
            configActualDeviceView.showAndWait();
        });


        Button generateBtn = new Button("Generate Project");
        generateBtn.setOnAction(event -> {
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
        });
        Button uploadBtn = new Button("Upload");
        uploadBtn.setOnAction(event -> {
            SingletonUploadClick.getInstance().click();
            UploadTask uploadTask = new UploadTask(project);

            UploadDialogView uploadDialogView = new UploadDialogView(uploadTask);
            uploadDialogView.progressProperty().bind(uploadTask.progressProperty());
            uploadDialogView.descriptionProperty().bind(uploadTask.messageProperty());
            uploadDialogView.logProperty().bind(uploadTask.logProperty());
            uploadDialogView.show();

            new Thread(uploadTask).start();
        });

        VBox projectButton = new VBox();
        projectButton.setStyle("-fx-background-color : #1c1c1c");
        projectButton.setSpacing(2.0);
        projectButton.getChildren().addAll(configureBtn, generateBtn, uploadBtn);
        projectButton.setAlignment(Pos.CENTER);
        projectButton.setPadding(new Insets(20.0,20.0,20.0,20.0));

        configureBtn.prefWidthProperty().bind(projectButton.widthProperty());
        generateBtn.prefWidthProperty().bind(projectButton.widthProperty());
        uploadBtn.prefWidthProperty().bind(projectButton.widthProperty());

        AnchorPane.setLeftAnchor(devicePanelView,0.0);
        AnchorPane.setRightAnchor(devicePanelView,0.0);
        AnchorPane.setTopAnchor(devicePanelView,0.0);
        AnchorPane.setBottomAnchor(devicePanelView,120.0);
        AnchorPane.setLeftAnchor(projectButton,0.0);
        AnchorPane.setRightAnchor(projectButton,0.0);
        AnchorPane.setBottomAnchor(projectButton,0.0);

        getChildren().addAll(devicePanelView, projectButton);
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
