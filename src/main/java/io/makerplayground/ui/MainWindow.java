package io.makerplayground.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.makerplayground.helper.Singleton;
import io.makerplayground.helper.SingletonLaunch;
import io.makerplayground.helper.SingletonUtilTools;
import io.makerplayground.project.Project;
import io.makerplayground.ui.canvas.CanvasView;
import io.makerplayground.ui.canvas.CanvasViewModel;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.TilePane;
import javafx.stage.FileChooser;
import javafx.stage.WindowEvent;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Nuntipat Narkthong on 6/6/2017 AD.
 */
public class MainWindow extends SplitPane {

    private final Project project;

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
        CanvasView canvasView = new CanvasView(canvasViewModel);

        setDividerPositions(0.8, 0.2);
        getItems().addAll(canvasView, rightPanel);
    }

    public Project getProject() {
        return project;
    }
}
