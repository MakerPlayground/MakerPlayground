package io.makerplayground.device;

import io.makerplayground.helper.Unit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NumericConstraintTest {

    @Test
    void isCompatibleShouldBeCorrected() {
        NumericConstraint tester = new NumericConstraint(0, 100, Unit.CENTIMETER);

        /* Not a Numeric Constraint and Not NONE */
        Constraint c1 = new CategoricalConstraint("");
        assertFalse(tester.isCompatible(c1));

        /* Constraint.NONE*/
        Constraint c2 = Constraint.NONE;
        assertTrue(tester.isCompatible(c2));

        /* In Range, same UNIT */
        NumericConstraint c3 = new NumericConstraint(0, 80, Unit.CENTIMETER);
        assertTrue(tester.isCompatible(c3));

        /* In Range at boundary, same UNIT */
        NumericConstraint c4 = new NumericConstraint(0, 100, Unit.CENTIMETER);
        assertTrue(tester.isCompatible(c4));

        /* Not in Range, same UNIT */
        NumericConstraint c5 = new NumericConstraint(-0.1, 100.1, Unit.CENTIMETER);
        assertFalse(tester.isCompatible(c5));

        /* In Range, different UNIT */
        NumericConstraint c6 = new NumericConstraint(0, 80, Unit.METER);
        assertFalse(tester.isCompatible(c6));

        /* In Range at boundary, different UNIT */
        NumericConstraint c7 = new NumericConstraint(0, 100, Unit.METER);
        assertFalse(tester.isCompatible(c7));

        /* Not in Range, different UNIT */
        NumericConstraint c8 = new NumericConstraint(-0.1, 100.1, Unit.METER);
        assertFalse(tester.isCompatible(c8));

        /* In Range, NOT_SPECIFIED UNIT */
        NumericConstraint c9 = new NumericConstraint(0, 80, Unit.NOT_SPECIFIED);
        assertFalse(tester.isCompatible(c9));

        /* In Range at boundary, NOT_SPECIFIED UNIT */
        NumericConstraint c10 = new NumericConstraint(0, 100, Unit.NOT_SPECIFIED);
        assertFalse(tester.isCompatible(c10));

        /* Not in Range, NOT_SPECIFIED UNIT */
        NumericConstraint c11 = new NumericConstraint(-0.1, 100.1, Unit.NOT_SPECIFIED);
        assertFalse(tester.isCompatible(c11));
    }

    @Test
    void unionShouldBeCorrected() {

    }

    @Test
    void test1() {
    }

    @Test
    void test2() {
    }

}