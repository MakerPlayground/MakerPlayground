package io.makerplayground.project.expression;

import io.makerplayground.device.NumericConstraint;
import io.makerplayground.device.Parameter;
import io.makerplayground.helper.NumberWithUnit;
import io.makerplayground.helper.Unit;
import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.term.NumberWithUnitTerm;
import io.makerplayground.project.term.OperatorTerm;
import io.makerplayground.project.term.Term;
import io.makerplayground.project.term.ValueTerm;

import java.util.Collections;
import java.util.List;

public class ValueLinkingExpression extends Expression {

    private final Parameter destParam;

    private static final List<Term.Type> termType = List.of(Term.Type.OPERATOR, Term.Type.OPERATOR, Term.Type.OPERATOR
            , Term.Type.VALUE, Term.Type.OPERATOR, Term.Type.NUMBER, Term.Type.OPERATOR, Term.Type.OPERATOR, Term.Type.OPERATOR
            , Term.Type.NUMBER, Term.Type.OPERATOR, Term.Type.NUMBER, Term.Type.OPERATOR, Term.Type.OPERATOR, Term.Type.OPERATOR
            , Term.Type.OPERATOR, Term.Type.NUMBER, Term.Type.OPERATOR, Term.Type.NUMBER, Term.Type.OPERATOR, Term.Type.OPERATOR
            , Term.Type.OPERATOR, Term.Type.NUMBER);
    private static final List<Object> termValue = List.of(OperatorTerm.Operator.OPEN_PARENTHESIS, OperatorTerm.Operator.OPEN_PARENTHESIS
            , OperatorTerm.Operator.OPEN_PARENTHESIS, NumberWithUnit.ZERO, OperatorTerm.Operator.MINUS, NumberWithUnit.ZERO, OperatorTerm.Operator.CLOSE_PARENTHESIS
            , OperatorTerm.Operator.DIVIDE, OperatorTerm.Operator.OPEN_PARENTHESIS, NumberWithUnit.ZERO, OperatorTerm.Operator.MINUS
            , NumberWithUnit.ZERO, OperatorTerm.Operator.CLOSE_PARENTHESIS, OperatorTerm.Operator.CLOSE_PARENTHESIS, OperatorTerm.Operator.MULTIPLY
            , OperatorTerm.Operator.OPEN_PARENTHESIS, NumberWithUnit.ZERO, OperatorTerm.Operator.MINUS, NumberWithUnit.ZERO, OperatorTerm.Operator.CLOSE_PARENTHESIS
            , OperatorTerm.Operator.CLOSE_PARENTHESIS, OperatorTerm.Operator.PLUS, NumberWithUnit.ZERO);

    public ValueLinkingExpression(Parameter destParam) {
        this(destParam, Collections.emptyList());
    }

    /**
     *
     * @param t list of {@link Term} in the following format
     *              (((fromValue - fromMinRange)/(fromMaxRange - fromMinRange)) * (toMaxRange - toMinRange)) + toMinRange
     */
    public ValueLinkingExpression(Parameter destParam, List<Term> t) {
        super(Type.VALUE_LINKING);
        this.destParam = destParam;

        if (t.isEmpty()) {
            for (int i=0; i<termType.size(); i++) {
                if (termType.get(i) == Term.Type.OPERATOR) {
                    terms.add(new OperatorTerm((OperatorTerm.Operator) termValue.get(i)));
                } else if (termType.get(i) == Term.Type.VALUE) {
                    terms.add(new ValueTerm(null));
                } else if (termType.get(i) == Term.Type.NUMBER) {
                    terms.add(new NumberWithUnitTerm(new NumberWithUnit(0, Unit.NOT_SPECIFIED)));
                } else {
                    throw new IllegalStateException();
                }
            }

            NumberWithUnitTerm toMinRangeTerm = new NumberWithUnitTerm(new NumberWithUnit(destParam.getMinimumValue(), destParam.getUnit().get(0)));
            terms.set(18, toMinRangeTerm);
            terms.set(22, toMinRangeTerm);

            NumberWithUnitTerm toMaxRangeTerm = new NumberWithUnitTerm(new NumberWithUnit(destParam.getMaximumValue(), destParam.getUnit().get(0)));
            terms.set(16, toMaxRangeTerm);
        } else if (t.size() == 23) {
            boolean valid = true;
            for (int i=0; i<termType.size(); i++) {
                if (t.get(i).getType() != termType.get(i)) {
                    valid = false;
                    break;
                }
                if ((t.get(i).getType() == Term.Type.OPERATOR) && (t.get(i).getValue() != termValue.get(i))) {
                    valid = false;
                    break;
                }
            }
            if (!t.get(5).equals(t.get(11)) || !t.get(18).equals(t.get(22))) {
                valid = false;
            }
            if (valid) {
                terms.addAll(t);
            } else {
                throw new IllegalStateException("Found invalid term!!!");
            }
        } else {
            throw new IllegalStateException("Found invalid term!!!");
        }
    }

    public ValueLinkingExpression(ValueLinkingExpression e) {
        this(e.getDestinationParameter(), e.getTerms());
    }

    public ProjectValue getSourceValue() {
        return ((ValueTerm) terms.get(3)).getValue();
    }

    public ValueLinkingExpression setSourceValue(ProjectValue v) {
        ValueLinkingExpression newExpression = new ValueLinkingExpression(this);
        newExpression.terms.set(3, new ValueTerm(v));

        NumericConstraint constraint = (NumericConstraint) v.getValue().getConstraint();
        NumberWithUnitTerm newMinTerm = new NumberWithUnitTerm(new NumberWithUnit(
                (constraint.getMax() - constraint.getMin()) * 0.25 + constraint.getMin(), constraint.getUnit()));
        newExpression.terms.set(5, newMinTerm);
        newExpression.terms.set(11, newMinTerm);

        NumberWithUnitTerm newMaxTerm = new NumberWithUnitTerm(new NumberWithUnit(
                (constraint.getMax() - constraint.getMin()) * 0.75 + constraint.getMin(), constraint.getUnit()));
        newExpression.terms.set(9, newMaxTerm);

        return newExpression;
    }

    public NumberWithUnit getSourceLowValue() {
        return ((NumberWithUnitTerm) terms.get(5)).getValue();
    }

    public ValueLinkingExpression setSourceLowValue(NumberWithUnit n) {
        ValueLinkingExpression newExpression = new ValueLinkingExpression(this);
        NumberWithUnitTerm newMinTerm = new NumberWithUnitTerm(n);
        newExpression.terms.set(5, newMinTerm);
        newExpression.terms.set(11, newMinTerm);
        return newExpression;
    }

    public NumberWithUnit getSourceHighValue() {
        return ((NumberWithUnitTerm) terms.get(9)).getValue();
    }

    public ValueLinkingExpression setSourceHighValue(NumberWithUnit n) {
        ValueLinkingExpression newExpression = new ValueLinkingExpression(this);
        NumberWithUnitTerm newMaxTerm = new NumberWithUnitTerm(n);
        newExpression.terms.set(9, newMaxTerm);
        return newExpression;
    }

    public Parameter getDestinationParameter() {
        return destParam;
    }

    public NumberWithUnit getDestinationLowValue() {
        return ((NumberWithUnitTerm) terms.get(18)).getValue();
    }

    public ValueLinkingExpression setDestinationLowValue(NumberWithUnit n) {
        ValueLinkingExpression newExpression = new ValueLinkingExpression(this);
        NumberWithUnitTerm newMinTerm = new NumberWithUnitTerm(n);
        newExpression.terms.set(18, newMinTerm);
        newExpression.terms.set(22, newMinTerm);
        return newExpression;
    }

    public NumberWithUnit getDestinationHighValue() {
        return ((NumberWithUnitTerm) terms.get(16)).getValue();
    }

    public ValueLinkingExpression setDestinationHighValue(NumberWithUnit n) {
        ValueLinkingExpression newExpression = new ValueLinkingExpression(this);
        NumberWithUnitTerm newMaxTerm = new NumberWithUnitTerm(n);
        newExpression.terms.set(16, newMaxTerm);
        return newExpression;
    }

    @Override
    public boolean isValid() {
        return super.isValid();
    }
}


