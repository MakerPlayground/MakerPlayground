package io.makerplayground.ui.canvas.node.expression;

import io.makerplayground.device.shared.NumberWithUnit;
import io.makerplayground.device.shared.Parameter;
import io.makerplayground.device.shared.Unit;
import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.expression.Expression;
import io.makerplayground.ui.canvas.node.expression.numberwithunit.NumberWithUnitControl;
import io.makerplayground.ui.canvas.node.expression.numberwithunit.NumericTextFieldWithUnit;
import javafx.collections.ObservableList;

import java.util.List;

public class NumericTextFieldWithUnitExpressionControl extends NumberWithUnitExpressionControl {
    public NumericTextFieldWithUnitExpressionControl(Parameter p, ObservableList<ProjectValue> projectValues, Expression expression) {
        super(p, projectValues, expression);
    }

    @Override
    protected NumberWithUnitControl createNumberWithUnitControl(double min, double max, List<Unit> unit, NumberWithUnit initialValue) {
        return new NumericTextFieldWithUnit(min, max, unit, initialValue);
    }
}
