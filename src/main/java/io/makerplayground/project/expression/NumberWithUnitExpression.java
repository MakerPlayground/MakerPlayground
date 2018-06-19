package io.makerplayground.project.expression;

import io.makerplayground.helper.NumberWithUnit;

public class NumberWithUnitExpression extends Expression {
    private final NumberWithUnit numberWithUnit;

    public NumberWithUnitExpression(NumberWithUnit numberWithUnit) {
        this.numberWithUnit = numberWithUnit;
    }

    public NumberWithUnit getNumberWithUnit() {
        return numberWithUnit;
    }
}
