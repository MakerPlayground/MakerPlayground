package io.makerplayground.ui;

import io.makerplayground.project.Project;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
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

        CanvasViewModel canvasViewModel = new CanvasViewModel(project);
        CanvasView canvasView = new CanvasView(canvasViewModel);

        // TODO: we are in great danger
//        StateViewModel stateViewModel = new StateViewModel(project.getDiagram().vertexSet().iterator().next());
//        StateView stateView = new StateView(stateViewModel);
//
//        Pane p = new Pane();
//        p.setPrefWidth(500);
//        p.setPrefHeight(500);
//        p.getChildren().add(stateView);
//
//
//        Button btnAddState = new Button("Add State");
//        btnAddState.setOnAction((event) -> {
//            // Button was clicked, do something...
//            project.addState();
//        });
//
//        HBox hb = new HBox();
//        hb.setPadding(new Insets(0, 10, 10, 10));
//        hb.setSpacing(10);
//        hb.getChildren().addAll(btnAddState);
//
//        AnchorPane anchorpane = new AnchorPane();
//        anchorpane.getChildren().addAll(hb);   // Add grid from Example 1-5
//        AnchorPane.setTopAnchor(hb, 8.0);
//        AnchorPane.setRightAnchor(hb, 5.0);
//
//        ScrollPane scrollPane = new ScrollPane();
//        scrollPane.setContent(p);

        SplitPane mainPane = new SplitPane();
        mainPane.setOrientation(Orientation.HORIZONTAL);
        mainPane.getItems().addAll(canvasView ,devicePanelView);
        mainPane.setDividerPositions(0.8);

        setCenter(mainPane);
    }
}
