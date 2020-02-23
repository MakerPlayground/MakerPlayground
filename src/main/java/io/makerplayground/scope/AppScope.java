package io.makerplayground.scope;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.saxsys.mvvmfx.MvvmFX;
import de.saxsys.mvvmfx.Scope;
import io.makerplayground.generator.source.SourceCodeGenerator;
import io.makerplayground.generator.source.SourceCodeResult;
import io.makerplayground.generator.upload.UploadManager;
import io.makerplayground.project.Project;
import io.makerplayground.ui.ArduinoExportTask;
import io.makerplayground.ui.ProjectExportTask;
import io.makerplayground.ui.dialog.TaskDialogView;
import io.makerplayground.ui.dialog.UnsavedDialog;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

public class AppScope implements Scope {

    private ObjectProperty<File> latestProjectDirectory = new SimpleObjectProperty<>();
    private ObjectProperty<Stage> rootStage = new SimpleObjectProperty<>();
    private StringProperty toolbarStatusMessage = new SimpleStringProperty();
    private ObjectProperty<Project> currentProject = new SimpleObjectProperty<>();
    private ObjectProperty<UploadManager> uploadManager = new SimpleObjectProperty<>(new UploadManager(currentProject));

    public ObjectProperty<File> latestProjectDirectoryProperty() {
        return latestProjectDirectory;
    }

    public void setLatestProjectDirectory(File latestProjectDirectory) {
        this.latestProjectDirectory.set(latestProjectDirectory);
    }

    public StringProperty toolbarStatusMessageProperty() {
        return toolbarStatusMessage;
    }

    public void setToolbarStatusMessage(String toolbarStatusMessage) {
        this.toolbarStatusMessage.set(toolbarStatusMessage);
    }

    public void setRootStage(Stage stage) {
        rootStage.set(stage);
    }

    public Stage getRootStage() {
        return rootStage.get();
    }

    public ObjectProperty<Stage> rootStageProperty() {
        return rootStage;
    }

    public Project getCurrentProject() {
        return currentProject.get();
    }

    public ObjectProperty<Project> currentProjectProperty() {
        return currentProject;
    }

    public void setCurrentProject(Project currentProject) {
        this.currentProject.set(currentProject);
    }

    public UploadManager getUploadManager() {
        return uploadManager.get();
    }

    public ObjectProperty<UploadManager> uploadManagerProperty() {
        return uploadManager;
    }

    public void setUploadManager(UploadManager uploadManager) {
        this.uploadManager.set(uploadManager);
    }

    public void saveProjectToFile(File selectedFile) {
        if (selectedFile != null) {
            setLatestProjectDirectory(selectedFile.getParentFile());
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.writeValue(selectedFile, getCurrentProject());
            } catch (IOException e) {
                e.printStackTrace();
            }
            getCurrentProject().setFilePath(selectedFile.getAbsolutePath());
            setToolbarStatusMessage("Saved");
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    setToolbarStatusMessage("");
                }
            }, 3000);
        } else {
            setToolbarStatusMessage("");
        }
    }

    public void saveProject() {
        if (currentProject.get().filePathProperty().get().isEmpty()) {
            saveProjectAs();
        } else {
            setToolbarStatusMessage("Saving...");
            saveProjectToFile(new File(currentProject.get().filePathProperty().get()));
        }
    }

    public void saveProjectAs() {
        setToolbarStatusMessage("Saving...");
        try {
            File selectedFile;
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save File As");
            if (latestProjectDirectory != null) {
                fileChooser.setInitialDirectory(latestProjectDirectory.getValue());
            }
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("MakerPlayground Projects", "*.mp"));
            fileChooser.setInitialFileName("*.mp");
            selectedFile = fileChooser.showSaveDialog(getRootStage().getScene().getWindow());

            if (selectedFile != null) {
                latestProjectDirectory.set(selectedFile.getParentFile());
                ObjectMapper mapper = new ObjectMapper();
                mapper.writeValue(selectedFile, currentProject.get());
                currentProject.get().setFilePath(selectedFile.getAbsolutePath());
                setToolbarStatusMessage("Saved");
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        setToolbarStatusMessage("");
                    }
                }, 3000);
            } else {
                setToolbarStatusMessage("");
            }
        } catch (Exception e) {
            setToolbarStatusMessage("");
            e.printStackTrace();
        }
    }

    public void requestNewProject() {
        if (getCurrentProject().hasUnsavedModification()) {
            UnsavedDialog.Response retVal = new UnsavedDialog(getRootStage().getScene().getWindow()).showAndGetResponse();
            if (retVal == UnsavedDialog.Response.CANCEL) {
                return;
            } else if (retVal == UnsavedDialog.Response.SAVE) {
                saveProject();
            }
        }
        newProject();
    }

    public void newProject() {
        currentProject.set(new Project());
        currentProject.get().removeAllVariables();
    }

    public void requestLoadProject() {
        if (currentProject.get().hasUnsavedModification()) {
            UnsavedDialog.Response retVal = new UnsavedDialog(getRootStage().getScene().getWindow()).showAndGetResponse();
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
        File selectedFile = fileChooser.showOpenDialog(getRootStage().getScene().getWindow());
        if (selectedFile != null) {
            loadProject(selectedFile);
        }
    }

    public void loadProject(File selectedFile) {
        Optional<Project> p = Project.loadProject(selectedFile);
        if (p.isPresent()) {
            currentProject.set(p.get());
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "The program does not support this previous project version.", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void exportProject() {
        setToolbarStatusMessage("Exporting...");

        File selectedFile;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export File As");
        if (latestProjectDirectory != null) {
            fileChooser.setInitialDirectory(latestProjectDirectory.get());
        }
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Zip Archive File", "*.zip"));
        fileChooser.setInitialFileName("*.zip");
        selectedFile = fileChooser.showSaveDialog(getRootStage().getScene().getWindow());

        if (selectedFile != null) {
            SourceCodeResult sourceCode = SourceCodeGenerator.generate(currentProject.get());
            if (currentProject.get().getProjectConfiguration().getPlatform().isArduino()) {
                ProjectExportTask exportTask = new ArduinoExportTask(currentProject.get(), sourceCode, selectedFile.getAbsolutePath());
                TaskDialogView<ProjectExportTask> dialogView = new TaskDialogView<>(getRootStage().getScene().getWindow(), exportTask, "Export");
                dialogView.show();
                new Thread(exportTask).start();
            } else {
                throw new IllegalStateException("Not implemented yet");
            }
            setToolbarStatusMessage("Exported");
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    setToolbarStatusMessage("");
                }
            }, 3000);
        } else {
            setToolbarStatusMessage("");
        }
    }

    public void requestClose(WindowEvent event) {
        if (getCurrentProject().hasUnsavedModification()) {
            UnsavedDialog.Response retVal = new UnsavedDialog(getRootStage().getScene().getWindow()).showAndGetResponse();
            if (retVal == UnsavedDialog.Response.CANCEL) {
                event.consume();
                return;
            } else if (retVal == UnsavedDialog.Response.SAVE) {
                saveProject();
            }
        }
        getRootStage().close();
        Platform.exit();
        System.exit(0);
    }
}
