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


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.makerplayground.device.Device;
import io.makerplayground.device.GenericDevice;
import io.makerplayground.device.Value;
import io.makerplayground.helper.Platform;
import io.makerplayground.helper.SingletonAddDevice;
import io.makerplayground.helper.SingletonDelDevice;
import io.makerplayground.version.ProjectVersionControl;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represent a project
 */
@JsonSerialize(using = ProjectSerializer.class)
@JsonDeserialize(using = ProjectDeserializer.class)
public class Project {
    private StringProperty projectName;
    private ReadOnlyObjectWrapper<Platform> platform;
    private ObjectProperty<Device> controller;
    private final ObservableList<ProjectDevice> sensor;
    private final ObservableList<ProjectDevice> actuator;
    private final ObservableList<ProjectDevice> connectivity;
    private final ObservableList<Scene> scene;
    private final ObservableList<Condition> condition;
    private final ObservableList<Line> line;
    private final Begin begin;

    private final ObservableList<ProjectDevice> unmodifiableSensor;
    private final ObservableList<ProjectDevice> unmodifiableActuator;
    private final ObservableList<ProjectDevice> unmodifiableConnectivity;
    private final ObservableList<Scene> unmodifiableScene;
    private final ObservableList<Condition> unmodifiableCondition;
    private final ObservableList<Line> unmodifiableLine;

    private final StringProperty filePath;
    private static final Pattern sceneNameRegex = Pattern.compile("scene\\d+");
    private static final Pattern conditionNameRegex = Pattern.compile("condition\\d+");

    public Project() {
        projectName = new SimpleStringProperty("Untitled Project");
        platform = new ReadOnlyObjectWrapper<>(Platform.MP_ARDUINO);
        controller = new SimpleObjectProperty<>();
        actuator = FXCollections.observableArrayList();
        sensor = FXCollections.observableArrayList();
        connectivity = FXCollections.observableArrayList();
        scene = FXCollections.observableArrayList();
        condition = FXCollections.observableArrayList();
        line = FXCollections.observableArrayList();
        begin = new Begin();
        filePath = new SimpleStringProperty("");

        unmodifiableActuator = FXCollections.unmodifiableObservableList(actuator);
        unmodifiableSensor = FXCollections.unmodifiableObservableList(sensor);
        unmodifiableConnectivity = FXCollections.unmodifiableObservableList(connectivity);
        unmodifiableScene = FXCollections.unmodifiableObservableList(scene);
        unmodifiableCondition = FXCollections.unmodifiableObservableList(condition);
        unmodifiableLine = FXCollections.unmodifiableObservableList(line);
    }

    public Project(String name, Device controller, Platform platform, ObservableList<ProjectDevice> sensor, ObservableList<ProjectDevice> actuator
            , ObservableList<ProjectDevice> connectivity, ObservableList<Scene> scene, ObservableList<Condition> condition
            , ObservableList<Line> line, Begin begin, String filePath) {
        this.projectName = new SimpleStringProperty(name);
        this.controller = new SimpleObjectProperty<>(controller);
        this.platform = new ReadOnlyObjectWrapper<>(platform);
        this.sensor = sensor;
        this.actuator = actuator;
        this.connectivity = connectivity;
        this.scene = scene;
        this.condition = condition;
        this.line = line;
        this.begin = begin;
        this.filePath = new SimpleStringProperty(filePath);

        unmodifiableActuator = FXCollections.unmodifiableObservableList(actuator);
        unmodifiableSensor = FXCollections.unmodifiableObservableList(sensor);
        unmodifiableConnectivity = FXCollections.unmodifiableObservableList(connectivity);
        unmodifiableScene = FXCollections.unmodifiableObservableList(scene);
        unmodifiableCondition = FXCollections.unmodifiableObservableList(condition);
        unmodifiableLine = FXCollections.unmodifiableObservableList(line);
    }

    public ObservableList<ProjectDevice> getActuator() {
        return unmodifiableActuator;
    }

    public void addActuator(GenericDevice device) {
        Pattern p = Pattern.compile(device.getName()+"\\d+");
        int id = actuator.stream()
                .filter(projectDevice -> projectDevice.getGenericDevice() == device)
                .filter(projectDevice -> p.matcher(projectDevice.getName()).matches())
                .mapToInt(value -> Integer.parseInt(value.getName().substring(device.getName().length())))
                .max()
                .orElse(0);
        ProjectDevice projectDevice = new ProjectDevice(device.getName() + (id + 1), device);
        actuator.add(projectDevice);
        SingletonAddDevice.getInstance().setAll(device.getName(), "123");
    }

    public Platform getPlatform() {
        return platform.get();
    }

    public ReadOnlyObjectProperty<Platform> platformProperty() {
        return platform.getReadOnlyProperty();
    }

    public void setPlatform(Platform platform) {
        this.platform.set(platform);
        // controller must be cleared every time platform has changed
        setController(null);
    }

    public boolean removeActuator(ProjectDevice device) {
        for (Scene s : scene) {
            s.removeDevice(device);
        }

        SingletonDelDevice.getInstance().setAll(device.getGenericDevice().getName(), "456");
        return actuator.remove(device);
    }

    public ObservableList<ProjectDevice> getSensor() {
        return unmodifiableSensor;
    }

    public void addSensor(GenericDevice device) {
        Pattern p = Pattern.compile(device.getName()+"\\d+");
        int id = sensor.stream()
                .filter(projectDevice -> projectDevice.getGenericDevice() == device)
                .filter(projectDevice -> p.matcher(projectDevice.getName()).matches())
                .mapToInt(value -> Integer.parseInt(value.getName().substring(device.getName().length())))
                .max()
                .orElse(0);
        ProjectDevice projectDevice = new ProjectDevice(device.getName() + (id + 1), device);
        sensor.add(projectDevice);
        SingletonAddDevice.getInstance().setAll(device.getName(), "123");
    }

    public boolean removeSensor(ProjectDevice device) {
        for (Scene s : scene) {
            s.removeDevice(device);
        }
        for (Condition c : condition) {
            c.removeDevice(device);
        }

        SingletonDelDevice.getInstance().setAll(device.getGenericDevice().getName(), "456");
        return sensor.remove(device);
    }

    public ObservableList<ProjectDevice> getConnectivity() {
        return unmodifiableConnectivity;
    }

    public void addConnectivity(GenericDevice device) {
        Pattern p = Pattern.compile(device.getName()+"\\d+");
        int id = connectivity.stream()
                .filter(projectDevice -> projectDevice.getGenericDevice() == device)
                .filter(projectDevice -> p.matcher(projectDevice.getName()).matches())
                .mapToInt(value -> Integer.parseInt(value.getName().substring(device.getName().length())))
                .max()
                .orElse(0);
        ProjectDevice projectDevice = new ProjectDevice(device.getName() + (id + 1), device);
        connectivity.add(projectDevice);
        SingletonAddDevice.getInstance().setAll(device.getName(), "123");
    }

    public boolean removeConnectivity(ProjectDevice device) {
        for (Scene s : scene) {
            s.removeDevice(device);
        }
        for (Condition c : condition) {
            c.removeDevice(device);
        }

        return connectivity.remove(device);
    }

    public List<ProjectDevice> getInputDevice() {
        return Stream.concat(sensor.stream(), connectivity.filtered(device -> !device.getGenericDevice().getCondition().isEmpty()).stream())
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    public List<ProjectDevice> getOutputDevice() {
        return Stream.concat(actuator.stream(), connectivity.filtered(device -> !device.getGenericDevice().getAction().isEmpty()).stream())
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
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

    public Scene addState(Scene s) {
        int id = scene.stream()
                .filter(scene1 -> sceneNameRegex.matcher(scene1.getName()).matches())
                .mapToInt(scene1 -> Integer.parseInt(scene1.getName().substring(5)))
                .max()
                .orElse(0);

        Scene newScene = new Scene(s, "scene" + (id + 1));
        scene.add(newScene);
        return newScene;
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

    public Condition addCondition(Condition c) {
        int id = condition.stream()
                .filter(condition -> conditionNameRegex.matcher(condition.getName()).matches())
                .mapToInt(condition -> Integer.parseInt(condition.getName().substring(9)))
                .max()
                .orElse(0);

        Condition newCondition = new Condition(c, "condition" + (id + 1));
        condition.add(newCondition);
        return newCondition;
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

    public boolean hasLineFrom(NodeElement source) {
        return line.stream().anyMatch(line1 -> line1.getSource() == source);
    }

    public List<Line> getLineFrom(NodeElement source) {
        return line.stream().filter(line1 -> line1.getSource() == source).collect(Collectors.toList());
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
        for (ProjectDevice projectDevice : sensor) {
            for (Value v : projectDevice.getGenericDevice().getValue()) {
                value.add(new ProjectValue(projectDevice, v));
            }
        }
        for (ProjectDevice projectDevice : connectivity) {
            for (Value v : projectDevice.getGenericDevice().getValue()) {
                value.add(new ProjectValue(projectDevice, v));
            }
        }
        return value;
    }

    public Device getController() {
        return controller.get();
    }

//    public ObjectProperty<Device> controllerProperty() {
//        return controller;
//    }

    public void setController(Device controller) {
        this.controller.set(controller);
        // remove all port and actual device assignment when the controller is changed
        for (ProjectDevice projectDevice : getAllDevice()) {
            projectDevice.removeAllDeviceConnection();
            projectDevice.setActualDevice(null);
            projectDevice.setAutoSelectDevice(true);
        }
    }

    public List<ProjectDevice> getAllDevice() {
        return Stream.concat(Stream.concat(sensor.stream(), actuator.stream()), connectivity.stream())
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
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

    public Map<ProjectDevice, Set<Value>> getAllValueUsedMap() {
        HashMap<ProjectDevice, Set<Value>> allValueUsed = new HashMap<>();
        for (Scene s : scene) {
            for (UserSetting userSetting : s.getSetting())
                for (Map.Entry<ProjectDevice, Set<Value>> entry : userSetting.getAllValueUsed().entrySet()) {
                    if (!allValueUsed.containsKey(entry.getKey())) {
                        allValueUsed.put(entry.getKey(), new HashSet<>());
                    }
                    allValueUsed.get(entry.getKey()).addAll(entry.getValue());
                }
        }

        for (Condition c : condition) {
            for (UserSetting userSetting : c.getSetting()) {
                for (Map.Entry<ProjectDevice, Set<Value>> entry : userSetting.getAllValueUsed().entrySet()) {
                    if (!allValueUsed.containsKey(entry.getKey())) {
                        allValueUsed.put(entry.getKey(), new HashSet<>());
                    }
                    allValueUsed.get(entry.getKey()).addAll(entry.getValue());
                }
            }
        }
        return allValueUsed;
    }

    public String getFilePath() {
        return filePath.get();
    }

    public StringProperty filePathProperty() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath.set(filePath);
    }

    public boolean hasUnsavedModification() {
        if (getFilePath().isEmpty()) {
            // A hack way to check for project modification in case that it hasn't been saved
            return !(platform.get() == Platform.MP_ARDUINO && controller.get() == null
                    && sensor.isEmpty() && actuator.isEmpty() && connectivity.isEmpty()
                    && scene.isEmpty() && condition.isEmpty() && line.isEmpty()
                    && begin.getTop() == 200 && begin.getLeft() == 20); // begin hasn't been moved
        } else {
            ObjectMapper mapper = new ObjectMapper();
            String newContent;
            try {
                newContent = mapper.writeValueAsString(this);
            } catch (JsonProcessingException e) {
                return true;
            }

            String oldContent;
            try {
                oldContent = new String(Files.readAllBytes(new File(getFilePath()).toPath()));
            } catch (IOException e) {
                return true;
            }

            return !oldContent.equals(newContent);
        }
    }

    public Set<String> getAllDeviceName(){
        Set<String> deviceName = new HashSet<>();
        for (ProjectDevice projectDevice : this.getAllDeviceUsed()){
            deviceName.add(projectDevice.getName());
        }
        return deviceName;
    }

    public static Project loadProject(File f) {
        ObjectMapper mapper = new ObjectMapper();
        Project p = null;
        try {
            p = mapper.readValue(f, Project.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        p.setFilePath(f.getAbsolutePath());
        return p;
    }

    public boolean isNameDuplicate(String newName) {
        for (ProjectDevice projectDevice : this.getAllDevice()) {
            if (projectDevice.getName().equals(newName)) {
                return true;
            }
        }
        return false;
    }
}
