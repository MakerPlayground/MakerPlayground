package io.makerplayground.project.expression;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.makerplayground.project.term.StringTerm;

public class SimpleStringExpression extends Expression {

    public SimpleStringExpression(String str) {
        super(Type.SIMPLE_STRING);
        this.getTerms().add(new StringTerm(str));
    }

    SimpleStringExpression(SimpleStringExpression e) {
        super(e);
    }

    @JsonIgnore
    public String getString() {
        if (this.getTerms().size() > 0) {
            return ((StringTerm) this.getTerms().get(0)).getValue();
        }
        return null;
    }
}
