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
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

public class MainWindow extends BorderPane {

    private Project currentProject;
    private Node diagramEditor;

    private final BooleanProperty diagramEditorShowing;
    private final BooleanProperty deviceConfigShowing;

    public MainWindow(ObjectProperty<Project> project) {
        currentProject = project.get();
        diagramEditor = initDiagramEditor();

        diagramEditorShowing = new SimpleBooleanProperty();
        diagramEditorShowing.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                setCenter(diagramEditor);
            }
        });
        deviceConfigShowing = new SimpleBooleanProperty();
        deviceConfigShowing.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                setCenter(initConfigDevice());
            }
        });

        project.addListener((observable, oldValue, newValue) -> {
            currentProject = newValue;
            diagramEditor = initDiagramEditor();
            if (diagramEditorShowing.get()) {
                setCenter(diagramEditor);
            } else {    // deviceConfigShowing must be true
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
        deviceLibraryPanel.setOnDevicePressed(currentProject::addDevice);

        ProjectDevicePanel projectDevicePanel = new ProjectDevicePanel(currentProject);

        SplitPane panelSplitPane = new SplitPane();
        panelSplitPane.setMinWidth(200);
        panelSplitPane.setMaxWidth(300);
        panelSplitPane.setOrientation(Orientation.VERTICAL);
        panelSplitPane.getItems().addAll(projectDevicePanel, deviceLibraryPanel);

        CanvasViewModel canvasViewModel = new CanvasViewModel(currentProject);
        CanvasView canvasView = new CanvasView(canvasViewModel);

        SplitPane mainSplitPane = new SplitPane();
        mainSplitPane.setDividerPositions(0.75);
        mainSplitPane.setOrientation(Orientation.HORIZONTAL);
        mainSplitPane.getItems().addAll(canvasView, panelSplitPane);

        return mainSplitPane;
    }

    private Node initConfigDevice() {
        StackPane rightView = new StackPane();

        Runnable generateViewCreator = () -> {
            rightView.getChildren().clear();

            DeviceMapperResult mappingResult = DeviceMapper.autoAssignDevices(currentProject);
            SourceCodeResult codeGeneratorResult = SourceCodeGenerator.generateCode(currentProject, true);

            GenerateViewModel generateViewModel = new GenerateViewModel(currentProject, codeGeneratorResult);
            GenerateView generateView = new GenerateView(generateViewModel);
            rightView.getChildren().add(generateView);

            String errorMessage = null;
            if (mappingResult != DeviceMapperResult.OK) {
                errorMessage = mappingResult.getErrorMessage();
            } else if (codeGeneratorResult.hasError()) {
                errorMessage = codeGeneratorResult.getError().getDescription();
            }
            if (errorMessage != null) {
                generateView.setDisable(true);
                // overlay the generate view with a warning icon and an error message
                ImageView warningIcon = new ImageView(new Image(getClass().getResourceAsStream("/css/dialog/warning.png")));
                Label warningMessage = new Label(errorMessage);
                warningMessage.setTextAlignment(TextAlignment.CENTER);
                warningMessage.setWrapText(true);
                VBox errorPane = new VBox();
                errorPane.setPadding(new Insets(20, 20, 20, 20));
                errorPane.setAlignment(Pos.CENTER);
                errorPane.getChildren().addAll(warningIcon, warningMessage);
                rightView.getChildren().add(errorPane);
            }
        };

        // device config
        ConfigActualDeviceViewModel configActualDeviceViewModel = new ConfigActualDeviceViewModel(currentProject);
        configActualDeviceViewModel.setConfigChangedCallback(generateViewCreator);
        ConfigActualDeviceView configActualDeviceView = new ConfigActualDeviceView(configActualDeviceViewModel);

        // generate view
        generateViewCreator.run();

        SplitPane mainLayout = new SplitPane();
        mainLayout.setDividerPositions(0.5);
        mainLayout.setOrientation(Orientation.HORIZONTAL);
        mainLayout.getItems().addAll(configActualDeviceView, rightView);
        return mainLayout;
    }
}
