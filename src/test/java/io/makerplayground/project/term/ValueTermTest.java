package io.makerplayground.project.term;

import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectDevice;
import io.makerplayground.project.ProjectValue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValueTermTest {
    private ValueTerm tester;
    private ProjectValue projectValue;

    ValueTermTest() {
        DeviceLibrary library = DeviceLibrary.INSTANCE;
        library.loadDeviceFromJSON();
        Project project = new Project();
        project.addSensor(library.getGenericDevice("Accelerometer"));
        projectValue = project.getAvailableValue().get(0);
        tester = new ValueTerm(projectValue);
    }

    @Test
    void isValidTestMustBeTrueIfProjectValueIsNotNull() {
        assertTrue(tester.isValid());
    }

    @Test
    void isValidMustBeFalseIfProjectValueIsNull() {
        ValueTerm testerNull;
        testerNull = new ValueTerm(null);
        assertFalse(testerNull.isValid());
    }

    @Test
    void ValueToCCode() {
        String expectingName = String.format("_%s_%s", projectValue.getDevice().getName(), projectValue.getValue().getName());
        assertEquals(expectingName, tester.toCCode());
    }
}