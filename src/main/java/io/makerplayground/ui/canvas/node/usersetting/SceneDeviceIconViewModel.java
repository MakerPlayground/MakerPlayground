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

import io.makerplayground.device.shared.Action;
import io.makerplayground.project.*;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.StringProperty;

/**
 * Created by tanyagorn on 6/12/2017.
 */
public class SceneDeviceIconViewModel {

    private final UserSetting userSetting;
    private final Project project;
    private final NodeElement nodeElement;

    public SceneDeviceIconViewModel(UserSetting userSetting, NodeElement nodeElement, Project project) {
        this.userSetting = userSetting;
        this.nodeElement = nodeElement;
        this.project = project;
    }

    public String getName() {
        return userSetting.getProjectDevice().getName();
    }

    public StringProperty nameProperty() {
        return userSetting.getProjectDevice().nameProperty();
    }

    public String getImageName() {
        return userSetting.getProjectDevice().getGenericDevice().getName();
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
        return userSetting.getProjectDevice();
    }

    public NodeElement getNodeElement() {
        return nodeElement;
    }

    public UserSetting getUserSetting() {
        return userSetting;
    }

    public Project getProject() {
        return project;
    }
}
