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


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.makerplayground.device.GenericDevice;
import io.makerplayground.device.Value;
import io.makerplayground.helper.Platform;
import io.makerplayground.helper.SingletonAddDevice;
import io.makerplayground.helper.SingletonDelDevice;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Represent a project
 */
@JsonSerialize(using = ProjectSerializer.class)
@JsonDeserialize(using = ProjectDeserializer.class)
public class Project {
    private StringProperty projectName;
    private final ProjectController controller;
    private final ObservableList<ProjectDevice> inputDevice;
    private final ObservableList<ProjectDevice> outputDevice;
    private final ObservableList<Scene> scene;
    private final ObservableList<Condition> condition;
    private final ObservableList<Line> line;
    private final Begin begin;

    private final ObservableList<ProjectDevice> unmodifiableInputDevice;
    private final ObservableList<ProjectDevice> unmodifiableOutputDevice;
    private final ObservableList<Scene> unmodifiableScene;
    private final ObservableList<Condition> unmodifiableCondition;
    private final ObservableList<Line> unmodifiableLine;

    private String filePath;
    private static final Pattern sceneNameRegex = Pattern.compile("scene\\d+");
    private static final Pattern conditionNameRegex = Pattern.compile("condition\\d+");

    public Project() {
        projectName = new SimpleStringProperty("Untitled Project");
        controller = new ProjectController(Platform.ARDUINO);
        outputDevice = FXCollections.observableArrayList();
        inputDevice = FXCollections.observableArrayList();
        scene = FXCollections.observableArrayList();
        condition = FXCollections.observableArrayList();
        line = FXCollections.observableArrayList();
        begin = new Begin();
        filePath = null;

        unmodifiableOutputDevice = FXCollections.unmodifiableObservableList(outputDevice);
        unmodifiableInputDevice = FXCollections.unmodifiableObservableList(inputDevice);
        unmodifiableScene = FXCollections.unmodifiableObservableList(scene);
        unmodifiableCondition = FXCollections.unmodifiableObservableList(condition);
        unmodifiableLine = FXCollections.unmodifiableObservableList(line);
    }

    public Project(String name, ProjectController controller, ObservableList<ProjectDevice> inputDevice, ObservableList<ProjectDevice> outputDevice, ObservableList<Scene> scene, ObservableList<Condition> condition, ObservableList<Line> line, Begin begin, String filePath) {
        this.projectName = new SimpleStringProperty(name);
        this.controller = controller;
        this.inputDevice = inputDevice;
        this.outputDevice = outputDevice;
        this.scene = scene;
        this.condition = condition;
        this.line = line;
        this.begin = begin;
        this.filePath = filePath;

        unmodifiableOutputDevice = FXCollections.unmodifiableObservableList(outputDevice);
        unmodifiableInputDevice = FXCollections.unmodifiableObservableList(inputDevice);
        unmodifiableScene = FXCollections.unmodifiableObservableList(scene);
        unmodifiableCondition = FXCollections.unmodifiableObservableList(condition);
        unmodifiableLine = FXCollections.unmodifiableObservableList(line);
    }

    public ObservableList<ProjectDevice> getOutputDevice() {
        return unmodifiableOutputDevice;
    }

    public void addOutputDevice(GenericDevice device) {
        Pattern p = Pattern.compile(device.getName()+"\\d+");
        int id = outputDevice.stream()
                .filter(projectDevice -> projectDevice.getGenericDevice() == device)
                .filter(projectDevice -> p.matcher(projectDevice.getName()).matches())
                .mapToInt(value -> Integer.parseInt(value.getName().substring(device.getName().length())))
                .max()
                .orElse(0);
        ProjectDevice projectDevice = new ProjectDevice(device.getName() + (id + 1), device);
        outputDevice.add(projectDevice);
        SingletonAddDevice.getInstance().setAll(device.getName(), "123");
    }

    public boolean removeOutputDevice(ProjectDevice device) {
        for (Scene s : scene) {
            s.removeDevice(device);
        }

        SingletonDelDevice.getInstance().setAll(device.getGenericDevice().getName(), "456");
        return outputDevice.remove(device);
    }

    public ObservableList<ProjectDevice> getInputDevice() {
        return unmodifiableInputDevice;
    }

    public void addInputDevice(GenericDevice device) {
        Pattern p = Pattern.compile(device.getName()+"\\d+");
        int id = inputDevice.stream()
                .filter(projectDevice -> projectDevice.getGenericDevice() == device)
                .filter(projectDevice -> p.matcher(projectDevice.getName()).matches())
                .mapToInt(value -> Integer.parseInt(value.getName().substring(device.getName().length())))
                .max()
                .orElse(0);
        ProjectDevice projectDevice = new ProjectDevice(device.getName() + (id + 1), device);
        inputDevice.add(projectDevice);
        SingletonAddDevice.getInstance().setAll(device.getName(), "123");
    }

    public boolean removeInputDevice(ProjectDevice device) {
        for (Condition c : condition) {
            c.removeDevice(device);
        }

        SingletonDelDevice.getInstance().setAll(device.getGenericDevice().getName(), "456");
        return inputDevice.remove(device);
    }

    public ObservableList<Scene> getScene() {
        return unmodifiableScene;
    }

    public void addState() {
        int id = scene.stream()
                .filter(scene1 -> sceneNameRegex.matcher(scene1.getName()).matches())
                .mapToInt(scene1 -> Integer.parseInt(scene1.getName().substring(5)))
                .max()
                .orElse(0);

        Scene s = new Scene();
        s.setName("scene" + (id + 1));
        scene.add(s);
    }

    public void removeState(Scene s) {
        scene.remove(s);
        for (int i=line.size()-1; i>=0; i--) {
            Line l = line.get(i);
            if (l.getSource() == s || l.getDestination() == s) {
                line.remove(l);
            }
        }
    }

    public void addCondition() {
        int id = condition.stream()
                .filter(condition -> conditionNameRegex.matcher(condition.getName()).matches())
                .mapToInt(condition -> Integer.parseInt(condition.getName().substring(9)))
                .max()
                .orElse(0);

        Condition c = new Condition();
        c.setName("condition" + (id + 1));
        condition.add(c);
    }

    public void removeCondition(Condition c) {
        condition.remove(c);
        for (int i=line.size()-1; i>=0; i--) {
            Line l = line.get(i);
            if (l.getSource() == c || l.getDestination() == c) {
                line.remove(l);
            }
        }
    }

    public ObservableList<Condition> getCondition() {
        return unmodifiableCondition;
    }

    public void addLine(NodeElement source, NodeElement destination) {
        // do not create new line if there existed a line with identical source and destination
        if (line.stream().noneMatch(line1 -> (line1.getSource() == source) && (line1.getDestination() == destination))) {
            Line l = new Line(source, destination);
            line.add(l);
        }
    }

    public void removeLine(Line l) {
        line.remove(l);
    }

    public boolean hasLine(NodeElement source, NodeElement destination) {
        return line.stream().anyMatch(line1 -> (line1.getSource() == source) && (line1.getDestination() == destination));
    }

    public ObservableList<Line> getLine() {
        return unmodifiableLine;
    }

    public String getProjectName() {
        return projectName.get();
    }

    public StringProperty projectNameProperty() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName.set(projectName);
    }

    public List<ProjectValue> getAvailableValue() {
        List<ProjectValue> value = new ArrayList<>();
        for (ProjectDevice projectDevice : inputDevice) {
            for (Value v : projectDevice.getGenericDevice().getValue()) {
                value.add(new ProjectValue(projectDevice, v));
            }
        }
        return value;
    }

    public ProjectController getController() {
        return controller;
    }

    public ObservableList<ProjectDevice> getAllDevice() {
        ObservableList<ProjectDevice> allDevice = FXCollections.observableArrayList();
        allDevice.addAll(inputDevice);
        allDevice.addAll(outputDevice);
        return allDevice;
    }

    public Begin getBegin() { return begin; }

    public Set<GenericDevice> getAllDeviceTypeUsed() {
        Set<GenericDevice> deviceType = new HashSet<>();
        for (ProjectDevice projectDevice : getAllDeviceUsed()) {
            deviceType.add(projectDevice.getGenericDevice());
        }
        return deviceType;
    }

    public Set<ProjectDevice> getAllDeviceUsed() {
        Set<ProjectDevice> deviceType = new HashSet<>();

        for (Scene s : scene) {
            for (UserSetting userSetting : s.getSetting()) {
                deviceType.add(userSetting.getDevice());
                deviceType.addAll(userSetting.getAllValueUsed().keySet());
            }
        }

        for (Condition c : condition) {
            for (UserSetting userSetting : c.getSetting()) {
                deviceType.add(userSetting.getDevice());
                deviceType.addAll(userSetting.getAllValueUsed().keySet());
            }
        }

        return deviceType;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
