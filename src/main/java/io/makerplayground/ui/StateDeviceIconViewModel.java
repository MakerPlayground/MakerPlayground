package io.makerplayground.ui;

import io.makerplayground.device.Action;
import io.makerplayground.device.GenericDevice;
import io.makerplayground.device.Parameter;
import io.makerplayground.project.ProjectDevice;
import io.makerplayground.project.UserSetting;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableMap;

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

    public Action getAction() {
        return userSetting.getAction();
        //return action.get();
    }

    public ObjectProperty<Action> actionProperty() {
        //return action;
        return userSetting.actionProperty();
    }

    public ProjectDevice getProjectDevice() {
        return userSetting.getDevice();
    }

    public GenericDevice getGenericDevice() {
        return userSetting.getDevice().getGenericDevice();
    }

    public Object getParameterValue(Parameter p) {
        //System.out.println("will return " + userSetting.getValueMap().get(p));
        return userSetting.getValueMap().get(p);
    }

    public Object setParameterValue(Parameter p, Object o) {
        //System.out.println("will set " + o);
        //System.out.println(userSetting.getValueMap());
        return userSetting.getValueMap().replace(p, o);
    }

}
