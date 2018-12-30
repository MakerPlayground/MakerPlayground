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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;

public class MainWindow extends BorderPane {

    private final Project project;
    private final Node diagramEditor;
    private GenerateView generateView;

    private final BooleanProperty diagramEditorShowing;
    private final BooleanProperty deviceConfigShowing;

    public MainWindow(Project project) {
        this.project = project;
        this.diagramEditor = initDiagramEditor();

        diagramEditorShowing = new SimpleBooleanProperty();
        diagramEditorShowing.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                generateView = null;    // clear to prevent memory leak
                setCenter(diagramEditor);
            }
        });
        deviceConfigShowing = new SimpleBooleanProperty();
        deviceConfigShowing.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                setCenter(initConfigDevice());
            }
        });
    }

    public boolean isDiagramEditorShowing() {
        return diagramEditorShowing.get();
    }

    public BooleanProperty diagramEditorShowingProperty() {
        return diagramEditorShowing;
    }

    public boolean isDeviceConfigShowing() {
        return deviceConfigShowing.get();
    }

    public BooleanProperty deviceConfigShowingProperty() {
        return deviceConfigShowing;
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
}
