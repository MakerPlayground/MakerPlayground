package io.makerplayground.project.expression;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.makerplayground.helper.NumberWithUnit;
import io.makerplayground.project.term.NumberWithUnitTerm;

public class NumberWithUnitExpression extends Expression {

    public NumberWithUnitExpression(NumberWithUnit numberWithUnit) {
        super(Type.NUMBER_WITH_UNIT);
        terms.add(new NumberWithUnitTerm(numberWithUnit));
    }

    NumberWithUnitExpression(NumberWithUnitExpression e) {
        super(e);
    }

    @JsonIgnore
    public NumberWithUnit getNumberWithUnit() {
        return ((NumberWithUnitTerm) terms.get(0)).getValue();
    }
}
