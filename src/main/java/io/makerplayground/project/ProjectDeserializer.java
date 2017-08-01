package io.makerplayground.project;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.makerplayground.device.*;
import io.makerplayground.helper.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        Begin begin = new Begin(node.get("begin").get("top").asDouble()
                , node.get("begin").get("left").asDouble());

        ObservableList<Scene> scenes = FXCollections.observableArrayList();
        for (JsonNode sceneNode : node.get("scene")) {
            scenes.add(deserializeScene(mapper, sceneNode, outputDevices));
        }

        ObservableList<Condition> conditions = FXCollections.observableArrayList();
        for(JsonNode conditionNode : node.get("condition")) {
            conditions.add(deserializeCondition(mapper, conditionNode, inputDevices));
        }

        ObservableList<Line> lines = FXCollections.observableArrayList();
        for (JsonNode lineNode : node.get("line")) {
            NodeElement source = null;
            if (lineNode.get("source").asText().equals("begin")) {  // TODO: hard code
                source = begin;
            } else {
                for (Scene s : scenes) {
                    if (s.getName().equals(lineNode.get("source").asText())) {
                        source = s;
                    }
                }
                for (Condition c : conditions) {
                    if (c.getName().equals(lineNode.get("source").asText())) {
                        source = c;
                    }
                }
            }

            NodeElement dest = null;
            for (Scene s : scenes) {
                if (s.getName().equals(lineNode.get("destination").asText())) {
                    dest = s;
                }
            }
            for (Condition c : conditions) {
                if (c.getName().equals(lineNode.get("destination").asText())) {
                    dest = c;
                }
            }

            lines.add(new Line(source, dest));
        }

        return new Project(projectName, projectController, inputDevices, outputDevices, scenes, conditions, lines, begin);
    }

    public Scene deserializeScene(ObjectMapper mapper, JsonNode node, ObservableList<ProjectDevice> outputDevice) throws IOException {
        String name = node.get("name").asText();

        List<UserSetting> setting = new ArrayList<>();
        for (JsonNode sceneSettingNode : node.get("setting")) {
            ProjectDevice projectDevice = outputDevice.stream().filter(projectDevice1 ->
                    projectDevice1.getName().equals(sceneSettingNode.get("device").asText())).findFirst().get();
            setting.add(deserializeUserSetting(mapper, sceneSettingNode, projectDevice));
        }

        double delay = node.get("delay").asDouble();
        Scene.DelayUnit delayUnit = Scene.DelayUnit.valueOf(node.get("delayUnit").asText());

        double top = node.get("position").get("top").asDouble();
        double left = node.get("position").get("left").asDouble();
        double width = node.get("position").get("width").asDouble();
        double height = node.get("position").get("height").asDouble();

        return new Scene(top, left, width, height, name, setting, delay, delayUnit);
    }

    public Condition deserializeCondition(ObjectMapper mapper, JsonNode node, ObservableList<ProjectDevice> inputDevice) throws IOException {
        String name = node.get("name").asText();

        List<UserSetting> setting = new ArrayList<>();
        for (JsonNode conditionSettingNode : node.get("setting")) {
            ProjectDevice projectDevice = inputDevice.stream().filter(projectDevice1 ->
                    projectDevice1.getName().equals(conditionSettingNode.get("device").asText())).findFirst().get();
            setting.add(deserializeUserSetting(mapper, conditionSettingNode, projectDevice));
        }

        double top = node.get("position").get("top").asDouble();
        double left = node.get("position").get("left").asDouble();
        double width = node.get("position").get("width").asDouble();
        double height = node.get("position").get("height").asDouble();

        return new Condition(top, left, width, height, name, setting);
    }

    public UserSetting deserializeUserSetting(ObjectMapper mapper, JsonNode node, ProjectDevice projectDevice) throws IOException {
        Action action = projectDevice.getGenericDevice().getAction(node.get("action").asText());

        Map<Parameter, Object> valueMap = new HashMap<>();
        for (JsonNode parameterNode : node.get("valueMap")) {
            Parameter parameter = action.getParameter(parameterNode.get("name").asText());
            Object object = null;
            if (parameterNode.get("value").isObject()) {
                object = mapper.treeToValue(parameterNode.get("value"), NumberWithUnit.class);
            } else {
                object = parameterNode.get("value").asText();
            }
            valueMap.put(parameter, object);
        }

        Map<Value, ObservableList<Expression>> expressionMap = new HashMap<>();
        for (JsonNode valueNode : node.get("expression")) {
            Value value = projectDevice.getGenericDevice().getValue(valueNode.get("name").asText());
            ObservableList<Expression> expressionList = FXCollections.observableArrayList();
            for (JsonNode expressionNode : valueNode.get("expression")) {
                expressionList.add(mapper.treeToValue(expressionNode, Expression.class));
            }
        }

        return new UserSetting(projectDevice, action, valueMap, expressionMap);
    }

    public Expression deserializeExpression(ObjectMapper mapper, JsonNode node, ObservableList<ProjectDevice> inputDevice, ObservableList<ProjectDevice> outputDevice) throws IOException, JsonProcessingException {
        Unit unit = Unit.valueOf(node.get("unit").asText());
        Operator operator = Operator.valueOf(node.get("operator").asText());

        Object firstOperand;
        if (node.get("firstOperand").isObject()) {
            firstOperand = deserializeProjectValue(mapper, node.get("firstOperand"), inputDevice, outputDevice);
        } else {
            firstOperand = node.get("firstOperand").asDouble();
        }

        Object secondOperand;
        if (node.get("secondOperand").isObject()) {
            secondOperand = deserializeProjectValue(mapper, node.get("secondOperand"), inputDevice, outputDevice);
        } else {
            secondOperand = node.get("secondOperand").asDouble();
        }

        OperandType operandType = OperandType.valueOf(node.get("operandType").asText());

        return new Expression(unit, operator, firstOperand, secondOperand, operandType);
    }

    public ProjectValue deserializeProjectValue(ObjectMapper mapper, JsonNode node, ObservableList<ProjectDevice> inputDevice, ObservableList<ProjectDevice> outputDevice) throws IOException, JsonProcessingException {
        String deviceName = node.get("device").asText();
        ProjectDevice device = null;
        for (ProjectDevice projectDevice : inputDevice) {
            if (deviceName.equals(projectDevice.getName()))
                device = projectDevice;
        }
        for (ProjectDevice projectDevice : outputDevice) {
            if (deviceName.equals(projectDevice.getName()))
                device = projectDevice;
        }

        Value value = device.getGenericDevice().getValue(node.get("value").asText());

        return new ProjectValue(device, value);
    }

}
