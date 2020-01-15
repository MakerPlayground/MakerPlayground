package io.makerplayground.project.expression;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.makerplayground.project.term.IntegerTerm;

public class SimpleIntegerExpression extends Expression {

    public SimpleIntegerExpression(Integer i) {
        super(Type.SIMPLE_INTEGER);
        terms.add(new IntegerTerm(i));
    }

    public SimpleIntegerExpression(IntegerTerm term) {
        super(Type.SIMPLE_INTEGER);
        terms.add(term);
    }

    SimpleIntegerExpression(SimpleIntegerExpression e) {
        super(e);
    }

    @JsonIgnore
    public Integer getInteger() {
        return ((IntegerTerm) terms.get(0)).getValue();
    }

    @Override
    public SimpleIntegerExpression deepCopy() {
        return new SimpleIntegerExpression(this);
    }
}
