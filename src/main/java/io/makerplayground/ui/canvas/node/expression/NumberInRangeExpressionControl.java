package io.makerplayground.ui.canvas.node.expression;

import io.makerplayground.device.NumericConstraint;
import io.makerplayground.device.Value;
import io.makerplayground.project.expression.NumberInRangeExpression;
import io.makerplayground.ui.canvas.node.expression.numberwithunit.RangeSliderWithOperator;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class NumberInRangeExpressionControl extends HBox {
    private final ReadOnlyObjectWrapper<NumberInRangeExpression> expression;
    private final Value value;

    private final RangeSliderWithOperator rangeSlider = new RangeSliderWithOperator();
    private final TextField lowTextField = new TextField();
    private final TextField highTextField = new TextField();

    public NumberInRangeExpressionControl(NumberInRangeExpression expression, Value value) {
        this.expression = new ReadOnlyObjectWrapper<>(expression);
        this.value = value;
        initView();
    }

    private void initView() {
        NumericConstraint constraint = (NumericConstraint) value.getConstraint();

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
            lowTextField.setText(String.valueOf(rangeSlider.getLowValue()));
            updateExpression();
        });
        rangeSlider.highValueProperty().addListener((observable, oldValue, newValue) -> {
            highTextField.setText(String.valueOf(rangeSlider.getHighValue()));
            updateExpression();
        });
        rangeSlider.lowThumbOperatorProperty().addListener((observable, oldValue, newValue) -> updateExpression());
        rangeSlider.highThumbOperatorProperty().addListener((observable, oldValue, newValue) -> updateExpression());

        lowTextField.setText(String.valueOf(rangeSlider.getLowValue()));
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

        highTextField.setText(String.valueOf(rangeSlider.getHighValue()));
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
}
