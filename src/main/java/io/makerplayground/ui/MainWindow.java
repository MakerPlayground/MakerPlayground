package io.makerplayground.ui;

import io.makerplayground.project.Project;
import io.makerplayground.ui.canvas.CanvasView;
import io.makerplayground.ui.canvas.CanvasViewModel;
import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;

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
        RightPanel rightPanel = new RightPanel(project);

        CanvasViewModel canvasViewModel = new CanvasViewModel(project);
        CanvasView canvasView = new CanvasView(canvasViewModel);

        SplitPane mainPane = new SplitPane();
        mainPane.setOrientation(Orientation.HORIZONTAL);
        mainPane.getItems().addAll(canvasView, rightPanel);
        mainPane.setDividerPositions(0.8);

        setCenter(mainPane);
    }
}
