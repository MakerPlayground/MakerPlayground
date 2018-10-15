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
        assertNotNull(library.getGenericInputDevice());
        assertFalse(library.getGenericInputDevice().isEmpty());
    }

    @Test
    void getGenericOutputDevice() {
        assertNotNull(library.getGenericOutputDevice());
        assertFalse(library.getGenericOutputDevice().isEmpty());
    }

    @Test
    void getGenericConnectivityDevice() {
        assertNotNull(library.getGenericConnectivityDevice());
        assertFalse(library.getGenericConnectivityDevice().isEmpty());
    }

    @Test
    void getActualDevice() {
        assertNotNull(library.getActualDevice());
        assertFalse(library.getActualDevice().isEmpty());

        assertNotNull(library.getActualDevice("MP-0000"));
        assertNull(library.getActualDevice(""));

        assertNotNull(library.getActualDevice(Platform.MP_ARDUINO));
        assertFalse(library.getActualDevice(Platform.MP_ARDUINO).isEmpty());
    }
}