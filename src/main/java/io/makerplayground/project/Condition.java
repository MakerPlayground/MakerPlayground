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
import io.makerplayground.project.expression.Expression;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.*;

/**
 *
 */
@JsonSerialize(using = ConditionSerializer.class)
public class Condition extends NodeElement {
    private final StringProperty name;
    private final ObservableList<UserSetting> setting;
    private final ObservableList<UserSetting> unmodifiableSetting;

    Condition(Project project) {
        super(20,20,217, 112, project);

        this.name = new SimpleStringProperty();
        this.setting = FXCollections.observableArrayList();

        this.unmodifiableSetting = FXCollections.unmodifiableObservableList(setting);
        invalidate();
    }

    public Condition(double top, double left, double width, double height
            , String name, List<UserSetting> setting, Project project) {
        super(top, left, width, height, project);

        this.name = new SimpleStringProperty(name);
        this.setting = FXCollections.observableList(setting);

        this.unmodifiableSetting = FXCollections.unmodifiableObservableList(this.setting);
        invalidate();
    }

    public Condition(Condition c, String name, Project project) {
        super(c.getTop(), c.getLeft(), c.getWidth(), c.getHeight(), project);

        this.name = new SimpleStringProperty(name);
        this.setting = FXCollections.observableArrayList();
        for (UserSetting u : c.setting) {
            this.setting.add(new UserSetting(u));
        }

        this.unmodifiableSetting = FXCollections.unmodifiableObservableList(this.setting);
        invalidate();
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
        setting.add(new UserSetting(device, false));
        invalidate();
    }

    public void removeDevice(ProjectDevice device) {
        for (int i = setting.size() - 1; i >= 0; i--) {
            if (setting.get(i).getDevice() == device) {
                setting.remove(i);
            }
        }
        invalidate();
    }

    public ObservableList<UserSetting> getSetting() {
        return unmodifiableSetting;
    }

    @Override
    protected DiagramError checkError() {
        if (setting.isEmpty()) {
            return DiagramError.CONDITION_EMPTY;
        }

        // value of every parameters should not be null
        if (setting.stream().flatMap(userSetting -> userSetting.getValueMap().values().stream())
                .anyMatch(Objects::isNull)) {
            return DiagramError.CONDITION_INVALID_PARAM;
        }

        // every expression must be valid
        if (!setting.stream().flatMap(userSetting -> userSetting.getExpression().values().stream())
                .allMatch(Expression::isValid)) {
            return DiagramError.CONDITION_INVALID_EXPRESSION;
        }

        // at least one expression must be enable
        for (UserSetting userSetting : setting) {
            if (!userSetting.getExpressionEnable().isEmpty() && !userSetting.getExpressionEnable().values().contains(true)) {
                return DiagramError.CONDITION_NO_ENABLE_EXPRESSION;
            }
        }

        return DiagramError.NONE;
    }
}
