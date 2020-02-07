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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.makerplayground.device.shared.DataType;
import io.makerplayground.device.shared.Unit;
import io.makerplayground.device.shared.Value;
import io.makerplayground.device.shared.constraint.Constraint;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Created by USER on 14-Jul-17.
 */
public class ProjectDeserializer extends JsonDeserializer<Project> {

    final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Project deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        Project project = new Project();
        Begin defaultBegin = project.getBegin().get(0);

        SimpleModule module = new SimpleModule();
        module.addDeserializer(ProjectConfiguration.class, new ProjectConfigurationDeserializer(project));
        module.addDeserializer(UserSetting.class, new UserSettingDeserializer(project));
        module.addDeserializer(Scene.class, new SceneDeserializer(project));
        module.addDeserializer(Condition.class, new ConditionDeserializer(project));
        module.addDeserializer(Delay.class, new DelayDeserializer(project));

        mapper.registerModule(module);

        String projectName = node.get("projectName").asText();
        project.setProjectName(projectName);

        List<ProjectDevice> devices = mapper.readValue(node.get("devices").traverse(), new TypeReference<List<ProjectDevice>>() {});
        if (devices.size() != devices.stream().map(ProjectDevice::getName).distinct().count()) {
            throw new IllegalStateException("Cannot parse mp file because multiple devices share the same name.");
        }
        devices.forEach(project::addDevice);

        for (JsonNode beginNode: node.get("begins")) {
            Begin begin = new Begin(project);
            begin.setName(beginNode.get("name").asText());
            begin.setLeft(beginNode.get("left").asDouble());
            begin.setTop(beginNode.get("top").asDouble());
            project.addBegin(begin);
        }
        project.removeBegin(defaultBegin);

        if (node.has("variables")) {
            List<String> variableNames = mapper.readValue(node.get("variables").traverse(), new TypeReference<List<String>>() {});
            variableNames.forEach(varName -> VirtualProjectDevice.Memory.variables.add(new ProjectValue(VirtualProjectDevice.Memory.projectDevice, new Value(varName, DataType.DOUBLE, Constraint.createNumericConstraint(-Double.MAX_VALUE, Double.MAX_VALUE, Unit.NOT_SPECIFIED)))));
        }

        List<Scene> scenes = mapper.readValue(node.get("scenes").traverse(), new TypeReference<List<Scene>>() {});
        scenes.forEach(project::addScene);

        List<Condition> conditions = mapper.readValue(node.get("conditions").traverse(), new TypeReference<List<Condition>>() {});
        conditions.forEach(project::addCondition);

        List<Delay> delays = mapper.readValue(node.get("delays").traverse(), new TypeReference<List<Delay>>() {});
        delays.forEach(project::addDelay);


        for (JsonNode lineNode : node.get("lines")) {
            NodeElement source = null;
//            if (lineNode.get("source").asText().equals("begin")) {  // TODO: hard code
//                source = begin;
//            } else {
            Optional<Begin> begin = project.getBegin(lineNode.get("source").asText());
            if (begin.isPresent()) {
                source = begin.get();
            }
            Optional<Scene> scene = project.getUnmodifiableScene(lineNode.get("source").asText());
            if (scene.isPresent()) {
                source = scene.get();
            }
            Optional<Condition> condition = project.getUnmodifiableCondition(lineNode.get("source").asText());
            if (condition.isPresent()) {
                source = condition.get();
            }
            Optional<Delay> delay = project.getUnmodifiableDelay(lineNode.get("source").asText());
            if (delay.isPresent()) {
                source = delay.get();
            }

            NodeElement destination = null;
            Optional<Scene> s = project.getUnmodifiableScene(lineNode.get("destination").asText());
            if (s.isPresent()) {
                destination = s.get();
            }
            Optional<Condition> c = project.getUnmodifiableCondition(lineNode.get("destination").asText());
            if (c.isPresent()) {
                destination = c.get();
            }
            Optional<Delay> d = project.getUnmodifiableDelay(lineNode.get("destination").asText());
            if (d.isPresent()) {
                destination = d.get();
            }
            project.addLine(source, destination);
        }

        ProjectConfiguration config = mapper.readValue(node.get("projectConfiguration").traverse(), ProjectConfiguration.class);
        project.setProjectConfiguration(config);

        return project;
    }

//    public ProjectDevice deserializeProjectDevice(ObjectMapper mapper, JsonNode node) throws IOException {
//        String name = node.get("name").asText();
//        GenericDevice genericDevice = DeviceLibrary.INSTANCE.getGenericDevice(node.get("genericDevice").asText());
//
//        ActualDevice actualDevice = null;
//        String actualDeviceType = null;
//        JsonNode actualDeviceNode = node.get("actualDevice");
//        if (actualDeviceNode.has("type")) {
//            actualDeviceType = actualDeviceNode.get("type").asText();
//            if (actualDeviceType.equals("share")) {
//                // skip for now as we will handle it below after creating the ProjectDevice instance
//            } else if (actualDeviceType.equals("integrated")) {
//                String deviceId = actualDeviceNode.get("id").asText();
//                ActualDevice parentDevice = DeviceLibrary.INSTANCE.getActualDevice(deviceId);
//                if (parentDevice == null) {
//                    throw new IllegalStateException("Can't find actual device with id (" + deviceId + ")");
//                }
//                String integratedDeviceName = actualDeviceNode.get("name").asText();
//                IntegratedActualDevice integratedActualDevice = parentDevice.getIntegratedDevices(integratedDeviceName)
//                        .orElseThrow(() -> new IllegalStateException("Can't find integrated device with name (" + integratedDeviceName + ")"));
//                actualDevice = integratedActualDevice;
//                if (actualDevice == null) {
//                    throw new IllegalStateException("Can't find actual device with id (" + deviceId + ")");
//                }
//            } else if (actualDeviceType.equals("single")) {
//                String deviceId = actualDeviceNode.get("id").asText();
//                actualDevice = DeviceLibrary.INSTANCE.getActualDevice(deviceId);
//            } else {
//                throw new IllegalStateException("Invalid actual device type (" + actualDeviceType + ")");
//            }
//        }
//        if (actualDevice == null) {
//            throw new IllegalStateException("Can't deserialize actual device");
//        }
//
//        ProjectConfiguration configuration = mapper.readValue(node.get("config").traverse(), new TypeReference<ProjectConfiguration>() {});
//
//        String dependentDeviceId = node.get("actualDevice").asText();
//        ActualDevice dependentDevice = null;
//        if (!dependentDeviceId.isEmpty()) {
//            dependentDevice = DeviceLibrary.INSTANCE.getActualDevice(dependentDeviceId);
//        }
//
//        /* TODO: uncomment this */
////        Map<Peripheral, List<DevicePort>> dependentDeviceConnection = new HashMap<>();
////        for (JsonNode connection : node.get("dependentDeviceConnection")) {
////            Peripheral source = Peripheral.valueOf(connection.get("devicePeripheral").asText());
////            //Peripheral dest = Peripheral.valueOf(connection.get("controllerPeripheral").asText());
////            List<DevicePort> port = new ArrayList<>();
////            for (JsonNode controllerPeripheral : connection.get("controllerPeripheral")) {
////                String portName = controllerPeripheral.asText();
////                port.add(controller.getPort(portName));
////            }
////            actualDeviceConnection.put(source, port);
////            dependentDeviceConnection.put(source, port);
////        }
//
//        Map<Property, Object> property = new HashMap<>();
//        for (JsonNode propertyNode : node.get("property")) {
//            String propertyName = propertyNode.get("name").asText();
//            Property p = actualDevice.getProperty(propertyName).orElseThrow();
//            Object value;
//            switch (p.getDataType()) {
//                case STRING:
//                case ENUM:
//                    value = propertyNode.get("value").asText();
//                    break;
//                case INTEGER:
//                case DOUBLE:
//                    double num = propertyNode.get("value").get("value").asDouble();
//                    Unit unit = Unit.valueOf(propertyNode.get("value").get("unit").asText());
//                    value = new NumberWithUnit(num, unit);
//                    break;
//                case INTEGER_ENUM:
//                    value = propertyNode.get("value").asInt();
//                    break;
//                case BOOLEAN_ENUM:
//                    value = propertyNode.get("value").asBoolean();
//                    break;
//                case AZURE_COGNITIVE_KEY:
//                    if (propertyNode.get("value").has("name")) {
//                        value = new AzureCognitiveServices(propertyNode.get("value").get("name").asText()
//                                , propertyNode.get("value").get("location").asText()
//                                , propertyNode.get("value").get("key1").asText()
//                                , propertyNode.get("value").get("key2").asText());
//                    } else {
//                        value = null;
//                    }
//                    break;
//                case AZURE_IOTHUB_KEY:
//                    if (propertyNode.get("value").has("deviceId")) {
//                        value = new AzureIoTHubDevice(propertyNode.get("value").get("deviceId").asText()
//                                , propertyNode.get("value").get("connectionString").asText());
//                    } else {
//                        value = null;
//                    }
//                    break;
//                default:
//                    throw new IllegalStateException("Found invalid datatype while deserialize property");
//            }
//            property.put(p, value);
//        }
//
//        /* TODO: uncomment this & add actual device connection and dependent device connection to project device  */
//        ProjectDevice projectDevice = new ProjectDevice(name, genericDevice);
//        if (actualDeviceNode.has("type") && actualDeviceType.equals("share")) {
//            shareActualDeviceMap.put(projectDevice, actualDeviceNode.get("parent").asText());
//        }
//        return projectDevice;
//    }
}
