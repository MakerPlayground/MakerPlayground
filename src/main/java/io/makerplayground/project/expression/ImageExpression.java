package io.makerplayground.project.expression;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.term.ValueTerm;

public class ImageExpression extends Expression {
    public ImageExpression() {
        super(Type.IMAGE);
        terms.add(new ValueTerm(null));
    }

    public ImageExpression(ProjectValue projectValue) {
        super(Type.IMAGE);
        terms.add(new ValueTerm(projectValue));
    }

    ImageExpression(ImageExpression e) {
        super(e);
    }

    @JsonIgnore
    public ProjectValue getProjectValue() {
        return ((ValueTerm) terms.get(0)).getValue();
    }

    public ImageExpression setProjectValue(ProjectValue projectValue) {
        ImageExpression expression = new ImageExpression(this);
        expression.terms.set(0, new ValueTerm(projectValue));
        return expression;
    }

    @Override
    public ImageExpression deepCopy() {
        return new ImageExpression(this);
    }
}
