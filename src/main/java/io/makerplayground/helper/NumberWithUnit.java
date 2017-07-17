package io.makerplayground.helper;

/**
 * Created by tanyagorn on 7/11/2017.
 */
public class NumberWithUnit {
    private double d;
    private Unit u;

    public NumberWithUnit(double d, Unit u) {
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
        return "NumberWithUnit{" +
                "d=" + d +
                ", u=" + u +
                '}';
    }
}
