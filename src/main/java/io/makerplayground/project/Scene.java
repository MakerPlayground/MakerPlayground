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
import io.makerplayground.device.Parameter;
import io.makerplayground.project.expression.Expression;
import javafx.beans.Observable;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

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
    private final ReadOnlyBooleanWrapper error;

    Scene() {
        super(20,20,200, 130);

        this.name = new SimpleStringProperty("");
        // fire update event when actionProperty is invalidated / changed
        this.setting = FXCollections.observableArrayList(item -> new Observable[]{item.actionProperty()});
        this.delay = new SimpleDoubleProperty(0);
        this.delayUnit = new SimpleObjectProperty<>(DelayUnit.Second);
        this.error = new ReadOnlyBooleanWrapper(checkError());
    }

    Scene(double top, double left, double width, double height
            , String name, List<UserSetting> setting, double delay, DelayUnit delayUnit) {
        super(top, left, width, height);
        this.name = new SimpleStringProperty(name);
        this.setting = FXCollections.observableList(setting);
        this.delay = new SimpleDoubleProperty(delay);
        this.delayUnit = new SimpleObjectProperty<>(delayUnit);
        this.error = new ReadOnlyBooleanWrapper(checkError());
    }

    Scene(Scene s, String name) {
        super(s.getTop(), s.getLeft(), s.getWidth(), s.getHeight());
        this.name = new SimpleStringProperty(name);
        this.setting = FXCollections.observableArrayList(item -> new Observable[]{item.actionProperty()});
        for (UserSetting u : s.setting) {
            this.setting.add(new UserSetting(u));
        }
        this.delay = new SimpleDoubleProperty(s.getDelay());
        this.delayUnit = new SimpleObjectProperty<>(s.getDelayUnit());
        this.error = new ReadOnlyBooleanWrapper(checkError());
    }

    public void addDevice(ProjectDevice device) {
        setting.add(new UserSetting(device, true));
        invalidate();
    }

    public void removeDevice(ProjectDevice device) {
        for (int i = setting.size() - 1; i >= 0; i--) {
            if (setting.get(i).getDevice() == device) {
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

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
        invalidate();
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

    private boolean checkError() {
        return name.get().isEmpty() || setting.stream()
                .flatMap(userSetting -> userSetting.getValueMap().values().stream())
                .anyMatch(o -> (o == null) || !o.isValid());
    }

    public boolean isError() {
        return error.get();
    }

    public ReadOnlyBooleanProperty errorProperty() {
        return error.getReadOnlyProperty();
    }

    @Override
    public void invalidate() {
        super.invalidate();
        error.set(checkError());
    }
}
