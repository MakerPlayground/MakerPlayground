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

package io.makerplayground.project;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.makerplayground.device.shared.Condition;
import io.makerplayground.device.shared.*;
import io.makerplayground.device.shared.constraint.NumericConstraint;
import io.makerplayground.project.VirtualProjectDevice.Memory;
import io.makerplayground.project.expression.ConditionalExpression;
import io.makerplayground.project.expression.Expression;
import io.makerplayground.project.expression.NumberInRangeExpression;
import io.makerplayground.project.expression.RecordExpression;
import io.makerplayground.project.term.Term;
import io.makerplayground.project.term.ValueTerm;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableMap;
import lombok.Getter;
import lombok.ToString;

import java.util.*;


/**
 *
 */
@JsonSerialize(using = UserSettingSerializer.class)
@ToString
public class UserSetting {
    private final Project project;
    @Getter private final ProjectDevice device;
    private final ReadOnlyObjectWrapper<Action> action = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyObjectWrapper<Condition> condition = new ReadOnlyObjectWrapper<>();

    @Getter private final ObservableMap<Parameter, Expression> parameterMap = FXCollections.observableHashMap();
    @Getter private final ObservableMap<Value, Expression> expression = FXCollections.observableHashMap();
    @Getter private final ObservableMap<Value, Boolean> expressionEnable = FXCollections.observableHashMap();

    // TODO: previous implementation of allValueUsed ignore value from Memory device which is confusing and incorrect when the caller expect all values so we create a new property and retain old one for compatibility before refactoring in the future
    private ReadOnlyObjectWrapper<Map<ProjectDevice, Set<Value>>> allNonVirtualProjectDeviceValueUsed = new ReadOnlyObjectWrapper<>(Collections.emptyMap());
    private ReadOnlyObjectWrapper<Map<ProjectDevice, Set<Value>>> allValueUsed = new ReadOnlyObjectWrapper<>(Collections.emptyMap());

    public UserSetting(Project project, ProjectDevice device, Action supportingAction) {
        this.project = project;
        this.device = device;
        this.action.set(supportingAction);
        for (Parameter param : supportingAction.getParameter()) {
            parameterMap.put(param, Expression.fromDefaultParameter(param));
        }
        initEvent();
    }

    public UserSetting(Project project, ProjectDevice device, Condition supportingCondition) {
        this.project = project;
        this.device = device;
        this.condition.set(supportingCondition);

        for (Parameter param : supportingCondition.getParameter()) {
            parameterMap.put(param, Expression.fromDefaultParameter(param));
        }

        for (Value v : device.getGenericDevice().getValue()) {
            expressionEnable.put(v, false);
            if (v.getType() == DataType.DOUBLE || v.getType() == DataType.INTEGER) {
                expression.put(v, new ConditionalExpression(device, v));
            }
        }

        if (Memory.projectDevice.equals(device)) {
            for (ProjectValue pv : project.getUnmodifiableVariable()) {
                Value v = pv.getValue();
                expressionEnable.put(v, false);
                if (v.getType() == DataType.DOUBLE || v.getType() == DataType.INTEGER) {
                    expression.put(v, new ConditionalExpression(device, v));
                }
            }
        }

        initEvent();
    }

    UserSetting(Project project, ProjectDevice device, Action action, Map<Parameter, Expression> parameterMap, Map<Value, Expression> expression, Map<Value, Boolean> enable) {
        this(project, device, action, null, parameterMap, expression, enable);
    }

    UserSetting(Project project, ProjectDevice device, Condition condition, Map<Parameter, Expression> parameterMap, Map<Value, Expression> expression, Map<Value, Boolean> enable) {
        this(project, device, null, condition, parameterMap, expression, enable);
    }

    private UserSetting(Project project, ProjectDevice device, Action action, Condition condition, Map<Parameter, Expression> parameterMap, Map<Value, Expression> expression, Map<Value, Boolean> enable) {
        this.project = project;
        this.device = device;
        this.action.set(action);
        this.condition.set(condition);

        // replace all default map values with these maps
        this.parameterMap.putAll(parameterMap);
        this.expression.putAll(expression);
        this.expressionEnable.putAll(enable);

        initEvent();
    }

    UserSetting(UserSetting u) {
        this.project = u.project;
        this.device = u.device;
        this.action.set(u.action.get());
        this.condition.set(u.condition.get());

        // replace values by the deepCopy version
        for (var entry: u.parameterMap.entrySet()) {
            this.parameterMap.put(entry.getKey(), entry.getValue().deepCopy());
        }
        this.expressionEnable.putAll(u.expressionEnable);
        for (var entry : u.expression.entrySet()) {
            this.expression.put(entry.getKey(), entry.getValue().deepCopy());
        }

        initEvent();
    }

    private void initEvent() {
        this.action.addListener((observable, oldValue, newValue) -> {
            parameterMap.clear();
            for (Parameter param : newValue.getParameter()) {
                parameterMap.put(param, Expression.fromDefaultParameter(param));
            }
        });

        this.condition.addListener((observable, oldValue, newValue) -> {
            parameterMap.clear();
            for (Parameter param : newValue.getParameter()) {
                parameterMap.put(param, Expression.fromDefaultParameter(param));
            }
        });

        if (Memory.projectDevice.equals(device)) {
            // TODO: this may be unsafe as list can be permuted or updated
            project.getUnmodifiableVariable().addListener((ListChangeListener<? super ProjectValue>) c -> {
                while (c.next()) {
                    if (c.wasAdded()) {
                        c.getAddedSubList().forEach(o -> {
                            Value v = o.getValue();
                            if (expression.containsKey(v)) {
                                return;
                            }
                            expressionEnable.put(v, false);
                            if (v.getType() == DataType.DOUBLE || v.getType() == DataType.INTEGER) {
                                expression.put(v, new ConditionalExpression(device, v));
                            }
                        });
                    }
                    if (c.wasRemoved()) {
                        c.getRemoved().forEach(o -> {
                            Value v = o.getValue();
                            expression.remove(v);
                            expressionEnable.remove(v);
                        });
                    }
                }
            });
        }

        parameterMap.addListener((InvalidationListener) observable -> updateAllValueUsed());
        expression.addListener((InvalidationListener) observable -> updateAllValueUsed());
        expressionEnable.addListener((InvalidationListener) observable -> updateAllValueUsed());
        updateAllValueUsed();
    }

    private void updateAllValueUsed() {
        allNonVirtualProjectDeviceValueUsed.setValue(calculateAllValueUsed(EnumSet.allOf(DataType.class), false));
        allValueUsed.setValue(calculateAllValueUsed(EnumSet.allOf(DataType.class), true));
    }

    private void initValueMap() {
        parameterMap.clear();
        if (action.get() != null) {
            for (Parameter param : action.get().getParameter()) {
                parameterMap.put(param, Expression.fromDefaultParameter(param));
            }
        }
        if (condition.get() != null) {
            for (Parameter param : condition.get().getParameter()) {
                parameterMap.put(param, Expression.fromDefaultParameter(param));
            }
        }
    }

    public Action getAction() {
        return action.get();
    }

    public Condition getCondition() {
        return condition.get();
    }

    public void setAction(Action action) {
        this.action.set(action);
        initValueMap();
    }

    public void setCondition(Condition condition) {
        this.condition.set(condition);
        initValueMap();
    }

    public ReadOnlyObjectProperty<Action> actionProperty() {
        return action.getReadOnlyProperty();
    }

    public ReadOnlyObjectProperty<Condition> conditionProperty() {
        return condition.getReadOnlyProperty();
    }

    private Map<ProjectDevice, Set<Value>> calculateAllValueUsed(Set<DataType> dataType, boolean includeVirtualDevice) {
        Map<ProjectDevice, Set<Value>> result = new HashMap<>();

        // list value use in parameters ex. show value of 7-Segment / LCD
        for (Map.Entry<Parameter, Expression> entry : parameterMap.entrySet()) {
            Expression exp = entry.getValue();
            for (Term term: exp.getTerms()) {
                if (term instanceof ValueTerm && term.isValid() && (includeVirtualDevice || !(((ValueTerm) term).getValue().getDevice() instanceof VirtualProjectDevice))) {
                    ProjectValue projectValue = ((ValueTerm) term).getValue();
                    if (projectValue != null && dataType.contains(projectValue.getValue().getType())) {
                        ProjectDevice projectDevice = projectValue.getDevice();
                        if (result.containsKey(projectDevice)) {
                            result.get(projectDevice).add(projectValue.getValue());
                        } else {
                            result.put(projectDevice, new HashSet<>(Collections.singletonList(projectValue.getValue())));
                        }
                    }
                }
            }
            if (exp instanceof RecordExpression) {
                ((RecordExpression) exp).getRecord().getEntryList().stream().flatMap(recordEntry -> recordEntry.getValue().getTerms().stream()).filter(term -> term instanceof ValueTerm).forEach(term -> {
                    ProjectValue projectValue = ((ValueTerm) term).getValue();
                    if (!includeVirtualDevice && (projectValue.getDevice() instanceof VirtualProjectDevice)) {
                        return;
                    }
                    if (projectValue != null && dataType.contains(projectValue.getValue().getType())) {
                        ProjectDevice projectDevice = projectValue.getDevice();
                        if (result.containsKey(projectDevice)) {
                            result.get(projectDevice).add(projectValue.getValue());
                        } else {
                            result.put(projectDevice, new HashSet<>(Collections.singletonList(projectValue.getValue())));
                        }
                    }
                });
            }
        }

        for (Value v : expression.keySet()) {
            // skip if this expression is disabled
            if (expressionEnable.get(v)) {
                Set<ProjectValue> valueUsed = expression.get(v).getValueUsed();
                for (ProjectValue pv : valueUsed) {
                    if (pv.getDevice() instanceof VirtualProjectDevice && !includeVirtualDevice) {
                        continue;
                    }
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

    public Map<ProjectDevice, Set<Value>> getNonVirtualProjectDevicesValueUsed() {
        return allNonVirtualProjectDeviceValueUsed.get();
    }

    public Map<ProjectDevice, Set<Value>> getNonVirtualProjectDevicesValueUsed(Set<DataType> dataType) {
        return calculateAllValueUsed(dataType, false);
    }

    public Map<ProjectDevice, Set<Value>> getAllValueUsed() {
        return allValueUsed.get();
    }

    public Map<ProjectDevice, Set<Value>> getAllValueUsed(Set<DataType> dataType) {
        return calculateAllValueUsed(dataType, true);
    }

    public ReadOnlyObjectProperty<Map<ProjectDevice, Set<Value>>> allValueUsedByAllDeviceProperty() {
        return allValueUsed.getReadOnlyProperty();
    }

    public boolean isDataBindingUsed() {
        return getParameterMap().values().stream()
                .anyMatch(expression1 -> (expression1.getRefreshInterval() != Expression.RefreshInterval.ONCE));
    }

    public boolean isDataBindingUsed(Parameter p) {
        return parameterMap.get(p).getRefreshInterval() != Expression.RefreshInterval.ONCE;
    }

    public long getNumberOfDatabindParams() {
        return getParameterMap().keySet().stream()
                .filter(parameter -> (parameter.getDataType() == DataType.DOUBLE)
                        || (parameter.getDataType() == DataType.INTEGER))
                .count();
    }
}
