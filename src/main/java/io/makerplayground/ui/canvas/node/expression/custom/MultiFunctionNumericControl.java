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

package io.makerplayground.ui.canvas.node.expression.custom;

import io.makerplayground.device.shared.Parameter;
import io.makerplayground.device.shared.Unit;
import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.expression.CustomNumberExpression;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.text.DecimalFormat;
import java.util.List;

public class MultiFunctionNumericControl extends HBox {

    private final ReadOnlyObjectWrapper<CustomNumberExpression> expression;

    private static final DecimalFormat df = new DecimalFormat("0.###E0");

    public MultiFunctionNumericControl(Parameter p, ObservableList<ProjectValue> projectValues, CustomNumberExpression expression) {
        this.expression = new ReadOnlyObjectWrapper<>(expression);

        NumericChipField chipField = new NumericChipField(expression, projectValues);
        chipField.expressionProperty().addListener((observable, oldValue, newValue) -> this.expression.set(newValue));
        getChildren().add(chipField);

        if (!p.getUnit().isEmpty() && p.getUnit().get(0) != Unit.NOT_SPECIFIED) {
            Label unitLabel = new Label(p.getUnit().get(0).toString());
            unitLabel.setMinHeight(27);
            getChildren().add(unitLabel);
        }

        String minValue;
        if (p.getMinimumValue() == -Double.MAX_VALUE || p.getMinimumValue() == Integer.MIN_VALUE) {
            minValue = "-\u221E";
        } else if (p.getMinimumValue() > 99999 || p.getMinimumValue() < -99999) {
            minValue = df.format(p.getMinimumValue());
        } else {
            minValue = String.valueOf(p.getMinimumValue());
        }
        String maxValue;
        if (p.getMaximumValue() == Double.MAX_VALUE || p.getMaximumValue() == Integer.MAX_VALUE) {
            maxValue = "\u221E";
        } else if (p.getMaximumValue() > 99999 || p.getMaximumValue() < -99999) {
            maxValue = df.format(p.getMaximumValue());
        } else {
            maxValue = String.valueOf(p.getMaximumValue());
        }
        Label rangeLabel = new Label("(" + minValue + ", " + maxValue + ")");
        rangeLabel.setMinHeight(27);
        getChildren().add(rangeLabel);

        setAlignment(Pos.TOP_LEFT);
        setSpacing(5);
    }

    public CustomNumberExpression getExpression() {
        return expression.get();
    }

    public ReadOnlyObjectProperty<CustomNumberExpression> expressionProperty() {
        return expression.getReadOnlyProperty();
    }
}
