/*
 * Copyright (c) 2020. The Maker Playground Authors.
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

package io.makerplayground.ui.canvas.node;

import io.makerplayground.project.*;
import io.makerplayground.ui.canvas.helper.DynamicViewModelCreator;
import io.makerplayground.ui.canvas.node.usersetting.SceneDeviceIconViewModel;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.ObservableList;

import java.util.List;

public class ConditionViewModel {
    private final Condition condition;
    private final Project project;

    private final DynamicViewModelCreator<UserSetting, SceneDeviceIconViewModel> deviceViewModelCreator;
    private final DynamicViewModelCreator<UserSetting, SceneDeviceIconViewModel> virtualDeviceViewModelCreator;

    private final BooleanProperty hasDeviceToAdd;
    private final BooleanProperty hasLineIn;
    private final BooleanProperty hasLineOut;

    public ConditionViewModel(Condition condition, Project project) {
        this.condition = condition;
        this.project = project;

        this.deviceViewModelCreator = new DynamicViewModelCreator<>(condition.getSetting(), userSetting -> new SceneDeviceIconViewModel(userSetting, condition, project));
        this.virtualDeviceViewModelCreator = new DynamicViewModelCreator<>(condition.getVirtualDeviceSetting(), userSetting -> new SceneDeviceIconViewModel(userSetting, condition, project));

        hasDeviceToAdd = new SimpleBooleanProperty();
        hasDeviceToAdd.bind(Bindings.size(project.getDeviceWithCondition()).add(VirtualProjectDevice.devicesWithCondition.size())
                .greaterThan(Bindings.size(condition.getSetting()).add(Bindings.size(condition.getVirtualDeviceSetting()))));

        hasLineIn = new SimpleBooleanProperty();
        hasLineIn.bind(Bindings.size(project.getUnmodifiableLine().filtered(line -> line.getDestination() == condition)).greaterThan(0));

        hasLineOut = new SimpleBooleanProperty();
        hasLineOut.bind(Bindings.size(project.getUnmodifiableLine().filtered(line -> line.getSource() == condition)).greaterThan(0));
    }

    public DynamicViewModelCreator<UserSetting, SceneDeviceIconViewModel> getDeviceViewModelCreator() {
        return deviceViewModelCreator;
    }

    public DynamicViewModelCreator<UserSetting, SceneDeviceIconViewModel> getVirtualDeviceViewModelCreator() {
        return virtualDeviceViewModelCreator;
    }

    public String getName() {
        return condition.getName();
    }

    public void setName(String name) {
        condition.setName(name);
    }

    public double getX() { return condition.getLeft(); }

    public DoubleProperty xProperty() { return condition.leftProperty();}

    public double getY() { return condition.getTop(); }

    public DoubleProperty yProperty() { return condition.topProperty(); }

    public double getSourcePortX() {
        return condition.getSourcePortX();
    }

    public DoubleProperty sourcePortXProperty() {
        return condition.sourcePortXProperty();
    }

    public double getSourcePortY() {
        return condition.getSourcePortY();
    }

    public DoubleProperty sourcePortYProperty() {
        return condition.sourcePortYProperty();
    }

    public double getDestPortX() {
        return condition.getDestPortX();
    }

    public DoubleProperty destPortXProperty() {
        return condition.destPortXProperty();
    }

    public double getDestPortY() {
        return condition.getDestPortY();
    }

    public DoubleProperty destPortYProperty() {
        return condition.destPortYProperty();
    }

    public List<ProjectDevice> getProjectInputDevice() {
        return project.getDeviceWithCondition();
    }

    public Condition getCondition() { return condition; }

    public ObservableList<UserSetting> getDeviceSetting() {
        return condition.getSetting();
    }

    public ObservableList<UserSetting> getVirtualDeviceSetting() {
        return condition.getVirtualDeviceSetting();
    }

    public void removeUserSetting(UserSetting setting) { condition.removeUserSetting(setting); }

    public BooleanProperty hasDeviceToAddProperty() { return hasDeviceToAdd; }

    public ReadOnlyBooleanProperty hasLineInProperty() {
        return hasLineIn;
    }

    public ReadOnlyBooleanProperty hasLineOutProperty() {
        return hasLineOut;
    }

    public boolean hasConnectionFrom(NodeElement other) {
        return project.hasLine(other, condition);
    }

    public boolean hasConnectionTo(NodeElement other) {
        return project.hasLine(condition, other);
    }

    public final DiagramError getError() {
        return condition.getError();
    }

    public final ReadOnlyObjectProperty<DiagramError> errorProperty() {
        return condition.errorProperty();
    }
}
