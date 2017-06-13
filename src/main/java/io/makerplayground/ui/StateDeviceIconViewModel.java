package io.makerplayground.ui;

import io.makerplayground.project.DeviceSetting;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Created by tanyagorn on 6/12/2017.
 */
public class StateDeviceIconViewModel {

    private final DeviceSetting deviceSetting;
    private final SimpleStringProperty name;

    public StateDeviceIconViewModel(DeviceSetting deviceSetting) {
        this.deviceSetting = deviceSetting;
        this.name = new SimpleStringProperty(deviceSetting.getDevice().getName());
    }

    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public String getImageName() {
        return deviceSetting.getDevice().getDevice().getName();
    }
}
