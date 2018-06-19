package io.makerplayground.project;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.makerplayground.device.*;
import io.makerplayground.helper.NumberWithUnit;
import io.makerplayground.helper.Peripheral;
import io.makerplayground.helper.Platform;
import io.makerplayground.helper.Unit;
import io.makerplayground.project.chip.*;
import io.makerplayground.project.expression.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

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

        ObservableList<ProjectDevice> inputDevices = FXCollections.observableArrayList();
        for (JsonNode inputDeviceNode : node.get("inputDevice")) {
            //inputDevices.add(mapper.treeToValue(inputDeviceNode, ProjectDevice.class));
            inputDevices.add(deserializeProjectDevice(mapper, inputDeviceNode, controller));
        }

        ObservableList<ProjectDevice> outputDevices = FXCollections.observableArrayList();
        for (JsonNode outputDeviceNode : node.get("outputDevice")) {
            //outputDevices.add(mapper.treeToValue(outputDeviceNode, ProjectDevice.class));
            outputDevices.add(deserializeProjectDevice(mapper, outputDeviceNode, controller));
        }

        ObservableList<ProjectDevice> connectivityDevices = FXCollections.observableArrayList();
        for (JsonNode connectivityDeviceNode : node.get("connectivityDevice")) {
            connectivityDevices.add(deserializeProjectDevice(mapper, connectivityDeviceNode, controller));
        }

        Begin begin = new Begin(node.get("begin").get("top").asDouble()
                , node.get("begin").get("left").asDouble());

        String filePath = null;

        ObservableList<Scene> scenes = FXCollections.observableArrayList();
        for (JsonNode sceneNode : node.get("scene")) {
            scenes.add(deserializeScene(mapper, sceneNode, inputDevices,  outputDevices, connectivityDevices));
        }

        ObservableList<Condition> conditions = FXCollections.observableArrayList();
        for(JsonNode conditionNode : node.get("condition")) {
            conditions.add(deserializeCondition(mapper, conditionNode, inputDevices, outputDevices, connectivityDevices));
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

        return new Project(projectName, controller, platform, inputDevices, outputDevices, connectivityDevices, scenes, conditions, lines, begin, filePath);
    }

    public Scene deserializeScene(ObjectMapper mapper, JsonNode node, ObservableList<ProjectDevice> inputDevice
            , ObservableList<ProjectDevice> outputDevice, ObservableList<ProjectDevice> connectivityDevices) throws IOException {
        String name = node.get("name").asText();

        List<UserSetting> setting = new ArrayList<>();
        for (JsonNode sceneSettingNode : node.get("setting")) {
            ProjectDevice projectDevice = Stream.concat(outputDevice.stream(), connectivityDevices.stream()).filter(projectDevice1 ->
                    projectDevice1.getName().equals(sceneSettingNode.get("device").asText())).findFirst().get();
            setting.add(deserializeUserSetting(mapper, sceneSettingNode, projectDevice, inputDevice, outputDevice, connectivityDevices));
        }

        double delay = node.get("delay").asDouble();
        Scene.DelayUnit delayUnit = Scene.DelayUnit.valueOf(node.get("delayUnit").asText());

        double top = node.get("position").get("top").asDouble();
        double left = node.get("position").get("left").asDouble();
        double width = node.get("position").get("width").asDouble();
        double height = node.get("position").get("height").asDouble();

        return new Scene(top, left, width, height, name, setting, delay, delayUnit);
    }

    public Condition deserializeCondition(ObjectMapper mapper, JsonNode node, ObservableList<ProjectDevice> inputDevice
            , ObservableList<ProjectDevice> outputDevice, ObservableList<ProjectDevice> connectivityDevices) throws IOException {
        String name = node.get("name").asText();

        List<UserSetting> setting = new ArrayList<>();
        for (JsonNode conditionSettingNode : node.get("setting")) {
            ProjectDevice projectDevice = Stream.concat(inputDevice.stream(), connectivityDevices.stream()).filter(projectDevice1 ->
                    projectDevice1.getName().equals(conditionSettingNode.get("device").asText())).findFirst().get();
            setting.add(deserializeUserSetting(mapper, conditionSettingNode, projectDevice, inputDevice, outputDevice, connectivityDevices));
        }

        double top = node.get("position").get("top").asDouble();
        double left = node.get("position").get("left").asDouble();
        double width = node.get("position").get("width").asDouble();
        double height = node.get("position").get("height").asDouble();

        return new Condition(top, left, width, height, name, setting);
    }

    public UserSetting deserializeUserSetting(ObjectMapper mapper, JsonNode node, ProjectDevice projectDevice
            , ObservableList<ProjectDevice> inputDevice, ObservableList<ProjectDevice> outputDevice
            , ObservableList<ProjectDevice> connectivityDevices) throws IOException {
        Action action = projectDevice.getGenericDevice().getAction(node.get("action").asText());
        // TODO: find a better way
        if (action == null) {
            action = projectDevice.getGenericDevice().getCondition(node.get("action").asText());
        }

        Map<Parameter, Expression> valueMap = new HashMap<>();
        for (JsonNode parameterNode : node.get("valueMap")) {
            Parameter parameter = action.getParameter(parameterNode.get("name").asText());
            Expression object = null;
            String expressionType = parameterNode.get("type").asText();
            if (ProjectValueExpression.class.getName().equals(expressionType)) {
                object = mapper.treeToValue(parameterNode.get("value"), ProjectValueExpression.class);
            } else if (ConstantExpression.class.getName().equals(expressionType)) {
                object = mapper.treeToValue(parameterNode.get("value"), ConstantExpression.class);
            } else if (CustomNumberExpression.class.getName().equals(expressionType)) {
                object = mapper.treeToValue(parameterNode.get("value"), CustomNumberExpression.class);
            } else if (NumberWithUnitExpression.class.getName().equals(expressionType)) {
                object = mapper.treeToValue(parameterNode.get("value"), NumberWithUnitExpression.class);
            } else if (SimpleStringExpression.class.getName().equals(expressionType)) {
                object = mapper.treeToValue(parameterNode.get("value"), SimpleStringExpression.class);
            }
            valueMap.put(parameter, object);
        }

        Map<Value, Expression> expressionMap = new HashMap<>();
        for (JsonNode valueNode : node.get("expression")) {
            Value value = projectDevice.getGenericDevice().getValue(valueNode.get("name").asText());
            boolean enable = valueNode.get("enable").asBoolean();
            String type = valueNode.get("type").asText();

            Expression expression;
            if (type.equals("simple")) {
                expression = deserializeSimpleExpression(mapper, valueNode.get("expression"), projectDevice, value);
                expression.setEnable(enable);
            } else {
                throw new IllegalStateException("Unknown expression type");
            }

            expressionMap.put(value, expression);
        }

        return new UserSetting(projectDevice, action, valueMap, expressionMap);
    }

    public Expression deserializeSimpleExpression(ObjectMapper mapper, JsonNode node, ProjectDevice device, Value value) throws IOException, JsonProcessingException {
        NumberInRangeExpression simpleExpression = new NumberInRangeExpression(device, value);
        if (node.get(0).get("type").asText().equals(ChipType.VALUE.name())
            && node.get(1).get("type").asText().equals(ChipType.OPERATOR.name())
                && node.get(1).get("value").asText().equals(ChipOperator.LESS_THAN.name())
                && node.get(2).get("type").asText().equals(ChipType.NUMBER.name())
                && node.get(3).get("type").asText().equals(ChipType.OPERATOR.name())
                && node.get(3).get("value").asText().equals(ChipOperator.AND.name())
                && node.get(4).get("type").asText().equals(ChipType.VALUE.name())
                && node.get(5).get("type").asText().equals(ChipType.OPERATOR.name())
                && node.get(5).get("value").asText().equals(ChipOperator.GREATER_THAN.name())
                && node.get(6).get("type").asText().equals(ChipType.NUMBER.name())) {
            simpleExpression.setHighValue(node.get(2).get("value").get("value").asDouble());
            simpleExpression.setLowValue(node.get(6).get("value").get("value").asDouble());
        } else {
            throw new IllegalStateException("Simple expression parsing fail");
        }
        return simpleExpression;
    }

    public Expression deserializeExpression(ObjectMapper mapper, JsonNode node, ObservableList<ProjectDevice> inputDevice
            , ObservableList<ProjectDevice> outputDevice, ObservableList<ProjectDevice> connectivityDevices) throws IOException, JsonProcessingException {
        Expression expression = new Expression();

        for (JsonNode termNode : node) {
            Term term = null;

            ChipType type = ChipType.valueOf(termNode.get("type").asText());
            if (type == ChipType.NUMBER) {
                term = new NumberWithUnitTerm(new NumberWithUnit(termNode.get("value").get("value").asDouble()
                        , Unit.valueOf(termNode.get("value").get("unit").asText())));
            } else if (type == ChipType.STRING) {
                term = new StringTerm(termNode.get("value").asText());
            } else if (type == ChipType.OPERATOR) {
                term = new OperatorTerm(ChipOperator.valueOf(termNode.get("value").asText()));
            } else if (type == ChipType.VALUE) {
                ProjectValue pv = deserializeProjectValue(mapper, termNode.get("value"), inputDevice, outputDevice, connectivityDevices);
                term = new ValueTerm(pv);
            } else {
                throw new IllegalStateException("Found unknown term " + type);
            }

            expression.getTerms().add(term);
        }

        return expression;
    }

    public ProjectValue deserializeProjectValue(ObjectMapper mapper, JsonNode node, ObservableList<ProjectDevice> inputDevice
            , ObservableList<ProjectDevice> outputDevice, ObservableList<ProjectDevice> connectivityDevices) throws IOException, JsonProcessingException {
        String deviceName = node.get("name").asText();
        ProjectDevice device = null;
        for (ProjectDevice projectDevice : inputDevice) {
            if (deviceName.equals(projectDevice.getName()))
                device = projectDevice;
        }
        for (ProjectDevice projectDevice : outputDevice) {
            if (deviceName.equals(projectDevice.getName()))
                device = projectDevice;
        }
        for (ProjectDevice projectDevice : connectivityDevices) {
            if (deviceName.equals(projectDevice.getName()))
                device = projectDevice;
        }

        Value value = device.getGenericDevice().getValue(node.get("value").asText());

        return new ProjectValue(device, value);
    }

    public ProjectDevice deserializeProjectDevice(ObjectMapper mapper, JsonNode node, Device controller) {
        String name = node.get("name").asText();
        GenericDevice genericDevice = DeviceLibrary.INSTANCE.getGenericDevice(node.get("genericDevice").asText());
        boolean autoSelect = node.get("autoselect").asBoolean();

        String actualDeviceId = node.get("actualDevice").asText();
        Device actualDevice = null;
        if (!actualDeviceId.isEmpty()) {
            actualDevice = DeviceLibrary.INSTANCE.getActualDevice(actualDeviceId);
        }

        Map<Peripheral, List<DevicePort>> actualDeviceConnection = new HashMap<>();
        for  (JsonNode connection : node.get("actualDeviceConnection")) {
            Peripheral source = Peripheral.valueOf(connection.get("devicePeripheral").asText());
            //Peripheral dest = Peripheral.valueOf(connection.get("controllerPeripheral").asText());
            List<DevicePort> port = new ArrayList<>();
            for (JsonNode controllerPeripheral : connection.get("controllerPeripheral")) {
                String portName = controllerPeripheral.asText();
                port.add(controller.getPort(portName));
            }
            actualDeviceConnection.put(source, port);
        }

        String dependentDeviceId = node.get("actualDevice").asText();
        Device dependentDevice = null;
        if (!dependentDeviceId.isEmpty()) {
            dependentDevice = DeviceLibrary.INSTANCE.getActualDevice(dependentDeviceId);
        }

        Map<Peripheral, List<DevicePort>> dependentDeviceConnection = new HashMap<>();
        for (JsonNode connection : node.get("dependentDeviceConnection")) {
            Peripheral source = Peripheral.valueOf(connection.get("devicePeripheral").asText());
            //Peripheral dest = Peripheral.valueOf(connection.get("controllerPeripheral").asText());
            List<DevicePort> port = new ArrayList<>();
            for (JsonNode controllerPeripheral : connection.get("controllerPeripheral")) {
                String portName = controllerPeripheral.asText();
                port.add(controller.getPort(portName));
            }
            actualDeviceConnection.put(source, port);
            dependentDeviceConnection.put(source, port);
        }

        Map<Property, String> property = new HashMap<>();
        for (JsonNode propertyNode : node.get("property")) {
            String propertyName = propertyNode.get("name").asText();
            Property p = genericDevice.getProperty(propertyName);
            property.put(p, propertyNode.get("value").asText());
        }

        return new ProjectDevice(name, genericDevice, autoSelect, actualDevice, actualDeviceConnection
                , dependentDevice, dependentDeviceConnection, property);
    }
}
