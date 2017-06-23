package io.makerplayground.device;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;

/**
 * Class to represent constrains of a value which could be a number (min/max/unit) or a list of String
 * which is considered to be valid.
 * Created by Nuntipat Narkthong on 6/19/2017 AD.
 */
public class Constraint {
    private final double min;
    private final double max;
    private final Unit unit;
    private final List<String> value;

    /**
     *
     */
    public static final Constraint NONE = new Constraint();
    /**
     *
     */
    public static final Constraint ZERO_TO_HUNDRED = new Constraint(0, 100, Unit.NOT_SPECIFIED);

    Constraint() {
        this.min = 0;
        this.max = 0;
        this.unit = Unit.NOT_SPECIFIED;
        this.value = Collections.emptyList();
    }

    @JsonCreator
    Constraint(@JsonProperty("min") double min,@JsonProperty("max") double max,@JsonProperty("unit") Unit unit,@JsonProperty("value") List<String> value) {
        this.min = min;
        this.max = max;
        this.unit = unit;
        this.value = value;
    }

    Constraint(double min, double max, Unit unit) {
        this.min = min;
        this.max = max;
        this.unit = unit;
        this.value = Collections.emptyList();
    }

    Constraint(List<String> value) {
        this.min = 0;
        this.max = 0;
        this.unit = Unit.NOT_SPECIFIED;
        this.value = value;
    }

    /**
     *
     * @return
     */
    public double getMin() {
        return min;
    }

    /**
     *
     * @return
     */
    public double getMax() {
        return max;
    }

    /**
     *
     * @return
     */
    public Unit getUnit() {
        return unit;
    }

    /**
     *
     * @return
     */
    public List<String> getValue() {
        return value;
    }
}
