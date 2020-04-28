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
import io.makerplayground.project.expression.Expression;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import java.util.List;
import java.util.Objects;

/**
 *
 */
@JsonSerialize(using = SceneSerializer.class)
public class Scene extends NodeElement {

    private final ObservableList<UserSetting> allSettings;
    private final ObservableList<UserSetting> setting;
    private final ObservableList<UserSetting> virtualSetting;

    Scene(Project project) {
        super(20, 20, 205, 124, project);

        this.name = "";

        // fire update event when actionProperty is invalidated / changed
        this.allSettings = FXCollections.observableArrayList(item -> new Observable[]{item.actionProperty()});
        this.setting = FXCollections.unmodifiableObservableList(new FilteredList<>(allSettings, userSetting -> !(userSetting.getDevice() instanceof VirtualProjectDevice)));
        this.virtualSetting = FXCollections.unmodifiableObservableList(new FilteredList<>(allSettings, userSetting -> userSetting.getDevice() instanceof VirtualProjectDevice));
    }

    Scene(double top, double left, double width, double height
            , String name, List<UserSetting> allSettings, Project project) {
        // TODO: ignore width and height field to prevent line from drawing incorrectly when read file from old version as scene can't be resized anyway
        super(top, left, 205, 124, project);
        this.name = name;
        // fire update event when actionProperty is invalidated / changed
        this.allSettings = FXCollections.observableArrayList(item -> new Observable[]{item.actionProperty()});
        this.setting = FXCollections.unmodifiableObservableList(new FilteredList<>(this.allSettings, userSetting -> userSetting.getDevice() instanceof ProjectDevice && !(userSetting.getDevice() instanceof VirtualProjectDevice)));
        this.virtualSetting = FXCollections.unmodifiableObservableList(new FilteredList<>(this.allSettings, userSetting -> userSetting.getDevice() instanceof VirtualProjectDevice));
        this.allSettings.addAll(allSettings);
    }

    Scene(Scene s, String name, Project project) {
        super(s.getTop(), s.getLeft(), s.getWidth(), s.getHeight(), project);
        this.name = name;
        this.allSettings = FXCollections.observableArrayList(item -> new Observable[]{item.actionProperty()});
        this.setting = FXCollections.unmodifiableObservableList(new FilteredList<>(allSettings, userSetting -> userSetting.getDevice() instanceof ProjectDevice && !(userSetting.getDevice() instanceof VirtualProjectDevice)));
        this.virtualSetting = FXCollections.unmodifiableObservableList(new FilteredList<>(allSettings, userSetting -> userSetting.getDevice() instanceof VirtualProjectDevice));
        for (UserSetting u : s.allSettings) {
            this.allSettings.add(new UserSetting(u));
        }
    }

    public void addDevice(ProjectDevice device) {
        if (device.getGenericDevice().getAction().isEmpty()) {
            throw new IllegalStateException(device.getGenericDevice().getName() + " needs to have action.");
        } else {
            allSettings.add(new UserSetting(project, device, device.getGenericDevice().getAction().get(0)));
        }
        project.invalidateDiagram();
    }


    public void addVirtualDevice(ProjectDevice device) {
        if (!VirtualProjectDevice.getDevices().contains(device)) {
            throw new IllegalStateException("Device to be added is not a virtual device");
        }
        allSettings.add(new UserSetting(project, device, device.getGenericDevice().getAction().get(0)));
    }

    public void removeDevice(ProjectDevice device) {
        for (int i = allSettings.size() - 1; i >= 0; i--) {
            if (allSettings.get(i).getDevice() == device) {
                allSettings.remove(i);
            }
        }
        for (UserSetting userSetting : allSettings) {
            for (Parameter parameter : userSetting.getParameterMap().keySet()) {
                Expression expression = userSetting.getParameterMap().get(parameter);
                if (expression.getTerms().stream().anyMatch(term -> term.getValue() instanceof ProjectValue
                        && (((ProjectValue) term.getValue()).getDevice() == device))) {
                    userSetting.getParameterMap().replace(parameter, Expression.fromDefaultParameter(parameter));
                }
            }
        }
        project.invalidateDiagram();
    }

    @Override
    public void setName(String name) {
        this.name = name;
        project.invalidateDiagram();
    }

    /**
     * Get a list of all {@link UserSetting}.
     * @return The unmodifiable obsevable list that contains {@link UserSetting} of both {@link ProjectDevice} and {@link VirtualProjectDevice}.
     */
    public ObservableList<UserSetting> getAllSettings() { return FXCollections.unmodifiableObservableList(allSettings); }

    /**
     * Get a list of {@link UserSetting} that associates with actual {@link ProjectDevice}.
     * @return The unmodifiable obsevable list that contains {@link UserSetting} of {@link ProjectDevice} but not {@link VirtualProjectDevice}.
     */
    public ObservableList<UserSetting> getSetting() {
        return setting;
    }

    /**
     * Get a list of {@link UserSetting} that associates with {@link VirtualProjectDevice}.
     * @return The unmodifiable obsevable list that contains {@link UserSetting} of {@link VirtualProjectDevice}.
     */
    public ObservableList<UserSetting> getVirtualDeviceSetting() {
        return virtualSetting;
    }

    public void removeUserSetting(UserSetting userSetting) {
        this.allSettings.remove(userSetting);
        project.invalidateDiagram();
    }

    @Override
    protected DiagramError checkError() {
        // name should contain only english alphanumeric characters, underscores and spaces and it should not be empty
        if (!name.matches("\\w[\\w| ]*")) {
            return DiagramError.SCENE_INVALID_NAME;
        }

        // name should be unique
        for (Scene s : project.getUnmodifiableScene()) {
            if ((this != s) && getNameSanitized().equals(s.getNameSanitized())) {
                return DiagramError.SCENE_DUPLICATE_NAME;
            }
        }

        // parameter should not be null and should be valid
        if (setting.stream().flatMap(userSetting -> userSetting.getParameterMap().values().stream())
                .anyMatch(o -> Objects.isNull(o) || !o.isValid())) {
            return DiagramError.SCENE_INVALID_PARAM;
        }

        if (virtualSetting.stream().flatMap(userSetting -> userSetting.getParameterMap().values().stream())
                .anyMatch(o -> Objects.isNull(o) || !o.isValid())) {
            return DiagramError.SCENE_INVALID_PARAM;
        }

        return DiagramError.NONE;
    }
}
