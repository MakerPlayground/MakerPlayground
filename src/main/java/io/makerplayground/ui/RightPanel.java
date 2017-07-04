package io.makerplayground.ui;

import io.makerplayground.project.Project;
import io.makerplayground.ui.devicepanel.DevicePanelView;
import io.makerplayground.ui.devicepanel.DevicePanelViewModel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
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
        DevicePanelViewModel devicePanelViewModel = new DevicePanelViewModel(project);
        DevicePanelView devicePanelView = new DevicePanelView(devicePanelViewModel);
        Button configureBtn = new Button("Configure GenericDevice");
        Button generateBtn = new Button("Generate");
        Button uploadBtn = new Button("Upload");

        VBox projectButton = new VBox();
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
