/*
 * Copyright (c) 2018. The Maker Playground Authors.
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

        mapper.registerModule(module);

        String projectName = node.get("projectName").asText();
        project.setProjectName(projectName);

//        Platform platform = Platform.valueOf(node.get("controller").get("platform").asText());
//        project.setPlatform(platform);

//        if (node.has("cloudplatform")) {
//            for (JsonNode cloudPlatformNode : node.get("cloudplatform")) {
//                CloudPlatform cloudPlatform = CloudPlatform.valueOf(cloudPlatformNode.get("name").asText());
//                for (JsonNode parameterNode : cloudPlatformNode.get("parameter")) {
//                    String name = parameterNode.get("name").asText();
//                    String value = parameterNode.get("value").asText();
//                    project.setCloudPlatformParameter(cloudPlatform, name, value);
//                }
//            }
//        }

//        ActualDevice controller = null;
//        if (!node.get("controller").get("device").asText().isEmpty()) {
//            controller = DeviceLibrary.INSTANCE.getActualDevice(node.get("controller").get("device").asText());
//        }
//        project.setController(controller);

        List<ProjectDevice> devices = mapper.readValue(node.get("devices").traverse(), new TypeReference<List<ProjectDevice>>() {});
        devices.forEach(project::addDevice);

//        for (ProjectDevice projectDevice : shareActualDeviceMap.keySet()) {
//            String parentName = shareActualDeviceMap.get(projectDevice);
//            ProjectDevice parentDevice = deviceList.stream()
//                    .filter(projectDevice1 -> projectDevice1.getName().equals(parentName))
//                    .findAny().orElseThrow(() -> new IllegalStateException("Can't find project device with name = " + parentName));
//            project.getProjectConfiguration().setIdenticalDevice(projectDevice, parentDevice);
//        }

        for (JsonNode beginNode: node.get("begins")) {
            Begin begin = new Begin(project);
            begin.setName(beginNode.get("name").asText());
            begin.setLeft(beginNode.get("left").asDouble());
            begin.setTop(beginNode.get("top").asDouble());
            project.addBegin(begin);
        }
        project.removeBegin(defaultBegin);

        List<Scene> scenes = mapper.readValue(node.get("scenes").traverse(), new TypeReference<List<Scene>>() {});
        scenes.forEach(project::addScene);

        List<Condition> conditions = mapper.readValue(node.get("conditions").traverse(), new TypeReference<List<Condition>>() {});
        conditions.forEach(project::addCondition);


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

            NodeElement destination = null;
            Optional<Scene> s = project.getUnmodifiableScene(lineNode.get("destination").asText());
            if (s.isPresent()) {
                destination = s.get();
            }
            Optional<Condition> c = project.getUnmodifiableCondition(lineNode.get("destination").asText());
            if (c.isPresent()) {
                destination = c.get();
            }
            project.addLine(source, destination);
        }

        ProjectConfiguration config = mapper.readValue(node.get("projectConfiguration").traverse(), ProjectConfiguration.class);
        project.setProjectConfiguration(config);

        return project;
    }

//    private Expression deserializeExpression(ObjectMapper mapper, Parameter parameter, JsonNode parameterNode
//            , Collection<ProjectDevice> allProjectDevices) throws IOException {
//        Expression expression = null;
//        String expressionType = parameterNode.get("type").asText();
//        JsonNode valueNode = parameterNode.get("value");
//        List<Term> terms = new ArrayList<>();
//        for (JsonNode term_node : valueNode.get("terms")) {
//            terms.add(deserializeTerm(mapper, parameter, parameterNode, term_node, allProjectDevices));
//        }
//        if (ProjectValueExpression.class.getSimpleName().equals(expressionType)) {
//            expression = new ProjectValueExpression(((ValueTerm) terms.get(0)).getValue());
//        } else if (CustomNumberExpression.class.getSimpleName().equals(expressionType)) {
//            expression = new CustomNumberExpression(terms);
//        } else if (NumberWithUnitExpression.class.getSimpleName().equals(expressionType)) {
//            expression = new NumberWithUnitExpression(((NumberWithUnitTerm) terms.get(0)).getValue());
//        } else if (SimpleStringExpression.class.getSimpleName().equals(expressionType)) {
//            expression = new SimpleStringExpression(((StringTerm) terms.get(0)).getValue());
//        } else if (ValueLinkingExpression.class.getSimpleName().equals(expressionType)){
//            boolean inverse = false;
//            if (valueNode.has("inverse")) {
//                inverse = valueNode.get("inverse").asBoolean();
//            }
//            expression = new ValueLinkingExpression(parameter, terms, inverse);
//        } else if (SimpleRTCExpression.class.getSimpleName().equals(expressionType)) {
//            expression = new SimpleRTCExpression(((RTCTerm)(terms.get(0))).getValue());
//        } else if (RecordExpression.class.getSimpleName().equals(expressionType)) {
//            expression = new RecordExpression(((RecordTerm)(terms.get(0))).getValue());
//        } else {
//            throw new IllegalStateException("expression type not supported");
//        }
//
//        Expression.RefreshInterval refreshInterval = Expression.RefreshInterval.valueOf(valueNode.get("refreshInterval").asText());
//        NumberWithUnit interval = mapper.readValue(valueNode.get("userDefinedInterval").traverse()
//                , new TypeReference<NumberWithUnit>(){});
//        expression.setRefreshInterval(refreshInterval);
//        expression.setUserDefinedInterval(interval);
//        return expression;
//    }
//
//    private Term deserializeTerm(ObjectMapper mapper, Parameter parameter, JsonNode parameterNode, JsonNode term_node, Collection<ProjectDevice> allProjectDevices) throws IOException {
//        String term_type = term_node.get("type").asText();
//        Term term;
//        if (Term.Type.NUMBER.name().equals(term_type)) {
//            double num = term_node.get("value").get("value").asDouble();
//            Unit unit = Unit.valueOf(term_node.get("value").get("unit").asText());
//            NumberWithUnit numberWithUnit = new NumberWithUnit(num, unit);
//            term = new NumberWithUnitTerm(numberWithUnit);
//        } else if (Term.Type.OPERATOR.name().equals(term_type)) {
//            String operator = term_node.get("value").asText();
//            term = new OperatorTerm(Operator.valueOf(operator));
//        } else if (Term.Type.VALUE.name().equals(term_type)) {
//            if ("null".equals(term_node.get("value").asText())) {
//                term = new ValueTerm(null);
//            } else {
//                String projectDeviceName = term_node.get("value").get("name").asText();
//                String valueName = term_node.get("value").get("value").asText();
//                ProjectDevice device = allProjectDevices.stream().filter(pj -> pj.getName().equals(projectDeviceName)).findFirst().get();
//                Value value = device.getGenericDevice().getValue(valueName).orElseThrow();
//                term = new ValueTerm(new ProjectValue(device, value));
//            }
//        } else if (Term.Type.STRING.name().equals(term_type)) {
//            String word = term_node.get("value").asText();
//            term = new StringTerm(word);
//        } else if (Term.Type.DATETIME.name().equals(term_type)) {
//            JsonNode temp_node = term_node.get("value").get("localDateTime");
//            int year = temp_node.get("year").asInt();
//            int month = temp_node.get("monthValue").asInt();
//            int day = temp_node.get("dayOfMonth").asInt();
//            int hour = temp_node.get("hour").asInt();
//            int minute = temp_node.get("minute").asInt();
//            int second = temp_node.get("second").asInt();
//            LocalDateTime localDateTime = LocalDateTime.of(year, month, day, hour, minute, second);
//            RealTimeClock.Mode mode = RealTimeClock.Mode.valueOf(term_node.get("value").get("mode").asText());
//            term = new RTCTerm(new RealTimeClock(mode, localDateTime));
//        } else if (Term.Type.RECORD.name().equals(term_type)) {
//            List<RecordEntry> recordEntryList = new ArrayList<>();
//            for (JsonNode entryNode : term_node.get("value").get("entryList")) {
//                String fieldName = entryNode.get("field").asText();
//                Expression expression = deserializeExpression(mapper, parameter, entryNode, allProjectDevices);
//                recordEntryList.add(new RecordEntry(fieldName, expression));
//            }
//            term = new RecordTerm(new Record(recordEntryList));
//
//        } else {
//            throw new IllegalStateException("deserialize unsupported term");
//        }
//        return term;
//    }

//    public Expression deserializeNumberInRangeExpression(ObjectMapper mapper, JsonNode node, ProjectDevice device, Value value) {
//        if (node.get(0).get("type").asText().equals(Term.Type.VALUE.name())
//            && node.get(1).get("type").asText().equals(Term.Type.OPERATOR.name())
//                && (node.get(1).get("value").asText().equals(Operator.LESS_THAN.name())
//                || node.get(1).get("value").asText().equals(Operator.LESS_THAN_OR_EQUAL.name()))
//                && node.get(2).get("type").asText().equals(Term.Type.NUMBER.name())
//                && node.get(3).get("type").asText().equals(Term.Type.OPERATOR.name())
//                && node.get(3).get("value").asText().equals(Operator.AND.name())
//                && node.get(4).get("type").asText().equals(Term.Type.VALUE.name())
//                && node.get(5).get("type").asText().equals(Term.Type.OPERATOR.name())
//                && (node.get(5).get("value").asText().equals(Operator.GREATER_THAN.name())
//                || node.get(5).get("value").asText().equals(Operator.GREATER_THAN_OR_EQUAL.name()))
//                && node.get(6).get("type").asText().equals(Term.Type.NUMBER.name())) {
//            return new NumberInRangeExpression(device, value)
//                    .setLowValue(node.get(6).get("value").get("value").asDouble())
//                    .setHighValue(node.get(2).get("value").get("value").asDouble())
//                    .setLowOperator(Operator.valueOf(node.get(5).get("value").asText()))
//                    .setHighOperator(Operator.valueOf(node.get(1).get("value").asText()));
//        } else {
//            throw new IllegalStateException("Simple expression parsing fail");
//        }
//    }

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
