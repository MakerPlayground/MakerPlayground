package io.makerplayground.project.expression;

import io.makerplayground.device.NumericConstraint;
import io.makerplayground.device.Value;
import io.makerplayground.helper.NumberWithUnit;
import io.makerplayground.helper.Unit;
import io.makerplayground.project.ProjectDevice;
import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.chip.ChipOperator;
import io.makerplayground.project.chip.NumberWithUnitTerm;
import io.makerplayground.project.chip.OperatorTerm;
import io.makerplayground.project.chip.ValueTerm;

public class NumberInRangeExpression extends Expression {

    public NumberInRangeExpression(ProjectDevice device, Value value) {
        NumericConstraint constraint = (NumericConstraint) value.getConstraint();
        getTerms().addAll(new ValueTerm(new ProjectValue(device, value))
                , new OperatorTerm(ChipOperator.LESS_THAN)
                , new NumberWithUnitTerm(new NumberWithUnit((constraint.getMax() - constraint.getMin()) * 0.75 + constraint.getMin(), constraint.getUnit()))
                , new OperatorTerm(ChipOperator.AND)
                , new ValueTerm(new ProjectValue(device, value))
                , new OperatorTerm(ChipOperator.GREATER_THAN)
                , new NumberWithUnitTerm(new NumberWithUnit((constraint.getMax() - constraint.getMin()) * 0.25 + constraint.getMin(), constraint.getUnit())));
    }

    protected NumberInRangeExpression(NumberInRangeExpression s) {
        super(s);
    }

    public double getLowValue() {
        return ((NumberWithUnit) getTerms().get(6).getValue()).getValue();
    }

    public void setLowValue(double d) {
        getTerms().set(6, new NumberWithUnitTerm(new NumberWithUnit(d, getUnit())));
    }

    public double getHighValue() {
        return ((NumberWithUnit) getTerms().get(2).getValue()).getValue();
    }

    public void setHighValue(double d) {
        getTerms().set(2, new NumberWithUnitTerm(new NumberWithUnit(d, getUnit())));
    }

    public Unit getUnit() {
        return ((NumberWithUnit) getTerms().get(2).getValue()).getUnit();
    }

    public void setUnit(Unit u) {
        getTerms().set(6, new NumberWithUnitTerm(new NumberWithUnit(getLowValue(), u)));
        getTerms().set(2, new NumberWithUnitTerm(new NumberWithUnit(getHighValue(), u)));
    }

    @Override
    public boolean isValid() {
        return true;    // simple expression is always valid
    }
}
