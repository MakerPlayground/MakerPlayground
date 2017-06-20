package io.makerplayground.device;

/**
 *
 * Created by Nuntipat Narkthong on 6/19/2017 AD.
 */
public class Value {
    private final String name;
    private final Constraint constraint;

    public Value(String name, Constraint constraint) {
        this.name = name;
        this.constraint = constraint;
    }

    public String getName() {
        return name;
    }

    public Constraint getConstraint() {
        return constraint;
    }

}
