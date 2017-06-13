package io.makerplayground.ui;

import io.makerplayground.project.Project;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

/**
 *
 * Created by Nuntipat Narkthong on 6/6/2017 AD.
 */
public class MainWindow extends BorderPane {

    private final Project project;

    public MainWindow(Project project) {
        this.project = project;
        initView();
    }

    private void initView() {
//        DevicePanelViewModel devicePanelViewModel = new DevicePanelViewModel(project);
//        DevicePanelView devicePanelView = new DevicePanelView(devicePanelViewModel);
//
//        Button configureBtn = new Button("Configure Device");
//        //configureBtn.prefWidthProperty().bind((ObservableValue<? extends Number>) projectButton.widthProperty());
//        Button generateBtn = new Button("Generate");
//        //generateBtn.prefWidthProperty().bind((ObservableValue<? extends Number>) projectButton.widthProperty());
//        Button uploadBtn = new Button("Upload");
//        //uploadBtn.prefWidthProperty().bind((ObservableValue<? extends Number>) projectButton.widthProperty());
//
//        VBox projectButton = new VBox();
//        projectButton.getChildren().addAll(configureBtn, generateBtn, uploadBtn);
//        projectButton.setAlignment(Pos.CENTER);
//        projectButton.setPadding(new Insets(20.0,20.0,20.0,20.0));
//
//        getChildren().addAll(devicePanelView,projectButton);
//
//        AnchorPane.setLeftAnchor(devicePanelView,0.0);
//        AnchorPane.setRightAnchor(devicePanelView,0.0);
//        AnchorPane.setTopAnchor(devicePanelView,0.0);
//
//        AnchorPane.setLeftAnchor(projectButton,0.0);
//        AnchorPane.setRightAnchor(projectButton,0.0);
//        AnchorPane.setBottomAnchor(projectButton,0.0);


        RightPanel rightPanel = new RightPanel(project);

        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(new ScrollPane(), rightPanel);
        //rightPanel.prefWidthProperty().bind(splitPane.prefWidthProperty());
        setCenter(splitPane);
        splitPane.setDividerPositions(0.8);
    }
}
