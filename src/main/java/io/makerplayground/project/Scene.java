/*
 * Copyright 2017 The Maker Playground Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.makerplayground.project;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.makerplayground.device.shared.Parameter;
import io.makerplayground.project.expression.Expression;
import javafx.beans.Observable;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 *
 */
@JsonSerialize(using = SceneSerializer.class)
public class Scene extends NodeElement {
    public enum DelayUnit {
        MilliSecond, Second;

        @Override
        public String toString() {
            switch(this) {
                case MilliSecond: return "ms";
                case Second: return "s";
                default: throw new IllegalArgumentException();
            }
        }
    }

    private final StringProperty name;
    private final ObservableList<UserSetting> setting;
    private final DoubleProperty delay;
    private final ObjectProperty<DelayUnit> delayUnit;

    private final Set<NodeElement> roots = new HashSet<>();

    public Set<NodeElement> getRoots() {
        return roots;
    }

    public void addRoot(NodeElement root) {
        if (root instanceof Begin) {
            roots.add(root);
            return;
        }
        throw new IllegalStateException("Root must be Begin or Task");
    }

    public void clearRoot() {
        roots.clear();
    }

    Scene(Project project) {
        super(20, 20, 205, 124, project);

        this.name = new SimpleStringProperty("");
        // fire update event when actionProperty is invalidated / changed
        this.setting = FXCollections.observableArrayList(item -> new Observable[]{item.actionProperty()});
        this.delay = new SimpleDoubleProperty(0);
        this.delayUnit = new SimpleObjectProperty<>(DelayUnit.Second);
        invalidate();
    }

    Scene(double top, double left, double width, double height
            , String name, List<UserSetting> setting, double delay, DelayUnit delayUnit, Project project) {
        // TODO: ignore width and height field to prevent line from drawing incorrectly when read file from old version as scene can't be resized anyway
        super(top, left, 205, 124, project);
        this.name = new SimpleStringProperty(name);
        this.setting = FXCollections.observableList(setting);
        this.delay = new SimpleDoubleProperty(delay);
        this.delayUnit = new SimpleObjectProperty<>(delayUnit);
        invalidate();
    }

    Scene(Scene s, String name, Project project) {
        super(s.getTop(), s.getLeft(), s.getWidth(), s.getHeight(), project);
        this.name = new SimpleStringProperty(name);
        this.setting = FXCollections.observableArrayList(item -> new Observable[]{item.actionProperty()});
        for (UserSetting u : s.setting) {
            this.setting.add(new UserSetting(u));
        }
        this.delay = new SimpleDoubleProperty(s.getDelay());
        this.delayUnit = new SimpleObjectProperty<>(s.getDelayUnit());
        invalidate();
    }

    public void addDevice(ProjectDevice device) {
        if (device.getGenericDevice().getAction().isEmpty()) {
            throw new IllegalStateException(device.getGenericDevice().getName() + " needs to have action.");
        } else {
            setting.add(new UserSetting(device, device.getGenericDevice().getAction().get(0)));
        }
        invalidate();
    }

    public void removeDevice(ProjectDevice device) {
        for (int i = setting.size() - 1; i >= 0; i--) {
            if (setting.get(i).getProjectDevice() == device) {
                setting.remove(i);
            }
        }
        for (UserSetting userSetting : setting) {
            for (Parameter parameter : userSetting.getValueMap().keySet()) {
                Expression expression = userSetting.getValueMap().get(parameter);
                if (expression.getTerms().stream().anyMatch(term -> term.getValue() instanceof ProjectValue
                        && (((ProjectValue) term.getValue()).getDevice() == device))) {
                    userSetting.getValueMap().replace(parameter, Expression.fromDefaultParameter(parameter));
                }
            }
        }
        invalidate();
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
        invalidate();
        // invalidate other scene as every scene needs to check for duplicate name
        for (Scene s : project.getScene()) {
            s.invalidate();
        }
    }

    public double getDelay() {
        return delay.get();
    }

    public DoubleProperty delayProperty() {
        return delay;
    }

    public void setDelay(double delay) {
        this.delay.set(delay);
    }

    public DelayUnit getDelayUnit() {
        return delayUnit.get();
    }

    public ObjectProperty<DelayUnit> delayUnitProperty() {
        return delayUnit;
    }

    public void setDelayUnit(DelayUnit delayUnit) {
        this.delayUnit.set(delayUnit);
    }

    public ObservableList<UserSetting> getSetting() {
        return setting;
    }

    public void removeUserSetting(UserSetting userSetting) {
        this.setting.remove(userSetting);
    }

    @Override
    protected DiagramError checkError() {
        // name should contain only english alphabets and an underscore and it should not be empty
        if (!name.get().matches("\\w+")) {
            return DiagramError.SCENE_INVALID_NAME;
        }

        // name should be unique
        for (Scene s : project.getScene()) {
            if ((this != s) && name.get().equals(s.name.get())) {
                return DiagramError.SCENE_DUPLICATE_NAME;
            }
        }

        // parameter should not be null and should be valid
        if (setting.stream().flatMap(userSetting -> userSetting.getValueMap().values().stream())
                .anyMatch(o -> Objects.isNull(o) || !o.isValid())) {
            return DiagramError.SCENE_INVALID_PARAM;
        }

        return DiagramError.NONE;
    }
}
