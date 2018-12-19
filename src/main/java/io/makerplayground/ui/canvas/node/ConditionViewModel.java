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

package io.makerplayground.ui.canvas.node;

import io.makerplayground.project.*;
import io.makerplayground.ui.canvas.node.usersetting.SceneDeviceIconViewModel;
import io.makerplayground.ui.canvas.helper.DynamicViewModelCreator;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.ObservableList;

import java.util.List;

/**
 * Created by USER on 05-Jul-17.
 */
public class ConditionViewModel {
    private final Condition condition;
    private final Project project;

    private final DynamicViewModelCreator<UserSetting, SceneDeviceIconViewModel> dynamicViewModelCreator;

    private final BooleanProperty hasDeviceToAdd;

    public ConditionViewModel(Condition condition, Project project) {
        this.condition = condition;
        this.project = project;

        this.dynamicViewModelCreator = new DynamicViewModelCreator<>(condition.getSetting(), userSetting -> new SceneDeviceIconViewModel(userSetting, condition, project));

        hasDeviceToAdd = new SimpleBooleanProperty();
        hasDeviceToAdd.bind(Bindings.size(project.getDeviceWithCondition()).greaterThan(Bindings.size(condition.getSetting())));
    }

    public DynamicViewModelCreator<UserSetting, SceneDeviceIconViewModel> getDynamicViewModelCreator() {
        return dynamicViewModelCreator;
    }

    public double getX() { return condition.getLeft(); }

    public DoubleProperty xProperty() { return condition.leftProperty();}

    public double getY() { return condition.getTop(); }

    public DoubleProperty yProperty() { return condition.topProperty(); }

    public List<ProjectDevice> getProjectInputDevice() {
        return project.getDeviceWithCondition();
    }

    public Condition getCondition() { return condition; }

    public ObservableList<UserSetting> getConditionDevice() { return condition.getSetting(); }

    public void removeConditionDevice(ProjectDevice projectDevice) { condition.removeDevice(projectDevice); }

    public BooleanProperty hasDeviceToAddProperty() { return hasDeviceToAdd; }

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
