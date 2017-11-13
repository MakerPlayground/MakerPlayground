package io.makerplayground.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.helper.*;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectHelper;
import io.makerplayground.ui.canvas.TutorialView;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.Effect;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Nuntipat Narkthong on 6/6/2017 AD.
 */
public class Main extends Application {
    @FXML
    private TextField projectNameTextField;
    @FXML
    private Label statusLabel;
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
    private Button hpl;

    private Project project;
    private BorderPane borderPane;
    private Timer timer;
    private ObjectMapper mapper;

    @FXML
    public void onTutorialButtonClick(){
    }

    private boolean flag = false; // for the first tutorial tracking

    @Override
    public void start(Stage primaryStage) throws Exception {
        SingletonConnectDB.getINSTANCE();

        // TODO: show progress indicator while loading if need
        DeviceLibrary.INSTANCE.loadDeviceFromJSON();
        mapper = new ObjectMapper();

        project = new Project();
        MainWindow mainWindow = new MainWindow(project);

        borderPane = new BorderPane();
        borderPane.setCenter(mainWindow);

        final Scene scene = new Scene(borderPane, 800, 600);

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/ToolBar.fxml"));
        fxmlLoader.setRoot(borderPane);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Write to azure every 10 minutes
        timer = new Timer();
//        timer.scheduleAtFixedRate(new TimerTask() {
//            public void run() {
//                try {
//                    File selectedFile;
//                    if (project.getFilePath() != null) {
//                        selectedFile = new File(project.getFilePath());
//                        mapper.writeValue(selectedFile, project);
//                        SingletonUtilTools.getInstance().setAll("SAVE");
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }, 10000, 10000);

        // close program
//        primaryStage.setOnCloseRequest(event -> {
//            SingletonLaunch.getInstance().closeProgram();
//
//            StringWriter stringWrite = new StringWriter();
//            try {
//                mapper.writeValue(stringWrite, mainWindow.getProject());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            timer.cancel();
//            //String s = stringWrite.toString();
//            //SingletonGraphJson.getInstance().setAll(s);
//            SingletonConnectDB.getINSTANCE().close();
//        });

        projectNameTextField.setText(project.getProjectName());
        projectNameTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                project.setProjectName(projectNameTextField.getText());
            } else {
                projectNameTextField.setText(project.getProjectName());
            }
        });

        newButton.setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Current project is modified");
            alert.setContentText("Save?");
            ButtonType okButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
            ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.NO);
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(okButton, noButton, cancelButton);
            alert.showAndWait().ifPresent(type -> {
                if (type.getButtonData() != ButtonBar.ButtonData.CANCEL_CLOSE) {
                    if (type.getButtonData() == ButtonBar.ButtonData.YES) {
                        saveProject();
                        project.setFilePath("");
                        System.out.println(project.getFilePath());
                    }

                    projectNameTextField.textProperty().unbindBidirectional(project.projectNameProperty());
                    project = new Project();
                    projectNameTextField.textProperty().bindBidirectional(project.projectNameProperty());
                    MainWindow mw = new MainWindow(project);
                    borderPane.setCenter(mw);
                    SingletonUtilTools.getInstance().setAll("NEW");
                }
            });
        });

        loadButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open File");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("MakerPlayground Projects", "*.mp"),
                    new FileChooser.ExtensionFilter("All Files", "*.*"));
            File selectedFile = fileChooser.showOpenDialog(primaryStage);
            if (selectedFile != null) {
                projectNameTextField.textProperty().unbindBidirectional(project.projectNameProperty());
                project = ProjectHelper.loadProject(selectedFile);
                project.setFilePath(selectedFile.getAbsolutePath());
                projectNameTextField.textProperty().bindBidirectional(project.projectNameProperty());
                MainWindow mw = new MainWindow(project);
                borderPane.setCenter(mw);
            }
            SingletonUtilTools.getInstance().setAll("LOAD");
        });

        saveButton.setOnAction((ActionEvent event) -> {
            saveProject();
        });

        tutorialButton.setOnAction(event -> {
            if (flag) {
                flag = false;
            }
            else {
                Singleton.getInstance().setFlagFirstTime(false);
                SingletonTutorial.getInstance().clickCount();
                SingletonTutorial.getInstance().setIsClick(1);
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

        project.filePathProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("old Value : " + oldValue);
            System.out.println("new : " +newValue);
            if (newValue.isEmpty()) {
                primaryStage.setTitle("MakerPlayground - Untitled Project");
            } else {
                primaryStage.setTitle("MakerPlayground - " + project.getFilePath());
            }
        });
        if (project.getFilePath().isEmpty()) {
            primaryStage.setTitle("MakerPlayground - Untitled Project");
        } else {
            primaryStage.setTitle("MakerPlayground - " + project.getFilePath());
        }
        primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("/icons/logo_taskbar.png")));
        primaryStage.setScene(scene);
        primaryStage.show();

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(1000),
                ae -> {
                    Singleton.getInstance().setFlagFirstTime(true);
                    SingletonTutorial.getInstance().setIsClick(0);
                    SingletonTutorial.getInstance().openTime();
                    flag = true;
                    tutorialButton.fire();
                }));
        timeline.play();

        hpl.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {

                SingletonUtilTools.getInstance().setAll("FEEDBACK");

                String s = "https://goo.gl/forms/NrXDr2z1Q3RwdSU92";
                Desktop desktop = Desktop.getDesktop();
                try {
                    desktop.browse(URI.create(s));
                } catch (IOException ev) {
                    ev.printStackTrace();
                }
            }
        });

    }

    private void saveProject() {
        statusLabel.setText("Saving...");
        System.out.println("saveeeeefaasdasdasd");
        try {
            File selectedFile;
            if (project.getFilePath().isEmpty()) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save File");
                fileChooser.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter("MakerPlayground Projects", "*.mp"),
                        new FileChooser.ExtensionFilter("All Files", "*.*"));
                selectedFile = fileChooser.showSaveDialog(borderPane.getScene().getWindow());
                System.out.println("selectFile : " + selectedFile);
            } else {
                selectedFile = new File(project.getFilePath());
            }

            if (selectedFile != null) {
                mapper.writeValue(selectedFile, project);
                System.out.println("file before: " + project.getFilePath());
                System.out.println("1");
                System.out.println("selectFile.AbsolutePath " + selectedFile.getAbsolutePath()
                );
                project.setFilePath(selectedFile.getAbsolutePath());
                System.out.println("2");
                System.out.println("file : " + project.getFilePath());
                statusLabel.setText("Saved");
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> statusLabel.setText(""));
                    }
                }, 3000);
                SingletonUtilTools.getInstance().setAll("SAVE");
            } else {
                statusLabel.setText("");
            }
        } catch (IOException x) {
            x.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
