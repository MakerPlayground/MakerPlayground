package io.makerplayground.project.expression;

import io.makerplayground.project.ProjectValue;

public class ValueTerm extends Term {

    public ValueTerm(ProjectValue value) {
        super(ChipType.VALUE, value);
    }

    @Override
    public ProjectValue getValue() {
        return (ProjectValue) value;
    }
}
