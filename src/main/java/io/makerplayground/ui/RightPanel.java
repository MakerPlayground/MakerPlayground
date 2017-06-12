package io.makerplayground.ui;


import io.makerplayground.project.Project;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

/**
 * Created by Mai.Manju on 12-Jun-17.
 */
public class RightPanel extends AnchorPane {
    private final Project project;

//    @FXML private AnchorPane panelWindow ;
//    @FXML private VBox devicePanelPane;
//    @FXML private VBox projectButtonPane;
//    @FXML private Button configureBtn;
//    @FXML private Button generateBtn;
//    @FXML private Button uploadBtn;
    public RightPanel(Project project){
        this.project = project;
        initView();
    }
    private void initView() {
        DevicePanelViewModel devicePanelViewModel = new DevicePanelViewModel(project);
        DevicePanelView devicePanelView = new DevicePanelView(devicePanelViewModel);
        VBox projectButton = new VBox();
            Button configureBtn = new Button("Configure Device");
            configureBtn.prefWidthProperty().bind((ObservableValue<? extends Number>) projectButton.widthProperty());
            Button generateBtn = new Button("Generate");
            generateBtn.prefWidthProperty().bind((ObservableValue<? extends Number>) projectButton.widthProperty());
            Button uploadBtn = new Button("Upload");
            uploadBtn.prefWidthProperty().bind((ObservableValue<? extends Number>) projectButton.widthProperty());

        projectButton.getChildren().addAll(configureBtn,generateBtn,uploadBtn);
        projectButton.setAlignment(Pos.CENTER);
        projectButton.setPadding(new Insets(20.0,20.0,20.0,20.0));

        getChildren().addAll(devicePanelView,projectButton);
        AnchorPane.setLeftAnchor(devicePanelView,0.0);
        AnchorPane.setRightAnchor(devicePanelView,0.0);
        AnchorPane.setTopAnchor(devicePanelView,0.0);
        AnchorPane.setLeftAnchor(projectButton,0.0);
        AnchorPane.setRightAnchor(projectButton,0.0);
        AnchorPane.setBottomAnchor(projectButton,0.0);
    }
}
