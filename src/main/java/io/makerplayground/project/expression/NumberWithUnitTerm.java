package io.makerplayground.project.expression;

import io.makerplayground.helper.NumberWithUnit;

public class NumberWithUnitTerm extends Term {

    public NumberWithUnitTerm(NumberWithUnit value) {
        super(ChipType.NUMBER, value);
    }

    @Override
    public NumberWithUnit getValue() {
        return (NumberWithUnit) value;
    }
}
