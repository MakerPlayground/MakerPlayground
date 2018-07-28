package io.makerplayground.ui.canvas.node.expressioncontrol;

import io.makerplayground.helper.NumberWithUnit;
import io.makerplayground.helper.Unit;
import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.expression.Expression;
import io.makerplayground.ui.canvas.node.usersetting.expression.SpinnerWithUnit;

import java.util.List;

public class SpinnerNumberWithUnitExpressionControl extends NumberWithUnitExpressionControl {

    public SpinnerNumberWithUnitExpressionControl(double minimumValue, double maximumValue, List<Unit> units, List<ProjectValue> projectValues, Expression expression) {
        super(minimumValue, maximumValue, units, projectValues, expression);
    }

    @Override
    protected NumberWithUnitControl createNumberWithUnitControl(double min, double max, List<Unit> unit, NumberWithUnit initialValue) {
        return new SpinnerWithUnit(min, max, unit, initialValue);
    }
}
