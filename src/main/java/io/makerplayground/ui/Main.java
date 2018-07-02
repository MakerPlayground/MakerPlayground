package io.makerplayground.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fazecast.jSerialComm.SerialPort;
import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.helper.Singleton;
import io.makerplayground.helper.SingletonConnectDB;
import io.makerplayground.helper.SingletonTutorial;
import io.makerplayground.helper.SingletonUtilTools;
import io.makerplayground.project.Project;
import io.makerplayground.ui.canvas.TutorialView;
import io.makerplayground.version.ProjectVersionControl;
import io.makerplayground.version.SoftwareVersionControl;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import org.controlsfx.control.action.Action;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
    private Button saveAsButton;
    @FXML
    private Button loadButton;
    @FXML
    private Button newButton;
    @FXML
    private Button deviceMonitorButton;
    @FXML
    private Button tutorialButton;
    @FXML
    private AnchorPane toolBarPane;
    @FXML
    private Button hpl;

    private Project project;
    private BorderPane borderPane;
    private Timer timer = new Timer();
    private ObjectMapper mapper = new ObjectMapper();
    private ChangeListener<String> projectPathListener;
    private File latestProjectDirectory;

    private boolean flag = false; // for the first tutorial tracking

    @Override
    public void start(Stage primaryStage) throws Exception {
        SingletonConnectDB.getINSTANCE();

        // TODO: show progress indicator while loading if need
        DeviceLibrary.INSTANCE.loadDeviceFromJSON();

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

        projectPathListener = (observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                primaryStage.setTitle(SoftwareVersionControl.CURRENT_BUILD_NAME + " - Untitled Project");
            } else {
                primaryStage.setTitle(SoftwareVersionControl.CURRENT_BUILD_NAME + " - " + project.getFilePath());
            }
        };

        project.filePathProperty().addListener(projectPathListener);
        updatePathTextField(primaryStage);

        // close program
        primaryStage.setOnCloseRequest(event -> {
            if (project.hasUnsavedModification()) {
                ButtonType retVal = showConfirmationDialog();
                if (retVal.getButtonData() == ButtonBar.ButtonData.CANCEL_CLOSE) {
                    event.consume();
                    return;
                } else if (retVal.getButtonData() == ButtonBar.ButtonData.YES) {
                    saveProject();
                }
            }

            primaryStage.close();
            Platform.exit();
            System.exit(0);
        });

        projectNameTextField.setText(project.getProjectName());
        projectNameTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                project.setProjectName(projectNameTextField.getText());
            } else {
                projectNameTextField.setText(project.getProjectName());
            }
        });

        // setup keyboard shortcut for new, save and load
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.isShortcutDown() && event.getCode() == KeyCode.O) {
                loadProject(primaryStage);
            } else if (event.isShortcutDown() && event.getCode() == KeyCode.N) {
                newProject(primaryStage);
            } else if (event.isShortcutDown() && event.getCode() == KeyCode.S) {
                saveProject();
            }
        });

        newButton.setOnAction(event -> newProject(primaryStage));
        loadButton.setOnAction(event -> loadProject(primaryStage));
        saveButton.setOnAction(event -> saveProject());
        saveAsButton.setOnAction(event -> saveProjectAs());
        deviceMonitorButton.setOnAction(event -> deviceMonitor());

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

        primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("/icons/logo_taskbar.png")));
        primaryStage.setScene(scene);
        primaryStage.show();

        new Thread(() -> {
            SoftwareVersionControl.getLatestVersionInfo().ifPresent(version -> {
                if (!version.getVersionString().equals(SoftwareVersionControl.CURRENT_VERSION)) {
                    Platform.runLater(() -> {
                        Action navigate = new Action("Download", actionEvent -> {
                            try {
                                Desktop.getDesktop().browse(new URI(version.getDownloadURL()));
                            } catch (IOException | URISyntaxException e) {
                                e.printStackTrace();
                            }
                        });
                        Notifications.create()
                                .title(version.getBuildName() + " has been released")
                                .text("Update now?")
                                .owner(mainWindow.getCanvasView())
                                .action(navigate)
                                .hideAfter(Duration.seconds(10))
                                .show();
                    });
                }
            });
        }).start();

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

    private void updatePathTextField(Stage primaryStage) {
        if (project.getFilePath().isEmpty()) {
            primaryStage.setTitle(SoftwareVersionControl.CURRENT_BUILD_NAME + " - Untitled Project");
        } else {
            primaryStage.setTitle(SoftwareVersionControl.CURRENT_BUILD_NAME + " - " + project.getFilePath());
        }
    }

    private ButtonType showConfirmationDialog() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Current project is modified");
        alert.setContentText("Save?");
        alert.getButtonTypes().setAll(new ButtonType("Yes", ButtonBar.ButtonData.YES)
                , new ButtonType("No", ButtonBar.ButtonData.NO)
                , new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE));
        return alert.showAndWait().get();
    }

    private void newProject(Stage primaryStage) {
        if (project.hasUnsavedModification()) {
            ButtonType retVal = showConfirmationDialog();
            if (retVal.getButtonData() == ButtonBar.ButtonData.CANCEL_CLOSE) {
                return;
            } else if (retVal.getButtonData() == ButtonBar.ButtonData.YES) {
                saveProject();
            }
        }

        projectNameTextField.textProperty().unbindBidirectional(project.projectNameProperty());
        project.filePathProperty().removeListener(projectPathListener);
        project = new Project();
        project.filePathProperty().addListener(projectPathListener);
        projectNameTextField.textProperty().bindBidirectional(project.projectNameProperty());
        MainWindow mw = new MainWindow(project);
        borderPane.setCenter(mw);
        updatePathTextField(primaryStage);
    }

    private void loadProject(Stage primaryStage) {
        if (project.hasUnsavedModification()) {
            ButtonType retVal = showConfirmationDialog();
            if (retVal.getButtonData() == ButtonBar.ButtonData.CANCEL_CLOSE) {
                return;
            } else if (retVal.getButtonData() == ButtonBar.ButtonData.YES) {
                saveProject();
            }
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("MakerPlayground Projects", "*.mp"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));
        File selectedFile = fileChooser.showOpenDialog(primaryStage);
        if (selectedFile != null) {
//            read projectVersion from selectedFile
            boolean canLoad = false;
            String projectVersion = ProjectVersionControl.readProjectVersion(selectedFile);
            if (ProjectVersionControl.CURRENT_VERSION.equals(projectVersion)) {
                canLoad = true;
            }
            else if (ProjectVersionControl.isConvertibleToCurrentVersion(projectVersion)) {
                /* TODO: ask user to convert file */
                ProjectVersionControl.convertToCurrentVersion(selectedFile);
                canLoad = true;
            }
            if (!canLoad){
                (new Alert(Alert.AlertType.ERROR, "The program does not support this previous project version.", ButtonType.OK)).showAndWait();
                return;
            }
            else {
                projectNameTextField.textProperty().unbindBidirectional(project.projectNameProperty());
                project.filePathProperty().removeListener(projectPathListener);
                project = Project.loadProject(selectedFile);
                project.filePathProperty().addListener(projectPathListener);
                projectNameTextField.textProperty().bindBidirectional(project.projectNameProperty());
                MainWindow mw = new MainWindow(project);
                borderPane.setCenter(mw);
                updatePathTextField(primaryStage);
            }

        }
    }

    private void saveProject() {
        statusLabel.setText("Saving...");
        try {
            File selectedFile;
            if (project.getFilePath().isEmpty()) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save File");
                if (latestProjectDirectory != null) {
                    fileChooser.setInitialDirectory(latestProjectDirectory);
                }
                fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("MakerPlayground Projects", "*.mp"));
                fileChooser.setInitialFileName("*.mp");
                selectedFile = fileChooser.showSaveDialog(borderPane.getScene().getWindow());
            } else {
                selectedFile = new File(project.getFilePath());
            }

            if (selectedFile != null) {
                latestProjectDirectory = selectedFile.getParentFile();
                mapper.writeValue(selectedFile, project);
                project.setFilePath(selectedFile.getAbsolutePath());
                statusLabel.setText("Saved");
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> statusLabel.setText(""));
                    }
                }, 3000);
            } else {
                statusLabel.setText("");
            }
        } catch (IOException x) {
            x.printStackTrace();
        }
    }

    private void saveProjectAs() {
        statusLabel.setText("Saving...");
        try {
            File selectedFile;
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save File As");
            if (latestProjectDirectory != null) {
                fileChooser.setInitialDirectory(latestProjectDirectory);
            }
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("MakerPlayground Projects", "*.mp"));
            fileChooser.setInitialFileName("*.mp");
            selectedFile = fileChooser.showSaveDialog(borderPane.getScene().getWindow());

            if (selectedFile != null) {
                latestProjectDirectory = selectedFile.getParentFile();
                mapper.writeValue(selectedFile, project);
                project.setFilePath(selectedFile.getAbsolutePath());
                statusLabel.setText("Saved");
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> statusLabel.setText(""));
                    }
                }, 3000);
            } else {
                statusLabel.setText("");
            }
        } catch (IOException x) {
            x.printStackTrace();
        }
    }
    private void deviceMonitor(){
            if (SerialPort.getCommPorts().length > 0) {
                DeviceMonitor deviceMonitor = new DeviceMonitor(project);
                deviceMonitor.showAndWait();
            }
            else {
                ErrorDialogView errorDialogView = new ErrorDialogView("There is no connected serial port.\nPlease connect the board with computer.");
                errorDialogView.showAndWait();
            }
        }


    public static void main(String[] args) {
        launch(args);
    }
}
