package io.makerplayground.project;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.makerplayground.device.Device;
import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.helper.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;

/**
 * Created by USER on 14-Jul-17.
 */
public class ProjectDeserializer extends StdDeserializer<Project> {
    public ProjectDeserializer() {
        this(null);
    }

    public ProjectDeserializer(Class<Project> t) {
        super(t);
    }

    @Override
    public Project deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        String projectName = node.get("projectName").asText();
        Platform platform = Platform.valueOf(node.get("controller").get("platform").asText());
        Device controller = null;
        if (!node.get("controller").get("device").asText().isEmpty()) {
            controller = DeviceLibrary.INSTANCE.getActualDevice().stream().filter(
                    device -> device.getId().equals(node.get("controller").get("device").asText())).findFirst().get();
        }
        ProjectController projectController = new ProjectController(platform, controller);

        ObservableList<ProjectDevice> inputDevices = FXCollections.observableArrayList();
        for (JsonNode inputDeviceNode : node.get("inputDevice")) {
            inputDevices.add(mapper.treeToValue(inputDeviceNode, ProjectDevice.class));
        }

        ObservableList<ProjectDevice> outputDevices = FXCollections.observableArrayList();
        for (JsonNode outputDeviceNode : node.get("outputDevice")) {
            outputDevices.add(mapper.treeToValue(outputDeviceNode, ProjectDevice.class));
        }

        SceneDeserializer sceneDeserializer = new SceneDeserializer(inputDevices, outputDevices);
        ObservableList<Scene> scenes = FXCollections.observableArrayList();
        for (JsonNode sceneNode : node.get("scene")) {
            scenes.add(sceneDeserializer.deserialize(sceneNode.traverse(), deserializationContext));
        }

        ConditionDeserializer conditionDeserializer = new ConditionDeserializer(inputDevices, outputDevices, scenes);
        ObservableList<Condition> conditions = FXCollections.observableArrayList();
        for(JsonNode conditionNode : node.get("condition")) {
            conditions.add(conditionDeserializer.deserialize(conditionNode.traverse(), deserializationContext));
        }

        ObservableList<Line> lines = FXCollections.observableArrayList();
        for (JsonNode lineNode : node.get("line")) {
            NodeElement source = null;
            for (Scene s : scenes) {
                if (s.getName().equals(lineNode.get("source"))) {
                    source = s;
                }
            }
            for (Condition c : conditions) {
                if (c.getName().equals(lineNode.get("source"))) {
                    source = c;
                }
            }

            NodeElement dest = null;
            for (Scene s : scenes) {
                if (s.getName().equals(lineNode.get("destination"))) {
                    dest = s;
                }
            }
            for (Condition c : conditions) {
                if (c.getName().equals(lineNode.get("destination"))) {
                    dest = c;
                }
            }

            lines.add(new Line(source, dest));
        }

        Begin begin = new Begin(node.get("begin").get("top").asDouble()
                , node.get("begin").get("left").asDouble());

        return new Project(projectName, projectController, inputDevices, outputDevices, scenes, conditions, lines, begin);
    }


}
