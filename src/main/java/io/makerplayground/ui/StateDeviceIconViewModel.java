package io.makerplayground.ui;

import io.makerplayground.project.UserSetting;
import javafx.beans.property.SimpleStringProperty;

/**
 * Created by tanyagorn on 6/12/2017.
 */
public class StateDeviceIconViewModel {

    private final UserSetting userSetting;
    private final SimpleStringProperty name;

    public StateDeviceIconViewModel(UserSetting userSetting) {
        this.userSetting = userSetting;
        this.name = new SimpleStringProperty(userSetting.getDevice().getName());
    }

    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public String getImageName() {
        return userSetting.getDevice().getGenericDevice().getName();
    }
}
