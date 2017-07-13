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
import io.makerplayground.device.Action;
import io.makerplayground.device.Parameter;
import io.makerplayground.device.Value;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

import java.util.List;

/**
 *
 */
@JsonSerialize(using = UserSettingSerializer.class)
public class UserSetting {
    private final ProjectDevice device;
    private final ObjectProperty<Action> action;
    private final ObservableMap<Parameter, Object> valueMap;
    private final ObservableMap<Value, ObservableList<Expression>> expression;

    UserSetting(ProjectDevice device) {
        this.device = device;
        this.action = new SimpleObjectProperty<>();
        this.valueMap = FXCollections.observableHashMap();

        this.expression = FXCollections.observableHashMap();

        // Initialize the map with default action and it's parameters
        List<Action> actionList = device.getGenericDevice().getAction();
        if (!actionList.isEmpty()) {
            Action action = actionList.get(0);
            this.action.set(action);
            for (Parameter param : action.getParameter()) {
                this.valueMap.put(param, param.getDefaultValue());
            }
        }

        // Reset value of the valueMap every time that the action is changed
        this.action.addListener((observable, oldValue, newValue) -> {
            valueMap.clear();
            for (Parameter param : action.get().getParameter()) {
                valueMap.put(param, param.getDefaultValue());
            }

        });

        // Initialize expression list
        for (Value v : device.getGenericDevice().getValue()) {
            expression.put(v, FXCollections.observableArrayList());
        }
    }

    public ProjectDevice getDevice() {
        return device;
    }

    public Action getAction() {
        return action.get();
    }

    public ObjectProperty<Action> actionProperty() {
        return action;
    }

    public void setAction(Action action) {
        this.action.set(action);
    }

    public ObservableMap<Parameter, Object> getValueMap() {
        return valueMap;
    }

    public ObservableMap<Value, ObservableList<Expression>> getExpression() {
        return expression;
    }
}
