package io.makerplayground.ui;

import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.device.GenericDevice;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectHelper;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * Created by Nuntipat Narkthong on 6/6/2017 AD.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        primaryStage.setTitle("MakerPlayground");
        primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("/icons/logo_taskbar.png")));
        primaryStage.setScene(new Scene(new MainWindow(ProjectHelper.loadDummyProject()), 800, 600));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
