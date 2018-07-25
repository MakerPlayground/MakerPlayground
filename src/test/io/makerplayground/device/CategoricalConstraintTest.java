package io.makerplayground.device;

import io.makerplayground.helper.Unit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class CategoricalConstraintTest {
    private  CategoricalConstraint tester;

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