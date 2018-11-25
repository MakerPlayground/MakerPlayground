package io.makerplayground.converter;

import io.makerplayground.device.shared.NumberWithUnit;
import io.makerplayground.device.shared.Unit;

import java.util.function.Function;

public abstract class UnitConverter implements Function<NumberWithUnit, NumberWithUnit> {

    Unit unitFrom;
    Unit unitTo;
    Function<Double, Double> fn;

    protected UnitConverter(Unit from, Unit to) {
        this.unitFrom = from;
        this.unitTo = to;
        fn = this.selectFunction(from, to);
    }

    protected abstract Function<Double, Double> selectFunction(Unit from, Unit to);

    public Unit getUnitFrom() {
        return unitFrom;
    }

    public Unit getUnitTo() {
        return unitTo;
    }

    @Override
    public NumberWithUnit apply(NumberWithUnit aDouble) {
        if (aDouble.getUnit() != unitFrom) {
            throw new ClassCastException();
        }
        return new NumberWithUnit(fn.apply(aDouble.getValue()), unitTo);
    }

    public UnitConverter getConverter(Unit from, Unit to) {
        if (from.getType().equals(to.getType())) {
            switch (from.getType()) {
                case TEMPERATURE:
                    return new TemperatureConverter(from, to);
                case ACCELERATION:
                    break;
                case DISTANCE:
                    break;
                case VELOCITY:
                    break;
                case ANGULAR_VELOCITY:
                    break;
                case SOUND_INTENSITY:
                    break;
                case MAGNETIC_FIELD:
                    break;
                case PRESSURE:
                    break;
                case LIGHT_INTENSITY:
                    break;
                case ANGULAR_DISTANCE:
                    break;
                case ELECTRIC_CURRENT:
                    break;
                case TIME:
                    break;
                case FREQUENCY:
                    break;
                case NOT_SPECIFIED:
                    break;
            }
            throw new IllegalStateException("Not implemented yet");
        }
        throw new ClassCastException();
    }
}
