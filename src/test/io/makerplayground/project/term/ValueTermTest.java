package io.makerplayground.project.term;

import io.makerplayground.project.ProjectValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValueTermTest {
    private ValueTerm tester;

    //@BeforeEach
    //void setTester(){ tester = new ValueTerm(new ProjectValue("",""));}

    @Test
    void isValidTest() {
        assertNotNull(tester);
    }

    @Test
    void ValueToCCode() {
        assertEquals("",tester.toCCode());
    }
}