/*
 * Copyright (c) 2019. The Maker Playground Authors.
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
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.ObservableList;

import java.util.List;

/**
 * Created by tanyagorn on 6/26/2017.
 */
public class SceneViewModel {
    private final Scene scene;
    private final Project project;

    private final DynamicViewModelCreator<UserSetting, SceneDeviceIconViewModel> dynamicViewModelCreator;

    private final BooleanProperty hasDeviceToAdd;

    public SceneViewModel(Scene scene, Project project) {
        this.scene = scene;
        this.project = project;

        this.dynamicViewModelCreator = new DynamicViewModelCreator<>(scene.getSetting(), userSetting -> new SceneDeviceIconViewModel(userSetting, scene, project));

        hasDeviceToAdd = new SimpleBooleanProperty();
        hasDeviceToAdd.bind(Bindings.size(project.getDeviceWithAction()).greaterThan(0));
    }

    public String getName() {
        return scene.getName();
    }

    public void setName(String name) {
        scene.setName(name);
    }

    public DynamicViewModelCreator<UserSetting, SceneDeviceIconViewModel> getDynamicViewModelCreator() {
        return dynamicViewModelCreator;
    }

    public double getX() {
        return scene.getLeft();
    }

    public DoubleProperty xProperty() {
        return scene.leftProperty();
    }

    public double getY() {
        return scene.getTop();
    }

    public DoubleProperty yProperty() {
        return scene.topProperty();
    }

    public double getSourcePortX() {
        return scene.getSourcePortX();
    }

    public DoubleProperty sourcePortXProperty() {
        return scene.sourcePortXProperty();
    }

    public double getSourcePortY() {
        return scene.getSourcePortY();
    }

    public DoubleProperty sourcePortYProperty() {
        return scene.sourcePortYProperty();
    }

    public double getDestPortX() {
        return scene.getDestPortX();
    }

    public DoubleProperty destPortXProperty() {
        return scene.destPortXProperty();
    }

    public double getDestPortY() {
        return scene.getDestPortY();
    }

    public DoubleProperty destPortYProperty() {
        return scene.destPortYProperty();
    }

    public List<ProjectDevice> getProjectOutputDevice() {
        return project.getDeviceWithAction();
    }

    public Scene getScene() {
        return scene;
    }

    public ObservableList<UserSetting> getStateDevice() {
        return scene.getSetting();
    }

    public void removeUserSetting(UserSetting userSetting) {
        scene.removeUserSetting(userSetting);
    }

    public BooleanProperty hasDeviceToAddProperty() {
        return hasDeviceToAdd;
    }

    public boolean hasConnectionFrom(NodeElement other) {
        return project.hasLine(other, scene);
    }

    public boolean hasConnectionTo(NodeElement other) {
        return project.hasLine(scene, other);
    }

    public final DiagramError getError() {
        return scene.getError();
    }

    public final ReadOnlyObjectProperty<DiagramError> errorProperty() {
        return scene.errorProperty();
    }
}
