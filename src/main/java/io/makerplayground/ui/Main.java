package io.makerplayground.ui;

import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.project.Project;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * Created by Nuntipat Narkthong on 6/6/2017 AD.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // TODO: to be removed
        Project dummyProject = new Project();
        dummyProject.addOutputDevice(DeviceLibrary.INSTANCE.getOutputDevice("led"));
        dummyProject.addOutputDevice(DeviceLibrary.INSTANCE.getOutputDevice("speaker"));
        dummyProject.addState();

        primaryStage.setTitle("Maker Playground");
        primaryStage.setScene(new Scene(new MainWindow(dummyProject), 800, 600));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
