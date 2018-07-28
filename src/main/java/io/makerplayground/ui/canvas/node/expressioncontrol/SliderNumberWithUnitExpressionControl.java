package io.makerplayground.ui.canvas.node.expressioncontrol;

import io.makerplayground.helper.NumberWithUnit;
import io.makerplayground.helper.Unit;
import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.expression.Expression;
import io.makerplayground.ui.canvas.node.usersetting.expression.SliderWithUnit;

import java.util.List;

public class SliderNumberWithUnitExpressionControl extends NumberWithUnitExpressionControl {

    public SliderNumberWithUnitExpressionControl(double minimumValue, double maximumValue, List<Unit> units, List<ProjectValue> projectValues, Expression expression) {
        super(minimumValue, maximumValue, units, projectValues, expression);
    }

    @Override
    protected NumberWithUnitControl createNumberWithUnitControl(double min, double max, List<Unit> unit, NumberWithUnit initialValue) {
        return new SliderWithUnit(min, max, unit, initialValue);
    }
}
