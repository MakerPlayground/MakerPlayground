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
import io.makerplayground.helper.OperandType;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    public Project() {
        projectName = new SimpleStringProperty("Untitled Project");
        controller = new ProjectController(Platform.ARDUINO);
        outputDevice = FXCollections.observableArrayList();
        inputDevice = FXCollections.observableArrayList();
        scene = FXCollections.observableArrayList();
        condition = FXCollections.observableArrayList();
        line = FXCollections.observableArrayList();
        begin = new Begin();

        unmodifiableOutputDevice = FXCollections.unmodifiableObservableList(outputDevice);
        unmodifiableInputDevice = FXCollections.unmodifiableObservableList(inputDevice);
        unmodifiableScene = FXCollections.unmodifiableObservableList(scene);
        unmodifiableCondition = FXCollections.unmodifiableObservableList(condition);
        unmodifiableLine = FXCollections.unmodifiableObservableList(line);
    }

    public Project(String name, ProjectController controller, ObservableList<ProjectDevice> inputDevice, ObservableList<ProjectDevice> outputDevice, ObservableList<Scene> scene, ObservableList<Condition> condition, ObservableList<Line> line, Begin begin) {
        this.projectName = new SimpleStringProperty(name);
        this.controller = controller;
        this.inputDevice = inputDevice;
        this.outputDevice = outputDevice;
        this.scene = scene;
        this.condition = condition;
        this.line = line;
        this.begin = begin;

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
        int maxCount = 0;
        // Find all the same genericDevice's name in outputDevice list
        // to get count for creating running number
        List<ProjectDevice> deviceSameType = outputDevice.stream()
                .filter(d -> d.getGenericDevice().getName().equals(device.getName()))
                .collect(Collectors.toList());
        for (ProjectDevice d : deviceSameType) {
            if (d.getName().contains(device.getName())) {
                // Extract number from string for creating running number
                Pattern lastIntPattern = Pattern.compile("[^0-9]+([0-9]+)$");
                Matcher matcher = lastIntPattern.matcher(d.getName());
                if (matcher.find()) {
                    String someNumberStr = matcher.group(1);
                    int lastNumberInt = Integer.parseInt(someNumberStr);
                    if (lastNumberInt >= maxCount) {
                        maxCount = lastNumberInt;
                    }
                }
            }
        }

        String name;
        if (device.getName().contains("7"))
            name = "Seven Seg" + (maxCount+1);
        else
            name = device.getName() + (maxCount+1);

        ProjectDevice projectDevice = new ProjectDevice(name, device);

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
        int maxCount = 0;
        // Find all the same genericDevice's name in outputDevice list
        // to get count for creating running number
        List<ProjectDevice> deviceSameType = inputDevice.stream()
                .filter(d -> d.getGenericDevice().getName().equals(device.getName()))
                .collect(Collectors.toList());
        for (ProjectDevice d : deviceSameType) {
            if (d.getName().contains(device.getName())) {
                // Extract number from string for creating running number
                Pattern lastIntPattern = Pattern.compile("[^0-9]+([0-9]+)$");
                Matcher matcher = lastIntPattern.matcher(d.getName());
                if (matcher.find()) {
                    String someNumberStr = matcher.group(1);
                    int lastNumberInt = Integer.parseInt(someNumberStr);
                    if (lastNumberInt >= maxCount) {
                        maxCount = lastNumberInt;
                    }
                }
            }
        }

        // TODO: Add to condition

        inputDevice.add(new ProjectDevice(device.getName() + (maxCount+1), device));
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

    public Scene addState() {
        Scene s = new Scene();
        // TODO: check for duplicate name
        s.setName("scene" + (scene.size() + 1));

        // Add every output device to be displayed in new scene
//        for (ProjectDevice projectDevice: outputDevice) {
//            s.addDevice(projectDevice);
//        }

        //diagram.addVertex(s);
        scene.add(s);
        return s;
    }

    public void removeState(Scene s) {
        // TODO: remove edge connect to that scene
        //diagram.removeVertex(s);
        scene.remove(s);
        for (int i=line.size()-1; i>=0; i--) {
            Line l = line.get(i);
            if (l.getSource() == s || l.getDestination() == s) {
                line.remove(l);
            }
        }
    }

    // TODO: add method to manage condition
    public Condition addCondition() {
        Condition c = new Condition();
        // TODO: check for duplicate name
        c.setName("condition" + (condition.size() + 1));
        condition.add(c);
        return c;
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

    public Line addLine(NodeElement source, NodeElement destination) {
        Line l = new Line(source, destination);
        line.add(l);
        return l;
    }

    public void removeLine(Line l) {
        line.remove(l);
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
            }
        }

        for (Condition c : condition) {
            for (UserSetting userSetting : c.getSetting()) {
                deviceType.add(userSetting.getDevice());
                for (ObservableList<Expression> expressionList : userSetting.getExpression().values()) {
                    for (Expression expression : expressionList) {
                        if (expression.getOperandType() == OperandType.VARIABLE) {
                            deviceType.add(((ProjectValue) expression.getFirstOperand()).getDevice());
                            if (expression.getOperator().isBetween()) {
                                deviceType.add(((ProjectValue) expression.getSecondOperand()).getDevice());
                            }
                        }
                    }
                }
            }
        }

        return deviceType;
    }
}
