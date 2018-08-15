package io.makerplayground.project.expression;

import io.makerplayground.device.NumericConstraint;
import io.makerplayground.device.Value;
import io.makerplayground.helper.NumberWithUnit;
import io.makerplayground.helper.Unit;
import io.makerplayground.project.ProjectDevice;
import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.term.NumberWithUnitTerm;
import io.makerplayground.project.term.OperatorTerm;
import io.makerplayground.project.term.ValueTerm;

import java.util.List;

public class NumberInRangeExpression extends Expression {

    public NumberInRangeExpression(ProjectDevice device, Value value) {
        super(Expression.Type.NUMBER_IN_RANGE);
        NumericConstraint constraint = (NumericConstraint) value.getConstraint();

        terms.addAll(List.of(new ValueTerm(new ProjectValue(device, value))
                , new OperatorTerm(OperatorTerm.Operator.LESS_THAN)
                , new NumberWithUnitTerm(new NumberWithUnit((constraint.getMax() - constraint.getMin()) * 0.75 + constraint.getMin(), constraint.getUnit()))
                , new OperatorTerm(OperatorTerm.Operator.AND)
                , new ValueTerm(new ProjectValue(device, value))
                , new OperatorTerm(OperatorTerm.Operator.GREATER_THAN)
                , new NumberWithUnitTerm(new NumberWithUnit((constraint.getMax() - constraint.getMin()) * 0.25 + constraint.getMin(), constraint.getUnit())))
        );
    }

    protected NumberInRangeExpression(NumberInRangeExpression s) {
        super(s);
    }

    public double getLowValue() {
        return ((NumberWithUnit) getTerms().get(6).getValue()).getValue();
    }

    public NumberInRangeExpression setLowValue(double d) {
        NumberInRangeExpression newExpression = new NumberInRangeExpression(this);
        newExpression.terms.set(6, new NumberWithUnitTerm(new NumberWithUnit(d, getUnit())));
        return newExpression;
    }

    public double getHighValue() {
        return ((NumberWithUnit) getTerms().get(2).getValue()).getValue();
    }

    public NumberInRangeExpression setHighValue(double d) {
        NumberInRangeExpression newExpression = new NumberInRangeExpression(this);
        newExpression.terms.set(2, new NumberWithUnitTerm(new NumberWithUnit(d, getUnit())));
        return newExpression;
    }

    public Unit getUnit() {
        return ((NumberWithUnit) getTerms().get(2).getValue()).getUnit();
    }

    public NumberInRangeExpression setUnit(Unit u) {
        NumberInRangeExpression newExpression = new NumberInRangeExpression(this);
        newExpression.terms.set(6, new NumberWithUnitTerm(new NumberWithUnit(getLowValue(), u)));
        newExpression.terms.set(2, new NumberWithUnitTerm(new NumberWithUnit(getHighValue(), u)));
        return newExpression;
    }

    @Override
    public boolean isValid() {
        return true;    // simple expression is always valid
    }
}
