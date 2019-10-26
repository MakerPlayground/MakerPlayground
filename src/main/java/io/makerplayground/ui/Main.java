/*
 * Copyright (c) 2019. The Maker Playground Authors.
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
import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.generator.upload.*;
import io.makerplayground.project.Project;
import io.makerplayground.ui.dialog.UnsavedDialog;
import io.makerplayground.version.SoftwareVersion;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

public class Main extends Application {

    private Toolbar toolbar;
    private ObjectProperty<Project> project;
    private File latestProjectDirectory;

    @Override
    public void start(Stage primaryStage) {
        // TODO: show progress indicator while loading if need

        DeviceLibrary.INSTANCE.loadDeviceFromFiles();

        // try to load a project file passed as a command line argument if existed
        List<String> parameters = getParameters().getUnnamed();
        if (!parameters.isEmpty()) {
            File f = new File(parameters.get(parameters.size() - 1));
            project = new SimpleObjectProperty<>(Project.loadProject(f).orElseGet(Project::new));
        } else {
            project = new SimpleObjectProperty<>(new Project());
        }

        UploadManager uploadManager = new UploadManager(project);

        toolbar = new Toolbar(project, uploadManager);
        toolbar.setOnNewButtonPressed(event -> newProject(primaryStage.getScene().getWindow()));
        toolbar.setOnLoadButtonPressed(event -> loadProject(primaryStage.getScene().getWindow()));
        toolbar.setOnSaveButtonPressed(event -> saveProject(primaryStage.getScene().getWindow()));
        toolbar.setOnSaveAsButtonPressed(event -> saveProjectAs(primaryStage.getScene().getWindow()));
        toolbar.setOnCloseButtonPressed(event -> primaryStage.fireEvent(new WindowEvent(primaryStage, WindowEvent.WINDOW_CLOSE_REQUEST)));

        MainWindow mainWindow = new MainWindow(project, getHostServices());
        mainWindow.diagramEditorShowingProperty().bind(toolbar.diagramEditorSelectProperty());
        mainWindow.deviceConfigShowingProperty().bind(toolbar.deviceConfigSelectProperty());

        BorderPane borderPane = new BorderPane();
        borderPane.setTop(toolbar);
        borderPane.setCenter(mainWindow);

        final Scene scene = new Scene(borderPane, 960, 600);
        scene.getStylesheets().add(getClass().getResource("/css/light-theme.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());

        ChangeListener<String> projectPathListener = (observable, oldValue, newValue) -> updatePath(primaryStage, newValue);
        project.get().filePathProperty().addListener(projectPathListener);
        updatePath(primaryStage, project.get().getFilePath());
        project.addListener((observable, oldValue, newValue) -> {
            if (oldValue != null) {
                oldValue.filePathProperty().removeListener(projectPathListener);
            }
            newValue.filePathProperty().addListener(projectPathListener);
            updatePath(primaryStage, newValue.getFilePath());
        });

        // close program
        primaryStage.setOnCloseRequest(event -> {
            if (project.get().hasUnsavedModification()) {
                UnsavedDialog.Response retVal = new UnsavedDialog(scene.getWindow()).showAndGetResponse();
                if (retVal == UnsavedDialog.Response.CANCEL) {
                    event.consume();
                    return;
                } else if (retVal == UnsavedDialog.Response.SAVE) {
                    saveProject(scene.getWindow());
                }
            }

            primaryStage.close();
            Platform.exit();
            System.exit(0);
        });

        primaryStage.getIcons().addAll(new Image(Main.class.getResourceAsStream("/icons/taskbar/logo_taskbar_16.png"))
                , new Image(Main.class.getResourceAsStream("/icons/taskbar/logo_taskbar_32.png"))
                , new Image(Main.class.getResourceAsStream("/icons/taskbar/logo_taskbar_48.png"))
                , new Image(Main.class.getResourceAsStream("/icons/taskbar/logo_taskbar_256.png")));
        primaryStage.setScene(scene);
        primaryStage.show();
        // prevent the window from being too small (primaryStage.setMinWidth(800) doesn't work as this function take
        // into account the title bar which is platform dependent so the window is actually a little bit larger than
        // 800x600 initially so we use primaryStage.getWidth/Height() to get the actual size and lock it)
        primaryStage.setMinWidth(primaryStage.getWidth());
        primaryStage.setMinHeight(primaryStage.getHeight());

        new UpdateNotifier(scene.getWindow(), getHostServices()).start();
    }

    private void updatePath(Stage stage, String path) {
        if (path.isEmpty()) {
            stage.setTitle(SoftwareVersion.getCurrentVersion().getBuildName() + " - Untitled Project");
        } else {
            stage.setTitle(SoftwareVersion.getCurrentVersion().getBuildName() + " - " + path);
        }
    }

    public void newProject(Window window) {
        if (project.get().hasUnsavedModification()) {
            UnsavedDialog.Response retVal = new UnsavedDialog(window).showAndGetResponse();
            if (retVal == UnsavedDialog.Response.CANCEL) {
                return;
            } else if (retVal == UnsavedDialog.Response.SAVE) {
                saveProject(window);
            }
        }
        project.set(new Project());
    }

    public void loadProject(Window window) {
        if (project.get().hasUnsavedModification()) {
            UnsavedDialog.Response retVal = new UnsavedDialog(window).showAndGetResponse();
            if (retVal == UnsavedDialog.Response.CANCEL) {
                return;
            } else if (retVal == UnsavedDialog.Response.SAVE) {
                saveProject(window);
            }
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("MakerPlayground Projects", "*.mp"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));
        File selectedFile = fileChooser.showOpenDialog(window);
        if (selectedFile != null) {
            Optional<Project> p = Project.loadProject(selectedFile);
            if (p.isPresent()) {
                project.set(p.get());
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "The program does not support this previous project version.", ButtonType.OK);
                alert.showAndWait();
            }
        }
    }

    public void saveProject(Window window) {
        toolbar.setStatusMessage("Saving...");
        try {
            File selectedFile;
            if (project.get().getFilePath().isEmpty()) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save File");
                if (latestProjectDirectory != null) {
                    fileChooser.setInitialDirectory(latestProjectDirectory);
                }
                fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("MakerPlayground Projects", "*.mp"));
                fileChooser.setInitialFileName("*.mp");
                selectedFile = fileChooser.showSaveDialog(window);
            } else {
                selectedFile = new File(project.get().getFilePath());
            }

            if (selectedFile != null) {
                latestProjectDirectory = selectedFile.getParentFile();
                ObjectMapper mapper = new ObjectMapper();
                mapper.writeValue(selectedFile, project.get());
                project.get().setFilePath(selectedFile.getAbsolutePath());
                toolbar.setStatusMessage("Saved");
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> toolbar.setStatusMessage(""));
                    }
                }, 3000);
            } else {
                toolbar.setStatusMessage("");
            }
        } catch (IOException x) {
            x.printStackTrace();
        } finally {
            toolbar.setStatusMessage("");
        }
    }

    public void saveProjectAs(Window window) {
        toolbar.setStatusMessage("Saving...");
        try {
            File selectedFile;
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save File As");
            if (latestProjectDirectory != null) {
                fileChooser.setInitialDirectory(latestProjectDirectory);
            }
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("MakerPlayground Projects", "*.mp"));
            fileChooser.setInitialFileName("*.mp");
            selectedFile = fileChooser.showSaveDialog(window);

            if (selectedFile != null) {
                latestProjectDirectory = selectedFile.getParentFile();
                ObjectMapper mapper = new ObjectMapper();
                mapper.writeValue(selectedFile, project.get());
                project.get().setFilePath(selectedFile.getAbsolutePath());
                toolbar.setStatusMessage("Saved");
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> toolbar.setStatusMessage(""));
                    }
                }, 3000);
            } else {
                toolbar.setStatusMessage("");
            }
        } catch (IOException x) {
            x.printStackTrace();
        } finally {
            toolbar.setStatusMessage("");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}