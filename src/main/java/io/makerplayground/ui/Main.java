package io.makerplayground.ui;

import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.device.GenericDevice;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectHelper;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.IOException;

/**
 * Created by Nuntipat Narkthong on 6/6/2017 AD.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // TODO: show progress indicator while loading if need
        DeviceLibrary.INSTANCE.loadDeviceFromJSON();

        MainWindow mainWindow = new MainWindow(ProjectHelper.loadDummyProject());
        final Scene s = new Scene(mainWindow, 800, 600);

        EventHandler<ActionEvent> e = null;
        EventHandler<ActionEvent> finalE = e;
        e = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Open File");
                fileChooser.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter("MakerPlayground Projects", "*.mp"),
                        new FileChooser.ExtensionFilter("All Files", "*.*"));
                File selectedFile = fileChooser.showOpenDialog(primaryStage);
                if (selectedFile != null) {
                    Project p = ProjectHelper.loadProject(selectedFile);
                    MainWindow mw = new MainWindow(p);
                    mw.onLoadPressed(finalE);
                    s.setRoot(mw);
                }
            }
        };

        mainWindow.onLoadPressed(e);

        primaryStage.setTitle("MakerPlayground");
        primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("/icons/logo_taskbar.png")));
        primaryStage.setScene(s);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
