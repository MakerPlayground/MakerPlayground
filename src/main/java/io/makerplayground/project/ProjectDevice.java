package io.makerplayground.project;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.makerplayground.device.Device;
import io.makerplayground.device.GenericDevice;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * Created by Nuntipat Narkthong on 6/2/2017 AD.
 */
@JsonSerialize (using = ProjectDeviceSerializer.class)
public class ProjectDevice {
    private String name;
    private final GenericDevice genericDevice;
    private Device actualDevice;

    public ProjectDevice(String name, GenericDevice genericDevice) {
        this.name = name;
        this.genericDevice = genericDevice;
    }

    public ProjectDevice(String name, GenericDevice genericDevice, Device device) {
        this.name = name;
        this.genericDevice = genericDevice;
        this.actualDevice = device;
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

    public Device getActualDevice() {
        return actualDevice;
    }

    public void setActualDevice(Device actualDevice) {
        this.actualDevice = actualDevice;
    }
}
