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
import io.makerplayground.device.DeviceLibraryUpdateHelper;
import io.makerplayground.generator.source.SourceCode;
import io.makerplayground.generator.source.SourceCodeResult;
import io.makerplayground.project.Project;
import io.makerplayground.ui.dialog.DeviceLibraryErrorDialogView;
import io.makerplayground.ui.dialog.TaskDialogView;
import io.makerplayground.ui.dialog.UnsavedDialog;
import io.makerplayground.util.PathUtility;
import io.makerplayground.util.ZipResourceExtractor;
import io.makerplayground.version.SoftwareVersion;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static javafx.concurrent.WorkerStateEvent.WORKER_STATE_SUCCEEDED;

public class Main extends Application {

    private Toolbar toolbar;
    private ObjectProperty<Project> project;
    private File latestProjectDirectory;

    @Override
    public void start(Stage primaryStage) {
        // TODO: show progress indicator while loading if need

        Map<Path, String> errors = DeviceLibrary.INSTANCE.loadDeviceLibrary();
        if (!errors.isEmpty()) {
            primaryStage.addEventHandler(WindowEvent.WINDOW_SHOWN, event -> {
                DeviceLibraryErrorDialogView dialogView = new DeviceLibraryErrorDialogView(primaryStage, errors);
                dialogView.showAndWait();
            });
        }

        // try to load a project file passed as a command line argument if existed
        List<String> parameters = getParameters().getUnnamed();
        if (!parameters.isEmpty()) {
            File f = new File(parameters.get(parameters.size() - 1));
            project = new SimpleObjectProperty<>(Project.loadProject(f).orElseGet(Project::new));
        } else {
            project = new SimpleObjectProperty<>(new Project());
        }

        toolbar = new Toolbar(project);
        toolbar.setOnNewButtonPressed(event -> newProject(primaryStage.getScene().getWindow()));
        toolbar.setOnLoadButtonPressed(event -> loadProject(primaryStage.getScene().getWindow()));
        toolbar.setOnSaveButtonPressed(event -> saveProject(primaryStage.getScene().getWindow()));
        toolbar.setOnSaveAsButtonPressed(event -> saveProjectAs(primaryStage.getScene().getWindow()));
        toolbar.setOnExportButtonPressed(event -> exportProject(primaryStage.getScene().getWindow()));
        toolbar.setOnCloseButtonPressed(event -> primaryStage.fireEvent(new WindowEvent(primaryStage, WindowEvent.WINDOW_CLOSE_REQUEST)));

        MainWindow mainWindow = new MainWindow(project, toolbar.selectingSerialPortProperty(), getHostServices());
        mainWindow.diagramEditorShowingProperty().bind(toolbar.diagramEditorSelectProperty());
        mainWindow.deviceConfigShowingProperty().bind(toolbar.deviceConfigSelectProperty());
        mainWindow.deviceMonitorShowingProperty().bind(toolbar.deviceMonitorSelectProperty());
        mainWindow.setOnLibraryUpdateButtonPressed(event -> updateDeviceLibrary(primaryStage.getScene().getWindow()));

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
                oldValue.getInteractiveModel().stop();
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
        } catch (Exception e) {
            toolbar.setStatusMessage("");
            e.printStackTrace();
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
        } catch (Exception e) {
            toolbar.setStatusMessage("");
            e.printStackTrace();
        }
    }

    private void exportProject(Window window) {
        toolbar.setStatusMessage("Exporting...");

        File selectedFile;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export File As");
        if (latestProjectDirectory != null) {
            fileChooser.setInitialDirectory(latestProjectDirectory);
        }
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Zip Archive File", "*.zip"));
        fileChooser.setInitialFileName("*.zip");
        selectedFile = fileChooser.showSaveDialog(window);

        if (selectedFile != null) {
            SourceCodeResult sourceCode = SourceCode.generate(project.get(), false);
            if (project.get().getProjectConfiguration().getPlatform().isArduino()) {
                ProjectExportTask exportTask = new ArduinoExportTask(project.get(), sourceCode, selectedFile.getAbsolutePath());
                TaskDialogView<ProjectExportTask> dialogView = new TaskDialogView<>(window, exportTask, "Export");
                dialogView.show();
                new Thread(exportTask).start();
            } else {
                throw new IllegalStateException("Not implemented yet");
            }
            toolbar.setStatusMessage("Exported");
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> toolbar.setStatusMessage(""));
                }
            }, 3000);
        } else {
            toolbar.setStatusMessage("");
        }
    }

    public void updateDeviceLibrary(Window window) {
        if (!DeviceLibraryUpdateHelper.isUpdateFileAvailable()) {
            return;
        }

        // save the current project as we need to reload the project after update the library
        if (project.get().hasUnsavedModification()) {
            UnsavedDialog.Response retVal = new UnsavedDialog(window).showAndGetResponse();
            if (retVal == UnsavedDialog.Response.CANCEL) {
                return;
            } else if (retVal == UnsavedDialog.Response.SAVE) {
                saveProject(window);
            }
        }

        // delete old library
        File oldLibraryDirectory = new File(PathUtility.getUserLibraryPath());
        if (oldLibraryDirectory.isDirectory()) {
            try {
                FileUtils.deleteDirectory(oldLibraryDirectory);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // install new library to platform default library dir
        Optional<File> updateFilePath = DeviceLibraryUpdateHelper.getUpdateFilePath().map(File::new);
        if (updateFilePath.isPresent()) {
            Map<Path, String> errors = new HashMap<>();
            Task<Void> extractTask  = ZipResourceExtractor.launchExtractTask(updateFilePath.get(), PathUtility.MP_WORKSPACE);
            extractTask.addEventHandler(WORKER_STATE_SUCCEEDED, (event) -> {
                // reload the library and project
                errors.putAll(DeviceLibrary.INSTANCE.loadDeviceLibrary());
                // reload current project from file
                if (!project.get().getFilePath().isEmpty()) {
                    Optional<Project> p = Project.loadProject(new File(project.get().getFilePath()));
                    if (p.isPresent()) {
                        project.set(p.get());
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "The program does not support this previous project version.", ButtonType.OK);
                        alert.showAndWait();
                    }
                } else {
                    project.set(new Project());
                }
                // delete the update file
                if (!updateFilePath.get().delete()) {
                    System.err.println("Warning: update file can't be deleted");
                }
            });
            TaskDialogView<Task<Void>> dialogView = new TaskDialogView<>(window, extractTask, "Install Library");
            dialogView.showAndWait();
            if (!errors.isEmpty()) {
                DeviceLibraryErrorDialogView dialogView1 = new DeviceLibraryErrorDialogView(window, errors);
                dialogView1.showAndWait();
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}