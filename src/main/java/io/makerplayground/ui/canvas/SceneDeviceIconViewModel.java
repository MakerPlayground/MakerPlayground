package io.makerplayground.ui.canvas;

import io.makerplayground.device.Action;
import io.makerplayground.device.GenericDevice;
import io.makerplayground.device.Parameter;
import io.makerplayground.project.ProjectDevice;
import io.makerplayground.project.StateDeviceSetting;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Created by tanyagorn on 6/12/2017.
 */
public class SceneDeviceIconViewModel {

    private final StateDeviceSetting stateDeviceSetting;
    private final SimpleStringProperty name;

    public SceneDeviceIconViewModel(StateDeviceSetting stateDeviceSetting) {
        this.stateDeviceSetting = stateDeviceSetting;
        this.name = new SimpleStringProperty(stateDeviceSetting.getDevice().getName());
    }

    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public String getImageName() {
        return stateDeviceSetting.getDevice().getGenericDevice().getName();
    }

    public Action getAction() {
        return stateDeviceSetting.getAction();
        //return action.get();
    }

    public ObjectProperty<Action> actionProperty() {
        //return action;
        return stateDeviceSetting.actionProperty();
    }

    public ProjectDevice getProjectDevice() {
        return stateDeviceSetting.getDevice();
    }

    public GenericDevice getGenericDevice() {
        return stateDeviceSetting.getDevice().getGenericDevice();
    }

    public Object getParameterValue(Parameter p) {
        //System.out.println("will return " + stateDeviceSetting.getValueMap().get(p));
        return stateDeviceSetting.getValueMap().get(p);
    }

    public Object setParameterValue(Parameter p, Object o) {
        //System.out.println("will set " + o);
        //System.out.println(stateDeviceSetting.getValueMap());
        return stateDeviceSetting.getValueMap().replace(p, o);
    }

}
