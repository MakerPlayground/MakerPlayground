package io.makerplayground.ui;

import java.util.ArrayList;
import java.util.List;
import io.makerplayground.generator.DeviceMapper;
import io.makerplayground.generator.Sourcecode;
import io.makerplayground.project.Project;
import io.makerplayground.ui.devicepanel.ConfigActualDeviceView;
import io.makerplayground.ui.devicepanel.ConfigActualDeviceViewModel;
import io.makerplayground.ui.devicepanel.DevicePanelView;
import io.makerplayground.ui.devicepanel.DevicePanelViewModel;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.stream.Collectors;

/**
 * Created by Mai.Manju on 12-Jun-17.
 */
public class RightPanel extends AnchorPane {
    private final Project project;

    public RightPanel(Project project){
        this.project = project;
        File f = new File("src/main/resources/css/RightPanel.css");
        getStylesheets().add("file:///" + f.getAbsolutePath().replace("\\", "/"));
        initView();
    }

    private void initView() {
        setStyle("-fx-background-color : #4d5666");
        setMaxWidth(330.0);
        DevicePanelViewModel devicePanelViewModel = new DevicePanelViewModel(project);
        DevicePanelView devicePanelView = new DevicePanelView(devicePanelViewModel);
        Button configureBtn = new Button("Configure Device");

        configureBtn.setOnAction(event -> {
            ConfigActualDeviceViewModel configActualDeviceViewModel = new ConfigActualDeviceViewModel(project);
            ConfigActualDeviceView configActualDeviceView = new ConfigActualDeviceView(configActualDeviceViewModel);
            configActualDeviceView.showAndWait();
        });

        //configureBtn.setOnAction(event -> DeviceMapper.getSupportedDeviceList(project));

        Button generateBtn = new Button("Generate Project");
        generateBtn.setOnAction(event -> {
            DeviceMapper.autoAssignDevices(project);
            Sourcecode code = Sourcecode.generateCode(project);
            if (code.getError() != null) {
                Alert alert = new Alert(Alert.AlertType.ERROR, code.getError().getDescription(), ButtonType.OK);
                alert.showAndWait();
            } else {
                GenerateViewModel generateViewModel = new GenerateViewModel(project, code);
                GenerateView generateView = new GenerateView(generateViewModel);
                generateView.showAndWait();
            }
        });
        Button uploadBtn = new Button("Upload");
        uploadBtn.setOnAction((ActionEvent event) -> {
            Sourcecode sourcecode = Sourcecode.generateCode(project);
            if (sourcecode.getError() != null) {
                Alert alert = new Alert(Alert.AlertType.ERROR, sourcecode.getError().getDescription(), ButtonType.OK);
                alert.showAndWait();
            } else {
                String code = sourcecode.getCode();
                List<String> library = project.getAllDeviceTypeUsed().stream()
                        .map(genericDevice -> "MP_" + genericDevice.getName().replace(" ", "_"))
                        .collect(Collectors.toList());
                // TODO: call platform io here
                System.out.println(code);
                System.out.println(library);
            }
        });

        VBox projectButton = new VBox();
        projectButton.setStyle("-fx-background-color : #313644");
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
}
