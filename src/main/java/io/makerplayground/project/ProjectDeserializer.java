package io.makerplayground.project;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.makerplayground.device.Device;
import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.device.GenericDevice;
import io.makerplayground.helper.Platform;
import io.makerplayground.helper.Unit;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.util.Arrays;

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
        Project project = null;
        String projectName = node.get("projectName").asText();

        //TODO: add JsonNode for projectController

        ObservableList<ProjectDevice> inputDevices = FXCollections.observableArrayList();
        for (JsonNode inputDeviceNode : node.get("inputDevice")) {
            ProjectDevice inputDevice = null;
            String name = inputDeviceNode.get("name").asText();     //TODO: fix later
            GenericDevice genericDevice = DeviceLibrary.INSTANCE.getGenericDevice(name);

            for (JsonNode actualDeviceNode : node.get("actualDevice")) {
                String brand = actualDeviceNode.get("brand").asText();
                String model = actualDeviceNode.get("model").asText();
                //TODO: load actualDevice to project
            }
            project.addInputDevice(genericDevice);
        }

        ObservableList<ProjectDevice> outputDevices = FXCollections.observableArrayList();
        for (JsonNode outputDeviceNode : node.get("outputDevice")) {
            ProjectDevice outputDevice = null;
            String name = outputDeviceNode.get("name").asText();    //TODO: fix later
            GenericDevice genericDevice = DeviceLibrary.INSTANCE.getGenericDevice(name);

            for (JsonNode actualDeviceNode : node.get("actualDevice")) {
                String brand = actualDeviceNode.get("brand").asText();
                String model = actualDeviceNode.get("model").asText();
                //TODO: load actualDevice to project
            }
            project.addOutputDevice(genericDevice);
        }

        ObservableList<Scene> scenes = FXCollections.observableArrayList();
        for (JsonNode sceneNode : node.get("scene")) {
            Scene scene = null;

            String name = sceneNode.get("name").asText();
            scene.setName(name);
            for (JsonNode sceneSettingNode : node.get("userSetting")) {
                UserSetting userSetting = null;
                String device = sceneSettingNode.get("device").asText();
                userSetting.getDevice().setName(device);
                //TODO: load UserSetting to project
            }
            scene.setDelay(sceneNode.get("delay").asDouble());
            Unit delayUnit = Unit.valueOf(sceneNode.get("delayUnit").asText());
            //TODO: load delayUnit
            JsonNode positionNode = sceneNode.get("position");
            scene.setTop(positionNode.get("top").asDouble());
            scene.setLeft(positionNode.get("left").asDouble());
            scene.setWidth(positionNode.get("width").asDouble());
            scene.setHeight(positionNode.get("height").asDouble());
            //TODO: add scene to project
            scenes.add(scene);
        }

        ObservableList<Condition> conditions = FXCollections.observableArrayList();
        for(JsonNode conditionNode : node.get("condition")) {
            Condition condition = null;

            for ( JsonNode conditionSettingNode : node.get("setting")) {
                UserSetting userSetting = null;
                String device = conditionSettingNode.get("device").asText();
                userSetting.getDevice().setName(device);
                //TODO: load UserSetting to project
            }
            //TODO: SourceNode and DestNode
            JsonNode positionNode = conditionNode.get("position");
            condition.setTop(positionNode.get("top").asDouble());
            condition.setLeft(positionNode.get("left").asDouble());
            condition.setWidth(positionNode.get("width").asDouble());
            condition.setHeight(positionNode.get("height").asDouble());

            conditions.add(condition);
        }

        ObservableList<Line> lines = FXCollections.observableArrayList();
        for (JsonNode lineNode : node.get("line")) {
            Line line = null;
            //TODO: load Line

            lines.add(line);
        }

        return null;    //TODO: fix later
    }


}
