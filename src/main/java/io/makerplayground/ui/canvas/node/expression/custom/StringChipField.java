package io.makerplayground.ui.canvas.node.expression.custom;

import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.expression.ComplexStringExpression;
import io.makerplayground.project.term.Term;

import java.util.List;

public class StringChipField extends ChipField<ComplexStringExpression> {

    public StringChipField(ComplexStringExpression expression, List<ProjectValue> projectValues) {
        super(expression, projectValues, true);
    }

    @Override
    ComplexStringExpression convertToExpression(List<Term> terms, boolean hasNonParseText) {
        if (hasNonParseText) {
            return ComplexStringExpression.INVALID;
        } else {
            return new ComplexStringExpression(terms);
        }
    }

}
