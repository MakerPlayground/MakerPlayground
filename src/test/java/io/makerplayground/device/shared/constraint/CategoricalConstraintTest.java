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

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class CategoricalConstraintTest {
    private CategoricalConstraint tester;

    @BeforeEach
    void  setTester(){ tester = new CategoricalConstraint("CENTIMETER");}

    @Test
    void shouldNotCompatibleWithNumeric(){
        Constraint constraint = new NumericConstraint(0, 100, Unit.CENTIMETER);
        assertFalse(tester.isCompatible(constraint));
    }

    @Test
    void shouldNotCompatibleWithNone(){
        Constraint constraint = Constraint.NONE;
        assertFalse(tester.isCompatible(constraint));
    }

    @Test
    void shouldNotCompatibleWithCategoricalNoContain(){
        Constraint constraint = new CategoricalConstraint("METER");
        assertFalse(tester.isCompatible(constraint));
    }

    @Test
    void shouldNotCompatibleWithCategoricalPartialContain(){
        Constraint constraint = new CategoricalConstraint(Arrays.asList("CENTIMETER","METER"));
        assertFalse(tester.isCompatible(constraint));
    }

    @Test
    void shouldCompatibleWithCategoricalContainAll(){
        Constraint constraint = new CategoricalConstraint(Arrays.asList("CENTIMETER","METER"));
        assertTrue(constraint.isCompatible(tester));
    }

    @Test
    void  unionWithNotCategoricalShouldThrow(){
        Constraint constraint = Constraint.NONE;
        NumericConstraint numericConstraint = new NumericConstraint(0, 100, Unit.CENTIMETER);
        assertThrows(ClassCastException.class,() -> tester.union(constraint));
        assertThrows(ClassCastException.class,() -> tester.union(numericConstraint));
    }

    @Test
    void unionWithCategoricalShouldSuccess(){
        Constraint categoricalConstraint = new CategoricalConstraint(Arrays.asList("METER","DEGREE"));
        assertEquals(new CategoricalConstraint(Arrays.asList("CENTIMETER","METER","DEGREE")),tester.union(categoricalConstraint));
    }


    @Test
    void testConstraintWithNumericShouldBeFalse() {
        assertFalse(tester.test(10,Unit.CENTIMETER));
    }

    @Test
    void testConstraintWithStringShouldBeTrue() {
        assertTrue(tester.test("CENTIMETER"));
    }
}