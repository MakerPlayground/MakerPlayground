package io.makerplayground.ui;

import io.makerplayground.generator.Diagram;
import io.makerplayground.generator.validGenericDevice;
import io.makerplayground.project.Project;
import io.makerplayground.ui.devicepanel.ConfigActualDeviceView;
import io.makerplayground.ui.devicepanel.ConfigActualDeviceViewModel;
import io.makerplayground.ui.devicepanel.DevicePanelView;
import io.makerplayground.ui.devicepanel.DevicePanelViewModel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.io.File;

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
        });

        //configureBtn.setOnAction(event -> validGenericDevice.getSupportedDeviceList(project));

        Button generateBtn = new Button("Generate Project");
        generateBtn.setOnAction(event -> {
            Dialog dialog = new Dialog();
            DialogPane dialogPane = dialog.getDialogPane();
            dialogPane.setContent(new Diagram(project));
            dialog.showAndWait();
        });
        Button uploadBtn = new Button("Upload");

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
