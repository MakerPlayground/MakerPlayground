package io.makerplayground.project;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.makerplayground.device.Device;
import io.makerplayground.device.DevicePort;
import io.makerplayground.device.GenericDevice;
import io.makerplayground.device.Property;
import io.makerplayground.helper.Peripheral;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * Created by Nuntipat Narkthong on 6/2/2017 AD.
 */
@JsonSerialize (using = ProjectDeviceSerializer.class)
public class ProjectDevice {
    private StringProperty name;
    private final GenericDevice genericDevice;
    private boolean autoSelectDevice;
    private Device actualDevice;
    private Map<Peripheral, List<DevicePort>> deviceConnection; // connection from this device (key) to the processor (value)
    private Device dependentDevice;
    private Map<Peripheral, List<DevicePort>> dependentDeviceConnection; // connection from this device (key) to the processor (value)
    private Map<Property, String> propertyValue;  // property needed by the actual device

    public ProjectDevice(String name, GenericDevice genericDevice) {
        this.name = new SimpleStringProperty(name);
        this.genericDevice = genericDevice;
        this.autoSelectDevice = true;
        this.actualDevice = null;
        this.deviceConnection = new HashMap<>();
        this.dependentDevice = null;
        this.dependentDeviceConnection = new HashMap<>();
        this.propertyValue = new HashMap<>();
    }

    ProjectDevice(String name, GenericDevice genericDevice, boolean autoSelectDevice, Device actualDevice, Map<Peripheral
            , List<DevicePort>> deviceConnection, Device dependentDevice, Map<Peripheral
            , List<DevicePort>> dependentDeviceConnection, Map<Property, String> propertyValue) {
        this.name = new SimpleStringProperty(name);
        this.genericDevice = genericDevice;
        this.autoSelectDevice = autoSelectDevice;
        this.actualDevice = actualDevice;
        this.deviceConnection = deviceConnection;
        this.dependentDevice = dependentDevice;
        this.dependentDeviceConnection = dependentDeviceConnection;
        this.propertyValue = propertyValue;
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public StringProperty nameProperty() {
        return name;
    }

    public GenericDevice getGenericDevice() {
        return genericDevice;
    }

    public boolean isAutoSelectDevice() {
        return autoSelectDevice;
    }

    public void setAutoSelectDevice(boolean autoSelectDevice) {
        this.autoSelectDevice = autoSelectDevice;
    }

    public Device getActualDevice() {
        return actualDevice;
    }

    public void setActualDevice(Device actualDevice) {
        this.actualDevice = actualDevice;
    }

    public Map<Peripheral, List<DevicePort>> getDeviceConnection() {
        return deviceConnection;
    }

//    public void setDeviceConnection(Map<Peripheral, Peripheral> deviceConnection) {
//        this.deviceConnection = deviceConnection;
//    }
    public void setDeviceConnection(Peripheral device, List<DevicePort> processor) {
        this.deviceConnection.put(device, processor);
    }

    public void removeDeviceConnection(Peripheral device) {
        this.deviceConnection.remove(device);
    }

    public void removeAllDeviceConnection() {
        this.deviceConnection.clear();
    }

    public Device getDependentDevice() {
        return dependentDevice;
    }

//    public void setDependentDevice(Device dependentDevice) {
//        this.dependentDevice = dependentDevice;
//    }

    public Map<Peripheral, List<DevicePort>> getDependentDeviceConnection() {
        return dependentDeviceConnection;
    }

//    public void setDependentDeviceConnection(Peripheral device, List<DevicePort> processor) {
//        this.dependentDeviceConnection.put(device, processor);
//    }

    public String getPropertyValue(Property p) {
        return propertyValue.get(p);
    }

    public void setPropertyValue(Property p, String value) {
        propertyValue.put(p, value);
    }

//    public void setDependentDeviceConnection(Map<Peripheral, Peripheral> dependentDeviceConnection) {
//        this.dependentDeviceConnection = dependentDeviceConnection;
//    }
}
