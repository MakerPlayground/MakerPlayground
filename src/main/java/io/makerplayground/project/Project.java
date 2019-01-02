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
import io.makerplayground.device.GenericDeviceType;
import io.makerplayground.device.actual.ActualDevice;
import io.makerplayground.device.actual.CloudPlatform;
import io.makerplayground.device.generic.GenericDevice;
import io.makerplayground.device.shared.Value;
import io.makerplayground.device.actual.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Represent a project
 */
@JsonSerialize(using = ProjectSerializer.class)
@JsonDeserialize(using = ProjectDeserializer.class)
public class Project {
    private StringProperty projectName;
    private ReadOnlyObjectWrapper<Platform> platform;
    private ObjectProperty<ActualDevice> controller;
    private final ObservableList<ProjectDevice> device;
    private final ObservableList<Scene> scene;
    private final ObservableList<Condition> condition;
    private final ObservableList<Line> line;
    private final Begin begin;
    private final Map<CloudPlatform, Map<String, String>> parameter;

    private final FilteredList<ProjectDevice> sensorDevice;
    private final FilteredList<ProjectDevice> actuatorDevice;
    private final FilteredList<ProjectDevice> utilityDevice;
    private final FilteredList<ProjectDevice> cloudDevice;
    private final FilteredList<ProjectDevice> interfaceDevice;
    private final FilteredList<ProjectDevice> deviceWithAction;
    private final FilteredList<ProjectDevice> deviceWithCondition;
    private final ObservableList<ProjectDevice> unmodifiableDevice;
    private final ObservableList<Scene> unmodifiableScene;
    private final ObservableList<Condition> unmodifiableCondition;
    private final ObservableList<Line> unmodifiableLine;

    private final StringProperty filePath;
    private static final Pattern sceneNameRegex = Pattern.compile("Scene\\d+");
    private static final Pattern conditionNameRegex = Pattern.compile("condition\\d+");

    public Project() {
        projectName = new SimpleStringProperty("Untitled Project");
        platform = new ReadOnlyObjectWrapper<>(Platform.ARDUINO_AVR8);
        controller = new SimpleObjectProperty<>();

        device = FXCollections.observableArrayList();
        unmodifiableDevice = FXCollections.unmodifiableObservableList(device);
        actuatorDevice = new FilteredList<>(device, projectDevice -> projectDevice.getGenericDevice().getType() == GenericDeviceType.ACTUATOR);
        sensorDevice = new FilteredList<>(device, projectDevice -> projectDevice.getGenericDevice().getType() == GenericDeviceType.SENSOR);
        utilityDevice = new FilteredList<>(device, projectDevice -> projectDevice.getGenericDevice().getType() == GenericDeviceType.UTILITY);
        cloudDevice = new FilteredList<>(device, projectDevice -> projectDevice.getGenericDevice().getType() == GenericDeviceType.CLOUD);
        interfaceDevice = new FilteredList<>(device, projectDevice -> projectDevice.getGenericDevice().getType() == GenericDeviceType.INTERFACE);
        deviceWithAction = new FilteredList<>(device, projectDevice -> projectDevice.getGenericDevice().hasAction());
        deviceWithCondition = new FilteredList<>(device, projectDevice -> projectDevice.getGenericDevice().hasCondition());

        scene = FXCollections.observableArrayList();
        condition = FXCollections.observableArrayList();
        line = FXCollections.observableArrayList();
        begin = new Begin(this);

        parameter = new EnumMap<>(CloudPlatform.class);
        filePath = new SimpleStringProperty("");

        unmodifiableScene = FXCollections.unmodifiableObservableList(scene);
        unmodifiableCondition = FXCollections.unmodifiableObservableList(condition);
        unmodifiableLine = FXCollections.unmodifiableObservableList(line);
    }

    // it is very difficult to directly clone an instance of the project class for many reasons e.g. UserSetting hold a
    // reference to ProjectDevice which need to be updated to the cloned ProjectDevice (complex mapping and searching)
    public static Project newInstance(Project project) {
        ObjectMapper mapper = new ObjectMapper();
        Project newProject = null;
        try {
            newProject = mapper.treeToValue(mapper.valueToTree(project), Project.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();    // this should not happen as we're parsing an in-memory stream
        }
        return newProject;
    }

    public ObservableList<ProjectDevice> getDevice() {
        return unmodifiableDevice;
    }

    public ObservableList<ProjectDevice> getActuatorDevice() {
        return actuatorDevice;
    }

    public ObservableList<ProjectDevice> getSensorDevice() {
        return sensorDevice;
    }

    public ObservableList<ProjectDevice> getUtilityDevice() {
        return utilityDevice;
    }

    public ObservableList<ProjectDevice> getCloudDevice() {
        return cloudDevice;
    }

    public ObservableList<ProjectDevice> getInterfaceDevice() {
        return interfaceDevice;
    }

    public ObservableList<ProjectDevice> getDeviceWithAction() {
        return deviceWithAction;
    }

    public ObservableList<ProjectDevice> getDeviceWithCondition() {
        return deviceWithCondition;
    }

    public Platform getPlatform() {
        return platform.get();
    }

    private String getDeviceVarName(GenericDevice device) {
        return device.getName().replaceAll("[()]", "");
    }

    private int getNextId(GenericDevice device) {
        String varName = getDeviceVarName(device);
        Pattern p = Pattern.compile(varName+"\\d+");
        return getDevice().stream()
                .filter(projectDevice -> p.matcher(projectDevice.getName()).matches())
                .mapToInt(value -> Integer.parseInt(value.getName().substring(varName.length())))
                .max()
                .orElse(0) + 1;
    }

    void addDevice(ProjectDevice projectDevice) {
        device.add(projectDevice);
    }

    public void addDevice(GenericDevice genericDevice) {
        String varName = getDeviceVarName(genericDevice);
        ProjectDevice projectDevice = new ProjectDevice(varName + getNextId(genericDevice), genericDevice);
        device.add(projectDevice);
    }

    public void removeDevice(ProjectDevice genericDevice) {
        scene.forEach(s->s.removeDevice(genericDevice));
        condition.forEach(c->c.removeDevice(genericDevice));
        if (!device.remove(genericDevice)) {
            throw new IllegalStateException("");
        }
    }

    public ReadOnlyObjectProperty<Platform> platformProperty() {
        return platform.getReadOnlyProperty();
    }

    public void setPlatform(Platform platform) {
        this.platform.set(platform);
        // controller must be cleared every time platform has changed
        setController(null);
    }

//    public List<ProjectDevice> getInputDevice() {
//        return Stream.of(sensorDevice, actuatorDevice, utilityDevice, cloudDevice, interfaceDevice)
//                .flatMap(Collection::stream)
//                .filter(device -> device.getGenericDevice().hasCondition())
//                .collect(Collectors.toUnmodifiableList());
//    }
//
//    public List<ProjectDevice> getOutputDevice() {
//        return Stream.of(sensorDevice, actuatorDevice, utilityDevice, cloudDevice, interfaceDevice)
//                .flatMap(Collection::stream)
//                .filter(device -> device.getGenericDevice().hasAction())
//                .collect(Collectors.toUnmodifiableList());
//    }

    public ObservableList<Scene> getScene() {
        return unmodifiableScene;
    }

    public Optional<Scene> getScene(String name) {
        return scene.stream().filter(s -> s.getName().equals(name)).findFirst();
    }

    public Scene newScene() {
        int id = scene.stream()
                .filter(scene1 -> sceneNameRegex.matcher(scene1.getName()).matches())
                .mapToInt(scene1 -> Integer.parseInt(scene1.getName().substring(5)))
                .max()
                .orElse(0);

        Scene s = new Scene(this);
        s.setName("Scene" + (id + 1));
        scene.add(s);
        checkAndInvalidateDiagram();
        return s;
    }

    public Scene newScene(Scene s) {
        int id = scene.stream()
                .filter(scene1 -> sceneNameRegex.matcher(scene1.getName()).matches())
                .mapToInt(scene1 -> Integer.parseInt(scene1.getName().substring(5)))
                .max()
                .orElse(0);

        Scene newScene = new Scene(s, "Scene" + (id + 1), this);
        scene.add(newScene);
        checkAndInvalidateDiagram();
        return newScene;
    }

    void addScene(Scene s) {
        scene.add(s);
    }

    public void removeScene(Scene s) {
        scene.remove(s);
        for (int i=line.size()-1; i>=0; i--) {
            Line l = line.get(i);
            if (l.getSource() == s || l.getDestination() == s) {
                line.remove(l);
            }
        }
        checkAndInvalidateDiagram();
    }

    public Condition newCondition() {
        int id = condition.stream()
                .filter(condition -> conditionNameRegex.matcher(condition.getName()).matches())
                .mapToInt(condition -> Integer.parseInt(condition.getName().substring(9)))
                .max()
                .orElse(0);

        Condition c = new Condition(this);
        c.setName("condition" + (id + 1));
        condition.add(c);
        checkAndInvalidateDiagram();
        return c;
    }

    public Condition newCondition(Condition c) {
        int id = condition.stream()
                .filter(condition -> conditionNameRegex.matcher(condition.getName()).matches())
                .mapToInt(condition -> Integer.parseInt(condition.getName().substring(9)))
                .max()
                .orElse(0);

        Condition newCondition = new Condition(c, "condition" + (id + 1), this);
        condition.add(newCondition);
        checkAndInvalidateDiagram();
        return newCondition;
    }

    void addCondition(Condition c) {
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
        checkAndInvalidateDiagram();
    }

    public ObservableList<Condition> getCondition() {
        return unmodifiableCondition;
    }

    public Optional<Condition> getCondition(String name) {
        return condition.stream().filter(c -> c.getName().equals(name)).findFirst();
    }

    public void addLine(NodeElement source, NodeElement destination) {
        // do not create new line if there existed a line with identical source and destination
        if (line.stream().noneMatch(line1 -> (line1.getSource() == source) && (line1.getDestination() == destination))) {
            Line l = new Line(source, destination, this);
            line.add(l);
        }
        checkAndInvalidateDiagram();
    }

    public void removeLine(Line l) {
        line.remove(l);
        checkAndInvalidateDiagram();
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

//    public Map<String, String> getCloudPlatformParameter(CloudPlatform cloudPlatform) {
//        return parameter.get(cloudPlatform);
//    }

    // TODO: need to get again after set
    public String getCloudPlatformParameter(CloudPlatform cloudPlatform, String parameterName) {
        if (parameter.containsKey(cloudPlatform)) {
            return parameter.get(cloudPlatform).get(parameterName);
        } else {
            return null;
        }
    }

    public void setCloudPlatformParameter(CloudPlatform cloudPlatform, String parameterName, String value) {
        if (parameter.containsKey(cloudPlatform)) {
            parameter.get(cloudPlatform).put(parameterName, value);
        } else {
            Map<String, String> parameterMap = new HashMap<>();
            parameterMap.put(parameterName, value);
            parameter.put(cloudPlatform, parameterMap);
        }
    }

    public Set<CloudPlatform> getCloudPlatformUsed() {
        return getAllDeviceUsed().stream().filter(projectDevice -> Objects.nonNull(projectDevice.getActualDevice())
                    && Objects.nonNull(projectDevice.getActualDevice().getCloudPlatform()))
                .map(projectDevice -> projectDevice.getActualDevice().getCloudPlatform())
                .collect(Collectors.toUnmodifiableSet());
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
        for (ProjectDevice projectDevice : device) {
            for (Value v : projectDevice.getGenericDevice().getValue()) {
                value.add(new ProjectValue(projectDevice, v));
            }
        }
        return value;
    }

    public ActualDevice getController() {
        return controller.get();
    }

//    public ObjectProperty<ActualDevice> controllerProperty() {
//        return controller;
//    }

    public void setController(ActualDevice controller) {
        this.controller.set(controller);
        // remove all port and actual device assignment when the controller is changed
        for (ProjectDevice projectDevice : getDevice()) {
            projectDevice.removeAllDeviceConnection();
            projectDevice.setActualDevice(null);
            projectDevice.setAutoSelectDevice(true);
        }
    }

    public Begin getBegin() { return begin; }

    public Set<ProjectDevice> getAllDeviceUsed() {
        Set<ProjectDevice> deviceUsed = new HashSet<>();

        Set<NodeElement> visited = new HashSet<>();
        Queue<NodeElement> queue = new LinkedList<>();
        queue.add(this.begin);
        while(!queue.isEmpty()) {
            NodeElement current = queue.remove();
            if (current instanceof Begin) {
                // No device in Begin Scene
            }
            else if (current instanceof Scene) {
                Scene temp = (Scene) current;
                temp.getSetting().forEach(s->{
                    deviceUsed.add(s.getDevice());
                    deviceUsed.addAll(s.getAllValueUsed().keySet());
                });
            }
            else if (current instanceof Condition) {
                Condition temp = (Condition) current;
                temp.getSetting().forEach(s->{
                    deviceUsed.add(s.getDevice());
                    deviceUsed.addAll(s.getAllValueUsed().keySet());
                });
            }
            visited.add(current);
            Set<NodeElement> unvisitedAdj = line.stream()
                    .filter(l->l.getSource() == current)
                    .map(Line::getDestination)
                    .dropWhile(visited::contains)
                    .collect(Collectors.toSet());
            queue.addAll(unvisitedAdj);
        }
        return deviceUsed;
    }

    public Set<ProjectDevice> getAllDeviceUnused() {
        Set<ProjectDevice> devicesNotUsed = new HashSet<>(this.getDevice());
        devicesNotUsed.removeAll(this.getAllDeviceUsed());
        return devicesNotUsed;
    }

    public Map<ProjectDevice, Set<Value>> getAllValueUsedMap() {
        HashMap<ProjectDevice, Set<Value>> allValueUsed = new HashMap<>();
        Set<NodeElement> visited = new HashSet<>();
        Queue<NodeElement> queue = new LinkedList<>();
        queue.add(this.begin);
        while(!queue.isEmpty()) {
            NodeElement current = queue.remove();
            if (current instanceof Begin) {
                // No Value in Begin Scene
            }
            else if (current instanceof Scene) {
                Scene temp = (Scene) current;
                temp.getSetting().forEach(s-> s.getAllValueUsed().forEach((key, value) -> {
                    allValueUsed.putIfAbsent(key, new HashSet<>());
                    allValueUsed.get(key).addAll(value);
                }));
            }
            else if (current instanceof Condition) {
                Condition temp = (Condition) current;
                temp.getSetting().forEach(s-> s.getAllValueUsed().forEach((key, value) -> {
                    allValueUsed.putIfAbsent(key, new HashSet<>());
                    allValueUsed.get(key).addAll(value);
                }));
            }
            visited.add(current);
            Set<NodeElement> unvisitedAdj = line.stream()
                    .filter(l->l.getSource() == current)
                    .map(Line::getDestination)
                    .dropWhile(visited::contains)
                    .collect(Collectors.toSet());
            queue.addAll(unvisitedAdj);
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
            return !(platform.get() == Platform.ARDUINO_AVR8 && controller.get() == null && device.isEmpty()
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
        for (ProjectDevice projectDevice : this.getDevice()) {
            if (projectDevice.getName().equals(newName)) {
                return true;
            }
        }
        return false;
    }

    private Map<List<Line>, DiagramError> diagramError = Collections.emptyMap();

    private void checkAndInvalidateDiagram() {
        Map<List<Line>, DiagramError> error = new HashMap<>();

        Map<NodeElement, List<Line>> lineFromSource = this.line.stream().collect(Collectors.groupingBy(Line::getSource));

        for (NodeElement nodeElement : lineFromSource.keySet()) {
            List<Line> lines = lineFromSource.get(nodeElement);

            // indicate error if there are lines connect to multiple scenes from any node
            List<Line> lineToScene = lines.stream().filter(line1 -> line1.getDestination() instanceof Scene).collect(Collectors.toList());
            if (lineToScene.size() > 1) {
                error.put(lineToScene, DiagramError.DIAGRAM_MULTIPLE_SCENE);
            }

            // indicate error if the current node is a condition and there is another condition in the list of the adjacent node
            List<Line> lineToCondition = lines.stream().filter(line1 -> line1.getDestination() instanceof Condition).collect(Collectors.toList());
            if ((nodeElement instanceof Condition) && !lineToCondition.isEmpty()) {
                error.put(lineToCondition, DiagramError.DIAGRAM_CHAIN_CONDITION);
            }

            // indicate error if the current node is a scene/begin and there are both scene and condition connect to it
            if (!(nodeElement instanceof Condition) && (!lineToScene.isEmpty() && !lineToCondition.isEmpty())) {
                error.put(lineToCondition, DiagramError.DIAGRAM_CONDITION_IGNORE);
            }
        }

        diagramError =  Collections.unmodifiableMap(error);

        // invalidate every lines
        line.forEach(Line::invalidate);
    }

    public Map<List<Line>, DiagramError> getDiagramStatus() {
        return diagramError;
    }
}
