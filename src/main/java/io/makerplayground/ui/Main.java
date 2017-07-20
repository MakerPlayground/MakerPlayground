package io.makerplayground.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.device.GenericDevice;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectHelper;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.IOException;

/**
 * Created by Nuntipat Narkthong on 6/6/2017 AD.
 */
public class Main extends Application {
    @FXML
    private TextField projectNameTextField;
    @FXML
    private Button saveButton;
    @FXML
    private Button loadButton;
    @FXML
    private AnchorPane toolBarPane;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // TODO: show progress indicator while loading if need
        DeviceLibrary.INSTANCE.loadDeviceFromJSON();
        ObjectMapper mapper = new ObjectMapper();

        MainWindow mainWindow = new MainWindow(new Project());

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(mainWindow);

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/ToolBar.fxml"));
        fxmlLoader.setRoot(borderPane);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        loadButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open File");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("MakerPlayground Projects", "*.mp"),
                    new FileChooser.ExtensionFilter("All Files", "*.*"));
            File selectedFile = fileChooser.showOpenDialog(primaryStage);
            if (selectedFile != null) {
                Project p = ProjectHelper.loadProject(selectedFile);
                MainWindow mw = new MainWindow(p);
                borderPane.setCenter(mw);
            }
        });

        saveButton.setOnAction(event -> {
            try {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save File");
                fileChooser.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter("MakerPlayground Projects", "*.mp"),
                        new FileChooser.ExtensionFilter("All Files", "*.*"));
                File selectedFile = fileChooser.showSaveDialog(borderPane.getScene().getWindow());
                if (selectedFile != null) {
                    mapper.writeValue(selectedFile, mainWindow.getProject());
                }
            } catch (IOException x) {
                x.printStackTrace();
            }
        });

        projectNameTextField.textProperty().bindBidirectional(mainWindow.getProject().projectNameProperty());

        primaryStage.setTitle("MakerPlayground");
        primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("/icons/logo_taskbar.png")));
        primaryStage.setScene(new Scene(borderPane, 800, 600));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
