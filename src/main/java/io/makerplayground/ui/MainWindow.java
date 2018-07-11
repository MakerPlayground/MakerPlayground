package io.makerplayground.ui;

import io.makerplayground.helper.SingletonLaunch;
import io.makerplayground.project.Project;
import io.makerplayground.ui.canvas.CanvasView;
import io.makerplayground.ui.canvas.CanvasViewModel;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.SplitPane;

import java.io.IOException;

/**
 * Created by Nuntipat Narkthong on 6/6/2017 AD.
 */
public class MainWindow extends SplitPane {

    private final Project project;
    private CanvasView canvasView;

    public MainWindow(Project project) {
        this.project = project;
        SingletonLaunch.getInstance().launchProgram();
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
        canvasView = new CanvasView(canvasViewModel);

        getItems().addAll(canvasView, rightPanel);
    }

    public Project getProject() {
        return project;
    }

    public CanvasView getCanvasView() {
        return canvasView;
    }
}
