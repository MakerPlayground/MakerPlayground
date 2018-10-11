/*
 * Copyright (c) 2018. The Maker Playground Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.makerplayground.device.shared.constraint;

import io.makerplayground.device.shared.Unit;

import java.util.*;
import java.util.function.Function;

/**
 * Represent a constraint for a numeric value
 */
public class NumericConstraint implements Constraint {
    private final double min;
    private final double max;
    private final Unit unit;

    /**
     * Construct a constraint based on the specify min, max and unit. The constructor should only
     * be invoked by the DeviceLibrary in order to rebuild the library from file.
     * @param min the minimum value (inclusive)
     * @param max the maximum value (inclusive)
     * @param unit the unit of the value
     */
    NumericConstraint(double min, double max, Unit unit) {
        this.min = min;
        this.max = max;
        this.unit = unit;
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

    @Override
    public boolean isCompatible(Constraint genericConstraint) {
        if (genericConstraint == Constraint.NONE) {
            return true;
        } else if (genericConstraint instanceof NumericConstraint) {
            return (min <= ((NumericConstraint) genericConstraint).min) && (max >= ((NumericConstraint) genericConstraint).max);
        } else {
            return false;
        }
    }

    @Override
    public boolean test(double d, Unit unit) {
        return (unit == this.unit) && (d >= min) && (d <= max);
    }

    @Override
    public boolean test(String s) {
        return false;
    }

    @Override
    public NumericConstraint union(Constraint constraint) {
        if (constraint == Constraint.NONE) {
            return this;
        }
        if (!(constraint instanceof NumericConstraint)) {
            throw new ClassCastException();
        }
        if (unit == ((NumericConstraint) constraint).unit) {
            return new NumericConstraint(Math.min(min, ((NumericConstraint) constraint).min)
                    , Math.max(max, ((NumericConstraint) constraint).max), unit);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public NumericConstraint intersect(Constraint constraint) {
        if (constraint == Constraint.NONE) {
            return this;
        }
        if (!(constraint instanceof NumericConstraint)) {
            throw new ClassCastException();
        }
        if (unit == ((NumericConstraint) constraint).unit) {
            return new NumericConstraint(Math.max(min, ((NumericConstraint) constraint).min)
                    , Math.min(max, ((NumericConstraint) constraint).max), unit);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public NumericConstraint intersect(Constraint constraint, Function<Double, Double> unitConverter) {
        if (constraint == Constraint.NONE) {
            return this;
        }
        if (!(constraint instanceof NumericConstraint)) {
            throw new ClassCastException();
        }
        return new NumericConstraint(Math.max(unitConverter.apply(min), unitConverter.apply(((NumericConstraint) constraint).min))
                    , Math.min(unitConverter.apply(max), unitConverter.apply(((NumericConstraint) constraint).max)), unit);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NumericConstraint that = (NumericConstraint) o;
        return Double.compare(that.min, min) == 0 &&
                Double.compare(that.max, max) == 0 &&
                unit == that.unit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(min, max, unit);
    }

    @Override
    public String toString() {
        return "NumericConstraint{" +
                "min=" + min +
                ", max=" + max +
                ", unit=" + unit +
                '}';
    }
}
