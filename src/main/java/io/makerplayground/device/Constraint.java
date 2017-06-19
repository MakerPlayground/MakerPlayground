package io.makerplayground.device;

import java.util.Collections;
import java.util.List;

/**
 *
 * Created by nuntipat on 6/19/2017 AD.
 */
public class Constraint {
    private double min;
    private double max;
    private Unit unit;
    private List<String> value;

    /**
     *
     */
    public static final Constraint NONE = new Constraint();
    public static final Constraint ZERO_TO_HUNDRED = new Constraint(0, 100);

    private Constraint() {
        this.min = 0;
        this.max = 0;
        this.value = Collections.EMPTY_LIST;
    }

    public Constraint(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public Constraint(List<String> value) {
        this.value = value;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public Unit getUnit() {
        return unit;
    }

    public List<String> getValue() {
        return value;
    }
}
