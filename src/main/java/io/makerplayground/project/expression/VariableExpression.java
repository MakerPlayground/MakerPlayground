package io.makerplayground.project.expression;

import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.term.ValueTerm;

import java.util.Optional;

public class VariableExpression extends Expression {

    public static final VariableExpression NO_VARIABLE_SELECTED = new VariableExpression();

    private VariableExpression() {
        super(Type.VARIABLE);
        terms.clear();
    }

    public VariableExpression(VariableExpression e) {
        super(e);
    }

    public VariableExpression(ProjectValue value) {
        super(Type.VARIABLE);
        terms.clear();
        terms.add(new ValueTerm(value));
    }

    public Optional<ProjectValue> getProjectValue() {
        if (NO_VARIABLE_SELECTED.equals(this)) {
            return Optional.empty();
        }
        return Optional.of(((ValueTerm) terms.get(0)).getValue());
    }

    public String getVariableName() {
        if (NO_VARIABLE_SELECTED.equals(this)) {
            return "";
        }
        return ((ValueTerm) terms.get(0)).getValue().getValue().getName();
    }

    @Override
    public Expression deepCopy() {
        return new VariableExpression(this);
    }
}
