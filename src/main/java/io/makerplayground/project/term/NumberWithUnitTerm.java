package io.makerplayground.project.term;

import io.makerplayground.helper.NumberWithUnit;

public class NumberWithUnitTerm extends Term {

    public NumberWithUnitTerm(NumberWithUnit value) {
        super(Type.NUMBER, value);
    }

    @Override
    public NumberWithUnit getValue() {
        return (NumberWithUnit) value;
    }
}
