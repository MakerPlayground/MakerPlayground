package io.makerplayground.project.term;

import io.makerplayground.project.ProjectValue;

public class ValueTerm extends Term {

    public ValueTerm(ProjectValue value) {
        super(Type.VALUE, value);
    }

    @Override
    public ProjectValue getValue() {
        return (ProjectValue) value;
    }
}
