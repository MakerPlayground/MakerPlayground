package io.makerplayground.ui;

import io.makerplayground.project.Project;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

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

        // TODO: we are in great danger
        StateViewModel stateViewModel = new StateViewModel(project.getDiagram().vertexSet().iterator().next());
        StateView stateView = new StateView(stateViewModel);

        Pane p = new Pane();
        p.setPrefWidth(500);
        p.setPrefHeight(500);
        p.getChildren().add(stateView);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(p);

        AnchorPane anchorpane = new AnchorPane();
        Button btnAddState = new Button("Add State");
        anchorpane.getChildren().addAll(btnAddState);

        SplitPane mainPane = new SplitPane();
        mainPane.setOrientation(Orientation.HORIZONTAL);
        mainPane.getItems().addAll(scrollPane, devicePanelView);
        mainPane.setDividerPositions(0.8);

        setCenter(mainPane);
    }
}
