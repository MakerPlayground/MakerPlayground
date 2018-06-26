package io.makerplayground.project.expression;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.chip.ValueTerm;

public class ProjectValueExpression extends Expression {

    public ProjectValueExpression() {
        super(Type.PROJECT_VALUE);
        this.getTerms().add(new ValueTerm(null));
    }

    public ProjectValueExpression(ProjectValue projectValue) {
        super(Type.PROJECT_VALUE);
        this.getTerms().add(new ValueTerm(projectValue));
    }

    ProjectValueExpression(ProjectValueExpression e) {
        super(e);
    }

    @JsonIgnore
    public ProjectValue getProjectValue() {
        if (this.getTerms().size() > 0) {
            return ((ValueTerm) this.getTerms().get(0)).getValue();
        }
        return null;
    }
}
