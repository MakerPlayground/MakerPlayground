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
import io.makerplayground.device.actual.ActualDevice;
import io.makerplayground.device.actual.CloudPlatform;
import io.makerplayground.generator.DeviceMapper;
import io.makerplayground.generator.DeviceMapperResult;
import io.makerplayground.generator.source.SourceCodeGenerator;
import io.makerplayground.generator.source.SourceCodeResult;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectDevice;
import io.makerplayground.ui.dialog.UnsavedDialog;
import io.makerplayground.ui.dialog.WarningDialogView;
import io.makerplayground.util.ZipResourceExtractor;
import io.makerplayground.version.SoftwareVersion;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.*;
import org.apache.commons.io.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Nuntipat Narkthong on 6/6/2017 AD.
 */
public class Main extends Application {

    private Toolbar toolbar;
    private ObjectProperty<Project> project;
    private File latestProjectDirectory;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // TODO: show progress indicator while loading if need
        DeviceLibrary.INSTANCE.loadDeviceFromJSON();

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
        toolbar.setOnExportButtonPressed(event -> exportIotEdge(primaryStage.getScene().getWindow()));
        toolbar.setOnCloseButtonPressed(event -> primaryStage.fireEvent(new WindowEvent(primaryStage, WindowEvent.WINDOW_CLOSE_REQUEST)));

        MainWindow mainWindow = new MainWindow(project);
        mainWindow.diagramEditorShowingProperty().bind(toolbar.diagramEditorSelectProperty());
        mainWindow.deviceConfigShowingProperty().bind(toolbar.deviceConfigSelectProperty());

        BorderPane borderPane = new BorderPane();
        borderPane.setTop(toolbar);
        borderPane.setCenter(mainWindow);

        final Scene scene = new Scene(borderPane, 800, 600);
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

    private void exportIotEdge(Window window) {
        Project project = this.project.get();
        if (project.getPlatform() != io.makerplayground.device.actual.Platform.RASPBERRYPI) {
            (new WarningDialogView(window, "IoT Edge deployment module is not support for the current platform.")).showAndWait();
            return;
        }
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Export to");
        File selectedFile = directoryChooser.showDialog(window);
        if (selectedFile != null) {
            DeviceMapperResult mappingResult = DeviceMapper.validateDeviceAssignment(project);
            if (mappingResult != DeviceMapperResult.OK) {
                (new WarningDialogView(window, mappingResult.getErrorMessage())).showAndWait();
                return;
            }

            SourceCodeResult sourcecode = SourceCodeGenerator.generate(project);
            if (sourcecode.getError() != null) {
                (new WarningDialogView(window, sourcecode.getError().getDescription())).showAndWait();
                return;
            }
            List<ActualDevice> actualDevicesUsed = project.getAllDeviceUsed().stream()
                    .map(ProjectDevice::getActualDevice)
                    .collect(Collectors.toList());
            Set<String> mpLibraries = actualDevicesUsed.stream()
                    .map(actualDevice -> actualDevice.getMpLibrary(project.getPlatform()))
                    .collect(Collectors.toSet());
            mpLibraries.add("MakerPlayground");
            Set<String> externalLibraries = actualDevicesUsed.stream()
                    .map(actualDevice -> actualDevice.getExternalLibrary(project.getPlatform()))
                    .flatMap(Collection::stream).collect(Collectors.toSet());

            // Add Cloud Platform libraries
            for(CloudPlatform cloudPlatform: project.getCloudPlatformUsed()) {
                // add abstract .h library for the cloudPlatform.
                mpLibraries.add(cloudPlatform.getLibName());

                // add controller-specific library when using cloudPlatform.
                mpLibraries.add(project.getController().getCloudPlatformLibraryName(cloudPlatform));

                // add controller-specific external dependency when using cloudPlatform.
                externalLibraries.addAll(project.getController().getCloudPlatformLibraryDependency(cloudPlatform));
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
            String projectPath = selectedFile.getAbsolutePath() + File.separator + dateFormat.format(new Date());
            String modulePath = projectPath + File.separator + "modules" + File.separator + "makerplayground";
            try {
                FileUtils.deleteDirectory(new File(modulePath));
                FileUtils.forceMkdir(new File(modulePath));
            } catch (IOException e) {
                (new WarningDialogView(window, "Cannot create project directory (permission denied)")).showAndWait();
                return;
            }

            // get path to the library directory
            Optional<String> libraryPath = DeviceLibrary.INSTANCE.getLibraryPath();
            if (!libraryPath.isPresent()) {
                (new WarningDialogView(window, "Error: Missing library directory")).showAndWait();
                return;
            }

            // generate source file
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(modulePath + File.separator + "main.py"))){
                bw.write(sourcecode.getCode());
            } catch (IOException e) {
                (new WarningDialogView(window, "Error: Cannot write code to project directory")).showAndWait();
                return;
            }

            // copy mp library
            for (String libName: mpLibraries) {
                File source = Paths.get(libraryPath.get(), "lib", project.getPlatform().getLibraryFolderName(), libName).toFile();
                File destination = Paths.get(modulePath, libName).toFile();
                try {
                    FileUtils.copyDirectory(source, destination);
                } catch (IOException e) {
                    (new WarningDialogView(window, "Error: Missing some libraries")).showAndWait();
                    return;
                }
            }

            //copy and extract external Libraries
            for (String libName : externalLibraries) {
                Path sourcePath = Paths.get(libraryPath.get(),"lib_ext", libName + ".zip");
                ZipResourceExtractor.ExtractResult extractResult = ZipResourceExtractor.extract(sourcePath, modulePath);
                if (extractResult != ZipResourceExtractor.ExtractResult.SUCCESS) {
                    (new WarningDialogView(window, "Error: Failed to extract libraries")).showAndWait();
                    return;
                }
            }

            // create Dockerfile
            String dockerFileFormat = "FROM resin/rpi-raspbian:stretch\n" +
                    "\n" +
                    "RUN [ \"cross-build-start\" ]\n" +
                    "\n" +
                    "# Install dependencies\n" +
                    "RUN apt-get update && apt-get install -y \\\n" +
                    "        python3 \\\n" +
                    "        python3-dev \\\n" +
                    "        python3-pip \\\n" +
                    "        wget \\\n" +
                    "        build-essential \\\n" +
                    "        i2c-tools \\\n" +
                    "        libboost-python1.62.0\n" +
                    "\n" +
                    "COPY requirements.txt ./\n" +
                    "\n" +
                    "RUN pip3 install --upgrade pip \n" +
                    "RUN pip3 install --upgrade setuptools \n" +
                    "\n" +
                    "WORKDIR /app\n" +
                    "\n" +
                    "COPY *.py ./\n" +
                    "\n" +
                    "RUN [ \"cross-build-end\" ]  \n" +
                    "\n" +
                    "ENTRYPOINT [ \"python3\", \"-u\", \"./main.py\" ]\n";
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(modulePath + File.separator + "Dockerfile.arm32v7"))){
                bw.write(dockerFileFormat);
            } catch (IOException e) {
                (new WarningDialogView(window, "Error: Cannot create Dockerfile.arm32v7")).showAndWait();
                return;
            }

            // create module.json
            String moduleJsonFormat = "{\n" +
                    "    \"$schema-version\": \"0.0.1\",\n" +
                    "    \"description\": \"\",\n" +
                    "    \"image\": {\n" +
                    "        \"repository\": \"<YOUR-REPOSITORY>\",\n" +
                    "        \"tag\": {\n" +
                    "            \"version\": \"1.0\",\n" +
                    "            \"platforms\": {\n" +
                    "                \"arm32v7\": \"./Dockerfile.arm32v7\"\n" +
                    "            }\n" +
                    "        }\n" +
                    "    },\n" +
                    "    \"language\": \"python\"\n" +
                    "}";
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(modulePath + File.separator + "module.json"))){
                bw.write(moduleJsonFormat);
            } catch (IOException e) {
                (new WarningDialogView(window, "Error: Cannot create module.json")).showAndWait();
                return;
            }

            String dotEnvFileFormat = "# Replace the value of these variables with your own container registry\n" +
                    "\n" +
                    "CONTAINER_REGISTRY_ADDRESS=\"makerplaygroundtest1.azurecr.io\"\n" +
                    "CONTAINER_REGISTRY_USERNAME=\"makerplaygroundtest1\"\n" +
                    "CONTAINER_REGISTRY_PASSWORD=\"K==3Tt09TiLXJ71wQmWIXkOpC3wPklDt\"\n";
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(projectPath + File.separator + ".env"))){
                bw.write(dotEnvFileFormat);
            } catch (IOException e) {
                (new WarningDialogView(window, "Error: Cannot create .env")).showAndWait();
                return;
            }

            String dotGitIgnoreFormat  = "config/\n" +
                    ".env";
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(projectPath + File.separator + ".gitignore"))){
                bw.write(dotGitIgnoreFormat);
            } catch (IOException e) {
                (new WarningDialogView(window, "Error: Cannot create .gitignore")).showAndWait();
                return;
            }

            String deploymentTemplateFileFormat = "{\n" +
                    "  \"moduleContent\": {\n" +
                    "    \"$edgeAgent\": {\n" +
                    "      \"properties.desired\": {\n" +
                    "        \"schemaVersion\": \"1.0\",\n" +
                    "        \"runtime\": {\n" +
                    "          \"type\": \"docker\",\n" +
                    "          \"settings\": {\n" +
                    "            \"minDockerVersion\": \"v1.25\",\n" +
                    "            \"loggingOptions\": \"\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"systemModules\": {\n" +
                    "          \"edgeAgent\": {\n" +
                    "            \"type\": \"docker\",\n" +
                    "            \"settings\": {\n" +
                    "              \"image\": \"microsoft/azureiotedge-agent:1.0-preview\",\n" +
                    "              \"createOptions\": \"\"\n" +
                    "            }\n" +
                    "          },\n" +
                    "          \"edgeHub\": {\n" +
                    "            \"type\": \"docker\",\n" +
                    "            \"status\": \"running\",\n" +
                    "            \"restartPolicy\": \"always\",\n" +
                    "            \"settings\": {\n" +
                    "              \"image\": \"microsoft/azureiotedge-hub:1.0-preview\",\n" +
                    "              \"createOptions\": \"\"\n" +
                    "            }\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"modules\": {\n" +
                    "          \"makerplayground\": {\n" +
                    "            \"version\": \"1.0\",\n" +
                    "            \"type\": \"docker\",\n" +
                    "            \"status\": \"running\",\n" +
                    "            \"restartPolicy\": \"always\",\n" +
                    "            \"settings\": {\n" +
                    "              \"image\": \"${MODULES.makerplayground.arm32v7}\",\n" +
                    "              \"createOptions\": \"{\\\"HostConfig\\\":{\\\"Devices\\\":[{\\\"PathOnHost\\\":\\\"/dev/i2c-1\\\",\\\"PathInContainer\\\":\\\"/dev/i2c-1\\\",\\\"CgroupPermissions\\\":\\\"mrw\\\"}]}}\"\n" +
                    "            }\n" +
                    "          }\n" +
                    "        }\n" +
                    "      }\n" +
                    "    },\n" +
                    "    \"$edgeHub\": {\n" +
                    "      \"properties.desired\": {\n" +
                    "        \"schemaVersion\": \"1.0\",\n" +
                    "        \"routes\": {\n" +
                    "          \"aiToCloud\": \"FROM /messages/modules/* INTO $upstream\"\n" +
                    "        },\n" +
                    "        \"storeAndForwardConfiguration\": {\n" +
                    "          \"timeToLiveSecs\": 7200\n" +
                    "        }\n" +
                    "      }\n" +
                    "    },\n" +
                    "    \"makerplayground\": {\n" +
                    "      \"properties.desired\": {\n" +
                    "        \n" +
                    "      }\n" +
                    "    }\n" +
                    "  }\n" +
                    "}";
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(projectPath + File.separator + "deployment.template.json"))){
                bw.write(deploymentTemplateFileFormat);
            } catch (IOException e) {
                (new WarningDialogView(window, "Error: Cannot create deplotment.template.json")).showAndWait();
                return;
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
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
