package io.makerplayground.project;

import io.makerplayground.device.Device;
import io.makerplayground.device.GenericDevice;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * Created by Nuntipat Narkthong on 6/2/2017 AD.
 */
public class ProjectDevice {
    private final StringProperty name;
    private final GenericDevice genericDevice;
    private Device actualDevice;

    public ProjectDevice(String name, GenericDevice genericDevice) {
        this.name = new SimpleStringProperty(name);
        this.genericDevice = genericDevice;
    }

    public ProjectDevice(String name, GenericDevice genericDevice, Device device) {
        this.name = new SimpleStringProperty(name);
        this.genericDevice = genericDevice;
        this.actualDevice = device;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public GenericDevice getGenericDevice() {
        return genericDevice;
    }

    public Device getActualDevice() {
        return actualDevice;
    }

    public void setActualDevice(Device actualDevice) {
        this.actualDevice = actualDevice;
    }
}
