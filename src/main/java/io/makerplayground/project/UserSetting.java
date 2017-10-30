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
import io.makerplayground.project.expression.Expression;
import io.makerplayground.project.expression.SimpleExpression;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import java.util.*;

/**
 *
 */
@JsonSerialize(using = UserSettingSerializer.class)
public class UserSetting {
    private final ProjectDevice device;
    private final ObjectProperty<Action> action;
    private final ObservableMap<Parameter, Object> valueMap;
    private final ObservableMap<Value, Expression> expression;

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
            expression.put(v, new SimpleExpression(device, v));
        }
    }

    UserSetting(ProjectDevice device, Action action, Map<Parameter, Object> valueMap, Map<Value, Expression> expression) {
        this.device = device;
        this.action = new SimpleObjectProperty<>(action);
        this.valueMap = FXCollections.observableMap(valueMap);
        this.expression = FXCollections.observableMap(expression);
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

    public ObservableMap<Value, Expression> getExpression() {
        return expression;
    }

    public Map<ProjectDevice, Set<Value>> getAllValueUsed() {
        Map<ProjectDevice, Set<Value>> result = new HashMap<>();

        // TODO: edit comment
        // assume that each expression contain reference to itself
        for (Expression exp : expression.values()) {
            // skip if this expression is disabled
            if (!exp.isEnable()) {
                continue;
            }
            Set<ProjectValue> valueUsed = exp.getValueUsed();
            for (ProjectValue pv : valueUsed) {
                if (result.containsKey(pv.getDevice())) {
                    result.get(pv.getDevice()).add(pv.getValue());
                } else {
                    result.put(pv.getDevice(), new HashSet<>(Collections.singletonList(pv.getValue())));
                }
            }
        }

//        Map<ProjectDevice, Set<Value>> tmp = expression.values().stream()
//                .map(Expression::getValueUsed)
//                .flatMap(Collection::stream)
//                .collect(Collectors.groupingBy(ProjectValue::getDevice,
//                        Collectors.mapping(ProjectValue::getValue, Collectors.toSet())));

        return result;
    }
}
