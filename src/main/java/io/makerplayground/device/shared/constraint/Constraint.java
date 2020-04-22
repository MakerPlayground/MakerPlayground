/*
 * Copyright (c) 2019. The Maker Playground Authors.
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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.makerplayground.device.shared.Unit;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * An interface for a constraint of some values ex. parameters of an action or possible values of an input device
 */
//@JsonDeserialize(using = ConstraintDeserializer.class)
public interface Constraint {

    /**
     * Test a given numberwithunit
     * @param d the value to be tested
     * @param unit the unit of the value to be tested
     * @throws IllegalArgumentException if there isn't any constraint specify for this unit
     * @return true if the value specify is valid otherwise return false
     */
    boolean test(double d, Unit unit);

    /**
     * Test a given string
     * @param s a string to be tested
     * @return true if the valid is valid otherwise return false
     */
    boolean test(String s);

    boolean test(Integer i);

    boolean isCompatible(Constraint constraint);

    Constraint union(Constraint constraint);

    Constraint intersect(Constraint constraint);

    /**
     * A special value indicating no constraint (returns true for every tests)
     */
    Constraint NONE = new Constraint() {
        @Override
        public boolean test(double d, Unit unit) {
            return true;
        }

        @Override
        public boolean test(String s) {
            return true;
        }

        @Override
        public boolean test(Integer i) {
            return true;
        }

        @Override
        public Constraint union(Constraint constraint) {
            return constraint;
        }

        @Override
        public Constraint intersect(Constraint constraint) {
            return NONE;
        }

        @Override
        public boolean isCompatible(Constraint constraint) { return true; }

        @Override
        public String toString() {
            return "Constraint{NONE}";
        }
    };

    /**
     * Create a constraint for a numeric value
     * @param min the minimum valid value (inclusive)
     * @param max the maximum valid value (inclusive)
     * @param unit unit of the value as an instance of {@link Unit}
     * @return an instance of {@link NumericConstraint}
     */
    static Constraint createNumericConstraint(double min, double max, Unit unit) {
        return new NumericConstraint(min, max, unit);
    }

//    /**
//     * Create a constraint for a numeric value with multiple unit
//     * @param constraintValues list of {@link NumericConstraint.Value} to be used to initialize the constraint object
//     * @return an instance of {@link NumericConstraint}
//     */
//    static Constraint createNumericConstraint(List<NumericConstraint.Value> constraintValues) {
//        return new NumericConstraint(constraintValues);
//    }

    /**
     * Create a constrint that match only the specify string
     * @param s the string to be matched
     * @return an instance of {@link CategoricalConstraint}
     */
    static Constraint createCategoricalConstraint(String s) {
        return new CategoricalConstraint(s);
    }

    /**
     * Create a constrint to match a list of strings given
     * @param value list of string to be matched
     * @return an instance of {@link CategoricalConstraint}
     */
    static Constraint createCategoricalConstraint(List<String> value) {
        return new CategoricalConstraint(value);
    }

    /**
     * Create a constrint to match a list of strings given
     * @param value list of string to be matched
     * @return an instance of {@link StringIntegerCategoricalConstraint}
     */
    static Constraint createStringIntegerCategoricalConstraint(LinkedHashMap<String, Integer> map) {
        return new StringIntegerCategoricalConstraint(map);
    }

    /**
     * Create a constrint that match only the specify string
     * @param s the string to be matched
     * @return an instance of {@link StringIntegerCategoricalConstraint}
     */
    static Constraint createStringIntegerCategoricalConstraint(String key, int value) {
        return new StringIntegerCategoricalConstraint(key, value);
    }

    /**
     * Create a constraint to match a list of integers given
     * @param value list of integer to be matched
     * @return an instance of {@link IntegerCategoricalConstraint}
     */
    static Constraint createIntegerCategoricalConstraint(Integer value) {
        return new IntegerCategoricalConstraint(value);
    }

    /**
     * Create a constraint to match a list of integers given
     * @param value list of integers to be matched
     * @return an instance of {@link IntegerCategoricalConstraint}
     */
    static Constraint createIntegerCategoricalConstraint(List<Integer> value) {
        return new IntegerCategoricalConstraint(value);
    }
}
