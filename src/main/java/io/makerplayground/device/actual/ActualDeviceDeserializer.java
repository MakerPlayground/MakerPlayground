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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.device.generic.GenericDevice;
import io.makerplayground.device.shared.Action;
import io.makerplayground.device.shared.Condition;
import io.makerplayground.device.shared.Parameter;
import io.makerplayground.device.shared.Value;
import io.makerplayground.device.shared.constraint.Constraint;

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
        throwIfMissingField(node, "pin_template", id);
        throwIfFieldIsNotArray(node, "platforms", id);

        createArrayNodeIfMissing(node, "cloud_provide");
        createArrayNodeIfMissing(node, "connection_provide", "connection_consume");
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

        /* Extract platformSourceCodeLibrary */
        Map<Platform, SourceCodeLibrary> platformSourceCodeLibrary = new HashMap<>();
        for (JsonNode platformNode : node.get("platforms")) {
            Platform platform = Platform.valueOf(platformNode.get("platform").asText());
            String classname = platformNode.get("classname").asText();
            List<String> externalLibraryList = mapper.readValue(platformNode.get("library_dependency").traverse()
                    , new TypeReference<List<String>>() {});
            platformSourceCodeLibrary.put(platform, new SourceCodeLibrary(classname, externalLibraryList));
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

        String templateName = node.get("pin_template").asText();
        if (!this.allPinTemplateMap.containsKey(templateName)) {
            throw new IllegalStateException("There is no pin_template named " + templateName);
        }
        Map<String, PinTemplate> devicePinTemplate = this.allPinTemplateMap.get(templateName);

        /* Pin, Port, Property */
        List<Connection> connectionProvide = loadConnection(node.get("connection_provide"), devicePinTemplate);
        List<Connection> connectionConsume = loadConnection(node.get("connection_consume"), devicePinTemplate);
        List<Property> property = mapper.readValue(node.get("property").traverse(), new TypeReference<List<Property>>() {});

        /* Compatibility */
        Map<GenericDevice, Compatibility> compatibilityMap = loadCompatibility(node);

        List<IntegratedActualDevice> integratedDevices = new ArrayList<>();
        for (JsonNode inNode : node.get("integrated_devices")) {

            /* Check for name */
            throwIfMissingField(inNode, "name", id, "integrated_device");

            /* Extract Device Name */
            String inDeviceName = inNode.get("name").asText();
            createArrayNodeIfMissing(inNode, "property");

            /* Check for required fields */
            throwIfMissingField(inNode, "pin_template", id, "integrated_device", inDeviceName);
            throwIfMissingField(inNode, "integrated_connection", id, "integrated_device", inDeviceName);
            throwIfMissingField(inNode, "compatibility", id, "integrated_device", inDeviceName);
            throwIfMissingField(inNode, "platforms", id, "integrated_device", inDeviceName);
            throwIfFieldIsNotArray(inNode, "platforms", id, "integrated_device", inDeviceName);

            String inTemplateName = inNode.get("pin_template").asText();
            if (!this.allPinTemplateMap.containsKey(inTemplateName)) {
                throw new IllegalStateException("There is no pin_template named " + inTemplateName);
            }
            Map<String, PinTemplate> inDevicePinTemplate = this.allPinTemplateMap.get(inTemplateName);

            List<Connection> inConnection = new ArrayList<>();
            List<Pin> inPins = new ArrayList<>();
            List<Pin> hostPins = new ArrayList<>();
            for (JsonNode integratedConnectionNode: inNode.get("integrated_connection")) {
                throwIfMissingField(integratedConnectionNode, "ref_to", id, "integrated_device", inDeviceName, "integrated_connection");
                throwIfMissingField(integratedConnectionNode, "host_ref_to", id, "integrated_device", inDeviceName, "integrated_connection");
                String refTo = integratedConnectionNode.get("ref_to").asText();
                String hostRefTo = integratedConnectionNode.get("host_ref_to").asText();
                if (!inDevicePinTemplate.containsKey(refTo)) {
                    throw new IllegalStateException("There is no pin named " + refTo + "in " + inTemplateName);
                }
                if (!devicePinTemplate.containsKey(hostRefTo)) {
                    throw new IllegalStateException("There is no pin named " + hostRefTo + "in " + templateName);
                }
                PinTemplate inPinTemplate = inDevicePinTemplate.get(refTo);
                boolean hasHwSerial = inPinTemplate.getFunction().contains(PinFunction.HW_SERIAL_TX) || inPinTemplate.getFunction().contains(PinFunction.HW_SERIAL_RX);
                inPins.add(new Pin(refTo, inPinTemplate.getCodingName(), inPinTemplate.getVoltageLevel(), inPinTemplate.getFunction(), hasHwSerial, -1, -1));

                PinTemplate hostPinTemplate = devicePinTemplate.get(hostRefTo);
                boolean hostHasHwSerial = hostPinTemplate.getFunction().contains(PinFunction.HW_SERIAL_TX) || hostPinTemplate.getFunction().contains(PinFunction.HW_SERIAL_RX);
                hostPins.add(new Pin(hostRefTo, hostPinTemplate.getCodingName(), hostPinTemplate.getVoltageLevel(), hostPinTemplate.getFunction(), hostHasHwSerial, -1, -1));
            }
            inConnection.add(new Connection(inDeviceName, ConnectionType.INTEGRATED, inPins, null));
            connectionProvide.add(new Connection(inDeviceName, ConnectionType.INTEGRATED, hostPins, null));

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
                                                                            inTemplateName,
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

        List<String> allConnectionName = Stream.of(connectionProvide.stream(), connectionConsume.stream())
                .reduce(Stream::concat)
                .orElseGet(Stream::empty)
                .map(Connection::getName)
                .collect(Collectors.toList());
        if (allConnectionName.stream().anyMatch(s -> Collections.frequency(allConnectionName, s) > 1)) {
            throw new IllegalStateException("There is a duplicate connection's name.");
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
                .cloudPlatformSourceCodeLibrary(cloudPlatformSourceCodeLibrary)
                .deviceType(deviceType)
                .needBreadboard(needBreadboard)
                .breadboardPlacement(breadboardPlacement)
                .pioBoardId(pioBoardId)
                .cloudConsume(cloudConsume)
                .connectionConsume(connectionConsume)
                .connectionProvide(connectionProvide)
                .property(property)
                .compatibilityMap(compatibilityMap)
                .integratedDevices(integratedDevices)
                .build();

        actualDevice.getIntegratedDevices().forEach(inActualDevice -> inActualDevice.setParent(actualDevice));

        return actualDevice;
    }

    private Map<GenericDevice, Compatibility> loadCompatibility(JsonNode node) throws JsonProcessingException {
        YAMLMapper mapper = new YAMLMapper();
        Map<GenericDevice, Compatibility> compatibilityMap = new HashMap<>();
        for (JsonNode compatibilityNode : node.get("compatibility")) {

            /* Query GenericDevice */
            String deviceName = compatibilityNode.get("name").asText();
            GenericDevice genericDevice = DeviceLibrary.INSTANCE.getGenericDevice(deviceName);

            /* Check and Preprocess */
            createArrayNodeIfMissing(compatibilityNode, "action");
            createArrayNodeIfMissing(compatibilityNode, "condition");
            createArrayNodeIfMissing(compatibilityNode, "value");

            LinkedHashMap<Action, Map<Parameter, Constraint>> deviceAction = new LinkedHashMap<>();
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
                Map<Parameter, Constraint> supportedParam = new HashMap<>();
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

                    /* Extract Constraint */
                    Constraint constraint = parameter.get().getConstraint();
                    if (parameterNode.has("constraint")) {
                        constraint = constraint.intersect(mapper.treeToValue(parameterNode.get("constraint"), Constraint.class));
                    }

                    /* Put parameter and constraint into Map */
                    supportedParam.put(parameter.get(), parameter.get().getConstraint().intersect(constraint));
                }

                /* Put action and param to Map */
                deviceAction.put(action.get(), supportedParam);
            }

            LinkedHashMap<Condition, Map<Parameter, Constraint>> deviceCondition = new LinkedHashMap<>();
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
                Map<Parameter, Constraint> supportedParam = new HashMap<>();
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

    private List<Connection> loadConnection(JsonNode portsNode, Map<String, PinTemplate> pinTemplateMap) throws IOException {
        List<Connection> connectionList = new ArrayList<>();
        for (JsonNode portNode: portsNode) {
            throwIfMissingField(portNode, "name", id, "connection");
            throwIfMissingField(portNode, "type", id, "connection");
            throwIfMissingField(portNode, "pins", id, "connection");
            throwIfFieldIsNotArray(portNode, "pins", id, "connection");

            String connectionName = portNode.get("name").asText();
            ConnectionType connectionType = ConnectionType.valueOf(portNode.get("type").asText());

            List<Pin> pins = new ArrayList<>();
            for (JsonNode pinNode: portNode.get("pins")) {
                throwIfMissingField(pinNode, "ref_to", id, "connection","pin");

                String refTo = pinNode.get("ref_to").asText();

                throwIfMissingField(pinNode, "x", "pin", refTo, "x");
                throwIfMissingField(pinNode, "y", "pin", refTo, "y");

                if (!pinTemplateMap.containsKey(refTo)) {
                    throw new IllegalStateException("There is no " + refTo + " in the pin_template");
                }

                PinTemplate pinTemplate = pinTemplateMap.get(refTo);
                double x = pinNode.get("x").asDouble();
                double y = pinNode.get("y").asDouble();
                VoltageLevel voltageLevel = pinNode.has("voltage_level") ? VoltageLevel.valueOf(pinNode.get("voltage_level").asText()) : pinTemplate.getVoltageLevel();

                List<PinFunction> function = pinTemplate.getFunction();
                boolean hasHwSerial = function.contains(PinFunction.HW_SERIAL_TX) || function.contains(PinFunction.HW_SERIAL_RX);
                if (pinNode.has("function")) {
                    if (pinNode.get("function").isArray()) {
                        function = mapper.readValue(pinNode.get("function").traverse(), new TypeReference<List<PinFunction>>() {});
                    } else {
                        function = List.of(PinFunction.valueOf(pinNode.get("function").asText()));
                    }
                }

                pins.add(new Pin(refTo, pinTemplate.getCodingName(), voltageLevel, function, hasHwSerial, x, y));
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
