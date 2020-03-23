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

package io.makerplayground.ui.canvas.node.expression;

import io.makerplayground.device.shared.DataType;
import io.makerplayground.device.shared.Unit;
import io.makerplayground.device.shared.Value;
import io.makerplayground.device.shared.constraint.NumericConstraint;
import io.makerplayground.project.ProjectDevice;
import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.expression.*;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.util.List;

public class ConditionalExpressionControl extends HBox {
    private final ProjectDevice projectDevice;
    private final Value value;
    private final Unit unit;
    private final List<ProjectValue> projectValues;
    private final ReadOnlyObjectWrapper<Expression> expression = new ReadOnlyObjectWrapper<>();
    private final boolean customExpressionOnly;

    public ConditionalExpressionControl(ProjectDevice projectDevice, Value v, List<ProjectValue> projectValues, Expression expression) {
        this.projectDevice = projectDevice;
        this.value = v;
        this.unit = ((NumericConstraint) v.getConstraint()).getUnit();
        this.projectValues = projectValues;
        this.expression.set(expression);
        double min = ((NumericConstraint) v.getConstraint()).getMin();
        double max = ((NumericConstraint) v.getConstraint()).getMax();
        this.customExpressionOnly = (min == -Double.MAX_VALUE || min == Integer.MIN_VALUE || max == Double.MAX_VALUE || max == Integer.MAX_VALUE);
        initView();
    }

    private void initView() {
        getChildren().clear();
        if (this.customExpressionOnly) {
            if (getExpression() instanceof ConditionalExpression) {
                CustomConditionalExpressionControl expressionControl = new CustomConditionalExpressionControl((ConditionalExpression) getExpression(), projectValues, unit);
                expressionControl.expressionProperty().addListener((observable, oldValue, newValue) -> expression.set(newValue));
                getChildren().addAll(expressionControl);
                setSpacing(5);
            } else {
                throw new IllegalStateException("Found unsupported expression!!! " + getExpression().getType());
            }
        } else {
            RadioMenuItem basicRadioMenuItem = new RadioMenuItem("Basic");
            RadioMenuItem customRadioMenuItem = new RadioMenuItem("Custom");

            ToggleGroup toggleGroup = new ToggleGroup();
            toggleGroup.getToggles().addAll(basicRadioMenuItem, customRadioMenuItem);

            ContextMenu contextMenu = new ContextMenu();
            contextMenu.getItems().addAll(basicRadioMenuItem, customRadioMenuItem);

            ImageView configButton = new ImageView(new Image(getClass().getResourceAsStream("/css/canvas/node/expressioncontrol/advance-setting.png")));
            configButton.setFitWidth(25);
            configButton.setFitHeight(25);
            configButton.setStyle("-fx-cursor: hand;");
            configButton.setPreserveRatio(true);
            configButton.setOnMousePressed(event -> contextMenu.show(configButton, Side.BOTTOM, 0, 0));

            Node control;
            if (getExpression() instanceof NumberInRangeExpression) {
                SimpleConditionalExpressionControl expressionControl = new SimpleConditionalExpressionControl((NumberInRangeExpression) getExpression(), value);
                expressionControl.useIntegerOnly(value.getType() == DataType.INTEGER);
                expressionControl.expressionProperty().addListener((observable, oldValue, newValue) -> expression.set(newValue));
                toggleGroup.selectToggle(basicRadioMenuItem);
                control = expressionControl;
            } else if (getExpression() instanceof ConditionalExpression) {
                CustomConditionalExpressionControl expressionControl = new CustomConditionalExpressionControl((ConditionalExpression) getExpression(), projectValues, unit);
                expressionControl.expressionProperty().addListener((observable, oldValue, newValue) -> expression.set(newValue));
                toggleGroup.selectToggle(customRadioMenuItem);
                control = expressionControl;
            } else {
                throw new IllegalStateException("Found unsupported expression!!!");
            }

            getChildren().addAll(control, configButton);
            setSpacing(5);

            toggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue == basicRadioMenuItem) {
                    expression.set(new NumberInRangeExpression(projectDevice, value));
                } else if (newValue == customRadioMenuItem) {
                    expression.set(new ConditionalExpression(projectDevice, value));
                }
                initView();
            });
        }
    }

    public Expression getExpression() {
        return expression.get();
    }

    public ReadOnlyObjectProperty<Expression> expressionProperty() {
        return expression.getReadOnlyProperty();
    }
}
