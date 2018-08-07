package io.makerplayground.project.term;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringTermTest {

    @Test
    void isValidMustBeTrueIfThereIsExistingString() {
        StringTerm tester = new StringTerm("TEST");
        assertTrue(tester.isValid());
    }

    @Test
    void isValidMustBeTrueIfThereIsEmptyString() {
        StringTerm tester = new StringTerm("");
        assertTrue(tester.isValid());
    }

    @Test
    void isValidMustBeFalseIfThereIsNoExistingString() {
        StringTerm tester = new StringTerm(null);
        assertFalse(tester.isValid());
    }

    @Test
    void StringTermToCCode() {
        StringTerm tester = new StringTerm("TEST");
        String expecting = String.format("\"%s\"", tester.getValue());
        assertEquals(expecting, tester.toCCode());
    }
}