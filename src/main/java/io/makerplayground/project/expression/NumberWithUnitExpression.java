package io.makerplayground.project.expression;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.makerplayground.helper.NumberWithUnit;
import io.makerplayground.project.term.NumberWithUnitTerm;

public class NumberWithUnitExpression extends Expression {

    public NumberWithUnitExpression(NumberWithUnit numberWithUnit) {
        super(Type.NUMBER_WITH_UNIT);
        this.getTerms().add(new NumberWithUnitTerm(numberWithUnit));
    }

    NumberWithUnitExpression(NumberWithUnitExpression e) {
        super(e);
    }

    @JsonIgnore
    public NumberWithUnit getNumberWithUnit() {
        if (this.getTerms().size() > 0) {
            return ((NumberWithUnitTerm) this.getTerms().get(0)).getValue();
        }
        return null;
    }
}
