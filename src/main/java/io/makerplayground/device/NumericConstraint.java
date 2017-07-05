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
import io.makerplayground.helper.Unit;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represent a constraint for a numeric value
 */
public class NumericConstraint implements Constraint {
    private final Map<Unit, Value> numericValue;

    /**
     * Construct a constraint based on the specify min, max and unit. The constructor should only
     * be invoked by the DeviceLibrary in order to rebuild the library from file.
     * @param min the minimum value (inclusive)
     * @param max the maximum value (inclusive)
     * @param type the type as an instance of {@link DataType}
     * @param unit the unit of the value
     */
    NumericConstraint(double min, double max, DataType type, Unit unit) {
        Value value = new Value(min, max, type, unit);
        this.numericValue = Collections.singletonMap(unit, value);
    }

    /**
     * Construct constraint for number with multiple units using list of {@link Value}.
     * The constructor should only be invoked by the DeviceLibrary in order to rebuild
     * the library from file.
     * @param value list of {@link Value} for initializing new constraint instance
     */
    NumericConstraint(List<Value> value) {
        this.numericValue = new HashMap<>();
        for (Value v : value) {
            this.numericValue.put(v.unit, v);
        }
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

    static class Value {
        public double min;
        public double max;
        public DataType type;
        public Unit unit;

        Value() {
        }

        Value(double min, double max, DataType type, Unit unit) {
            this.min = min;
            this.max = max;
            this.type = type;
            this.unit = unit;
        }

        @Override
        public String toString() {
            return "Value{" +
                    "min=" + min +
                    ", max=" + max +
                    ", type=" + type +
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
