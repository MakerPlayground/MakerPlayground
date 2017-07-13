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

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 */
public class Condition extends NodeElement {
    private final ObservableList<UserSetting> setting;
    private final ObjectProperty<Scene> sourceNode;
    private final ObjectProperty<Scene> destNode;

    private final ObservableList<UserSetting> unmodifiableSetting;

    Condition() {
        super(20,250,185, 115);

        this.setting = FXCollections.observableArrayList();
        this.sourceNode = new SimpleObjectProperty<>(null);
        this.destNode = new SimpleObjectProperty<>(null);

        this.unmodifiableSetting = FXCollections.unmodifiableObservableList(setting);
    }

    public Scene getSourceNode() {
        return sourceNode.get();
    }

    public ObjectProperty<Scene> sourceNodeProperty() {
        return sourceNode;
    }

    public Scene getDestNode() {
        return destNode.get();
    }

    public ObjectProperty<Scene> destNodeProperty() {
        return destNode;
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
