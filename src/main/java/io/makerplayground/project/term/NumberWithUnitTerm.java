package io.makerplayground.project.term;

import io.makerplayground.helper.NumberWithUnit;

import java.text.DecimalFormat;

public class NumberWithUnitTerm extends Term {
    private static final DecimalFormat df = new DecimalFormat("0.00");

    public NumberWithUnitTerm(NumberWithUnit value) {
        super(Type.NUMBER, value);
    }

    @Override
    public NumberWithUnit getValue() {
        return (NumberWithUnit) value;
    }

    @Override
    public String toCCode(){
        return df.format(((NumberWithUnit) value).getValue());
    }
}
