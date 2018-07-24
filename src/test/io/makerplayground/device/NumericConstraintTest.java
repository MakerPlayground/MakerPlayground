package io.makerplayground.device;

import io.makerplayground.helper.Unit;
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
    void isNotCompatibleWithCategorical() {
        Constraint constraint = new CategoricalConstraint("");
        assertFalse(tester.isCompatible(constraint));
    }


    @Test
    void isCompatibleWithNone() {
        Constraint constraint = Constraint.NONE;
        assertTrue(tester.isCompatible(constraint));
    }

    @Test
    void isCompatibleWithConstraintInRangeAndSameUnit() {
        /* In Range, same Unit*/
        Constraint constraint = new NumericConstraint(10, 80, Unit.CENTIMETER);
        assertTrue(tester.isCompatible(constraint));

        /* In Range at boundary, same UNIT */
        Constraint constraint1 = new NumericConstraint(0, 100, Unit.CENTIMETER);
        assertTrue(tester.isCompatible(constraint1));
    }

    @Test
    void isNotCompatibleWithConstraintNotInRangeAndSameUnit() {
        /* Not in Range, same UNIT */
        Constraint constraint = new NumericConstraint(-0.1, 100.1, Unit.CENTIMETER);
        assertFalse(tester.isCompatible(constraint));
    }

    @Test
    void isNotCompatibleWithConstraintDifferentUnit() {
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
    void unionWithCategoricalConstraintMustBeThrow() {
        /* Not a Numeric Constraint and Not NONE */
        Constraint constraint = new CategoricalConstraint("");
        assertThrows(ClassCastException.class, () -> tester.union(constraint));
    }

    @Test
    void unionWithDifferentUnitMustBeThrow() {
        /* Different Unit  */
        Constraint constraint = new NumericConstraint(0, 80, Unit.METER);
        assertThrows(IllegalStateException.class, () -> tester.union(constraint));
    }

    @Test
    void unionWithSameUnitMustSuccess() {
        /* Same Unit, should get Wider Range */
        Constraint c3 = new NumericConstraint(-10, 200, Unit.CENTIMETER);
        assertEquals(new NumericConstraint(-10, 200, Unit.CENTIMETER), tester.union(c3));

        /* Same Unit, should get Same Range */
        Constraint c4 = new NumericConstraint(10, 80, Unit.CENTIMETER);
        assertEquals(new NumericConstraint(0, 100, Unit.CENTIMETER), tester.union(c4));
    }

    @Test
    void testConstraintWithStringMustBeFalse() {
        assertFalse(tester.test(""));
        assertFalse(tester.test("Hello"));
    }

    @Test
    void testConstraintWithDifferentUnitMustBeThrown() {
        assertThrows(IllegalArgumentException.class, ()->tester.test(0, Unit.NOT_SPECIFIED));
        assertThrows(IllegalArgumentException.class, ()->tester.test(0, Unit.METER));
        assertThrows(IllegalArgumentException.class, ()->tester.test(0, Unit.CELSIUS));
    }
}