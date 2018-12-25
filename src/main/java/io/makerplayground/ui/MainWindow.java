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

import io.makerplayground.generator.DeviceMapper;
import io.makerplayground.generator.DeviceMapperResult;
import io.makerplayground.generator.source.SourceCodeGenerator;
import io.makerplayground.generator.source.SourceCodeResult;
import io.makerplayground.project.Project;
import io.makerplayground.ui.canvas.CanvasView;
import io.makerplayground.ui.canvas.CanvasViewModel;
import io.makerplayground.ui.dialog.configdevice.ConfigActualDeviceView;
import io.makerplayground.ui.dialog.configdevice.ConfigActualDeviceViewModel;
import io.makerplayground.ui.dialog.generate.GenerateView;
import io.makerplayground.ui.dialog.generate.GenerateViewModel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class MainWindow extends BorderPane {

    @FXML private RadioButton diagramEditorButton;
    @FXML private RadioButton deviceConfigButton;
    @FXML private Button uploadButton;
    @FXML private Button deviceMonitorButton;

    private final Project project;
    private final Node diagramEditor;
    private GenerateView generateView;

    public MainWindow(Project project) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/MainWindow.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        this.project = project;
        this.diagramEditor = initDiagramEditor();
        setCenter(diagramEditor);

        diagramEditorButton.setSelected(true);
        diagramEditorButton.setOnAction(event -> {
            generateView = null;    // clear to prevent memory leak
            setCenter(diagramEditor);
        });
        deviceConfigButton.setOnAction(event -> {
            setCenter(initConfigDevice());
        });

        ToggleGroup toggleGroup = new ToggleGroup();
        toggleGroup.getToggles().addAll(diagramEditorButton, deviceConfigButton);

//        uploadButton.setOnAction();
    }

    private Node initDiagramEditor() {
        DeviceLibraryPanel deviceLibraryPanel = new DeviceLibraryPanel();
        deviceLibraryPanel.setOnDevicePressed(project::addDevice);

        ProjectDevicePanel projectDevicePanel = new ProjectDevicePanel(project);

        SplitPane panelSplitPane = new SplitPane();
        panelSplitPane.setMinWidth(200);
        panelSplitPane.setMaxWidth(300);
        panelSplitPane.setOrientation(Orientation.VERTICAL);
        panelSplitPane.getItems().addAll(projectDevicePanel, deviceLibraryPanel);

        CanvasViewModel canvasViewModel = new CanvasViewModel(project);
        CanvasView canvasView = new CanvasView(canvasViewModel);

        SplitPane mainSplitPane = new SplitPane();
        mainSplitPane.setDividerPositions(0.75);
        mainSplitPane.setOrientation(Orientation.HORIZONTAL);
        mainSplitPane.getItems().addAll(canvasView, panelSplitPane);

        return mainSplitPane;
    }

    private Node initConfigDevice() {
        SplitPane mainLayout = new SplitPane();
        mainLayout.setDividerPositions(0.5);
        mainLayout.setOrientation(Orientation.HORIZONTAL);

        Runnable generateViewCreator = () -> {
            if (generateView != null) {
                mainLayout.getItems().remove(generateView);
            }
            DeviceMapperResult mappingResult = DeviceMapper.autoAssignDevices(project);
            if (mappingResult == DeviceMapperResult.NO_MCU_SELECTED) {
//                "Controller hasn't been selected"
            } else if (mappingResult == DeviceMapperResult.NOT_ENOUGH_PORT) {
//                "Not enough port"
            } else if (mappingResult == DeviceMapperResult.NO_SUPPORT_DEVICE) {
//                "Can't find any support device"
            } else if (mappingResult != DeviceMapperResult.OK) {
//                "Found unknown error!!!"
            } else if (mappingResult == DeviceMapperResult.OK) {
                SourceCodeResult code = SourceCodeGenerator.generateCode(project, true);
                if (code.hasError()) {
//                code.getError().getDescription()
                } else {
                    GenerateViewModel generateViewModel = new GenerateViewModel(project, code);
                    generateView = new GenerateView(generateViewModel);
                    mainLayout.getItems().add(generateView);
                }
            }
        };

        // device config
        ConfigActualDeviceViewModel configActualDeviceViewModel = new ConfigActualDeviceViewModel(project);
        configActualDeviceViewModel.setConfigChangedCallback(generateViewCreator);
        ConfigActualDeviceView configActualDeviceView = new ConfigActualDeviceView(configActualDeviceViewModel);
        mainLayout.getItems().add(configActualDeviceView);

        // generate view
        generateViewCreator.run();

        return mainLayout;
    }

//    private void handleUploadBtn(ActionEvent event) {
//        UploadTask uploadTask = new UploadTask(project);
//
//        UploadDialogView uploadDialogView = new UploadDialogView(getScene().getWindow(), uploadTask);
//        uploadDialogView.progressProperty().bind(uploadTask.progressProperty());
//        uploadDialogView.descriptionProperty().bind(uploadTask.messageProperty());
//        uploadDialogView.logProperty().bind(uploadTask.logProperty());
//        uploadDialogView.show();
//
//        new Thread(uploadTask).start();
//    }
}
