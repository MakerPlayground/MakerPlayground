package io.makerplayground.project.expression;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.term.ValueTerm;

public class ProjectValueExpression extends Expression {

    public ProjectValueExpression() {
        super(Type.PROJECT_VALUE);
        terms.add(new ValueTerm(null));
    }

    public ProjectValueExpression(ProjectValue projectValue) {
        super(Type.PROJECT_VALUE);
        terms.add(new ValueTerm(projectValue));
    }

    ProjectValueExpression(ProjectValueExpression e) {
        super(e);
    }

    @JsonIgnore
    public ProjectValue getProjectValue() {
        return ((ValueTerm) terms.get(0)).getValue();
    }
}
