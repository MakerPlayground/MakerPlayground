/*
 * Copyright (c) 2020. The Maker Playground Authors.
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

package io.makerplayground.ui.devicetab;

import io.makerplayground.generator.diagram.WiringDiagram;
import io.makerplayground.project.Project;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.converter.IntegerStringConverter;
import org.controlsfx.control.ToggleSwitch;

import java.io.IOException;

public class DeviceDiagramView extends VBox {
    private final Project project;

    @FXML private ScrollPane diagramScrollPane;
    @FXML private Button zoomInButton;
    @FXML private Button zoomOutButton;
    @FXML private Button zoomFitButton;
    @FXML private HBox interactiveBarHBox;
    @FXML private Circle interactiveStatusIndicator;
    @FXML private Label interactiveStatusLabel;
    @FXML private ToggleSwitch readSensorToggle;
    @FXML private HBox readingEveryHBox;
    @FXML private TextField readingRateTextField;

    public static final double DEFAULT_ZOOM_SCALE = 0.7;
    private final DoubleProperty scale = new SimpleDoubleProperty(DEFAULT_ZOOM_SCALE);

    private static final Color INTERACTIVE_RUNNING_COLOR = Color.web("#00cc6a");
    private static final Color INTERACTIVE_WARNING_COLOR = Color.web("#ffab00");

    public DeviceDiagramView(Project project) {
        this.project = project;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/devicetab/DeviceDiagramView.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        interactiveBarHBox.visibleProperty().bind(project.getInteractiveModel().startedProperty());
        interactiveBarHBox.managedProperty().bind(project.getInteractiveModel().startedProperty());
        interactiveStatusIndicator.setFill(INTERACTIVE_RUNNING_COLOR);
        interactiveStatusIndicator.setEffect(new DropShadow(BlurType.GAUSSIAN, INTERACTIVE_RUNNING_COLOR, 5, 0.5, 0, 0));
        interactiveStatusLabel.setText("Interactive mode is running");
        readingEveryHBox.disableProperty().bind(readSensorToggle.selectedProperty().not());
        readSensorToggle.selectedProperty().bindBidirectional(project.getInteractiveModel().sensorReadingProperty());
        readSensorToggle.setGraphicTextGap(0);
        readingRateTextField.setOnAction(event -> setReadingRateIfValid());
        readingRateTextField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                setReadingRateIfValid();
            }
        });
        project.getInteractiveModel().sensorReadingRateProperty().addListener((observable, oldValue, newValue) -> updateReadingRateTextField());
        updateReadingRateTextField();

        // TODO: DiagramV1 can generated a connection diagram even some devices or ports haven't been selected so the project's status property can't be used
        if (project.getProjectConfiguration().getController() != null) {
            initView();
        }
    }

    private void updateReadingRateTextField() {
        readingRateTextField.setText(String.valueOf(project.getInteractiveModel().getSensorReadingRate()));
    }

    private void setReadingRateIfValid() {
        if (readingRateTextField.getText().matches("\\d+")) {
            project.getInteractiveModel().sensorReadingRateProperty().set(Integer.parseInt(readingRateTextField.getText()));
        } else {
            updateReadingRateTextField();
        }
    }

    private void initView() {
        Pane wiringDiagram = WiringDiagram.make(project);
        wiringDiagram.scaleXProperty().bind(scale);
        wiringDiagram.scaleYProperty().bind(scale);

        zoomInButton.setOnAction(event -> scale.set(scale.get() + 0.1));
        zoomOutButton.setOnAction(event -> scale.set(Math.max(0.1, scale.get() - 0.1)));
        zoomFitButton.setOnAction(event -> {
            // add 5 pixels to the actual diagram dimension to prevent it from being too fit to the scroll pane and force
            // the scroll pane to show unnecessary horizontal scrollbar
            double width = wiringDiagram.getBoundsInLocal().getWidth() + 5;
            double height = wiringDiagram.getBoundsInLocal().getHeight() + 5;
            if (width > height) {
                scale.set(diagramScrollPane.getBoundsInLocal().getWidth() / width);
            } else {
                scale.set(diagramScrollPane.getBoundsInLocal().getHeight() / height);
            }
        });

        diagramScrollPane.setContent(new Group(wiringDiagram));
    }

    public void setZoomLevel(double scale) {
        this.scale.set(scale);
    }

    public DoubleProperty zoomLevelProperty() {
        return scale;
    }
}
