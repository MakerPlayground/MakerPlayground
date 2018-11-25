package io.makerplayground.converter;

import io.makerplayground.device.shared.Unit;

import java.util.function.Function;

public class TemperatureConverter extends UnitConverter {

    public TemperatureConverter(Unit from, Unit to) {
        super(from, to);
    }

    @Override
    protected Function<Double, Double> selectFunction(Unit from, Unit to) {
        if (from == Unit.CELSIUS && to == Unit.FAHRENHEIT) return c -> 1.8 * c + 32;
        if (from == Unit.CELSIUS && to == Unit.KELVIN) return c -> c + 273.15;
        if (from == Unit.FAHRENHEIT && to == Unit.CELSIUS) return f -> (f - 32.0) / 1.8;
        if (from == Unit.FAHRENHEIT && to == Unit.KELVIN) return f -> (f - 32.0) / 1.8 + 273.15;
        if (from == Unit.KELVIN && to == Unit.CELSIUS) return k -> k - 273.15;
        if (from == Unit.KELVIN && to == Unit.FAHRENHEIT) return k -> k - 273.15;
        throw new ClassCastException();
    }
}
