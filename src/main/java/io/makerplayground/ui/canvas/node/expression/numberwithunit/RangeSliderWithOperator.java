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

package io.makerplayground.ui.canvas.node.expression.numberwithunit;

import io.makerplayground.project.term.Operator;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import org.controlsfx.control.RangeSlider;

import java.util.Map;

public class RangeSliderWithOperator extends RangeSlider {
    private Pane lowThumb;
    private Pane highThumb;

    private boolean lowThumbDragged = false;
    private boolean highThumbDragged = false;

    private final ObjectProperty<Operator> lowThumbOperator;
    private final ObjectProperty<Operator> highThumbOperator;

    public RangeSliderWithOperator() {
        this(0, 1, 0.25, Operator.GREATER_THAN, 0.75, Operator.LESS_THAN);
    }

    public RangeSliderWithOperator(double min, double max, double lowValue, Operator lowOperator, double highValue, Operator highOperator) {
        super(min, max, lowValue, highValue);
        lowThumbOperator = new SimpleObjectProperty<>(lowOperator);
        highThumbOperator = new SimpleObjectProperty<>(highOperator);
        applyStyle();
    }

    private void applyStyle() {
        // initialize text to be inserted to the slider's thumb
        Text greaterThanText = new Text(Operator.GREATER_THAN.getDisplayString());
        greaterThanText.setBoundsType(TextBoundsType.VISUAL);

        Text greaterThanOrEqualText = new Text(Operator.GREATER_THAN_OR_EQUAL.getDisplayString());
        greaterThanOrEqualText.setBoundsType(TextBoundsType.VISUAL);

        Text lessThanText = new Text(Operator.LESS_THAN.getDisplayString());
        lessThanText.setBoundsType(TextBoundsType.VISUAL);

        Text lessThanOrEqualText = new Text(Operator.LESS_THAN_OR_EQUAL.getDisplayString());
        lessThanOrEqualText.setBoundsType(TextBoundsType.VISUAL);

        Map<Operator, Text> operatorTextMap = Map.of(Operator.GREATER_THAN, greaterThanText
                , Operator.GREATER_THAN_OR_EQUAL, greaterThanOrEqualText
                , Operator.LESS_THAN, lessThanText
                , Operator.LESS_THAN_OR_EQUAL, lessThanOrEqualText);

        // when the range slider is drawn, the layout bounds are changed. we then draw the low and high thumbs operator text.
        layoutBoundsProperty().addListener(new ChangeListener<>() {
            @Override
            public void changed(ObservableValue<? extends Bounds> observable, Bounds oldValue, Bounds newValue) {
                lowThumb = (Pane) RangeSliderWithOperator.this.lookup(".range-slider .low-thumb");
                lowThumb.getChildren().add(operatorTextMap.get(lowThumbOperator.get()));

                highThumb = (Pane) RangeSliderWithOperator.this.lookup(".range-slider .high-thumb");
                highThumb.getChildren().add(operatorTextMap.get(highThumbOperator.get()));

                RangeSliderWithOperator.this.initThumbEventHandler();
                RangeSliderWithOperator.this.layoutBoundsProperty().removeListener(this);
            }
        });

        // update thumb when it's operator has changed
        lowThumbOperator.addListener((observable, oldValue, newValue) -> {
            if (lowThumb != null) {
                lowThumb.getChildren().removeAll(operatorTextMap.values());
                lowThumb.getChildren().add(operatorTextMap.get(newValue));
            }
        });
        highThumbOperator.addListener((observable, oldValue, newValue) -> {
            if (highThumb != null) {
                highThumb.getChildren().removeAll(operatorTextMap.values());
                highThumb.getChildren().add(operatorTextMap.get(newValue));
            }
        });
    }

    private void initThumbEventHandler() {
        // toggle operator when the thumb is pressed (MousePressed and MouseDragged event filter are also needed to
        // differentiate between pressing and dragging the thumb so that dragging the thumb will not changed the operator
        lowThumb.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            lowThumbDragged = false;
        });
        lowThumb.addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> {
            lowThumbDragged = true;
        });
        lowThumb.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
            if (!lowThumbDragged) {
                if (lowThumbOperator.get() == Operator.GREATER_THAN) {
                    lowThumbOperator.set(Operator.GREATER_THAN_OR_EQUAL);
                } else {
                    lowThumbOperator.set(Operator.GREATER_THAN);
                }
            }
        });

        highThumb.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            highThumbDragged = false;
        });
        highThumb.addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> {
            highThumbDragged = true;
        });
        highThumb.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
            if (!highThumbDragged) {
                if (highThumbOperator.get() == Operator.LESS_THAN) {
                    highThumbOperator.set(Operator.LESS_THAN_OR_EQUAL);
                } else {
                    highThumbOperator.set(Operator.LESS_THAN);
                }
            }
        });
    }

    public Operator getLowThumbOperator() {
        return lowThumbOperator.get();
    }

    public ObjectProperty<Operator> lowThumbOperatorProperty() {
        return lowThumbOperator;
    }

    public void setLowThumbOperator(Operator lowThumbOperator) {
        this.lowThumbOperator.set(lowThumbOperator);
    }

    public Operator getHighThumbOperator() {
        return highThumbOperator.get();
    }

    public ObjectProperty<Operator> highThumbOperatorProperty() {
        return highThumbOperator;
    }

    public void setHighThumbOperator(Operator highThumbOperator) {
        this.highThumbOperator.set(highThumbOperator);
    }
}
