package io.makerplayground.helper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by tanyagorn on 7/11/2017.
 */
public class NumberWithUnit {
    private double d;
    private Unit u;

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
}
