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

import io.makerplayground.project.Project;
import io.makerplayground.ui.canvas.CanvasView;
import io.makerplayground.ui.canvas.CanvasViewModel;
import javafx.application.HostServices;
import javafx.beans.property.*;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;

public class MainWindow extends BorderPane {

    private final HostServices hostServices;

    private Project currentProject;
    private Node diagramEditor;
    private DeviceTab deviceTab;

    private final BooleanProperty diagramEditorShowing;
    private final BooleanProperty deviceConfigShowing;

    public MainWindow(ObjectProperty<Project> project, HostServices hostServices) {
        this.hostServices = hostServices;

        currentProject = project.get();
        diagramEditor = initDiagramEditor();
        deviceTab = new DeviceTab(project.get(), hostServices);

        diagramEditorShowing = new SimpleBooleanProperty();
        diagramEditorShowing.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                setCenter(diagramEditor);
            }
        });
        deviceConfigShowing = new SimpleBooleanProperty();
        deviceConfigShowing.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                deviceTab.refreshConfigDevicePane();
                setCenter(deviceTab);
            }
        });

        project.addListener((observable, oldValue, newValue) -> {
            currentProject = newValue;
            diagramEditor = initDiagramEditor();
            deviceTab = new DeviceTab(project.get(), hostServices);
            if (diagramEditorShowing.get()) {
                setCenter(diagramEditor);
            } else {    // deviceConfigShowing must be true
                setCenter(deviceTab);
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
        CanvasViewModel canvasViewModel = new CanvasViewModel(currentProject);
        return new CanvasView(canvasViewModel);
    }
}
