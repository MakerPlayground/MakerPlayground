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
import io.makerplayground.project.term.NumberAnimationTerm;
import io.makerplayground.project.term.StringAnimationTerm;
import io.makerplayground.project.term.Term;
import io.makerplayground.project.term.ValueTerm;
import io.makerplayground.ui.canvas.node.expression.custom.NumberAnimationChip;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableMap;
import lombok.Getter;
import lombok.ToString;

import java.util.*;
import java.util.stream.Collectors;


/**
 *
 */
@JsonSerialize(using = UserSettingSerializer.class)
@ToString
public class UserSetting {
    private final Project project;
    @Getter private final ProjectDevice device;
    private final ReadOnlyObjectWrapper<Action> action;
    private final ReadOnlyObjectWrapper<Condition> condition;

    private ReadOnlyObjectWrapper<Map<ProjectDevice, Set<Value>>> allValueUsed = new ReadOnlyObjectWrapper<>(Collections.emptyMap());

    @Getter private final ObservableMap<Parameter, Expression> parameterMap;
    @Getter private final ObservableMap<Value, Expression> expression;
    @Getter private final ObservableMap<Value, Boolean> expressionEnable;

    private UserSetting(Project project, ProjectDevice device) {
        this.project = project;
        this.device = device;
        this.action = new ReadOnlyObjectWrapper<>();
        this.condition = new ReadOnlyObjectWrapper<>();
        this.parameterMap = FXCollections.observableHashMap();
        this.expression = FXCollections.observableHashMap();
        this.expressionEnable = FXCollections.observableHashMap();

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

        parameterMap.addListener((InvalidationListener) observable -> calculateAllValueUsed());
        expression.addListener((InvalidationListener) observable -> calculateAllValueUsed());
        expressionEnable.addListener((InvalidationListener) observable -> calculateAllValueUsed());
    }

    public UserSetting(Project project, ProjectDevice device, Action supportingAction) {
        this(project, device);
        this.action.set(supportingAction);
    }

    public UserSetting(Project project, ProjectDevice device, Condition supportingCondition) {
        this(project, device);
        this.condition.set(supportingCondition);

        // Initialize expression list
        for (Value v : device.getGenericDevice().getValue()) {
            expressionEnable.put(v, false);
            if (v.getType() == DataType.DOUBLE || v.getType() == DataType.INTEGER) {
                NumericConstraint constraint = (NumericConstraint) v.getConstraint();
                boolean customExpressionOnly = (constraint.getMin() == -Double.MAX_VALUE || constraint.getMin() == Integer.MIN_VALUE || constraint.getMax() == Double.MAX_VALUE || constraint.getMax() == Integer.MAX_VALUE);
                if (customExpressionOnly) {
                    expression.put(v, new ConditionalExpression(device, v));
                } else {
                    expression.put(v, new NumberInRangeExpression(device, v));
                }
            }
        }

        if (Memory.projectDevice.equals(device)) {
            for (ProjectValue pv: project.getUnmodifiableVariable()) {
                Value v = pv.getValue();
                expressionEnable.put(v, false);
                if (v.getType() == DataType.DOUBLE || v.getType() == DataType.INTEGER) {
                    expression.put(v, new ConditionalExpression(device, v));
                }
            }

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
    }

    UserSetting(Project project, ProjectDevice device, Action action, Map<Parameter, Expression> parameterMap, Map<Value, Expression> expression, Map<Value, Boolean> enable) {
        this(project, device, action);

        // replace all default map values with these maps
        this.parameterMap.putAll(parameterMap);
        this.expression.putAll(expression);
        this.expressionEnable.putAll(enable);
    }

    UserSetting(Project project, ProjectDevice device, Condition condition, Map<Parameter, Expression> parameterMap, Map<Value, Expression> expression, Map<Value, Boolean> enable) {
        this(project, device, condition);

        // replace all default map values with these maps
        this.parameterMap.putAll(parameterMap);
        this.expression.putAll(expression);
        this.expressionEnable.putAll(enable);
    }

    UserSetting(UserSetting u) {
        this(u.project, u.device);

        this.action.set(u.action.get());
        this.condition.set(u.condition.get());

        // replace values by the deepCopy version
        for (var entry: u.parameterMap.entrySet()) {
            this.parameterMap.put(entry.getKey(), entry.getValue().deepCopy());
        }
        for (var entry : u.expression.entrySet()) {
            this.expression.put(entry.getKey(), entry.getValue().deepCopy());
        }
        this.expressionEnable.putAll(u.expressionEnable);

        if (Memory.projectDevice.equals(u.device)) {
            project.getUnmodifiableVariable().addListener((ListChangeListener<? super ProjectValue>) c -> {
                while (c.next()) {
                    if (c.wasAdded()) {
                        c.getAddedSubList().forEach(o -> {
                            Value v = o.getValue();
                            if (this.expression.containsKey(v)) {
                                return;
                            }
                            expressionEnable.put(v, false);
                            if (v.getType() == DataType.DOUBLE || v.getType() == DataType.INTEGER) {
                                this.expression.put(v, new ConditionalExpression(u.device, v));
                            }
                        });
                    }
                    if (c.wasRemoved()) {
                        c.getRemoved().forEach(o -> {
                            Value v = o.getValue();
                            this.expression.remove(v);
                            this.expressionEnable.remove(v);
                        });
                    }
                }
            });
        }
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

    public Map<ProjectDevice, Set<Value>> getAllValueUsed() {
        return this.getAllValueUsed(Set.of(DataType.values()));
    }

    public Map<ProjectDevice, Set<Value>> getAllValueUsed(Set<DataType> dataType) {
        Map<ProjectDevice, Set<Value>> result = new HashMap<>();

        // list value use in parameters ex. show value of 7-Segment / LCD
        for (Map.Entry<Parameter, Expression> entry : parameterMap.entrySet()) {
            Expression exp = entry.getValue();
            for (Term term: exp.getTerms()) {
                if (term instanceof ValueTerm && term.isValid() && !Memory.projectDevice.equals(((ValueTerm) term).getValue().getDevice())) {
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
                    if (Memory.projectDevice.equals(projectValue.getDevice())) {
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

        // list value use in every enable expression in a condition
        for (Value v : expression.keySet()) {
            // skip if this expression is disabled
            if (expressionEnable.get(v) && !Memory.projectDevice.equals(this.getDevice())) {
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

    public ReadOnlyObjectProperty<Map<ProjectDevice, Set<Value>>> allValueUsedProperty() {
        return allValueUsed.getReadOnlyProperty();
    }

    public void calculateAllValueUsed() {
        allValueUsed.setValue(getAllValueUsed(EnumSet.allOf(DataType.class)));
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

    public int getNumberOfContinuousAnimationUsed() {
        return getParameterMap().values().stream()
                .mapToInt((e) -> (int) e.getTerms().stream().filter((t) -> (t instanceof NumberAnimationTerm) && (t.getValue() instanceof ContinuousAnimatedValue)).count())
                .sum();
    }

    public int getNumberOfNumericCategoricalAnimationUsed() {
        return getParameterMap().values().stream()
                .mapToInt((e) -> (int) e.getTerms().stream().filter((t) -> (t instanceof NumberAnimationTerm) && (t.getValue() instanceof CategoricalAnimatedValue)).count())
                .sum();
    }

    public int getNumberOfStringCategoricalAnimationUsed() {
        return getParameterMap().values().stream()
                .mapToInt((e) -> (int) e.getTerms().stream().filter((t) -> (t instanceof StringAnimationTerm)).count())
                .sum();
    }

    public List<Integer> getStringLookupTableSize() {
        return getParameterMap().values().stream()
                .mapToInt((e) -> (int) e.getTerms().stream().filter((t) -> (t instanceof StringAnimationTerm)).count())
                .boxed().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
    }

    public List<Integer> getNumberLookupTableSize() {
        return getParameterMap().values().stream()
                .mapToInt((e) -> (int) e.getTerms().stream().filter((t) -> (t instanceof NumberAnimationTerm) && (t.getValue() instanceof CategoricalAnimatedValue)).count())
                .boxed().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
    }

//        List<Integer> tableSize = new ArrayList<>();
//        for (int i=0; i<animationCount; i++)
//            tableSize.add(0);
//
//        for (Expression expression : getParameterMap().values()) {
//            List<Integer> size = expression.getTerms().stream()
//                    .filter((t) -> (t instanceof StringAnimationTerm))
//                    .mapToInt((t) -> ((StringCategoricalAnimatedValue) t.getValue()).getKeyValues().size())
//                    .boxed().sorted(Comparator.reverseOrder()).limit(animationCount)
//                    .collect(Collectors.toList());
//            for (int i=0; i<animationCount; i++) {
//                if (tableSize.get(i) < size.get(i)) {
//                    tableSize.set(i, size.get(i));
//                }
//            }
//        }
//        return tableSize;
}
