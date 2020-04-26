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
import io.makerplayground.device.shared.Parameter;
import io.makerplayground.device.shared.Value;
import io.makerplayground.project.expression.Expression;
import io.makerplayground.project.expression.NumberInRangeExpression;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.List;
import java.util.Objects;

@JsonSerialize(using = ConditionSerializer.class)
public class Condition extends NodeElement {
    private final ObservableList<UserSetting> setting;
    private final ObservableList<UserSetting> unmodifiableSetting;
    private final ObservableList<UserSetting> virtualSetting;
    private final ObservableList<UserSetting> unmodifiableVirtualSetting;

    Condition(String name, Project project) {
        super(20,20,118,75, project);

        this.name = name;
        this.setting = FXCollections.observableArrayList();
        this.virtualSetting = FXCollections.observableArrayList();

        this.unmodifiableSetting = FXCollections.unmodifiableObservableList(setting);
        this.unmodifiableVirtualSetting = FXCollections.unmodifiableObservableList(virtualSetting);
    }

    public Condition(double top, double left, double width, double height
            , String name, List<UserSetting> setting, List<UserSetting> virtualSetting, Project project) {
        // TODO: ignore width and height field to prevent line from drawing incorrectly when read file from old version as condition can't be resized anyway
        super(top, left, 118, 75, project);

        this.name = name;
        this.setting = FXCollections.observableArrayList(setting);
        this.virtualSetting = FXCollections.observableArrayList(virtualSetting);

        this.unmodifiableSetting = FXCollections.unmodifiableObservableList(this.setting);
        this.unmodifiableVirtualSetting = FXCollections.unmodifiableObservableList(this.virtualSetting);
    }

    public Condition(Condition c, String name, Project project) {
        super(c.getTop(), c.getLeft(), c.getWidth(), c.getHeight(), project);

        this.name = name;
        this.setting = FXCollections.observableArrayList();
        for (UserSetting u : c.setting) {
            this.setting.add(new UserSetting(u));
        }
        this.virtualSetting = FXCollections.observableArrayList();
        for (UserSetting u : c.virtualSetting) {
            this.virtualSetting.add(new UserSetting(u));
        }

        this.unmodifiableSetting = FXCollections.unmodifiableObservableList(this.setting);
        this.unmodifiableVirtualSetting = FXCollections.unmodifiableObservableList(this.virtualSetting);
    }

    @Override
    public void setName(String name) {
        this.name = name;
        project.invalidateDiagram();
    }

    public void addDevice(ProjectDevice device) {
        if (device.getGenericDevice().getCondition().isEmpty()) {
            throw new IllegalStateException(device.getGenericDevice().getName() + " needs to have condition.");
        } else {
            setting.add(new UserSetting(device, device.getGenericDevice().getCondition().get(0)));
        }
        project.invalidateDiagram();
    }

    public void addVirtualDevice(ProjectDevice device) {
        if (!VirtualProjectDevice.getDevices().contains(device)) {
            throw new IllegalStateException("Device to be added is not a virtual device");
        }
        virtualSetting.add(new UserSetting(device, device.getGenericDevice().getCondition().get(0)));
        project.invalidateDiagram();
    }

    public void removeDevice(ProjectDevice device) {
        for (int i = setting.size() - 1; i >= 0; i--) {
            if (setting.get(i).getDevice() == device) {
                setting.remove(i);
            }
        }
        for (UserSetting userSetting : setting) {
            for (Parameter parameter : userSetting.getParameterMap().keySet()) {
                Expression expression = userSetting.getParameterMap().get(parameter);
                if (expression.getTerms().stream().anyMatch(term -> term.getValue() instanceof ProjectValue
                        && (((ProjectValue) term.getValue()).getDevice() == device))) {
                    userSetting.getParameterMap().replace(parameter, Expression.fromDefaultParameter(parameter));
                }
            }
            for (Value value : userSetting.getExpression().keySet()) {
                Expression expression = userSetting.getExpression().get(value);
                if (expression.getValueUsed().stream().anyMatch(projectValue -> projectValue.getDevice() == device)) {
                    userSetting.getExpression().put(value, new NumberInRangeExpression(device, value));
                }
            }
        }
        project.invalidateDiagram();
    }

    public void removeUserSetting(UserSetting userSetting) {
        setting.remove(userSetting);
        virtualSetting.remove(userSetting);
        project.invalidateDiagram();
    }

    public ObservableList<UserSetting> getSetting() {
        return unmodifiableSetting;
    }

    public ObservableList<UserSetting> getVirtualDeviceSetting() {
        return unmodifiableVirtualSetting;
    }

    @Override
    protected DiagramError checkError() {
        if (setting.isEmpty() && virtualSetting.isEmpty()) {
            return DiagramError.CONDITION_EMPTY;
        }

        // name should contain only english alphanumeric characters, underscores and spaces and it should not be empty
        if (!name.matches("\\w[\\w| ]*")) {
            return DiagramError.CONDITION_INVALID_NAME;
        }

        // name should be unique
        for (Condition c : project.getUnmodifiableCondition()) {
            if ((this != c) && getNameSanitized().equals(c.getNameSanitized())) {
                return DiagramError.CONDITION_DUPLICATE_NAME;
            }
        }

        for (UserSetting userSetting : setting) {
            // at least one expression must be enable and every expression mush be valid when the action is "Compare"
            if (userSetting.getCondition().getName().equals("Compare")) {
                if (!userSetting.getExpressionEnable().containsValue(true)) {
                    return DiagramError.CONDITION_NO_ENABLE_EXPRESSION;
                }
                if (userSetting.getExpression().values().stream().anyMatch(expression -> !expression.isValid())) {
                    return DiagramError.CONDITION_INVALID_EXPRESSION;
                }
            } else {    // otherwise value of every parameters should not be null and should be valid
                if (userSetting.getParameterMap().values().stream().anyMatch(o -> Objects.isNull(o) || !o.isValid())) {
                    return DiagramError.CONDITION_INVALID_PARAM;
                }
            }
        }

        for (UserSetting userSetting : virtualSetting) {
            // at least one expression must be enable and every expression mush be valid when the action is "Compare"
            if (VirtualProjectDevice.Memory.projectDevice.equals(userSetting.getDevice())) {
                if (!userSetting.getExpressionEnable().containsValue(true)) {
                    return DiagramError.CONDITION_NO_ENABLE_EXPRESSION;
                }
            }
            else if (userSetting.getCondition().getName().equals("Compare")) {
                if (!userSetting.getExpressionEnable().containsValue(true)) {
                    return DiagramError.CONDITION_NO_ENABLE_EXPRESSION;
                }
                if (userSetting.getExpression().values().stream().anyMatch(expression -> !expression.isValid())) {
                    return DiagramError.CONDITION_INVALID_EXPRESSION;
                }
            } else {    // otherwise value of every parameters should not be null and should be valid
                if (userSetting.getParameterMap().values().stream().anyMatch(o -> Objects.isNull(o) || !o.isValid())) {
                    return DiagramError.CONDITION_INVALID_PARAM;
                }
            }
        }

        return DiagramError.NONE;
    }
}
