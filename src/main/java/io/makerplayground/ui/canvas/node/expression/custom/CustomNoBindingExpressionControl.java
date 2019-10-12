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

package io.makerplayground.ui.canvas.node.expression.custom;

import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.expression.CustomNumberExpression;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.util.List;

public class CustomNoBindingExpressionControl extends GridPane {

    private final ReadOnlyObjectWrapper<CustomNumberExpression> expression;
    private final List<ProjectValue> projectValues;

    public CustomNoBindingExpressionControl(CustomNumberExpression expression, List<ProjectValue> projectValues) {
        this.expression = new ReadOnlyObjectWrapper<>(expression);
        this.projectValues = projectValues;

        Label fromLabel = new Label("set to");
        fromLabel.setMinHeight(25); // a hack to center the label to the height of 1 row control when the control spans to multiple rows
        GridPane.setConstraints(fromLabel, 0, 0, 1, 1, HPos.LEFT, VPos.TOP);

        NumericChipField chipField = new NumericChipField(expression, projectValues);
        chipField.expressionProperty().addListener((observable, oldValue, newValue) -> this.expression.set(newValue));
        GridPane.setConstraints(chipField, 1, 0);

        setHgap(10);
        setVgap(5);
        getChildren().addAll(fromLabel, chipField);
    }

    public CustomNumberExpression getExpression() {
        return expression.get();
    }

    public ReadOnlyObjectProperty<CustomNumberExpression> expressionProperty() {
        return expression.getReadOnlyProperty();
    }
}
