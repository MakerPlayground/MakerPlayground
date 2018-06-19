package io.makerplayground.project.expression;

import io.makerplayground.project.ProjectValue;

public class ProjectValueExpression extends Expression {

    private final ProjectValue projectValue;

    public ProjectValueExpression(ProjectValue projectValue) {
        this.projectValue = projectValue;
    }

    public ProjectValue getProjectValue() {
        return projectValue;
    }
}
