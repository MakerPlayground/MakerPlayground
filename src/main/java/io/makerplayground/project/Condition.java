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
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

/**
 *
 */
@JsonSerialize(using = ConditionSerializer.class)
public class Condition extends NodeElement {
    private final StringProperty name;
    private final ObservableList<UserSetting> setting;

    private final ObservableList<UserSetting> unmodifiableSetting;

    Condition() {
        super(20,250,185, 115);

        this.name = new SimpleStringProperty();
        this.setting = FXCollections.observableArrayList();

        this.unmodifiableSetting = FXCollections.unmodifiableObservableList(setting);
    }

    public Condition(double top, double left, double width, double height
            , String name, List<UserSetting> setting) {
        super(top, left, width, height);

        this.name = new SimpleStringProperty(name);
        this.setting = FXCollections.observableList(setting);

        this.unmodifiableSetting = FXCollections.unmodifiableObservableList(this.setting);
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

    public ObservableList<UserSetting> getSetting() {
        return unmodifiableSetting;
    }
}
