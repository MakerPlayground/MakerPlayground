package io.makerplayground.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.makerplayground.project.Project;
import io.makerplayground.ui.canvas.CanvasView;
import io.makerplayground.ui.canvas.CanvasViewModel;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.TilePane;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

/**
 * Created by Nuntipat Narkthong on 6/6/2017 AD.
 */
public class MainWindow extends BorderPane {
    @FXML
    private SplitPane mainPane;
    @FXML
    private TextField projectNameTextField;
    @FXML
    private Button saveButton;
    @FXML
    private Button loadButton;
    private final Project project;

    public MainWindow(Project project) {
        this.project = project;
        initView();
    }

    private void initView() {
        ObjectMapper mapper = new ObjectMapper();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/MainWindow.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        saveButton.setOnAction(event -> {
            try {
                mapper.writeValue(new File("C:\\Users\\USER\\Desktop\\file.json"), project);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        projectNameTextField.textProperty().bindBidirectional(project.projectNameProperty());

        RightPanel rightPanel = new RightPanel(project);

        CanvasViewModel canvasViewModel = new CanvasViewModel(project);
        CanvasView canvasView = new CanvasView(canvasViewModel);

        mainPane.setDividerPositions(0.8, 0.2);
        mainPane.getItems().addAll(canvasView, rightPanel);
    }

    public void onLoadPressed(EventHandler<javafx.event.ActionEvent> e) {
        loadButton.setOnAction(e);
    }
}
