package io.makerplayground.device;

/**
 *
 * Created by nuntipat on 6/19/2017 AD.
 */
public class Value {
    private final String name;
    private final Constraint constraint;
    private final Unit unit;

    public Value(String name, Constraint constraint, Unit unit) {
        this.name = name;
        this.constraint = constraint;
        this.unit = unit;
    }

    public String getName() {
        return name;
    }

    public Constraint getConstraint() {
        return constraint;
    }

    public Unit getUnit() {
        return unit;
    }
}
