package io.makerplayground.project;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.makerplayground.device.Device;
import io.makerplayground.device.GenericDevice;
import io.makerplayground.helper.Peripheral;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Map;

/**
 *
 * Created by Nuntipat Narkthong on 6/2/2017 AD.
 */
@JsonSerialize (using = ProjectDeviceSerializer.class)
public class ProjectDevice {
    private String name;
    private final GenericDevice genericDevice;
    private boolean autoSelectDevice;
    private Device actualDevice;
    private Map<Peripheral, Peripheral> deviceConnection; // connection from this device (key) to the processor (value)
    private Device dependentDevice;
    private Map<Peripheral, Peripheral> dependentDeviceConnection; // connection from this device (key) to the processor (value)
    //private Map<String, String> connectedPin;

    public ProjectDevice(String name, GenericDevice genericDevice) {
        this.name = name;
        this.genericDevice = genericDevice;
        this.autoSelectDevice = true;
    }

    public ProjectDevice(String name, GenericDevice genericDevice, Device device) {
        this.name = name;
        this.genericDevice = genericDevice;
        this.actualDevice = device;
        this.autoSelectDevice = true;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
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

    public Map<Peripheral, Peripheral> getDeviceConnection() {
        return deviceConnection;
    }

    public void setDeviceConnection(Map<Peripheral, Peripheral> deviceConnection) {
        this.deviceConnection = deviceConnection;
    }

    public Device getDependentDevice() {
        return dependentDevice;
    }

    public void setDependentDevice(Device dependentDevice) {
        this.dependentDevice = dependentDevice;
    }

    public Map<Peripheral, Peripheral> getDependentDeviceConnection() {
        return dependentDeviceConnection;
    }

    public void setDependentDeviceConnection(Map<Peripheral, Peripheral> dependentDeviceConnection) {
        this.dependentDeviceConnection = dependentDeviceConnection;
    }


}
