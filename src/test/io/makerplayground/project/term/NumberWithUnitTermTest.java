package io.makerplayground.project.term;

import io.makerplayground.device.shared.NumberWithUnit;
import io.makerplayground.device.shared.Unit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NumberWithUnitTermTest {
    private NumberWithUnitTerm tester;

    @BeforeEach
    void setTester(){ tester = new NumberWithUnitTerm(new NumberWithUnit(12.345,Unit.CENTIMETER));}

    @Test
    void isValidIsTrueIfThereExistNumberWithUnit() {
        assertTrue(tester.isValid());
    }

    @Test
    void isValidIsFalseIfThereIsNotExistNumberWithUnit() {
        NumberWithUnitTerm testerNull = new NumberWithUnitTerm(null);
        assertFalse(testerNull.isValid());
    }

    @Test
    void NumberWithUnitToCCode() {
        assertEquals("12.35",tester.toCCode());
    }
}