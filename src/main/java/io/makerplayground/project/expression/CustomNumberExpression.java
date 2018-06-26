package io.makerplayground.project.expression;

import io.makerplayground.device.Value;
import io.makerplayground.project.ProjectDevice;
import io.makerplayground.project.chip.Term;

import java.util.List;

public class CustomNumberExpression extends Expression {

    public CustomNumberExpression(List<Term> terms) {
        super(Type.CUSTOM_NUMBER);
        this.getTerms().addAll(terms);
    }

    CustomNumberExpression(CustomNumberExpression e) {
        super(e);
    }
}
