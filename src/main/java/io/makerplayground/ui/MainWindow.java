package io.makerplayground.ui;

import io.makerplayground.project.Project;
import io.makerplayground.ui.canvas.CanvasView;
import io.makerplayground.ui.canvas.CanvasViewModel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.TilePane;

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
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/MainWindow.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        RightPanel rightPanel = new RightPanel(project);

        CanvasViewModel canvasViewModel = new CanvasViewModel(project);
        CanvasView canvasView = new CanvasView(canvasViewModel);

        SplitPane mainPane = new SplitPane();
        mainPane.setStyle("-fx-box-border: transparent;");
        mainPane.setOrientation(Orientation.HORIZONTAL);
        mainPane.getItems().addAll(canvasView, rightPanel);
        mainPane.setDividerPositions(0.8);

        setCenter(mainPane);
    }
}
