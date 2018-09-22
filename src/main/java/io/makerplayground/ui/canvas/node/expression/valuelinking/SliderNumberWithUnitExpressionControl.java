package io.makerplayground.ui.canvas.node.expression.valuelinking;

import io.makerplayground.device.Parameter;
import io.makerplayground.helper.NumberWithUnit;
import io.makerplayground.helper.Unit;
import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.expression.Expression;
import io.makerplayground.ui.canvas.node.expression.numberwithunit.NumberWithUnitControl;
import io.makerplayground.ui.canvas.node.expression.numberwithunit.SliderWithUnit;

import java.util.List;

public class SliderNumberWithUnitExpressionControl extends NumberWithUnitExpressionControl {

    public SliderNumberWithUnitExpressionControl(Parameter p, List<ProjectValue> projectValues, Expression expression) {
        super(p, projectValues, expression);
    }

    @Override
    protected NumberWithUnitControl createNumberWithUnitControl(double min, double max, List<Unit> unit, NumberWithUnit initialValue) {
        return new SliderWithUnit(min, max, unit, initialValue);
    }
}
