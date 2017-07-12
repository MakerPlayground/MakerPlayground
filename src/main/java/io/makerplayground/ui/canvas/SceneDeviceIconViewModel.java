package io.makerplayground.ui.canvas;

import io.makerplayground.device.Action;
import io.makerplayground.device.GenericDevice;
import io.makerplayground.device.Parameter;
import io.makerplayground.device.Value;
import io.makerplayground.helper.Unit;
import io.makerplayground.project.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;

import java.util.List;

/**
 * Created by tanyagorn on 6/12/2017.
 */
public class SceneDeviceIconViewModel {

    private final UserSetting userSetting;
    private final SimpleStringProperty name;
    private final Project project;

    public SceneDeviceIconViewModel(UserSetting userSetting, Project project) {
        this.userSetting = userSetting;
        this.name = new SimpleStringProperty(userSetting.getDevice().getName());
        this.project = project;
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

    public ObservableList<Expression> getExpression(Value v) {
        return userSetting.getExpression().get(v);
    }

    public void setExpression(Value v, ObservableList<Expression> expressions) {
        userSetting.getExpression().replace(v, expressions);
    }

    public List<Value> getValue() {
        return userSetting.getDevice().getGenericDevice().getValue();
    }

    public List<ProjectValue> getProjectValue() {
        return project.getAvailableValue();
    }



}
