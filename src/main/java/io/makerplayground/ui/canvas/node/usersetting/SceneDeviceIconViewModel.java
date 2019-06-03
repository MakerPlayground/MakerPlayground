/*
 * Copyright (c) 2018. The Maker Playground Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.makerplayground.ui.canvas.node.usersetting;

import io.makerplayground.device.generic.GenericDevice;
import io.makerplayground.device.shared.Action;
import io.makerplayground.device.shared.DataType;
import io.makerplayground.device.shared.Parameter;
import io.makerplayground.device.shared.Value;
import io.makerplayground.project.*;
import io.makerplayground.project.expression.Expression;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.StringProperty;

import java.util.List;
import java.util.Set;

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
    }

    public void setAction(Action action) {
        userSetting.setAction(action);
    }

    public ReadOnlyObjectProperty<Action> actionProperty() {
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

    public boolean isExpressionEnable(Value v) {
        return userSetting.getExpressionEnable().get(v);
    }

    public void setExpressionEnable(Value v, boolean b) {
        userSetting.getExpressionEnable().replace(v, b);
    }

    public List<Value> getValue() {
        return userSetting.getDevice().getGenericDevice().getValue();
    }

    public List<ProjectValue> getProjectValue(Set<DataType> dataType) {
        return project.getAvailableValue(dataType);
    }

    public NodeElement getNodeElement() {
        return nodeElement;
    }

    public UserSetting getUserSetting() {
        return userSetting;
    }
}
