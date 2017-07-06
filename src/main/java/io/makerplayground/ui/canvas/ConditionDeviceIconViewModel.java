package io.makerplayground.ui.canvas;

import io.makerplayground.device.Action;
import io.makerplayground.device.GenericDevice;
import io.makerplayground.device.Parameter;
import io.makerplayground.project.ProjectDevice;
import io.makerplayground.project.UserSetting;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Created by USER on 05-Jul-17.
 */
public class ConditionDeviceIconViewModel {

    private final UserSetting userSetting;
    private final SimpleStringProperty name;

    public ConditionDeviceIconViewModel(UserSetting userSetting) {
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

    public Action getAction() {
        return userSetting.getAction();
    }

    public ObjectProperty<Action> actionProperty() {
        return userSetting.actionProperty();
    }

    public ProjectDevice getProjectDevice() { return userSetting.getDevice(); }

    public GenericDevice getGenericDevice() {
        return userSetting.getDevice().getGenericDevice();
    }

    public Object getParameterValue(Parameter p) {
        return userSetting.getValueMap().get(p);
    }

    public Object setParameterValue(Parameter p, Object o) {
        return userSetting.getValueMap().replace(p, o);
    }
}
