/*
 * Copyright 2017 The Maker Playground Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.makerplayground.ui.canvas;

import io.makerplayground.project.Line;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.PathElement;

/**
 *
 */
public class LineViewModel {
    private final Line line;
    private final ObservableList<PathElement> path;

    private final DoubleProperty centerX;
    private final DoubleProperty centerY;

    public LineViewModel(Line line) {
        this.line = line;

        MoveTo moveTo = new MoveTo();
        moveTo.xProperty().bind(line.getSource().destPortXProperty());
        moveTo.yProperty().bind(line.getSource().destPortYProperty());

        CubicCurveTo cubicCurveTo = new CubicCurveTo();
        cubicCurveTo.controlX1Property().bind(line.getSource().destPortXProperty().multiply(0.25)
                .add(line.getDestination().sourcePortXProperty().multiply(0.75)));
        cubicCurveTo.controlY1Property().bind(line.getSource().destPortYProperty());
        cubicCurveTo.controlX2Property().bind(line.getDestination().sourcePortXProperty().multiply(0.25)
                .add(line.getSource().destPortXProperty().multiply(0.75)));
        cubicCurveTo.controlY2Property().bind(line.getDestination().sourcePortYProperty());
        cubicCurveTo.xProperty().bind(line.getDestination().sourcePortXProperty());
        cubicCurveTo.yProperty().bind(line.getDestination().sourcePortYProperty());
        this.path = FXCollections.observableArrayList(moveTo, cubicCurveTo);

        centerX = new SimpleDoubleProperty();
        centerX.bind((moveTo.xProperty().multiply(0.125))
                .add(cubicCurveTo.controlX1Property().multiply(0.375))
                .add(cubicCurveTo.controlX2Property().multiply(0.375))
                .add(cubicCurveTo.xProperty().multiply(0.125)));
        centerY = new SimpleDoubleProperty();
        centerY.bind((moveTo.yProperty().multiply(0.125))
                .add(cubicCurveTo.controlY1Property().multiply(0.375))
                .add(cubicCurveTo.controlY2Property().multiply(0.375))
                .add(cubicCurveTo.yProperty().multiply(0.125)));
    }

    public Line getLine() {
        return line;
    }

    public ObservableList<PathElement> getPoint() {
        return path;
    }

    public double getCenterX() {
        return centerX.get();
    }

    public DoubleProperty centerXProperty() {
        return centerX;
    }

    public double getCenterY() {
        return centerY.get();
    }

    public DoubleProperty centerYProperty() {
        return centerY;
    }
}
