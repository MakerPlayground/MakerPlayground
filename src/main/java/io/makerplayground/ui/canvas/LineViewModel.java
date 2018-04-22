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
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.shape.*;

/**
 *
 */
public class LineViewModel {
    private final Line line;
    private final ObservableList<PathElement> path;

    private final DoubleProperty centerX;
    private final DoubleProperty centerY;

    private final MoveTo moveTo;
    private final LineTo lineTo;
    private final CubicCurveTo cubicCurveTo1, cubicCurveTo2;

    public LineViewModel(Line line) {
        this.line = line;

        moveTo = new MoveTo();
        moveTo.xProperty().bind(line.getSource().destPortXProperty());
        moveTo.yProperty().bind(line.getSource().destPortYProperty());

        cubicCurveTo1 = new CubicCurveTo();
        lineTo = new LineTo();
        cubicCurveTo2 = new CubicCurveTo();

        line.getSource().destPortXProperty().addListener(observable -> updateLine());
        line.getSource().destPortYProperty().addListener(observable -> updateLine());
        line.getDestination().sourcePortXProperty().addListener(observable -> updateLine());
        line.getDestination().sourcePortYProperty().addListener(observable -> updateLine());

        this.path = FXCollections.observableArrayList(moveTo, cubicCurveTo1, lineTo, cubicCurveTo2);
        updateLine();

        centerX = new SimpleDoubleProperty();
//        centerX.bind((moveTo.xProperty().multiply(0.125))
//                .add(cubicCurveTo1.controlX1Property().multiply(0.375))
//                .add(cubicCurveTo1.controlX2Property().multiply(0.375))
//                .add(cubicCurveTo1.xProperty().multiply(0.125)));
        centerY = new SimpleDoubleProperty();
//        centerY.bind((moveTo.yProperty().multiply(0.125))
//                .add(cubicCurveTo1.controlY1Property().multiply(0.375))
//                .add(cubicCurveTo1.controlY2Property().multiply(0.375))
//                .add(cubicCurveTo1.yProperty().multiply(0.125)));
    }

    private static final int STRAIGHT_LINE_THRESHOLD = 2;
    private static final int LOOP_THRESHOLD = 150;
    private static final int LOOP_HEIGHT = 120;
    private static final int MIN_ARC_THRESHOLD = 75;

    private void updateLine() {
        double sx = line.getSource().getDestPortX();
        double sy = line.getSource().getDestPortY();
        double ex = line.getDestination().getSourcePortX();
        double ey = line.getDestination().getSourcePortY();
        double mx = sx + (ex-sx) / 2;
        double my = sy + (ey-sy) / 2;

        if (ex >= sx) {
            if (Math.abs(sy - ey) <= STRAIGHT_LINE_THRESHOLD) {
                // unused
                cubicCurveTo1.setControlX1(sx);
                cubicCurveTo1.setControlY1(sy);
                cubicCurveTo1.setControlX2(sx);
                cubicCurveTo1.setControlY2(sy);
                cubicCurveTo1.setX(sx);
                cubicCurveTo1.setY(sy);

                lineTo.setX(ex);
                lineTo.setY(ey);

                // unused
                cubicCurveTo2.setControlX1(ex);
                cubicCurveTo2.setControlY1(ey);
                cubicCurveTo2.setControlX2(ex);
                cubicCurveTo2.setControlY2(ey);
                cubicCurveTo2.setX(ex);
                cubicCurveTo2.setY(ey);
            } else {
                // unused
                cubicCurveTo1.setControlX1(sx);
                cubicCurveTo1.setControlY1(sy);
                cubicCurveTo1.setControlX2(sx);
                cubicCurveTo1.setControlY2(sy);
                cubicCurveTo1.setX(sx);
                cubicCurveTo1.setY(sy);

                // unused
                lineTo.setX(sx);
                lineTo.setY(sy);

                cubicCurveTo2.setControlX1(Math.max((sx + 3 * ex) / 4, sx + MIN_ARC_THRESHOLD)); // sx + (ex-sx) * 0.75
                cubicCurveTo2.setControlY1(sy);
                cubicCurveTo2.setControlX2(Math.min((3 * sx + ex) / 4, ex - MIN_ARC_THRESHOLD)); // sx + (ex-sx) * 0.25
                cubicCurveTo2.setControlY2(ey);
                cubicCurveTo2.setX(ex);
                cubicCurveTo2.setY(ey);
            }
        } else {
            if (Math.abs(sy - ey) <= LOOP_THRESHOLD) {
                double cy = Math.min(sy, ey) - LOOP_HEIGHT;

                cubicCurveTo1.setControlX1(sx + MIN_ARC_THRESHOLD);
                cubicCurveTo1.setControlY1(sy);
                cubicCurveTo1.setControlX2(sx + MIN_ARC_THRESHOLD);
                cubicCurveTo1.setControlY2(cy);
                cubicCurveTo1.setX(ex + 7*(sx-ex)/8);
                cubicCurveTo1.setY(cy);

                lineTo.setX(ex + (sx-ex)/8);
                lineTo.setY(cy);

                cubicCurveTo2.setControlX1(ex - MIN_ARC_THRESHOLD);
                cubicCurveTo2.setControlY1(cy);
                cubicCurveTo2.setControlX2(ex - MIN_ARC_THRESHOLD);
                cubicCurveTo2.setControlY2(ey);
                cubicCurveTo2.setX(ex);
                cubicCurveTo2.setY(ey);
            } else {
                cubicCurveTo1.setControlX1(sx + MIN_ARC_THRESHOLD);
                cubicCurveTo1.setControlY1(sy);
                cubicCurveTo1.setControlX2(sx + MIN_ARC_THRESHOLD);
                cubicCurveTo1.setControlY2(my);
                cubicCurveTo1.setX(ex + 3*(sx-ex)/4);
                cubicCurveTo1.setY(my);

                lineTo.setX(ex + (sx-ex)/4);
                lineTo.setY(my);

                cubicCurveTo2.setControlX1(ex - MIN_ARC_THRESHOLD);
                cubicCurveTo2.setControlY1(my);
                cubicCurveTo2.setControlX2(ex - MIN_ARC_THRESHOLD);
                cubicCurveTo2.setControlY2(ey);
                cubicCurveTo2.setX(ex);
                cubicCurveTo2.setY(ey);
            }
        }
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
