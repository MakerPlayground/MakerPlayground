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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Nuntipat Narkthong on 6/19/2017 AD.
 */
@JsonDeserialize(using = ConstraintDeserializer.class)
public class Constraint {
    private final Map<Unit, Constraint.Value> numericValue;
    private final List<String> categoricalValue;

    /**
     * A special value indicating no constraint.
     */
    public static final Constraint NONE = new Constraint();

    private Constraint() {
        this.numericValue = Collections.emptyMap();
        this.categoricalValue = Collections.emptyList();
    }

    private Constraint(Map<Unit, Constraint.Value> numericValue, List<String> categoricalValue) {
        this.numericValue = numericValue;
        this.categoricalValue = categoricalValue;
    }

    static Constraint createNumericConstraint(Constraint.Value value) {
        return new Constraint(Collections.singletonMap(value.unit, value)
                , Collections.emptyList());
    }

    static Constraint createNumericConstraint(List<Constraint.Value> constraintValues) {
        Map<Unit, Constraint.Value> tmpNumericValue = new EnumMap<>(Unit.class);
        for (Constraint.Value cn : constraintValues) {
            tmpNumericValue.put(cn.unit, cn);
        }
        return new Constraint(tmpNumericValue, Collections.emptyList());
    }

    static Constraint createCategoricalConstraint(List<String> value) {
        return new Constraint(Collections.emptyMap(), value);
    }

    @Override
    public String toString() {
        return "Constraint{" +
                "numericValue=" + numericValue +
                ", categoricalValue=" + categoricalValue +
                '}';
    }

    static class Value {
        enum Type {
            INTEGER, FLOAT
        }

        public double min;
        public double max;
        public Type type;
        public Unit unit;

        Value() {
        }

        Value(double min, double max, Type type, Unit unit) {
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
}
