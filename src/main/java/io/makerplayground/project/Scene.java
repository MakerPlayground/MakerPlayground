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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import javafx.beans.Observable;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 */
@JsonSerialize(using = SceneSerializer.class)
public class Scene extends NodeElement {
    public enum DelayUnit {MilliSecond, Second}

    private final StringProperty name;
    private final ObservableList<UserSetting> setting;
    private final SimpleDoubleProperty delay;
    private final DelayUnit delayUnit;

    Scene() {
        super(20,20,200, 135);

        this.name = new SimpleStringProperty("");
        // fire update event when actionProperty is invalidated / changed
        this.setting = FXCollections.observableArrayList(item -> new Observable[]{item.actionProperty()});
        this.delay = new SimpleDoubleProperty(0);
        this.delayUnit = DelayUnit.Second;
    }

    public void addDevice(ProjectDevice device) {
        setting.add(new UserSetting(device));
    }

    public void removeDevice(ProjectDevice device) {
        for (int i = setting.size() - 1; i >= 0; i--) {
            if (setting.get(i).getDevice() == device) {
                setting.remove(i);
            }
        }
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public double getDelay() {
        return delay.get();
    }

    public SimpleDoubleProperty delayProperty() {
        return delay;
    }

    public void setDelay(double delay) {
        this.delay.set(delay);
    }

    public DelayUnit getDelayUnit() {
        return delayUnit;
    }

    public ObservableList<UserSetting> getSetting() {
        return setting;
    }
}
