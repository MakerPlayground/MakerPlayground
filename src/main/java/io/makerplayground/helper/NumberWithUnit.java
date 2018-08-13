package io.makerplayground.helper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Created by tanyagorn on 7/11/2017.
 */
public class NumberWithUnit {
    private double d;
    private Unit u;

    public static final NumberWithUnit ZERO = new NumberWithUnit(0, Unit.NOT_SPECIFIED);

    @JsonCreator
    public NumberWithUnit(@JsonProperty("value") double d, @JsonProperty("unit") Unit u) {
        this.d = d;
        this.u = u;
    }

    public double getValue() {
        return d;
    }

    public Unit getUnit() {
        return u;
    }

    @Override
    public String toString() {
        return String.valueOf(d) + " " + u.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NumberWithUnit that = (NumberWithUnit) o;
        return Double.compare(that.d, d) == 0 &&
                u == that.u;
    }

    @Override
    public int hashCode() {
        return Objects.hash(d, u);
    }
}
