package io.makerplayground.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.device.GenericDevice;
import io.makerplayground.helper.*;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectHelper;
import io.makerplayground.ui.canvas.TutorialView;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.Effect;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

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
    private Button newButton;
    @FXML
    private Button tutorialButton;
    @FXML
    private AnchorPane toolBarPane;

    @FXML
    public void onTutorialButtonClick(){

    }

    private boolean flag = false; // for the first tutorial tracking

    @Override
    public void start(Stage primaryStage) throws Exception {
        // TODO: show progress indicator while loading if need
        DeviceLibrary.INSTANCE.loadDeviceFromJSON();
        ObjectMapper mapper = new ObjectMapper();

        MainWindow mainWindow = new MainWindow(new Project());

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(mainWindow);

        Scene scene = new Scene(borderPane, 800, 600);

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/ToolBar.fxml"));
        fxmlLoader.setRoot(borderPane);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // close program
        primaryStage.setOnCloseRequest(event -> {
            SingletonLaunch.getInstance().closeProgram();
        });


        newButton.setOnAction(event -> {
            try {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save File");
                fileChooser.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter("MakerPlayground Projects", "*.mp"),
                        new FileChooser.ExtensionFilter("All Files", "*.*"));
                File selectedFile = fileChooser.showSaveDialog(borderPane.getScene().getWindow());
                if (selectedFile != null) {
                    Project p = new Project();
                    MainWindow mw = new MainWindow(p);
                    borderPane.setCenter(mw);
                    mapper.writeValue(selectedFile, mainWindow.getProject());
                }
                SingletonUtilTools.getInstance().setAll("NEW");
            } catch (IOException x) {
                x.printStackTrace();
            }
        });

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
            SingletonUtilTools.getInstance().setAll("LOAD");
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
                SingletonUtilTools.getInstance().setAll("SAVE");
            } catch (IOException x) {
                x.printStackTrace();
            }
        });

        tutorialButton.setOnAction(event -> {
            if (flag) {
                flag = false;
            }
            else {
                Singleton.getInstance().setFlagFirstTime(false);
                SingletonTutorial.getInstance().clickCount();
                SingletonTutorial.getInstance().openTime();
            }

            TutorialView tutorialView = new TutorialView(scene.getWindow());

            Parent rootPane = scene.getRoot();
            Effect previousEffect = rootPane.getEffect();
            //final BoxBlur blur = new BoxBlur(0, 0, 5);
            final GaussianBlur blur = new GaussianBlur(0);
            blur.setInput(previousEffect);
            rootPane.setEffect(blur);

            tutorialView.setOnHidden(t -> rootPane.setEffect(previousEffect));

            // Optional extra: fade the blur and dialog in:
            //scene.getRoot().setOpacity(0);
            Timeline timeline = new Timeline(new KeyFrame(Duration.millis(300),
                    //new KeyValue(blur.widthProperty(), 10),
                    //new KeyValue(blur.heightProperty(), 10),
                    new KeyValue(blur.radiusProperty(), 7)
                    //new KeyValue(scene.getRoot().opacityProperty(), 0.75)
            ));
            timeline.play();

            tutorialView.show();
        });

        projectNameTextField.textProperty().bindBidirectional(mainWindow.getProject().projectNameProperty());

        primaryStage.setTitle("MakerPlayground");
        primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("/icons/logo_taskbar.png")));
        primaryStage.setScene(scene);
        primaryStage.show();

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(1000),
                ae -> {
                    Singleton.getInstance().setFlagFirstTime(true);
                    SingletonFirstTutorial.getInstance().openTime();
                    flag = true;
                    tutorialButton.fire();
                }));
        timeline.play();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
