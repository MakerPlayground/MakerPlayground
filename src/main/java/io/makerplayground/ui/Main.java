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
import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.project.Project;
import io.makerplayground.ui.dialog.UnsavedDialog;
import io.makerplayground.version.SoftwareVersion;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
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
import java.util.*;

/**
 * Created by Nuntipat Narkthong on 6/6/2017 AD.
 */
public class Main extends Application {

    private Toolbar toolbar;
    private File latestProjectDirectory;
    private ObservableMap<Project, File> projectFilesMap = FXCollections.observableHashMap();
    private ObjectProperty<Project> focusProject = new SimpleObjectProperty<>();

    private Project getFirstProject() {
        return projectFilesMap.keySet().iterator().next();
    }

    @Override
    public void start(Stage primaryStage) {
        // TODO: show progress indicator while loading if need

        DeviceLibrary.INSTANCE.loadDeviceFromFiles();

        focusProject.addListener((observable, oldValue, newValue) -> {
            /* set title name */
            File file = projectFilesMap.get(newValue);
            String fileName = file == null ? "" : file.getName();
            primaryStage.setTitle(SoftwareVersion.getCurrentVersion().getBuildName() + " - " + (fileName.isEmpty() ? "Untitled Project" : fileName));
        });

        // try to load a project file passed as a command line argument if existed
        List<String> parameters = getParameters().getUnnamed();
        if (!parameters.isEmpty()) {
            File f = new File(parameters.get(parameters.size() - 1));
            Project.loadProject(f).ifPresentOrElse(project -> projectFilesMap.put(project, f), () -> projectFilesMap.put(new Project(), null));
        } else {
            projectFilesMap.put(new Project(), null);
        }

        focusProject.set(getFirstProject());

        toolbar = new Toolbar(focusProject);
        toolbar.setOnNewButtonPressed(event -> newProject(primaryStage.getScene().getWindow()));
        toolbar.setOnLoadButtonPressed(event -> loadProject(primaryStage.getScene().getWindow()));
        toolbar.setOnSaveButtonPressed(event -> saveProject(primaryStage.getScene().getWindow()));
        toolbar.setOnSaveAsButtonPressed(event -> saveProjectAs(primaryStage.getScene().getWindow()));
        toolbar.setOnCloseButtonPressed(event -> primaryStage.fireEvent(new WindowEvent(primaryStage, WindowEvent.WINDOW_CLOSE_REQUEST)));

        MainWindow mainWindow = new MainWindow(focusProject);
        mainWindow.diagramEditorShowingProperty().bind(toolbar.diagramEditorSelectProperty());
        mainWindow.deviceConfigShowingProperty().bind(toolbar.deviceConfigSelectProperty());

        BorderPane borderPane = new BorderPane();
        borderPane.setTop(toolbar);
        borderPane.setCenter(mainWindow);

        final Scene scene = new Scene(borderPane, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/css/light-theme.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());

        // close program
        primaryStage.setOnCloseRequest(event -> {
            if (focusProject.get().hasUnsavedModification(projectFilesMap.get(focusProject.get()))) {
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

    public void newProject(Window window) {
        /* TODO: Allow to create newProject without closing the current project when adding support of many tabs. */
        if (focusProject.get().hasUnsavedModification(projectFilesMap.get(focusProject.get()))) {
            UnsavedDialog.Response retVal = new UnsavedDialog(window).showAndGetResponse();
            if (retVal == UnsavedDialog.Response.CANCEL) {
                return;
            } else if (retVal == UnsavedDialog.Response.SAVE) {
                saveProject(window);
                projectFilesMap.remove(focusProject.get());
            }
        }
        projectFilesMap.put(new Project(), null);
        focusProject.set(getFirstProject());
    }

    public void loadProject(Window window) {
        if (focusProject.get().hasUnsavedModification(projectFilesMap.get(focusProject.get()))) {
            UnsavedDialog.Response retVal = new UnsavedDialog(window).showAndGetResponse();
            if (retVal == UnsavedDialog.Response.CANCEL) {
                return;
            } else if (retVal == UnsavedDialog.Response.SAVE) {
                saveProject(window);
                projectFilesMap.remove(focusProject.get());
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
                projectFilesMap.put(p.get(), selectedFile);
                focusProject.set(p.get());
                focusProject.get().calculateCompatibility();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "The program does not support this previous project version.", ButtonType.OK);
                alert.showAndWait();
            }
        }
    }

    public void saveProject(Window window) {
        toolbar.setStatusMessage("Saving...");
        try {
            File selectedFile = projectFilesMap.get(focusProject.get());
            if (selectedFile == null) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save File");
                if (latestProjectDirectory != null) {
                    fileChooser.setInitialDirectory(latestProjectDirectory);
                }
                fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("MakerPlayground Projects", "*.mp"));
                fileChooser.setInitialFileName("*.mp");
                selectedFile = fileChooser.showSaveDialog(window);
            }

            if (selectedFile != null) {
                latestProjectDirectory = selectedFile.getParentFile();
                ObjectMapper mapper = new ObjectMapper();
                mapper.writeValue(selectedFile, focusProject.get());
                projectFilesMap.replace(focusProject.get(), selectedFile);
                toolbar.setStatusMessage("Saved");
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> toolbar.setStatusMessage(""));
                    }
                }, 3000);
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
                mapper.writeValue(selectedFile, focusProject.get());
                projectFilesMap.replace(focusProject.get(), selectedFile);
                toolbar.setStatusMessage("Saved");
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> toolbar.setStatusMessage(""));
                    }
                }, 3000);
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
