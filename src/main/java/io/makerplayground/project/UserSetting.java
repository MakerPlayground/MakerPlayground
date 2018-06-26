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
import io.makerplayground.device.*;
import io.makerplayground.helper.DataType;
import io.makerplayground.helper.NumberWithUnit;
import io.makerplayground.project.expression.Expression;
import io.makerplayground.project.expression.NumberInRangeExpression;
import io.makerplayground.project.expression.NumberWithUnitExpression;
import io.makerplayground.project.expression.ProjectValueExpression;
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
    private final ObservableMap<Parameter, Expression> valueMap;
    private final ObservableMap<Value, Expression> expression;

    UserSetting(ProjectDevice device, boolean scene) {  // TODO: Remove boolean field!!!
        this.device = device;
        this.action = new SimpleObjectProperty<>();
        this.valueMap = FXCollections.observableHashMap();
        this.expression = FXCollections.observableHashMap();

        // Initialize the map with default action and it's parameters
        List<Action> actionList;
        if (scene) {
            actionList = device.getGenericDevice().getAction();
        } else {
            actionList = device.getGenericDevice().getCondition();
        }
        if (!actionList.isEmpty()) {
            Action action = actionList.get(0);
            this.action.set(action);
            for (Parameter param : action.getParameter()) {
                this.valueMap.put(param, Expression.fromDefaultParameter(param));
            }
        }

        // Reset value of the valueMap every time that the action is changed
        this.action.addListener((observable, oldValue, newValue) -> {
            valueMap.clear();
            for (Parameter param : action.get().getParameter()) {
                switch (param.getDataType()) {
                    case DOUBLE:
                        valueMap.put(param, new NumberWithUnitExpression((NumberWithUnit) param.getDefaultValue()));
                        break;
                    case STRING:
                        break;
                    case ENUM:
                        break;
                    case INTEGER:
                        break;
                    case VALUE:
                        break;
                }
            }
        });

        // Initialize expression list
        for (Value v : device.getGenericDevice().getValue()) {
            expression.put(v, new NumberInRangeExpression(device, v));
        }
    }

    UserSetting(ProjectDevice device, Action action, Map<Parameter, Expression> valueMap, Map<Value, Expression> expression) {
        this.device = device;
        this.action = new SimpleObjectProperty<>(action);
        this.valueMap = FXCollections.observableMap(valueMap);
        this.expression = FXCollections.observableMap(expression);
    }

    UserSetting(UserSetting u) {
        this.device = u.getDevice();
        this.action = new SimpleObjectProperty<>(u.getAction());
        this.valueMap = FXCollections.observableHashMap();
        for (Parameter p : u.getValueMap().keySet()) {
            Expression o = u.getValueMap().get(p);
            this.valueMap.put(p, Expression.deepCopy(o));
        }
        this.expression = FXCollections.observableHashMap();
        for (Value v : u.getExpression().keySet()) {
            this.expression.put(v, Expression.deepCopy(u.getExpression().get(v)));
        }

        // Reset value of the valueMap every time that the action is changed
        this.action.addListener((observable, oldValue, newValue) -> {
            valueMap.clear();
            for (Parameter param : action.get().getParameter()) {
                valueMap.put(param, Expression.fromDefaultParameter(param));
            }
        });
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

    public ObservableMap<Parameter, Expression> getValueMap() {
        return valueMap;
    }

    public ObservableMap<Value, Expression> getExpression() {
        return expression;
    }

    public Map<ProjectDevice, Set<Value>> getAllValueUsed() {
        Map<ProjectDevice, Set<Value>> result = new HashMap<>();

        // list value use in parameters ex. show value of 7-Segment / LCD
        for (Parameter parameter : valueMap.keySet()) {
            if (parameter.getDataType() != DataType.VALUE) {
                continue;
            }

            ProjectValue projectValue = ((ProjectValueExpression) valueMap.get(parameter)).getProjectValue();
            ProjectDevice projectDevice = projectValue.getDevice();
            if (result.containsKey(projectDevice)) {
                result.get(projectDevice).add(projectValue.getValue());
            } else {
                result.put(projectDevice, new HashSet<>(Collections.singletonList(projectValue.getValue())));
            }
        }

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

        return result;
    }
}
