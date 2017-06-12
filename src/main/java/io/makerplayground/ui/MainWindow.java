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

//    private AnchorPane panelWindow = new AnchorPane();
//    @FXML private VBox devicePanelPane;
//    @FXML private VBox projectButtonPane;
//    @FXML private Button configureBtn;
//    @FXML private Button generateBtn;
//    @FXML private Button uploadBtn;

    public MainWindow(Project project) {
        this.project = project;
        initView();
    }

    private void initView() {
        DevicePanelViewModel devicePanelViewModel = new DevicePanelViewModel(project);
        DevicePanelView devicePanelView = new DevicePanelView(devicePanelViewModel);
        RightPanel rightPanel =new RightPanel(project);

        SplitPane splitPane = new SplitPane();

        splitPane.getItems().addAll(new ScrollPane(),rightPanel);
        rightPanel.prefWidthProperty().bind(splitPane.prefWidthProperty());
        setCenter(splitPane);
        splitPane.setDividerPositions(0.8);
    }
}
