package io.makerplayground.project.term;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringTermTest {
    private StringTerm tester;

    @BeforeEach
    void setTester(){ tester = new StringTerm("TEST"); }

    @Test
    void isValidTest() {
        assertNotNull(tester);
    }

    @Test
    void StringTermToCCode() {
        assertEquals("\"TEST\"",tester.toCCode());
    }
}