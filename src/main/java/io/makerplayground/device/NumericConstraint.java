/*
 * Copyright 2017 The Maker Playground Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.makerplayground.device;

import io.makerplayground.helper.DataType;
import io.makerplayground.helper.NumberWithUnit;
import io.makerplayground.helper.Unit;

import java.util.*;

/**
 * Represent a constraint for a numeric value
 */
public class NumericConstraint implements Constraint {
    private final Map<Unit, Value> numericValue;    // TODO: edit because a map is not needed to store single constraint
    private Unit unit;

    /**
     * Construct a constraint based on the specify min, max and unit. The constructor should only
     * be invoked by the DeviceLibrary in order to rebuild the library from file.
     * @param min the minimum value (inclusive)
     * @param max the maximum value (inclusive)
     * @param unit the unit of the value
     */
    NumericConstraint(double min, double max, Unit unit) {
        Value value = new Value(min, max, unit);
        this.unit = unit;
        this.numericValue = Collections.singletonMap(unit, value);
    }

    /**
     * Construct constraint for number with multiple units using list of {@link Value}.
     * The constructor should only be invoked by the DeviceLibrary in order to rebuild
     * the library from file.
     * @param value list of {@link Value} for initializing new constraint instance
     */
    private NumericConstraint(Collection<Value> value) {
        this.numericValue = new EnumMap<>(Unit.class);
        // we only accept 1 pair of value and unit but we maintain this method for
        // backward compatibility
        if (value.size() != 1)
            throw new IllegalStateException("value size must be 1");
        for (Value v : value) {
            this.numericValue.put(v.unit, v);
            this.unit = v.unit;
        }
    }

//    public Collection<Unit> getUnit() {
//        return numericValue.keySet();
//    }

    public Unit getUnit() {
        return unit;
    }

    @Override
    public boolean isCompatible(Constraint genericConstraint) {
        if (genericConstraint instanceof NumericConstraint) {
            for (Unit unit : ((NumericConstraint) genericConstraint).numericValue.keySet()) {
                if (this.numericValue.containsKey(unit)) {
                    if (!((this.numericValue.get(unit).min <= ((NumericConstraint) genericConstraint).numericValue.get(unit).min)
                        && (this.numericValue.get(unit).max >= ((NumericConstraint) genericConstraint).numericValue.get(unit).max))) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
            return true;
        } else if (Constraint.NONE.equals(genericConstraint)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean test(double d, Unit unit) {
        Value v = numericValue.get(unit);
        if (v == null) {
            throw new IllegalArgumentException("Can't find a constraint for the given unit: " + unit);
        }
        return (d >= v.min) && (d <= v.max);
    }

    @Override
    public boolean test(String s) {
        return false;
    }

    @Override
    public Constraint union(Constraint constraint) {
        if (!(constraint instanceof NumericConstraint))
            throw new ClassCastException();
        NumericConstraint numericConstraint = (NumericConstraint) constraint;

        Map<Unit, Value> m = new HashMap<>(numericValue);
        Map<Unit, Value> m2 = numericConstraint.numericValue;
        for (Unit u : m2.keySet()) {
            if (m.containsKey(u)) {
                Value v1 = m.get(u);
                Value v2 = m2.get(u);
                m.put(u, new Value(v1.min < v2.min ? v1.min : v2.min
                        , v1.max > v2.max ? v1.max : v2.max
                        , u));
            } else {
                m.put(u, m2.get(u));
            }
        }

        return new NumericConstraint(m.values());
    }

    public double getMin() {
        return numericValue.get(unit).min;
    }

    public double getMax() {
        return numericValue.get(unit).max;
    }

    static class Value {
        public double min;
        public double max;
        public Unit unit;

        Value() {
        }

        Value(double min, double max, Unit unit) {
            this.min = min;
            this.max = max;
            this.unit = unit;
        }

        @Override
        public String toString() {
            return "Value{" +
                    "min=" + min +
                    ", max=" + max +
                    ", unit=" + unit +
                    '}';
        }

    }

    @Override
    public String toString() {
        return "NumericConstraint{" +
                "numericValue=" + numericValue +
                '}';
    }

}
