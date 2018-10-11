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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NumericConstraintTest {

    private NumericConstraint tester;

    @BeforeEach
    void setUp() {
        tester = new NumericConstraint(0, 100, Unit.CENTIMETER);
    }

    @Test
    void shouldNotCompatibleWithCategorical() {
        Constraint constraint = new CategoricalConstraint("");
        assertFalse(tester.isCompatible(constraint));
    }


    @Test
    void shouldCompatibleWithNone() {
        Constraint constraint = Constraint.NONE;
        assertTrue(tester.isCompatible(constraint));
    }

    @Test
    void shouldCompatibleWithConstraintInRangeAndSameUnit() {
        /* In Range, same Unit*/
        Constraint constraint = new NumericConstraint(10, 80, Unit.CENTIMETER);
        assertTrue(tester.isCompatible(constraint));

        /* In Range at boundary, same UNIT */
        Constraint constraint1 = new NumericConstraint(0, 100, Unit.CENTIMETER);
        assertTrue(tester.isCompatible(constraint1));
    }

    @Test
    void shouldNotCompatibleWithConstraintNotInRangeAndSameUnit() {
        /* Not in Range, same UNIT */
        Constraint constraint = new NumericConstraint(-0.1, 100.1, Unit.CENTIMETER);
        assertFalse(tester.isCompatible(constraint));
    }

    @Test
    void shouldNotCompatibleWithConstraintDifferentUnit() {
        /* In Range, different UNIT */
        Constraint constraint = new NumericConstraint(0, 80, Unit.METER);
        assertFalse(tester.isCompatible(constraint));

        /* In Range at boundary, different UNIT */
        Constraint constraint1 = new NumericConstraint(0, 100, Unit.METER);
        assertFalse(tester.isCompatible(constraint1));

        /* Not in Range, different UNIT */
        Constraint constraint2 = new NumericConstraint(-0.1, 100.1, Unit.METER);
        assertFalse(tester.isCompatible(constraint2));

        /* In Range, NOT_SPECIFIED UNIT */
        Constraint constraint3 = new NumericConstraint(0, 80, Unit.NOT_SPECIFIED);
        assertFalse(tester.isCompatible(constraint3));

        /* In Range at boundary, NOT_SPECIFIED UNIT */
        Constraint constraint4 = new NumericConstraint(0, 100, Unit.NOT_SPECIFIED);
        assertFalse(tester.isCompatible(constraint4));

        /* Not in Range, NOT_SPECIFIED UNIT */
        Constraint constraint5 = new NumericConstraint(-0.1, 100.1, Unit.NOT_SPECIFIED);
        assertFalse(tester.isCompatible(constraint5));
    }

    @Test
    void unionWithCategoricalConstraintShouldThrow() {
        /* Not a Numeric Constraint and Not NONE */
        Constraint constraint = new CategoricalConstraint("");
        assertThrows(ClassCastException.class, () -> tester.union(constraint));
    }

    @Test
    void unionWithDifferentUnitShouldThrow() {
        /* Different Unit  */
        Constraint constraint = new NumericConstraint(0, 80, Unit.METER);
        assertThrows(IllegalStateException.class, () -> tester.union(constraint));
    }

    @Test
    void unionWithSameUnitShouldSuccess() {
        /* Same Unit, should get Wider Range */
        Constraint c3 = new NumericConstraint(-10, 200, Unit.CENTIMETER);
        assertEquals(new NumericConstraint(-10, 200, Unit.CENTIMETER), tester.union(c3));

        /* Same Unit, should get Same Range */
        Constraint c4 = new NumericConstraint(10, 80, Unit.CENTIMETER);
        assertEquals(new NumericConstraint(0, 100, Unit.CENTIMETER), tester.union(c4));
    }

    @Test
    void testConstraintWithStringShouldBeFalse() {
        assertFalse(tester.test(""));
        assertFalse(tester.test("Hello"));
    }

    @Test
    void testConstraintWithDifferentUnitShouldBeFalse() {
        assertFalse(tester.test(0, Unit.NOT_SPECIFIED));
        assertFalse(tester.test(0, Unit.METER));
        assertFalse(tester.test(0, Unit.CELSIUS));
    }
}