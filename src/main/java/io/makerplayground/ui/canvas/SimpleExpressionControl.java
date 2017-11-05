package io.makerplayground.ui.canvas;

import io.makerplayground.device.NumericConstraint;
import io.makerplayground.device.Value;
import io.makerplayground.project.expression.Expression;
import io.makerplayground.project.expression.SimpleExpression;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import org.controlsfx.control.RangeSlider;

public class SimpleExpressionControl extends HBox {
    private final SimpleExpression expression;
    private final Value value;

    private final RangeSlider rangeSlider = new RangeSlider();
    private final TextField lowTextField = new TextField();
    private final TextField highTextField = new TextField();

    public SimpleExpressionControl(SimpleExpression expression, Value value) {
        this.expression = expression;
        this.value = value;
        initView();
    }

    private void initView() {
        NumericConstraint constraint = (NumericConstraint) value.getConstraint();

        rangeSlider.setMax(constraint.getMax());
        rangeSlider.setMin(constraint.getMin());
        rangeSlider.setHighValue(expression.getHighValue());
        rangeSlider.setLowValue(expression.getLowValue());
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

        // disable this control if the expression is disabled
        disableProperty().bind(expression.enableProperty().not());
    }

    private void updateExpression() {
        expression.setLowValue(rangeSlider.getLowValue());
        expression.setHighValue(rangeSlider.getHighValue());
    }

    public Expression getExpression() {
        return expression;
    }
}
