package io.makerplayground.ui.canvas.node.expression.custom;

import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.expression.CustomNumberExpression;
import io.makerplayground.project.term.Term;

import java.util.List;

public class NumericChipField extends ChipField<CustomNumberExpression> {

    public NumericChipField(CustomNumberExpression expression, List<ProjectValue> projectValues) {
        super(expression, projectValues, false);
    }

    @Override
    CustomNumberExpression convertToExpression(List<Term> terms, boolean hasNonParseText) {
        if (hasNonParseText) {
            return CustomNumberExpression.INVALID;
        } else {
            return new CustomNumberExpression(terms);
        }
    }

}
