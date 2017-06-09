package io.makerplayground.ui;

import io.makerplayground.project.Project;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

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
        DevicePanelViewModel devicePanelViewModel = new DevicePanelViewModel(project);
        DevicePanelView devicePanelView = new DevicePanelView(devicePanelViewModel);
        AnchorPane devicePanelAndSpecialBtnPane = new AnchorPane();

        VBox specialButtonPane = new VBox(5);
            Button configureDeviceBtn = new Button("Configure Device");
            configureDeviceBtn.prefWidthProperty().bind((ObservableValue<? extends Number>) specialButtonPane.widthProperty());
            Button generateBtn = new Button("Generate");
            generateBtn.prefWidthProperty().bind(specialButtonPane.widthProperty());
            Button upload = new Button("Upload");
            upload.prefWidthProperty().bind((ObservableValue<? extends Number>) specialButtonPane.widthProperty());
            specialButtonPane.setPadding(new Insets(20.0,20.0,20.0,20.0));
        specialButtonPane.getChildren().addAll(configureDeviceBtn,generateBtn,upload);
        specialButtonPane.setAlignment(Pos.BOTTOM_CENTER);

        AnchorPane.setBottomAnchor(specialButtonPane,0.0);
        AnchorPane.setRightAnchor(specialButtonPane,0.0);
        AnchorPane.setLeftAnchor(specialButtonPane,0.0);
        AnchorPane.setTopAnchor(devicePanelView,0.0);
        AnchorPane.setRightAnchor(devicePanelView,0.0);
        AnchorPane.setLeftAnchor(devicePanelView,0.0);
        devicePanelAndSpecialBtnPane.getChildren().addAll(devicePanelView,specialButtonPane);

        SplitPane mainPane = new SplitPane();
        mainPane.setOrientation(Orientation.HORIZONTAL);
        mainPane.getItems().addAll(new ScrollPane(), devicePanelAndSpecialBtnPane);

        mainPane.setDividerPositions(0.8);

        setCenter(mainPane);
    }
}
