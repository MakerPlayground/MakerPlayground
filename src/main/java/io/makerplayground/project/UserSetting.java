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
import io.makerplayground.project.expression.*;
import io.makerplayground.project.term.Term;
import io.makerplayground.project.term.ValueTerm;
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
    private final ObservableMap<Value, Boolean> expressionEnable;

    UserSetting(ProjectDevice device, boolean scene ) {  // TODO: Remove boolean field!!!
        this.device = device;
        this.action = new SimpleObjectProperty<>();
        this.valueMap = FXCollections.observableHashMap();
        this.expression = FXCollections.observableHashMap();
        this.expressionEnable = FXCollections.observableHashMap();

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
                        valueMap.put(param, new SimpleStringExpression((String) param.getDefaultValue()));
                        break;
                    case ENUM:
                        valueMap.put(param, new SimpleStringExpression((String) param.getDefaultValue()));
                        break;
                    case INTEGER:
                        valueMap.put(param, new NumberWithUnitExpression((NumberWithUnit) param.getDefaultValue()));
                        break;
                    case VALUE:
                        valueMap.put(param, new ProjectValueExpression());
                        break;
                }
            }
        });

        // Initialize expression list
        for (Value v : device.getGenericDevice().getValue()) {
            expression.put(v, new NumberInRangeExpression(device, v));
            expressionEnable.put(v, false);
        }
    }

    UserSetting(ProjectDevice device, Action action, Map<Parameter, Expression> valueMap, Map<Value, Expression> expression, Map<Value, Boolean> enable) {
        this.device = device;
        this.action = new SimpleObjectProperty<>(action);
        this.valueMap = FXCollections.observableMap(valueMap);
        this.expression = FXCollections.observableMap(expression);
        this.expressionEnable = FXCollections.observableMap(enable);
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
        this.expressionEnable = FXCollections.observableHashMap();
        for (Value v : u.getExpression().keySet()) {
            this.expression.put(v, Expression.deepCopy(u.getExpression().get(v)));
            this.expressionEnable.put(v, u.getExpressionEnable().get(v));
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

    public ObservableMap<Value, Boolean> getExpressionEnable() {
        return expressionEnable;
    }

    public Map<ProjectDevice, Set<Value>> getAllValueUsed() {
        Map<ProjectDevice, Set<Value>> result = new HashMap<>();

        // list value use in parameters ex. show value of 7-Segment / LCD
        for (Map.Entry<Parameter, Expression> entry : valueMap.entrySet()) {
            Expression exp = entry.getValue();
            for (Term term: exp.getTerms()) {
                if (term instanceof ValueTerm) {
                    ProjectValue projectValue = ((ValueTerm) term).getValue();
                    if (projectValue != null) {
                        ProjectDevice projectDevice = projectValue.getDevice();
                        if (result.containsKey(projectDevice)) {
                            result.get(projectDevice).add(projectValue.getValue());
                        } else {
                            result.put(projectDevice, new HashSet<>(Collections.singletonList(projectValue.getValue())));
                        }
                    }
                }
            }
        }

        // list value use in every enable expression in a condition
        for (Value v : expression.keySet()) {
            // skip if this expression is disabled
            if (expressionEnable.get(v)) {
                Set<ProjectValue> valueUsed = expression.get(v).getValueUsed();
                for (ProjectValue pv : valueUsed) {
                    if (result.containsKey(pv.getDevice())) {
                        result.get(pv.getDevice()).add(pv.getValue());
                    } else {
                        result.put(pv.getDevice(), new HashSet<>(Set.of(pv.getValue())));
                    }
                }
            }
        }

        return result;
    }
}
