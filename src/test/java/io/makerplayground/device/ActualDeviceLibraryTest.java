package io.makerplayground.device;

import io.makerplayground.device.actual.Platform;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ActualDeviceLibraryTest {

    private DeviceLibrary library = DeviceLibrary.INSTANCE;

    ActualDeviceLibraryTest() {
        library.loadDeviceFromJSON();
    }

    @Test
    void getGenericDevice() {
        assertNotNull(library.getGenericDevice("LED"));
        assertNull(library.getGenericDevice(""));
    }

    @Test
    void getGenericInputDevice() {
        assertNotNull(library.getGenericSensorDevice());
        assertFalse(library.getGenericSensorDevice().isEmpty());
    }

    @Test
    void getGenericOutputDevice() {
        assertNotNull(library.getGenericActuatorDevice());
        assertFalse(library.getGenericActuatorDevice().isEmpty());
    }

    @Test
    void getGenericConnectivityDevice() {
        assertNotNull(library.getGenericUtilityDevice());
        assertFalse(library.getGenericUtilityDevice().isEmpty());
    }

    @Test
    void getActualDevice() {
        assertNotNull(library.getActualDevice());
        assertFalse(library.getActualDevice().isEmpty());

        assertNotNull(library.getActualDevice("MP-0000"));
        assertNull(library.getActualDevice(""));

        assertNotNull(library.getActualDevice(Platform.ARDUINO_AVR8));
        assertFalse(library.getActualDevice(Platform.ARDUINO_AVR8).isEmpty());
    }
}