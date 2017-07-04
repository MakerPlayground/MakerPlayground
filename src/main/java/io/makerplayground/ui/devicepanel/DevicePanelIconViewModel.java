package io.makerplayground.ui.devicepanel;

import io.makerplayground.project.ProjectDevice;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * Created by Nuntipat Narkthong on 6/8/2017 AD.
 */
public class DevicePanelIconViewModel {

    private final ProjectDevice device;
    private final StringProperty name;

    public DevicePanelIconViewModel(ProjectDevice device) {
        this.device = device;
        this.name = new SimpleStringProperty(device.getName());
        this.name.addListener((observable, oldValue, newValue) -> this.device.setName(newValue));
    }

    public String getDeviceName() {
        return device.getGenericDevice().getName();
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

    public ProjectDevice getDevice() {
        return device;
    }
}
