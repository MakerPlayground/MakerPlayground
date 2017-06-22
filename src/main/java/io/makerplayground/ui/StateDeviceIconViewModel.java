package io.makerplayground.ui;

import io.makerplayground.device.Action;
import io.makerplayground.device.GenericDevice;
import io.makerplayground.project.UserSetting;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Created by tanyagorn on 6/12/2017.
 */
public class StateDeviceIconViewModel {

    private final UserSetting userSetting;
    private final SimpleObjectProperty<Action> action;
    private final SimpleStringProperty name;

    public StateDeviceIconViewModel(UserSetting userSetting) {
        this.userSetting = userSetting;
        this.name = new SimpleStringProperty(userSetting.getDevice().getName());
        this.action = new SimpleObjectProperty<>(userSetting.getAction());
        this.action.addListener((observable, oldValue, newValue) -> {
            userSetting.setAction(newValue);
        });
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

    public Action getAction() {
        return action.get();
    }

    public SimpleObjectProperty<Action> actionProperty() {
        return action;
    }

    public GenericDevice getGenericDevice() {
        return userSetting.getDevice().getGenericDevice();
    }


//    public UserSetting getUserSetting() {
//        return userSetting;
//    }
}
