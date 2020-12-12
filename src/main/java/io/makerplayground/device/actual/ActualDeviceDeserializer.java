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

package io.makerplayground.device.actual;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.device.generic.GenericDevice;
import io.makerplayground.device.shared.*;
import io.makerplayground.device.shared.constraint.Constraint;
import io.makerplayground.device.shared.constraint.ConstraintDeserializer;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.makerplayground.util.DeserializerHelper.*;

public class ActualDeviceDeserializer extends JsonDeserializer<ActualDevice> {

    private YAMLMapper mapper = new YAMLMapper();
    private String id;
    private Map<String, Map<String, PinTemplate>> allPinTemplateMap;

    public ActualDeviceDeserializer(Map<String, Map<String, PinTemplate>> allPinTemplateMap) {
        this.allPinTemplateMap = allPinTemplateMap;
    }

    @Override
    public ActualDevice deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
        JsonNode node = mapper.readTree(jsonParser);

        throwIfMissingField(node, "id", "device must have id");

        /* id */
        id = node.get("id").asText();

        throwIfMissingField(node, "type", id);
        throwIfMissingField(node, "brand", id);
        throwIfMissingField(node, "model", id);
        throwIfMissingField(node, "url", id);
        throwIfMissingField(node, "width", id);
        throwIfMissingField(node, "height", id);
        throwIfFieldIsNotArray(node, "platforms", id);

        createArrayNodeIfMissing(node, "cloud_provide");
        createArrayNodeIfMissing(node, "property");
        createArrayNodeIfMissing(node, "compatibility");
        createArrayNodeIfMissing(node, "integrated_devices");

        /* DeviceType */
        DeviceType deviceType = DeviceType.valueOf(node.get("type").asText());
        if (deviceType != DeviceType.VIRTUAL) {
            throwIfOneOfTheseFieldsNotExist(node, List.of("connection_provide", "connection_consume"), id);
        }

        boolean needBreadboard = node.has("need_breadboard") && node.get("need_breadboard").asBoolean();

        BreadboardPlacement breadboardPlacement = null;
        if (needBreadboard) {
            throwIfMissingField(node, "breadboard_placement");
            breadboardPlacement = BreadboardPlacement.valueOf(node.get("breadboard_placement").asText());
        }

        /* PioBoardId */
        String pioBoardId = "";
        if (deviceType == DeviceType.CONTROLLER) {
            throwIfMissingField(node, "pio_boardid", id);
            pioBoardId = node.get("pio_boardid").asText();
        }

        /* Extract platformSourceCodeLibrary and firmware path (if available) */
        Map<Platform, SourceCodeLibrary> platformSourceCodeLibrary = new HashMap<>();
        Map<Platform, List<String>> firmwarePath = new HashMap<>();
        for (JsonNode platformNode : node.get("platforms")) {
            Platform platform = Platform.valueOf(platformNode.get("platform").asText());
            String classname = platformNode.get("classname").asText();
            List<String> externalLibraryList = mapper.readValue(platformNode.get("library_dependency").traverse()
                    , new TypeReference<List<String>>() {});
            platformSourceCodeLibrary.put(platform, new SourceCodeLibrary(classname, externalLibraryList));

            if (platformNode.has("firmware")) {
                List<String> firmware = mapper.readValue(platformNode.get("firmware").traverse()
                        , new TypeReference<List<String>>() {});
                firmwarePath.put(platform, firmware);
            } else {
                firmwarePath.put(platform, Collections.emptyList());
            }
        }

        /* Extract cloudPlatformSourceCodeLibrary */
        Map<CloudPlatform, SourceCodeLibrary> cloudPlatformSourceCodeLibrary = new HashMap<>();
        for (JsonNode jsonNode : node.get("cloud_provide")) {
            CloudPlatform platform = CloudPlatform.valueOf(jsonNode.get("cloud_platform").asText());
            String classname = jsonNode.get("classname").asText();
            List<String> externalLibraryList = mapper.readValue(jsonNode.get("library_dependency").traverse()
                    , new TypeReference<List<String>>() {});
            cloudPlatformSourceCodeLibrary.put(platform, new SourceCodeLibrary(classname, externalLibraryList));
        }

        /* CloudPlatform */
        CloudPlatform cloudConsume = node.has("cloud_consume") ? CloudPlatform.valueOf(node.get("cloud_consume").asText()) : null;

        List<Connection> connectionProvide = new ArrayList<>();
        List<Connection> connectionConsume = new ArrayList<>();
        List<Connection> expandConnectionProvide;
        List<IntegratedActualDevice> integratedDevices = new ArrayList<>();
        if (deviceType != DeviceType.CONTROLLER) {
            if (node.has("connection_provide")) {
                connectionProvide = loadConnectionProvide(node.get("connection_provide"), Collections.emptyMap());
            }
            if (node.has("connection_consume")) {
                connectionConsume = loadConnectionConsume(node.get("connection_consume"), Collections.emptyMap());
            }
            expandConnectionProvide = expandConnection(connectionProvide);
        } else {
            throwIfMissingField(node, "pin_template", id);
            String templateName = node.get("pin_template").asText();
            if (!this.allPinTemplateMap.containsKey(templateName)) {
                throw new IllegalStateException("There is no pin_template named " + templateName);
            }
            Map<String, PinTemplate> devicePinTemplate = this.allPinTemplateMap.get(templateName);
            if (node.has("connection_provide")) {
                connectionProvide = loadConnectionProvide(node.get("connection_provide"), devicePinTemplate);
            }
            if (node.has("connection_consume")) {
                connectionConsume = loadConnectionConsume(node.get("connection_consume"), devicePinTemplate);
            }
            expandConnectionProvide = expandConnection(connectionProvide);

            /* Integrated Devices */
            integratedDevices = new ArrayList<>();
            for (JsonNode inNode : node.get("integrated_devices")) {

                /* Check for name */
                throwIfMissingField(inNode, "name", id, "integrated_device");

                /* Extract Device Name */
                String inDeviceName = inNode.get("name").asText();
                createArrayNodeIfMissing(inNode, "property");

                /* Check for required fields */
                throwIfMissingField(inNode, "integrated_connection", id, "integrated_device", inDeviceName);
                throwIfMissingField(inNode, "compatibility", id, "integrated_device", inDeviceName);
                throwIfMissingField(inNode, "platforms", id, "integrated_device", inDeviceName);
                throwIfFieldIsNotArray(inNode, "platforms", id, "integrated_device", inDeviceName);

                List<Connection> inConnection = new ArrayList<>();
                List<Pin> inPins = new ArrayList<>();
                List<Pin> hostPins = new ArrayList<>();
                for (JsonNode integratedConnectionNode: inNode.get("integrated_connection")) {
                    throwIfMissingField(integratedConnectionNode, "pin_function", id, "integrated_device", inDeviceName, "integrated_connection");
                    throwIfMissingField(integratedConnectionNode, "host_ref_to", id, "integrated_device", inDeviceName, "integrated_connection");
                    String hostRefTo = integratedConnectionNode.get("host_ref_to").asText();
                    if (!devicePinTemplate.containsKey(hostRefTo)) {
                        throw new IllegalStateException("There is no pin named " + hostRefTo + "in " + templateName);
                    }
                    PinTemplate hostPinTemplate = devicePinTemplate.get(hostRefTo);
                    boolean hostHasHwSerial = hostPinTemplate.getFunction().contains(PinFunction.HW_SERIAL_TX) || hostPinTemplate.getFunction().contains(PinFunction.HW_SERIAL_RX);
                    hostPins.add(new Pin(hostRefTo, hostPinTemplate.getCodingName(), hostPinTemplate.getVoltageLevel(), -1, -1, hostPinTemplate.getFunction(), hostHasHwSerial, -1, -1));

                    List<PinFunction> function = List.of(PinFunction.valueOf(integratedConnectionNode.get("pin_function").asText()));
                    boolean hasHwSerial = function.contains(PinFunction.HW_SERIAL_TX) || function.contains(PinFunction.HW_SERIAL_RX);
                    inPins.add(new Pin("", "", VoltageLevel.NOT_SPECIFIED, -1, -1, function, hasHwSerial, -1, -1));
                }
                inConnection.add(new Connection(inDeviceName, ConnectionType.INTEGRATED, inPins, null));
                expandConnectionProvide.add(new Connection(inDeviceName, ConnectionType.INTEGRATED, hostPins, null));

                /* Extract platformSourceCodeLibrary */
                Map<Platform, SourceCodeLibrary> inPlatformSourceCodeLibrary = new HashMap<>();
                for (JsonNode platformNode : inNode.get("platforms")) {
                    Platform platform = Platform.valueOf(platformNode.get("platform").asText());
                    String classname = platformNode.get("classname").asText();
                    List<String> externalLibraryList = mapper.readValue(platformNode.get("library_dependency").traverse()
                            , new TypeReference<List<String>>() {});
                    inPlatformSourceCodeLibrary.put(platform, new SourceCodeLibrary(classname, externalLibraryList));
                }

                List<Property> inProperty = mapper.readValue(inNode.get("property").traverse(), new TypeReference<List<Property>>() {});

                /* deallocate the created empty list and set to the shared static empty list instead */
                if (inProperty.isEmpty()) { inProperty = Collections.emptyList(); }

                /* Compatibility */
                Map<GenericDevice, Compatibility> inCompatibilityMap = loadCompatibility(inNode);

                IntegratedActualDevice inDevice = new IntegratedActualDevice(inDeviceName,
                        inProperty,
                        "",
                        inConnection,
                        inCompatibilityMap,
                        inPlatformSourceCodeLibrary);

                integratedDevices.add(inDevice);
            }

            List<String> allIntegratedDeviceName = integratedDevices.stream()
                    .map(IntegratedActualDevice::getId)
                    .collect(Collectors.toList());
            if (allIntegratedDeviceName.stream().anyMatch(s -> Collections.frequency(allIntegratedDeviceName, s) > 1)) {
                throw new IllegalStateException("There is a duplicate integrated device's name.");
            }
        }

        /* Property */
        List<Property> property = mapper.readValue(node.get("property").traverse(), new TypeReference<List<Property>>() {});

        /* Compatibility */
        Map<GenericDevice, Compatibility> compatibilityMap = loadCompatibility(node);

        List<String> allConnectionName = Stream.of(connectionProvide.stream(), connectionConsume.stream())
                .reduce(Stream::concat)
                .orElseGet(Stream::empty)
                .map(Connection::getName)
                .collect(Collectors.toList());
        String allDuplicateNames = allConnectionName.stream().filter(s -> Collections.frequency(allConnectionName, s) > 1).collect(Collectors.joining(", "));
        if (!allDuplicateNames.isEmpty()) {
            throw new IllegalStateException("Duplicate connection name [" + id + "] [" + allDuplicateNames + "]");
        }

        /* deallocate the created empty list and set to the shared static empty list instead */
        if (connectionProvide.isEmpty()) { connectionProvide = Collections.emptyList(); }
        if (connectionConsume.isEmpty()) { connectionConsume = Collections.emptyList(); }
        if (property.isEmpty()) { property = Collections.emptyList(); }
        if (cloudPlatformSourceCodeLibrary.isEmpty()) { cloudPlatformSourceCodeLibrary = Collections.emptyMap(); }
        if (integratedDevices.isEmpty()) { integratedDevices = Collections.emptyList(); }

        ActualDevice actualDevice = ActualDevice.builder()
                .id(id)
                .brand(node.get("brand").asText())
                .model(node.get("model").asText())
                .url(node.get("url").asText())
                .width(node.get("width").asDouble())
                .height(node.get("height").asDouble())
                .platformSourceCodeLibrary(platformSourceCodeLibrary)
                .firmwarePath(firmwarePath)
                .cloudPlatformSourceCodeLibrary(cloudPlatformSourceCodeLibrary)
                .deviceType(deviceType)
                .needBreadboard(needBreadboard)
                .breadboardPlacement(breadboardPlacement)
                .pioBoardId(pioBoardId)
                .cloudConsume(cloudConsume)
                .connectionConsume(connectionConsume)
                .connectionProvide(expandConnectionProvide)
                .property(property)
                .compatibilityMap(compatibilityMap)
                .integratedDevices(integratedDevices)
                .build();

        actualDevice.getIntegratedDevices().forEach(inActualDevice -> inActualDevice.setParent(actualDevice));

        return actualDevice;
    }

    private List<Connection> expandConnection(List<Connection> connectionProvide) {
        List<Connection> retVal = new ArrayList<>(connectionProvide);
        for (Connection connection: connectionProvide) {
            List<Connection> friendConnection = new ArrayList<>();
            if (connection.getType() != ConnectionType.WIRE && connection.getPins().size() != 1) {
                int i=0;
                for (Pin pin: connection.getPins()) {
                    Connection wireConnection = new Connection(connection.getName() + "_" + i, ConnectionType.WIRE, List.of(pin), null);
                    wireConnection.setFriendConnections(List.of(connection));
                    friendConnection.add(wireConnection);
                    i++;
                }
            }
            connection.setFriendConnections(Collections.unmodifiableList(friendConnection));
            retVal.addAll(friendConnection);
        }
        return retVal;
    }

    private Map<GenericDevice, Compatibility> loadCompatibility(JsonNode node) throws IOException {
        Map<GenericDevice, Compatibility> compatibilityMap = new HashMap<>();
        for (JsonNode compatibilityNode : node.get("compatibility")) {

            /* Query GenericDevice */
            String deviceName = compatibilityNode.get("name").asText();
            GenericDevice genericDevice = DeviceLibrary.INSTANCE.getGenericDevice(deviceName);

            /* Check and Preprocess */
            createArrayNodeIfMissing(compatibilityNode, "action");
            createArrayNodeIfMissing(compatibilityNode, "condition");
            createArrayNodeIfMissing(compatibilityNode, "value");

            LinkedHashMap<Action, LinkedHashMap<Parameter, Constraint>> deviceAction = new LinkedHashMap<>();
            for (JsonNode actionNode : compatibilityNode.get("action")) {

                /* Check and Preprocess */
                throwIfMissingField(actionNode, "name", id, "compatibility", genericDevice.getName() ,"action", "[?]");
                createArrayNodeIfMissing(actionNode, "parameter");

                /* Query Action */
                String actionName = actionNode.get("name").asText();
                Optional<Action> action = genericDevice.getAction(actionName);
                if (action.isEmpty()) {
                    throw new IllegalStateException("There is no action '" + actionName + "' in generic_device '" + genericDevice.getName() + "'");
                }

                /* Extract Parameter */
                LinkedHashMap<Parameter, Constraint> supportedParam = new LinkedHashMap<>();
                for (JsonNode parameterNode : actionNode.get("parameter")) {

                    /* Check and Preprocess */
                    throwIfMissingField(actionNode, "name", id, "compatibility", genericDevice.getName(), actionName, "parameter");
                    createArrayNodeIfMissing(actionNode, "parameter");

                    /* Extract Parameter */
                    String parameterName = parameterNode.get("name").asText();
                    Optional<Parameter> parameter = action.get().getParameter(parameterName);
                    if (parameter.isEmpty()) {
                        throw new IllegalStateException("There is no parameter '" + parameterName + "' for action '" + actionName + "' ");
                    }

                    DataType dataType = parameter.get().getDataType();
                    SimpleModule module = new SimpleModule();
                    module.addDeserializer(Constraint.class, new ConstraintDeserializer(dataType));
                    YAMLMapper mapper = new YAMLMapper();
                    mapper.registerModule(module);

                    /* Extract Constraint */
                    Constraint constraint = parameter.get().getConstraint();
                    if (parameterNode.has("constraint")) {
                        constraint = constraint.intersect(mapper.readValue(parameterNode.get("constraint").traverse(), Constraint.class));
                    }

                    /* Put parameter and constraint into Map */
                    supportedParam.put(parameter.get(), parameter.get().getConstraint().intersect(constraint));
                }

                /* Put action and param to Map */
                deviceAction.put(action.get(), supportedParam);
            }

            LinkedHashMap<Condition, LinkedHashMap<Parameter, Constraint>> deviceCondition = new LinkedHashMap<>();
            for (JsonNode conditionNode : compatibilityNode.get("condition")) {

                /* Check and Preprocess */
                throwIfMissingField(conditionNode, "name", id, "compatibility", genericDevice.getName() ,"condition");
                createArrayNodeIfMissing(conditionNode, "parameter");

                /* Query Action */
                String conditionName = conditionNode.get("name").asText();
                Optional<Condition> condition = genericDevice.getCondition(conditionName);
                if (condition.isEmpty()) {
                    throw new IllegalStateException("There is no condition '" + conditionName + "' in generic_device '" + genericDevice.getName() + "'");
                }

                /* Extract Parameter */
                LinkedHashMap<Parameter, Constraint> supportedParam = new LinkedHashMap<>();
                for (JsonNode parameterNode : conditionNode.get("parameter")) {

                    /* Check and Preprocess */
                    throwIfMissingField(conditionNode, "name", id, "compatibility", genericDevice.getName(), conditionName, "parameter");
                    createArrayNodeIfMissing(conditionNode, "parameter");

                    /* Extract Parameter */
                    String parameterName = parameterNode.get("name").asText();
                    Optional<Parameter> parameter = condition.get().getParameter(parameterName);
                    if (parameter.isEmpty()) {
                        throw new IllegalStateException("There is no parameter '" + parameterName + "' for condition '" + conditionName + "' ");
                    }

                    DataType dataType = parameter.get().getDataType();
                    SimpleModule module = new SimpleModule();
                    module.addDeserializer(Constraint.class, new ConstraintDeserializer(dataType));
                    YAMLMapper mapper = new YAMLMapper();
                    mapper.registerModule(module);

                    /* Extract Constraint */
                    Constraint constraint = parameter.get().getConstraint();
                    if (parameterNode.has("constraint")) {
                        constraint = constraint.intersect(mapper.treeToValue(parameterNode.get("constraint"), Constraint.class));
                    }

                    /* Put parameter and constraint into Map */
                    supportedParam.put(parameter.get(), constraint);
                }

                /* Put action and param to Map */
                deviceCondition.put(condition.get(), supportedParam);
            }

            LinkedHashMap<Value, Constraint> deviceValue = new LinkedHashMap<>();
            for (JsonNode valueNode : compatibilityNode.get("value")) {

                /* Check and Preprocess */
                throwIfMissingField(valueNode, "name", id, "compatibility", genericDevice.getName(), "value");

                /* Extract Value */
                String valueName = valueNode.get("name").asText();
                Optional<Value> value = genericDevice.getValue(valueName);
                if (value.isEmpty()) {
                    throw new IllegalStateException("No value '" + valueName + "' for '" + genericDevice.getName() + "'");
                }

                DataType dataType = value.get().getType();
                SimpleModule module = new SimpleModule();
                module.addDeserializer(Constraint.class, new ConstraintDeserializer(dataType));
                mapper.registerModule(module);

                /* Extract Constraint */
                Constraint constraint = valueNode.has("constraint") ? mapper.treeToValue(valueNode.get("constraint"), Constraint.class) : Constraint.NONE;

                /* Put value and constraint into map */
                deviceValue.put(value.get(), constraint);
            }

            /* Put genericdevice and compatibility into map */
            compatibilityMap.put(genericDevice, new Compatibility(deviceAction, deviceCondition, deviceValue));
        }
        return compatibilityMap;
    }

    private List<Connection> loadConnectionProvide(JsonNode connectionProvideNode, Map<String, PinTemplate> pinTemplateMap) throws IOException {
        throwIfMissingField(connectionProvideNode, "voltage_level", id, "connection_provide");
        throwIfMissingField(connectionProvideNode, "items", id, "connection_provide");
        VoltageLevel connectionVoltageLevel = VoltageLevel.valueOf(connectionProvideNode.get("voltage_level").asText());
        double operatingVoltage = connectionVoltageLevel.getVoltage();
        double connectionMinVoltage = connectionProvideNode.has("min_voltage") ? connectionProvideNode.get("min_voltage").asDouble() : 0.7 * operatingVoltage;
        double connectionMaxVoltage = connectionProvideNode.has("max_voltage") ? connectionProvideNode.get("max_voltage").asDouble() : operatingVoltage;
        if (connectionMinVoltage > operatingVoltage) {
            throw new IllegalStateException("connection_provide's min_voltage is higher than operating voltage_level");
        }
        if (connectionMaxVoltage < operatingVoltage) {
            throw new IllegalStateException("connection_provide's max_voltage is higher than operating voltage_level");
        }
        List<Connection> connectionList = new ArrayList<>();
        for (JsonNode portNode: connectionProvideNode.get("items")) {
            throwIfMissingField(portNode, "name", id, "connection_provide");
            throwIfMissingField(portNode, "type", id, "connection_provide");
            throwIfMissingField(portNode, "pins", id, "connection_provide");
            throwIfFieldIsNotArray(portNode, "pins", id, "connection_provide");

            String connectionName = portNode.get("name").asText();
            ConnectionType connectionType = ConnectionType.valueOf(portNode.get("type").asText());

            List<Pin> pins = new ArrayList<>();
            for (JsonNode pinNode: portNode.get("pins")) {
                throwIfMissingField(pinNode, "ref_to", id, "connection_provide","pin");

                String refTo = pinNode.get("ref_to").asText();

                throwIfMissingField(pinNode, "x", "pin", refTo, "x");
                throwIfMissingField(pinNode, "y", "pin", refTo, "y");

                if (!pinTemplateMap.containsKey(refTo)) {
                    throw new IllegalStateException("There is no " + refTo + " in the pin_template");
                }

                PinTemplate pinTemplate = pinTemplateMap.get(refTo);
                double x = pinNode.get("x").asDouble();
                double y = pinNode.get("y").asDouble();
                VoltageLevel pinVoltageLevel = pinNode.has("voltage_level") ? VoltageLevel.valueOf(pinNode.get("voltage_level").asText()) : connectionVoltageLevel;
                double pinOperatingVoltage = pinVoltageLevel.getVoltage();
                double pinMinVoltage = pinNode.has("min_voltage") ? pinNode.get("min_voltage").asDouble() : (pinNode.has("voltage_level") ? 0.7 * pinOperatingVoltage : connectionMinVoltage);
                double pinMaxVoltage = pinNode.has("max_voltage") ? pinNode.get("max_voltage").asDouble() : (pinNode.has("voltage_level") ? pinOperatingVoltage : connectionMaxVoltage);
                if (pinMinVoltage > pinOperatingVoltage) {
                    throw new IllegalStateException("min_voltage is higher than operating voltage_level '" + pinOperatingVoltage + "' for (" + connectionName + "->" + refTo + ")");
                }
                if (pinMaxVoltage < pinOperatingVoltage) {
                    throw new IllegalStateException("max_voltage is lower than operating voltage_level '" + pinOperatingVoltage + "' for (" + connectionName + "->" + refTo + ")");
                }

                List<PinFunction> function = pinTemplate.getFunction();
                if (pinNode.has("function")) {
                    if (pinNode.get("function").isArray()) {
                        function = mapper.readValue(pinNode.get("function").traverse(), new TypeReference<List<PinFunction>>() {});
                    } else {
                        function = List.of(PinFunction.valueOf(pinNode.get("function").asText()));
                    }
                }
                boolean hasHwSerial = function.contains(PinFunction.HW_SERIAL_TX) || function.contains(PinFunction.HW_SERIAL_RX);

                pins.add(new Pin(refTo, pinTemplate.getCodingName(), pinVoltageLevel, pinMinVoltage, pinMaxVoltage, function, hasHwSerial, x, y));
            }

//            if (connectionType.isSplittable()) {
//                for (int i=0; i<pins.size(); i++) {
//                    connectionList.add(new Connection(connectionName+"_"+(i+1), ConnectionType.WIRE, List.of(pins.get(i)), null));
//                }
//            }

            connectionList.add(new Connection(connectionName, connectionType, pins, null));
        }
        return connectionList;
    }

    private List<Connection> loadConnectionConsume(JsonNode connectionConsumeNode, Map<String, PinTemplate> pinTemplateMap) throws IOException {
        throwIfMissingField(connectionConsumeNode, "min_voltage", id, "connection_consume", "");
        throwIfMissingField(connectionConsumeNode, "max_voltage", id, "connection_consume", "");
        throwIfMissingField(connectionConsumeNode, "items", id, "connection_consume", "");
        VoltageLevel connectionVoltageLevel = connectionConsumeNode.has("voltage_level") ? VoltageLevel.valueOf(connectionConsumeNode.get("voltage_level").asText()) : VoltageLevel.NOT_SPECIFIED;
        double connectionMinVoltage = connectionConsumeNode.get("min_voltage").asDouble();
        double connectionMaxVoltage = connectionConsumeNode.get("max_voltage").asDouble();
        if (connectionMinVoltage > connectionMaxVoltage) {
            throw new IllegalStateException("connection_consume's min_voltage is higher than max_voltage");
        }
        if (connectionVoltageLevel != VoltageLevel.NOT_SPECIFIED && connectionMinVoltage > connectionVoltageLevel.getVoltage()) {
            throw new IllegalStateException("connection_consume's min_voltage is higher than operating voltage_level");
        }
        if (connectionVoltageLevel != VoltageLevel.NOT_SPECIFIED && connectionMaxVoltage < connectionVoltageLevel.getVoltage()) {
            throw new IllegalStateException("connection_consume's max_voltage is higher than operating voltage_level");
        }
        List<Connection> connectionList = new ArrayList<>();
        for (JsonNode portNode: connectionConsumeNode.get("items")) {
            throwIfMissingField(portNode, "name", id, "connection_consume");
            throwIfMissingField(portNode, "type", id, "connection_consume");
            throwIfMissingField(portNode, "pins", id, "connection_consume");
            throwIfFieldIsNotArray(portNode, "pins", id, "connection_consume");

            String connectionName = portNode.get("name").asText();
            ConnectionType connectionType = ConnectionType.valueOf(portNode.get("type").asText());

            List<Pin> pins = new ArrayList<>();
            for (JsonNode pinNode: portNode.get("pins")) {
                throwIfMissingField(pinNode, "pin_function", id, "connection_consume", connectionName, "pins");
                double x = pinNode.get("x").asDouble();
                double y = pinNode.get("y").asDouble();
                VoltageLevel pinVoltageLevel = pinNode.has("voltage_level") ? VoltageLevel.valueOf(pinNode.get("voltage_level").asText()) : connectionVoltageLevel;
                double pinOperatingVoltage = pinVoltageLevel.getVoltage();
                double pinMinVoltage = pinNode.has("min_voltage") ? pinNode.get("min_voltage").asDouble() : connectionMinVoltage;
                double pinMaxVoltage = pinNode.has("max_voltage") ? pinNode.get("max_voltage").asDouble() : connectionMaxVoltage;
                if (pinMinVoltage > pinMaxVoltage) {
                    throw new IllegalStateException("connection_consume's min_voltage is higher than max_voltage");
                }
                if (connectionVoltageLevel != VoltageLevel.NOT_SPECIFIED && pinMinVoltage > pinOperatingVoltage) {
                    throw new IllegalStateException("min_voltage is higher than operating voltage_level for (" + connectionName + "->" + x + "," + y + ")");
                }
                if (connectionVoltageLevel != VoltageLevel.NOT_SPECIFIED && pinMaxVoltage < pinOperatingVoltage) {
                    throw new IllegalStateException("max_voltage is higher than operating voltage_level for (" + connectionName + "->" + x + "," + y + ")");
                }
                List<PinFunction> function;
                if (pinNode.get("pin_function").isArray()) {
                    function = mapper.readValue(pinNode.get("pin_function").traverse(), new TypeReference<List<PinFunction>>() {});
                } else {
                    function = List.of(PinFunction.valueOf(pinNode.get("pin_function").asText()));
                }
                boolean hasHwSerial = function.contains(PinFunction.HW_SERIAL_TX) || function.contains(PinFunction.HW_SERIAL_RX);

                pins.add(new Pin("", "", pinVoltageLevel, pinMinVoltage, pinMaxVoltage, function, hasHwSerial, x, y));
            }

//            if (connectionType.isSplittable()) {
//                for (int i=0; i<pins.size(); i++) {
//                    connectionList.add(new Connection(connectionName+"_"+(i+1), ConnectionType.WIRE, List.of(pins.get(i)), null));
//                }
//            }

            connectionList.add(new Connection(connectionName, connectionType, pins, null));
        }
        return connectionList;
    }
}
