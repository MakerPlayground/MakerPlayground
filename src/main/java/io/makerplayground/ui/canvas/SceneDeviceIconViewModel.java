package io.makerplayground.ui.canvas;

import io.makerplayground.device.Action;
import io.makerplayground.device.GenericDevice;
import io.makerplayground.device.Parameter;
import io.makerplayground.device.Value;
import io.makerplayground.project.*;
import io.makerplayground.project.expression.Expression;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;

import java.util.List;

/**
 * Created by tanyagorn on 6/12/2017.
 */
public class SceneDeviceIconViewModel {

    private final UserSetting userSetting;
    //private final SimpleStringProperty name;
    private final Project project;
    private final NodeElement nodeElement;

    public SceneDeviceIconViewModel(UserSetting userSetting, NodeElement nodeElement, Project project) {
        this.userSetting = userSetting;
        //this.name = new SimpleStringProperty(userSetting.getDevice().getName());
        this.nodeElement = nodeElement;
        this.project = project;
    }

    public String getName() {
        return userSetting.getDevice().getName();
    }

    public StringProperty nameProperty() {
        return userSetting.getDevice().nameProperty();
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

    public Expression getParameterValue(Parameter p) {
        //System.out.println("will return " + userSetting.getValueMap().get(p));
        return userSetting.getValueMap().get(p);
    }

    public Expression setParameterValue(Parameter p, Expression o) {
        //System.out.println("will set " + o);
        //System.out.println(userSetting.getValueMap());
        return userSetting.getValueMap().replace(p, o);
    }

    public Expression getExpression(Value v) {
        return userSetting.getExpression().get(v);
    }

    public void setExpression(Value v, Expression expressions) {
        userSetting.getExpression().replace(v, expressions);
    }

    public List<Value> getValue() {
        return userSetting.getDevice().getGenericDevice().getValue();
    }

    public List<ProjectValue> getProjectValue() {
        return project.getAvailableValue();
    }

    public NodeElement getNodeElement() {
        return nodeElement;
    }
}
