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

import io.makerplayground.device.generic.GenericDevice;
import io.makerplayground.generator.devicemapping.ProjectLogic;
import io.makerplayground.generator.devicemapping.ProjectMappingResult;
import io.makerplayground.generator.source.SourceCodeGenerator;
import io.makerplayground.generator.source.SourceCodeResult;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectDevice;
import io.makerplayground.ui.canvas.CanvasView;
import io.makerplayground.ui.canvas.CanvasViewModel;
import io.makerplayground.ui.dialog.configdevice.ConfigActualDeviceView;
import io.makerplayground.ui.dialog.configdevice.ConfigActualDeviceViewModel;
import io.makerplayground.ui.dialog.generate.GenerateView;
import io.makerplayground.ui.dialog.generate.GenerateViewModel;
import io.makerplayground.ui.explorer.DeviceExplorerPanel;
import javafx.application.HostServices;
import javafx.beans.property.*;
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MainWindow extends BorderPane {

    private final HostServices hostServices;

    private Project currentProject;
    private Node diagramEditor;

    private final BooleanProperty diagramEditorShowing;
    private final BooleanProperty deviceConfigShowing;

    private final IntegerProperty currentTabIndex;
    private final DoubleProperty deviceDiagramZoomLevel;

    public MainWindow(ObjectProperty<Project> project, HostServices hostServices) {
        this.hostServices = hostServices;

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

        currentTabIndex = new SimpleIntegerProperty(GenerateView.DEFAULT_TAB_INDEX);
        deviceDiagramZoomLevel = new SimpleDoubleProperty(GenerateView.DEFAULT_ZOOM_SCALE);

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

        // device explorer
        DeviceExplorerPanel deviceExplorerPanel = new DeviceExplorerPanel(currentProject.getSelectedController(), hostServices);
        deviceExplorerPanel.setOnAddButtonPressed(actualDevice -> {
            List<ProjectDevice> projectDevices = new ArrayList<>();
            for (GenericDevice genericDevice : actualDevice.getSupportedGenericDevice()) {
                projectDevices.add(currentProject.addDevice(genericDevice));
            }

            // sort by name so that the top most device will be the parent device for the other project device
            projectDevices.sort(Comparator.comparing(ProjectDevice::getName));

            ProjectDevice parentDevice = projectDevices.get(0);
            currentProject.getProjectConfiguration().setActualDevice(parentDevice, actualDevice);
            for (int i=1; i<projectDevices.size(); i++) {
                currentProject.getProjectConfiguration().setIdenticalDevice(projectDevices.get(i), parentDevice);
            }

            setCenter(initConfigDevice());
        });

        Runnable generateViewCreator = () -> {
            deviceExplorerPanel.setController(currentProject.getSelectedController());

            rightView.getChildren().clear();

            ProjectMappingResult mappingResult = ProjectLogic.validateDeviceAssignment(currentProject);
            SourceCodeResult codeGeneratorResult = SourceCodeGenerator.generate(currentProject);

            GenerateViewModel generateViewModel = new GenerateViewModel(currentProject, codeGeneratorResult);
            GenerateView generateView = new GenerateView(generateViewModel);
            generateView.setZoomLevel(deviceDiagramZoomLevel.get());
            generateView.setTabIndex(currentTabIndex.get());
            generateView.setOnZoomLevelChanged(deviceDiagramZoomLevel::set);
            generateView.setOnTabIndexChanged(currentTabIndex::set);

            rightView.getChildren().add(generateView);

            String errorMessage = null;
            if (mappingResult != ProjectMappingResult.OK) {
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

        SplitPane leftLayout = new SplitPane();
        leftLayout.setMaxWidth(710);
        leftLayout.setDividerPositions(0.5);
        leftLayout.setOrientation(Orientation.VERTICAL);
        leftLayout.getItems().addAll(configActualDeviceView, deviceExplorerPanel);

        // generate view
        generateViewCreator.run();

        SplitPane mainLayout = new SplitPane();
        mainLayout.setDividerPositions(0.5);
        mainLayout.setOrientation(Orientation.HORIZONTAL);
        mainLayout.getItems().addAll(leftLayout, rightView);
        return mainLayout;
    }
}
