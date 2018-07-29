package io.makerplayground.project.expression;

import io.makerplayground.project.term.NumberWithUnitTerm;
import io.makerplayground.project.term.OperatorTerm;
import io.makerplayground.project.term.Term;
import io.makerplayground.project.term.ValueTerm;

import java.util.List;

public class CustomNumberExpression extends Expression {

    private double maxValue;
    private double minValue;

    public CustomNumberExpression(double maxValue, double minValue) {
        super(Type.CUSTOM_NUMBER);
        this.maxValue = maxValue;
        this.minValue = minValue;
    }

    public CustomNumberExpression(double maxValue, double minValue, List<Term> terms) {
        super(Type.CUSTOM_NUMBER);
        this.getTerms().addAll(terms);
        this.maxValue = maxValue;
        this.minValue = minValue;
    }

    CustomNumberExpression(CustomNumberExpression e) {
        super(e);
    }

    @Override
    public boolean isValid() {
        List<Term> terms = getTerms();
        if (terms.isEmpty()) {
            return false;
        }

        /* check parenthesis */
        int countParen = 0;
        for (Term term : terms) {
            if (term instanceof OperatorTerm) {
                if (OperatorTerm.Operator.OPEN_PARENTHESIS.equals(term.getValue())) { countParen++; }
                else if(OperatorTerm.Operator.CLOSE_PARENTHESIS.equals(term.getValue())) {
                    countParen--;
                    if (countParen < 0) { return false; }
                }
            }
        }
        if (countParen != 0) { return false; }
        /* check each term */
        for (Term t: terms) {
            if (!t.isValid()) {
                return false;
            }
        }
        /* check valid sequence */
        for (int i=0; i<terms.size()-1; i++) {
            Term term = terms.get(i);
            Term nextTerm = terms.get(i+1);
            if (isNumberOrValueTerm(term)) {
                if (isNumberOrValueTerm(nextTerm) || OperatorTerm.Operator.OPEN_PARENTHESIS.equals(nextTerm.getValue())) {
                    return false;
                }
            } else if (isOperationNotParenTerm(term)) {
                if (isOperationNotParenTerm(nextTerm) || OperatorTerm.Operator.CLOSE_PARENTHESIS.equals(nextTerm.getValue())) {
                    return false;
                }
            } else if (OperatorTerm.Operator.OPEN_PARENTHESIS.equals(term.getValue())) {
                if (isOperationNotParenTerm(nextTerm) || OperatorTerm.Operator.CLOSE_PARENTHESIS.equals(nextTerm.getValue())) {
                    return false;
                }
            } else if (OperatorTerm.Operator.CLOSE_PARENTHESIS.equals(term.getValue())) {
                if (isNumberOrValueTerm(nextTerm) || OperatorTerm.Operator.OPEN_PARENTHESIS.equals(nextTerm.getValue())) {
                    return false;
                }
            }
        }
        /* check last */
        if (terms.size() > 0) {
            Term last = terms.get(terms.size()-1);
            if (OperatorTerm.Operator.OPEN_PARENTHESIS.equals(last.getValue()) || isOperationNotParenTerm(last)) {
                return false;
            }
        }
        return true;
    }

    private boolean isParenTerm(Term t) {
        return t instanceof OperatorTerm &&
                (OperatorTerm.Operator.OPEN_PARENTHESIS.equals(t.getValue()) || OperatorTerm.Operator.CLOSE_PARENTHESIS.equals(t.getValue()));
    }

    private boolean isOperationNotParenTerm(Term t) {
        return t instanceof OperatorTerm && !isParenTerm(t);
    }

    private boolean isNumberOrValueTerm(Term t) {
        return t instanceof ValueTerm || t instanceof NumberWithUnitTerm;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public double getMinValue() {
        return minValue;
    }
}
