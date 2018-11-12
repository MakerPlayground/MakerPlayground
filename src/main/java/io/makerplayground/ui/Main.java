/*
 * Copyright (c) 2018. The Maker Playground Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.makerplayground.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fazecast.jSerialComm.SerialPort;
import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.project.Project;
import io.makerplayground.ui.dialog.DeviceMonitor;
import io.makerplayground.ui.dialog.UnsavedDialog;
import io.makerplayground.ui.dialog.tutorial.TutorialView;
import io.makerplayground.version.ProjectVersionControl;
import io.makerplayground.version.SoftwareVersion;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.effect.Effect;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

import java.io.File;
import java.io.IOException;
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
    private MenuButton deviceMonitorMenuButton;
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
        // TODO: show progress indicator while loading if need
        DeviceLibrary.INSTANCE.loadDeviceFromJSON();

        project = new Project();
        MainWindow mainWindow = new MainWindow(project);

        borderPane = new BorderPane();
        borderPane.setCenter(mainWindow);

        final Scene scene = new Scene(borderPane, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());

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
                primaryStage.setTitle(SoftwareVersion.CURRENT_VERSION.getBuildName() + " - Untitled Project");
            } else {
                primaryStage.setTitle(SoftwareVersion.CURRENT_VERSION.getBuildName() + " - " + project.getFilePath());
            }
        };

        project.filePathProperty().addListener(projectPathListener);
        updatePathTextField(primaryStage);

        // close program
        primaryStage.setOnCloseRequest(event -> {
            if (project.hasUnsavedModification()) {
                UnsavedDialog.Response retVal = showConfirmationDialog();
                if (retVal == UnsavedDialog.Response.CANCEL) {
                    event.consume();
                    return;
                } else if (retVal == UnsavedDialog.Response.SAVE) {
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
        deviceMonitorMenuButton.setOnShowing(this::deviceMonitorMenuShowing);

        tutorialButton.setOnAction(event -> {
            if (flag) {
                flag = false;
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
            SoftwareVersion.getLatestVersionInfo().ifPresent(version -> {
                if (version.compareTo(SoftwareVersion.CURRENT_VERSION) > 0) {
                    Platform.runLater(() -> {
                        ImageView icon = new ImageView(new Image(getClass().getResource("/icons/download-2.png").toExternalForm()));
                        icon.setFitWidth(50);
                        icon.setPreserveRatio(true);

                        Text text = new Text(version.getBuildName() + " has been released");
                        text.setId("text");
                        text.setWrappingWidth(250);

                        Button button = new Button("Download Now");
                        button.setId("UpdateButton");
                        button.setOnAction(event -> getHostServices().showDocument(version.getDownloadURL()));

                        VBox vBox = new VBox();
                        vBox.setSpacing(20);
                        vBox.setAlignment(Pos.TOP_CENTER);
                        vBox.getChildren().addAll(text, button);

                        HBox mainPane = new HBox();
                        mainPane.setPadding(new Insets(10));
                        mainPane.setSpacing(20);
                        mainPane.getStylesheets().add(getClass().getResource("/css/UpdateNotificationDialog.css").toExternalForm());
                        mainPane.setPrefSize(300, 80);
                        mainPane.getChildren().addAll(icon, vBox);

                        Notifications.create()
                                .graphic(mainPane)
                                .owner(mainWindow.getCanvasView())
                                .hideAfter(Duration.seconds(5))
                                .show();
                    });
                }
            });
        }).start();

//        hpl.setOnAction(new EventHandler<ActionEvent>() {
//            @Override
//            public void handle(ActionEvent e) {
//
//                SingletonUtilTools.getInstance().setAll("FEEDBACK");
//
//                String s = "https://goo.gl/forms/NrXDr2z1Q3RwdSU92";
//                Desktop desktop = Desktop.getDesktop();
//                try {
//                    desktop.browse(URI.create(s));
//                } catch (IOException ev) {
//                    ev.printStackTrace();
//                }
//            }
//        });

    }

    private void deviceMonitorMenuShowing(Event e) {
        MenuButton deviceMonitorButton = (MenuButton) e.getSource();
        deviceMonitorButton.getItems().clear();
        SerialPort[] commPorts = SerialPort.getCommPorts();
        if (commPorts.length > 0) {
            for ( SerialPort port: commPorts){
                MenuItem item = new MenuItem(port.getDescriptivePortName());
                // runLater to make sure that the menuitem is disappeared before open the DeviceMonitor
                item.setOnAction(event -> Platform.runLater(() -> openDeviceMonitor(port.getSystemPortName())));
                deviceMonitorButton.getItems().add(item);
            }
        }
        else {
            MenuItem item = new MenuItem("No connected serial port found.\nPlease connect the board with computer.");
            item.setDisable(true);
            deviceMonitorButton.getItems().add(item);
        }
    }

    private void updatePathTextField(Stage primaryStage) {
        if (project.getFilePath().isEmpty()) {
            primaryStage.setTitle(SoftwareVersion.CURRENT_VERSION.getBuildName() + " - Untitled Project");
        } else {
            primaryStage.setTitle(SoftwareVersion.CURRENT_VERSION.getBuildName() + " - " + project.getFilePath());
        }
    }

    private UnsavedDialog.Response showConfirmationDialog() {
        UnsavedDialog dialog = new UnsavedDialog(borderPane.getScene().getWindow());
        return dialog.showAndGetResponse();
    }

    private void newProject(Stage primaryStage) {
        if (project.hasUnsavedModification()) {
            UnsavedDialog.Response retVal = showConfirmationDialog();
            if (retVal == UnsavedDialog.Response.CANCEL) {
                return;
            } else if (retVal == UnsavedDialog.Response.SAVE) {
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
            UnsavedDialog.Response retVal = showConfirmationDialog();
            if (retVal == UnsavedDialog.Response.CANCEL) {
                return;
            } else if (retVal == UnsavedDialog.Response.SAVE) {
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

    private void openDeviceMonitor(String portName){
        SerialPort port = SerialPort.getCommPort(portName);
        //TODO: capture error in rare case the port is disconnected
        DeviceMonitor deviceMonitor = new DeviceMonitor(project, port);
        deviceMonitor.showAndWait();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
