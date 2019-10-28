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

package io.makerplayground.ui.dialog.generate;

import io.makerplayground.generator.diagram.WiringDiagram;
import io.makerplayground.project.Project;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import java.io.IOException;

public class DiagramPane extends HBox {
    private final Project project;

    @FXML private ScrollPane diagramScrollPane;
    @FXML private Button zoomInButton;
    @FXML private Button zoomOutButton;
    @FXML private Button zoomDefaultButton;

    public static final double DEFAULT_ZOOM_SCALE = 0.5;
    private final DoubleProperty scale = new SimpleDoubleProperty(DEFAULT_ZOOM_SCALE);

    public DiagramPane(Project project) {
        this.project = project;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/dialog/generate/DiagramPane.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // TODO: DiagramV1 can generated a connection diagram even some devices or ports haven't been selected so the project's status property can't be used
        if (project.getProjectConfiguration().getController() != null) {
            initView();
        }
    }

    private void initView() {
        zoomInButton.setOnAction(event -> scale.set(scale.get() + 0.1));
        zoomOutButton.setOnAction(event -> scale.set(Math.max(0.1, scale.get() - 0.1)));
        zoomDefaultButton.setOnAction(event -> scale.set(0.5));

        Pane wiringDiagram = WiringDiagram.make(project);
        wiringDiagram.scaleXProperty().bind(scale);
        wiringDiagram.scaleYProperty().bind(scale);

        diagramScrollPane.setContent(new Group(wiringDiagram));
    }

    public void setZoomLevel(double scale) {
        this.scale.set(scale);
    }

    public DoubleProperty zoomLevelProperty() {
        return scale;
    }
}
