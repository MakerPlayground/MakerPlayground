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
import io.makerplayground.device.actual.Platform;
import io.makerplayground.device.actual.Property;
import io.makerplayground.device.generic.GenericDevice;
import io.makerplayground.device.shared.DataType;
import io.makerplayground.device.shared.Value;
import io.makerplayground.generator.devicemapping.ProjectConfiguration;
import io.makerplayground.generator.devicemapping.ProjectConfigurationLogic;
import io.makerplayground.version.ProjectVersionControl;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Represent a project
 */
@JsonSerialize(using = ProjectSerializer.class)
@JsonDeserialize(using = ProjectDeserializer.class)
public class Project {
    private StringProperty projectName;
    private final ObservableList<ProjectDevice> device;
    private final ObservableList<Scene> scene;
    private final ObservableList<Condition> condition;
    private final ObservableList<Line> line;
    private final ObservableList<Begin> begins;

    @Getter private final FilteredList<ProjectDevice> sensorDevice;
    @Getter private final FilteredList<ProjectDevice> actuatorDevice;
    @Getter private final FilteredList<ProjectDevice> utilityDevice;
    @Getter private final FilteredList<ProjectDevice> cloudDevice;
    @Getter private final FilteredList<ProjectDevice> interfaceDevice;
    @Getter private final FilteredList<ProjectDevice> deviceWithAction;
    @Getter private final FilteredList<ProjectDevice> deviceWithCondition;

    @Getter private final ObservableList<ProjectDevice> unmodifiableProjectDevice;
    @Getter private final ObservableList<Scene> unmodifiableScene;
    @Getter private final ObservableList<Condition> unmodifiableCondition;
    @Getter private final ObservableList<Line> unmodifiableLine;

    private final StringProperty filePath;
    private static final Pattern sceneNameRegex = Pattern.compile("Scene\\d+");
    private static final Pattern beginNameRegex = Pattern.compile("Begin\\d+");
    private static final Pattern conditionNameRegex = Pattern.compile("condition\\d+");

    @Getter @Setter private ProjectConfiguration projectConfiguration;

    public Project() {
        projectName = new SimpleStringProperty("Untitled Project");

        device = FXCollections.observableArrayList();
        unmodifiableProjectDevice = FXCollections.unmodifiableObservableList(device);
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
        begins = FXCollections.observableArrayList();

        filePath = new SimpleStringProperty("");

        unmodifiableScene = FXCollections.unmodifiableObservableList(scene);
        unmodifiableCondition = FXCollections.unmodifiableObservableList(condition);
        unmodifiableLine = FXCollections.unmodifiableObservableList(line);

        projectConfiguration = ProjectConfigurationLogic.newConfiguration();
        newBegin();
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

    public Platform getSelectedPlatform() {
        return projectConfiguration.getPlatform();
    }

    private String getDeviceVarName(GenericDevice device) {
        return device.getName().replaceAll("[() ]", "");
    }

    private int getNextId(GenericDevice device) {
        String varName = getDeviceVarName(device);
        Pattern p = Pattern.compile(varName+"\\d+");
        return getUnmodifiableProjectDevice().stream()
                .filter(projectDevice -> p.matcher(projectDevice.getName()).matches())
                .mapToInt(value -> Integer.parseInt(value.getName().substring(varName.length())))
                .max()
                .orElse(0) + 1;
    }

    void addDevice(ProjectDevice projectDevice) {
        device.add(projectDevice);
    }

    public ProjectDevice addDevice(GenericDevice genericDevice) {
        String varName = getDeviceVarName(genericDevice);
        ProjectDevice projectDevice = new ProjectDevice(varName + getNextId(genericDevice), genericDevice, this);
        device.add(projectDevice);
        return projectDevice;
    }

    public void removeDevice(ProjectDevice pd) {
        scene.forEach(s->s.removeDevice(pd));
        condition.forEach(c->c.removeDevice(pd));
        if (!device.remove(pd)) {
            throw new IllegalStateException("");
        }
        // update other devices that share the actual device with the removed device
        for (ProjectDevice projectDevice : device) {
            if (projectDevice.getParentDevice() == pd) {
                projectDevice.setParentDevice(null);
            }
        }
    }

    public void setPlatform(Platform platform) {
        this.projectConfiguration = ProjectConfigurationLogic.changePlatform(projectConfiguration, platform);
    }

    public Optional<Scene> getUnmodifiableScene(String name) {
        return unmodifiableScene.stream().filter(s -> s.getName().equals(name)).findFirst();
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

    public Optional<Condition> getUnmodifiableCondition(String name) {
        return condition.stream().filter(c -> c.getName().equals(name)).findFirst();
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

    public boolean hasLine(NodeElement source, NodeElement destination) {
        return line.stream().anyMatch(line1 -> (line1.getSource() == source) && (line1.getDestination() == destination));
    }

    // TODO: need to get again after set
    public String getCloudPlatformParameter(CloudPlatform cloudPlatform, String parameterName) {
        var parameter = projectConfiguration.getUnmodifiableCloudParameterMap();
        if (parameter.containsKey(cloudPlatform)) {
            return parameter.get(cloudPlatform).get(parameterName);
        } else {
            return null;
        }
    }

    public void setCloudPlatformParameter(CloudPlatform cloudPlatform, String parameterName, String value) {
        projectConfiguration = ProjectConfigurationLogic.changeCloudPlatformParameter(projectConfiguration, cloudPlatform, parameterName, value);
    }

    public Set<CloudPlatform> getCloudPlatformUsed() {
        return getAllDeviceUsed().stream()
                .filter(projectDevice -> projectConfiguration.isActualDeviceSelected(projectDevice))
                .filter(projectDevice -> projectConfiguration.getCloudConsume(projectDevice).isPresent())
                .map(projectDevice -> projectConfiguration.getCloudConsume(projectDevice).orElseThrow())
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

    public List<ProjectValue> getAvailableValue(Set<DataType> dataType) {
        List<ProjectValue> value = new ArrayList<>();
        for (ProjectDevice projectDevice : device) {
            for (Value v : projectDevice.getGenericDevice().getValue()) {
                if (dataType.contains(v.getType())) {
                    value.add(new ProjectValue(projectDevice, v));
                }
            }
        }
        return value;
    }

    public ActualDevice getSelectedController() {
        return projectConfiguration.getController();
    }

    public void setController(ActualDevice controller) {
        projectConfiguration = ProjectConfigurationLogic.changeController(projectConfiguration, controller);
    }

    public ObservableList<Begin> getBegin() { return begins; }

    public Set<ProjectDevice> getAllDeviceUsed() {
        Set<ProjectDevice> deviceUsed = new HashSet<>();
        Set<NodeElement> visited = new HashSet<>();
        Deque<NodeElement> queue = new ArrayDeque<>(this.begins);
        while(!queue.isEmpty()) {
            NodeElement current = queue.remove();
            if (current instanceof Begin) {
                // No device in Begin Scene
            }
            else if (current instanceof Scene) {
                Scene temp = (Scene) current;
                temp.getSetting().forEach(s->{
                    deviceUsed.add(s.getDevice());
                    deviceUsed.addAll(s.getAllValueUsed(EnumSet.allOf(DataType.class)).keySet());
                });
            }
            else if (current instanceof Condition) {
                Condition temp = (Condition) current;
                temp.getSetting().forEach(s->{
                    deviceUsed.add(s.getDevice());
                    deviceUsed.addAll(s.getAllValueUsed(EnumSet.allOf(DataType.class)).keySet());
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
        Set<ProjectDevice> devicesNotUsed = new HashSet<>(this.getUnmodifiableProjectDevice());
        devicesNotUsed.removeAll(this.getAllDeviceUsed());
        return devicesNotUsed;
    }

    public Map<ProjectDevice, Set<Value>> getAllValueUsedMap(Set<DataType> dataType) {
        HashMap<ProjectDevice, Set<Value>> allValueUsed = new HashMap<>();
        Set<NodeElement> visited = new HashSet<>();
        Queue<NodeElement> queue = new LinkedList<>(this.begins);
        while(!queue.isEmpty()) {
            NodeElement current = queue.remove();
            if (current instanceof Begin) {
                // No Value in Begin Scene
            }
            else if (current instanceof Scene) {
                Scene temp = (Scene) current;
                temp.getSetting().forEach(s-> s.getAllValueUsed(dataType).forEach((key, value) -> {
                    allValueUsed.putIfAbsent(key, new HashSet<>());
                    allValueUsed.get(key).addAll(value);
                }));
            }
            else if (current instanceof Condition) {
                Condition temp = (Condition) current;
                temp.getSetting().forEach(s-> s.getAllValueUsed(dataType).forEach((key, value) -> {
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
            int beginCount = begins.size();
            Begin firstBegin = null;
            if(!begins.isEmpty()) {
                firstBegin = begins.get(0);
            }
            return !(projectConfiguration.getPlatform() == Platform.ARDUINO_AVR8
                    && projectConfiguration.getController() == null
                    && device.isEmpty()
                    && scene.isEmpty()
                    && condition.isEmpty()
                    && line.isEmpty()
                    && beginCount == 1
                    && firstBegin != null
                    && firstBegin.getTop() == 200
                    && firstBegin.getLeft() == 20); // begin hasn't been moved
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

    public static Optional<Project> loadProject(File f) {
        if (f.exists()) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                String projectVersion = ProjectVersionControl.readProjectVersion(f);
                if (ProjectVersionControl.canOpen(projectVersion)) {
                    Project p = mapper.readValue(f, Project.class);
                    p.setFilePath(f.getAbsolutePath());
                    return Optional.of(p);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return Optional.empty();
    }

    public boolean isNameDuplicate(String newName) {
        for (ProjectDevice projectDevice : this.getUnmodifiableProjectDevice()) {
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

        // Reassign root to all scene and conditions
        getUnmodifiableScene().forEach(Scene::clearRoot);
        getUnmodifiableCondition().forEach(Condition::clearRoot);
        getBegin().forEach(this::traverseAndSetRoot);

        Map<NodeElement, List<Line>> lineFromDest = this.line.stream().collect(Collectors.groupingBy(Line::getDestination));
        for (NodeElement nodeElement : lineFromDest.keySet()) {
            List<Line> lines = lineFromDest.get(nodeElement);

            if (nodeElement instanceof Scene && ((Scene) nodeElement).getRoots().size() > 1) {
                error.put(lines, DiagramError.DIAGRAM_MULTIPLE_BEGIN);
            } else if (nodeElement instanceof Condition && ((Condition) nodeElement).getRoots().size() > 1) {
                error.put(lines, DiagramError.DIAGRAM_MULTIPLE_BEGIN);
            }
        }

        diagramError =  Collections.unmodifiableMap(error);

        // invalidate every lines
        line.forEach(Line::invalidate);
    }

    private Set<NodeElement> getNextNodeElements(NodeElement from) {
        return getUnmodifiableLine().stream().filter(line1 -> line1.getSource() == from).map(Line::getDestination).collect(Collectors.toSet());
    }

    private void traverseAndSetRoot(NodeElement from) {
        Set<NodeElement> visited = new HashSet<>();
        Deque<NodeElement> remainingNodes = new ArrayDeque<>(getNextNodeElements(from));
        while(!remainingNodes.isEmpty()) {
            NodeElement node = remainingNodes.removeFirst();
            if (visited.contains(node)) {
                continue;
            }
            visited.add(node);

            if (node instanceof Scene) {
                ((Scene) node).addRoot(from);
            } else if (node instanceof Condition) {
                ((Condition) node).addRoot(from);
            }

            Set<NodeElement> nextNodes = getNextNodeElements(node);
            nextNodes.removeAll(visited);
            remainingNodes.addAll(nextNodes);
        }
    }

    public Map<List<Line>, DiagramError> getDiagramStatus() {
        return diagramError;
    }

    public void removeBegin(Begin begin) {
        if (begins.size() > 1) {
            begins.remove(begin);
            for (int i=line.size()-1; i>=0; i--) {
                Line l = line.get(i);
                if (l.getSource() == begin || l.getDestination() == begin) {
                    line.remove(l);
                }
            }
            checkAndInvalidateDiagram();
        }
    }

    public Begin newBegin() {
        int id = begins.stream()
                .filter(begin1 -> beginNameRegex.matcher(begin1.getName()).matches())
                .mapToInt(begin1 -> Integer.parseInt(begin1.getName().substring(5)))
                .max()
                .orElse(0);

        Begin begin = new Begin(this);
        begin.setName("Begin" + (id + 1));
        begins.add(begin);
        checkAndInvalidateDiagram();
        return begin;
    }

    public Optional<Begin> getBegin(String name) {
        return begins.stream().filter(b -> name.equals(b.getName())).findFirst();
    }

    public void addBegin(Begin begin) {
        begins.add(begin);
    }

    public boolean isActualDeviceSelected(ProjectDevice projectDevice) {
        return this.getProjectConfiguration().isActualDeviceSelected(projectDevice);
    }

    public Optional<ActualDevice> getActualDevice(ProjectDevice projectDevice) {
        return this.getProjectConfiguration().getActualDevice(projectDevice);
    }

    public Object getPropertyValue(ProjectDevice projectDevice, Property property) {
        return this.getProjectConfiguration().getPropertyValue(projectDevice, property);
    }

    public boolean isUsedSameDevice(ProjectDevice projectDevice) {
        return this.getProjectConfiguration().isUsedSameDevice(projectDevice);
    }

    public Optional<ProjectDevice> getParentDevice(ProjectDevice projectDevice) {
        return this.getProjectConfiguration().getParentDevice(projectDevice);
    }

    void setConfiguration(ProjectConfiguration readValueAs) {

    }
}
