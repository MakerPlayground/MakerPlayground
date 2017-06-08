package io.makerplayground.project;

import io.makerplayground.device.Device;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * Created by Nuntipat Narkthong on 6/2/2017 AD.
 */
public class ProjectDevice {
    private final StringProperty name;
    private final Device device;
    // TODO: add actual device

    public ProjectDevice(String name, Device device) {
        this.name = new SimpleStringProperty(name);
        this.device = device;
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

    public Device getDevice() {
        return device;
    }
}
