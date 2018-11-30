package io.makerplayground.device;

import io.makerplayground.device.generic.GenericDevice;

public enum GenericDeviceType {
    SENSOR, ACTUATOR, UTILITY, CLOUD, INTERFACE;

    // TODO: create lookup instead of searching
    public static GenericDeviceType of(GenericDevice genericDevice) {
        DeviceLibrary deviceLibrary = DeviceLibrary.INSTANCE;
        if (deviceLibrary.genericSensorDevice.contains(genericDevice)) return GenericDeviceType.SENSOR;
        if (deviceLibrary.genericActuatorDevice.contains(genericDevice)) return GenericDeviceType.ACTUATOR;
        if (deviceLibrary.genericUtilityDevice.contains(genericDevice)) return GenericDeviceType.UTILITY;
        if (deviceLibrary.genericCloudDevice.contains(genericDevice)) return GenericDeviceType.CLOUD;
        if (deviceLibrary.genericInterfaceDevice.contains(genericDevice)) return GenericDeviceType.INTERFACE;
        throw new IllegalStateException("Not support types");
    }
}
