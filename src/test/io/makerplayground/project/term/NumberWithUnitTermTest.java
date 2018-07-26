package io.makerplayground.project.term;

import io.makerplayground.helper.NumberWithUnit;
import io.makerplayground.helper.Unit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NumberWithUnitTermTest {
    private NumberWithUnitTerm tester;

    @BeforeEach
    void  setTester(){ tester = new NumberWithUnitTerm(new NumberWithUnit(12.345,Unit.CENTIMETER));}

    @Test
    void isValidTest() {
        assertNotNull(tester);
    }

    @Test
    void NumberWithUnitToCCode() {
        assertEquals("12.35",tester.toCCode());
    }
}