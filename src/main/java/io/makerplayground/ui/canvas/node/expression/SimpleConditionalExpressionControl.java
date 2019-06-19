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

package io.makerplayground.ui.canvas.node.expression;

import io.makerplayground.device.shared.constraint.NumericConstraint;
import io.makerplayground.project.expression.NumberInRangeExpression;
import io.makerplayground.ui.canvas.node.expression.numberwithunit.RangeSliderWithOperator;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

import java.text.DecimalFormat;

public class SimpleConditionalExpressionControl extends HBox {
    private final ReadOnlyObjectWrapper<NumberInRangeExpression> expression;
    private final Value value;

    private final RangeSliderWithOperator rangeSlider = new RangeSliderWithOperator();
    private final TextField lowTextField = new TextField();
    private final TextField highTextField = new TextField();

    private final DecimalFormat df = new DecimalFormat("0.####");

    private final StringConverter<Number> formatter = new StringConverter<>() {
        private final DecimalFormat df = new DecimalFormat("0.#");
        @Override
        public String toString(Number object) {
            return df.format(object.doubleValue());
        }

        @Override
        public Number fromString(String string) {
            return Double.parseDouble(string);
        }
    };

    public SimpleConditionalExpressionControl(NumberInRangeExpression expression, Value value) {
        this.expression = new ReadOnlyObjectWrapper<>(expression);
        this.value = value;
        initView();
    }

    private void initView() {
        NumericConstraint constraint = (NumericConstraint) value.getConstraint();
        rangeSlider.setLabelFormatter(formatter);
        rangeSlider.setMax(constraint.getMax());
        rangeSlider.setMin(constraint.getMin());
        rangeSlider.setHighValue(expression.get().getHighValue());
        rangeSlider.setLowValue(expression.get().getLowValue());
        // This line is needed for proper operation of the RangeSlider (See: https://bitbucket.org/controlsfx/controlsfx/issues/728/rangeslider-order-of-setting-low-and-high)
        rangeSlider.setHighValue(expression.get().getHighValue());
        rangeSlider.setLowThumbOperator(expression.get().getLowOperator());
        rangeSlider.setHighThumbOperator(expression.get().getHighOperator());
        rangeSlider.setShowTickMarks(true);
        rangeSlider.setShowTickLabels(true);
        rangeSlider.setBlockIncrement(1);
        rangeSlider.lowValueProperty().addListener((observable, oldValue, newValue) -> {
            lowTextField.setText(df.format(rangeSlider.getLowValue()));
            updateExpression();
        });
        rangeSlider.highValueProperty().addListener((observable, oldValue, newValue) -> {
            highTextField.setText(df.format(rangeSlider.getHighValue()));
            updateExpression();
        });
        rangeSlider.lowThumbOperatorProperty().addListener((observable, oldValue, newValue) -> updateExpression());
        rangeSlider.highThumbOperatorProperty().addListener((observable, oldValue, newValue) -> updateExpression());

        lowTextField.getStyleClass().add("rangeSliderTextField");
        lowTextField.setText(String.valueOf(rangeSlider.getLowValue()));
        lowTextField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.TAB) {
                try {
                    double value = Double.parseDouble(lowTextField.getText());
                    rangeSlider.setLowValue(value);
                } catch (NumberFormatException e) {
                    lowTextField.setText(String.valueOf(rangeSlider.getLowValue()));
                }
            }
        });
        lowTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                try {
                    double value = Double.parseDouble(lowTextField.getText());
                    rangeSlider.setLowValue(value);
                } catch (NumberFormatException e) {
                    lowTextField.setText(String.valueOf(rangeSlider.getLowValue()));
                }
            }
        });

        highTextField.getStyleClass().add("rangeSliderTextField");
        highTextField.setText(String.valueOf(rangeSlider.getHighValue()));
        highTextField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.TAB) {
                try {
                    double value = Double.parseDouble(highTextField.getText());
                    rangeSlider.setHighValue(value);
                } catch (NumberFormatException e) {
                    highTextField.setText(String.valueOf(rangeSlider.getHighValue()));
                }
            }
        });
        highTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                try {
                    double value = Double.parseDouble(highTextField.getText());
                    rangeSlider.setHighValue(value);
                } catch (NumberFormatException e) {
                    highTextField.setText(String.valueOf(rangeSlider.getHighValue()));
                }
            }
        });

        setSpacing(5);
        getChildren().addAll(lowTextField, rangeSlider, highTextField);
    }

    private void updateExpression() {
        expression.set(expression.get().setLowValue(rangeSlider.getLowValue())
                .setHighValue(rangeSlider.getHighValue())
                .setLowOperator(rangeSlider.getLowThumbOperator())
                .setHighOperator(rangeSlider.getHighThumbOperator()));
    }

    public NumberInRangeExpression getExpression() {
        return expression.get();
    }

    public ReadOnlyObjectProperty<NumberInRangeExpression> expressionProperty() {
        return expression.getReadOnlyProperty();
    }

    public void useIntegerOnly(boolean b) {
        if (b) {
            int lowValue = (int) Math.round(rangeSlider.getLowValue());
            int highValue = (int) Math.round(rangeSlider.getHighValue());
            lowValue = lowValue < rangeSlider.getMin() ? (int) Math.ceil(rangeSlider.getMin()) : lowValue;
            highValue = highValue > rangeSlider.getMax() ? (int) Math.floor(rangeSlider.getMax()) : highValue;
            rangeSlider.setLowValue(lowValue);
            rangeSlider.setHighValue(highValue);

            rangeSlider.lowValueProperty().addListener(this::forceLowToInt);
            rangeSlider.highValueProperty().addListener(this::forceHighToInt);
        } else {
            rangeSlider.lowValueProperty().removeListener(this::forceLowToInt);
            rangeSlider.highValueProperty().removeListener(this::forceHighToInt);
        }
    }

    private void forceLowToInt(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        rangeSlider.setLowValue(newValue.intValue());
    }

    private void forceHighToInt(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        rangeSlider.setHighValue(newValue.intValue());
    }
}
