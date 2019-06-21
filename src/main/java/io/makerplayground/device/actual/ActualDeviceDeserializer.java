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
import com.fasterxml.jackson.databind.ObjectMapper;
import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.device.generic.GenericDevice;
import io.makerplayground.device.shared.Action;
import io.makerplayground.device.shared.Condition;
import io.makerplayground.device.shared.Parameter;
import io.makerplayground.device.shared.Value;
import io.makerplayground.device.shared.constraint.Constraint;

import java.io.IOException;
import java.util.*;

import static io.makerplayground.util.DeserializerHelper.*;

public class ActualDeviceDeserializer extends JsonDeserializer<ActualDevice> {

    private String id;

    @Override
    public ActualDevice deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

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
        throwIfOneOfTheseFieldsNotExist(node, List.of("pin_provide", "pin_consume", "pin_unused"), id);
        createArrayNodeIfMissing(node, "pin_provide", "pin_consume", "pin_unused");
        createArrayNodeIfMissing(node, "port_provide", "port_consume");
        createArrayNodeIfMissing(node, "property");
        createArrayNodeIfMissing(node, "compatibility");
        createArrayNodeIfMissing(node, "integrated_devices");

        /* DeviceType */
        DeviceType deviceType = DeviceType.valueOf(node.get("type").asText());

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
        CloudPlatform cloudConsume = node.has("cloudConsume") ? CloudPlatform.valueOf(node.get("cloudConsume").asText()) : null;

        /* Pin, Port, Property */
        List<Pin> pinProvide = mapper.readValue(node.get("pin_provide").traverse(), new TypeReference<List<Pin>>() {});
        List<Pin> pinConsume = mapper.readValue(node.get("pin_consume").traverse(), new TypeReference<List<Pin>>() {});
        List<Pin> pinUnused = mapper.readValue(node.get("pin_unused").traverse(), new TypeReference<List<Pin>>() {});
        List<Port> portProvide = loadPort(node.get("port_provide"), pinProvide);
        List<Port> portConsume = loadPort(node.get("port_consume"), pinConsume);
        List<Property> property = mapper.readValue(node.get("property").traverse(), new TypeReference<List<Property>>() {});

        if (pinProvide.isEmpty()) { pinProvide = null; }
        if (pinConsume.isEmpty()) { pinConsume = null; }
        if (pinUnused.isEmpty()) { pinUnused = null; }
        if (portProvide.isEmpty()) { portProvide = null; }
        if (portConsume.isEmpty()) { portConsume = null; }
        if (property.isEmpty()) { property = null; }

        /* Compatibility */
        Map<GenericDevice, Compatibility> compatibilityMap = loadCompatibility(node);

        List<IntegratedActualDevice> integratedDevices = new ArrayList<>();
        for (JsonNode inNode : node.get("integrated_devices")) {

            /* Check for name */
            throwIfMissingField(inNode, "name", id, "integrated_device");

            /* Extract Device Name */
            String inDeviceName = inNode.get("name").asText();

            throwIfOneOfTheseFieldsNotExist(node, List.of("pin_provide", "pin_consume", "pin_unused"), id, "integrated_device", inDeviceName);
            createArrayNodeIfMissing(inNode, "pin_provide", "pin_consume", "pin_unused");
            createArrayNodeIfMissing(inNode, "port_provide", "port_consume");
            createArrayNodeIfMissing(inNode, "property");
            createArrayNodeIfMissing(inNode, "compatibility");

            /* Check for platforms library */
            throwIfMissingField(inNode, "platforms", id, "integrated_device", inDeviceName);
            throwIfFieldIsNotArray(inNode, "platforms", id, "integrated_device", inDeviceName);

            /* Extract platformSourceCodeLibrary */
            Map<Platform, SourceCodeLibrary> inPlatformSourceCodeLibrary = new HashMap<>();
            for (JsonNode platform_node: inNode.get("platforms")) {
                Platform platform = Platform.valueOf(platform_node.get("platform").asText());
                String classname = platform_node.get("classname").asText();
                List<String> externalLibraryList = mapper.readValue(platform_node.get("library_dependency").traverse()
                        , new TypeReference<List<String>>() {});
                inPlatformSourceCodeLibrary.put(platform, new SourceCodeLibrary(classname, externalLibraryList));
            }

            /* Extract cloudPlatformSourceCodeLibrary */
            Map<CloudPlatform, SourceCodeLibrary> inCloudPlatformSourceCodeLibrary = new HashMap<>();
            for (JsonNode platform_node : inNode.get("cloud_provide")) {
                CloudPlatform platform = CloudPlatform.valueOf(platform_node.get("cloud_platform").asText());
                String classname = platform_node.get("classname").asText();
                List<String> externalLibraryList = mapper.readValue(platform_node.get("library_dependency").traverse()
                        , new TypeReference<List<String>>() {});
                inCloudPlatformSourceCodeLibrary.put(platform, new SourceCodeLibrary(classname, externalLibraryList));
            }

            /* CloudPlatform */
            CloudPlatform inCloudConsume = inNode.has("cloudConsume") ? CloudPlatform.valueOf(node.get("cloudConsume").asText()) : null;

            /* Pin, Port, Property */
            List<Pin> inPinProvide = mapper.readValue(inNode.get("pinProvide").traverse(), new TypeReference<List<Pin>>() {});
            List<Pin> inPinConsume = mapper.readValue(inNode.get("pinConsume").traverse(), new TypeReference<List<Pin>>() {});
            List<Pin> inPinUnused = mapper.readValue(inNode.get("pinUnused").traverse(), new TypeReference<List<Pin>>() {});
            List<Port> inPortProvide = loadPort(inNode.get("portProvide"), pinProvide);
            List<Port> inPortConsume = loadPort(inNode.get("portConsume"), pinConsume);
            List<Property> inProperty = mapper.readValue(inNode.get("property").traverse(), new TypeReference<List<Property>>() {});

            if (inPinProvide.isEmpty()) { inPinProvide = null; }
            if (inPinConsume.isEmpty()) { inPinConsume = null; }
            if (inPinUnused.isEmpty()) { inPinUnused = null; }
            if (inPortProvide.isEmpty()) { inPortProvide = null; }
            if (inPortConsume.isEmpty()) { inPortConsume = null; }
            if (inProperty.isEmpty()) { inProperty = null; }

            /* Compatibility */
            Map<GenericDevice, Compatibility> inCompatibilityMap = loadCompatibility(inNode);

            IntegratedActualDevice.IntegratedActualDeviceBuilder()
                    .id(inDeviceName)
                    .brand("")
                    .model("")
                    .url("")
                    .width(0.0)
                    .height(0.0)
                    .platformSourceCodeLibrary(inPlatformSourceCodeLibrary)
                    .cloudPlatformSourceCodeLibrary(inCloudPlatformSourceCodeLibrary)
                    .deviceType(DeviceType.MODULE)
                    .pioBoardId("")
                    .cloudConsume(inCloudConsume)
                    .pinProvide(inPinProvide)
                    .pinConsume(inPinConsume)
                    .pinUnused(inPinUnused)
                    .portConsume(inPortConsume)
                    .portProvide(inPortProvide)
                    .property(inProperty)
                    .compatibilityMap(inCompatibilityMap)
                    .integratedDevices(Collections.emptyList())
                    .build();
        }

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
                .pioBoardId(pioBoardId)
                .cloudConsume(cloudConsume)
                .pinProvide(pinProvide)
                .pinConsume(pinConsume)
                .pinUnused(pinUnused)
                .portConsume(portConsume)
                .portProvide(portProvide)
                .property(property)
                .compatibilityMap(compatibilityMap)
                .integratedDevices(integratedDevices)
                .build();

        actualDevice.getIntegratedDevices().forEach(inActualDevice -> inActualDevice.setParent(actualDevice));

        return actualDevice;

//        List<IntegratedActualDevice> integratedDevices = new ArrayList<>();
//        if (node.has("integrated_device")) {
//            for (JsonNode deviceNode : node.get("integrated_device")) {
//                String integratedDeviceName = deviceNode.get("name").asText();
//                Map<Platform, String> integratedLibrary = new HashMap<>();
//                Map<Platform, List<String>> integratedExternalLibrary = new HashMap<>();
//                for (JsonNode platform_node: deviceNode.get("platforms")) {
//                    Platform platform = Platform.valueOf(platform_node.get("platform").asText());
//                    String classname = platform_node.get("classname").asText();
//                    List<String> externalLibraryList = mapper.readValue(platform_node.get("library_dependency").traverse()
//                            , new TypeReference<List<String>>() {});
//                    integratedLibrary.put(platform, classname);
//                    integratedExternalLibrary.put(platform, externalLibraryList);
//                }
//
//                List<DevicePort> integratedPort = mapper.readValue(deviceNode.get("port").traverse(),
//                        new TypeReference<List<DevicePort>>() {});
//                List<Peripheral> integratedConnectivity = mapper.readValue(deviceNode.get("connectivity").traverse(),
//                        new TypeReference<List<Peripheral>>() {});
//
//                Map<GenericDevice, Map<Action, Map<Parameter, Constraint>>> integratedSupportedDeviceaction = new HashMap<>();
//                Map<GenericDevice, Map<Action, Map<Parameter, Constraint>>> integratedSupportedDeviceCondition = new HashMap<>();
//                Map<GenericDevice, Map<Value, Constraint>> integratedSupportedDeviceValue = new HashMap<>();
//                readCompatibilityField(mapper, deviceNode, integratedSupportedDeviceaction,
//                        integratedSupportedDeviceCondition, integratedSupportedDeviceValue);
//
//                integratedDevices.add(new IntegratedActualDevice(integratedDeviceName, integratedLibrary,
//                        integratedExternalLibrary, integratedPort, integratedConnectivity,
//                        integratedSupportedDeviceaction, integratedSupportedDeviceCondition, integratedSupportedDeviceValue));
//            }
//        }
    }

    private Map<GenericDevice, Compatibility> loadCompatibility(JsonNode node) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Map<GenericDevice, Compatibility> compatibilityMap = new HashMap<>();
        for (JsonNode compatibilityNode : node.get("compatibility")) {

            /* Query GenericDevice */
            String deviceName = compatibilityNode.get("name").asText();
            GenericDevice genericDevice = DeviceLibrary.INSTANCE.getGenericDevice(deviceName);

            /* Check and Preprocess */
            createArrayNodeIfMissing(compatibilityNode, "action");
            createArrayNodeIfMissing(compatibilityNode, "condition");
            createArrayNodeIfMissing(compatibilityNode, "value");

            Map<Action, Map<Parameter, Constraint>> deviceAction = new HashMap<>();
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
                    Constraint constraint = parameterNode.has("constraint") ? mapper.treeToValue(parameterNode.get("constraint"), Constraint.class) : Constraint.NONE;

                    /* Put parameter and constraint into Map */
                    supportedParam.put(parameter.get(), constraint);
                }

                /* Put action and param to Map */
                deviceAction.put(action.get(), supportedParam);
            }

            Map<Condition, Map<Parameter, Constraint>> deviceCondition = new HashMap<>();
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
                        throw new IllegalStateException("There is no parameter '" + parameterName + "' for action '" + conditionName + "' ");
                    }

                    /* Extract Constraint */
                    Constraint constraint = parameterNode.has("constraint") ? mapper.treeToValue(parameterNode.get("constraint"), Constraint.class) : Constraint.NONE;

                    /* Put parameter and constraint into Map */
                    supportedParam.put(parameter.get(), constraint);
                }

                /* Put action and param to Map */
                deviceCondition.put(condition.get(), supportedParam);
            }

            Map<Value, Constraint> deviceValue = new HashMap<>();
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

    private List<Port> loadPort(JsonNode portsNode, List<Pin> pinList) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<Port> portList = new ArrayList<>();
        for (JsonNode portNode: portsNode) {
            throwIfMissingField(portNode, "name", id, "port");
            throwIfMissingField(portNode, "type", id, "port");
            throwIfMissingField(portNode, "elements", id, "port");
            throwIfFieldIsNotArray(portNode, "elements", id, "port");

            String portName = portNode.get("name").asText();

            PortConnectionType portConnectionType = PortConnectionType.valueOf(portNode.get("type").asText());
            List<Pin> portElements = new ArrayList<>();

            List<String> pinNames = mapper.readValue(portNode.get("elements").traverse(), new TypeReference<List<String>>() {});
            for(Pin pin: pinList) {
                if (pinNames.contains(pin.getName())) {
                    portElements.add(pin);
                }
            }
            portList.add(new Port(portName, portConnectionType, portElements));
        }
        return portList;
    }
}
