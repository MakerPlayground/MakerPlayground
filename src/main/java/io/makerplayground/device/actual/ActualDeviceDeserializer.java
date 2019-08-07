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

package io.makerplayground.device.actual;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.device.generic.GenericDevice;
import io.makerplayground.device.shared.Action;
import io.makerplayground.device.shared.Parameter;
import io.makerplayground.device.shared.Value;
import io.makerplayground.device.shared.constraint.Constraint;

import java.io.IOException;
import java.util.*;

/**
 * Created by Nititorn on 7/7/2017.
 */
public class ActualDeviceDeserializer extends StdDeserializer<ActualDevice> {

    public ActualDeviceDeserializer() {
        this(null);
    }

    public ActualDeviceDeserializer(Class<ActualDevice> t) {
        super(t);
    }

    @Override
    public ActualDevice deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        String id = node.get("id").asText();
        String brand = node.get("brand").asText();
        String model = node.get("model").asText();
        String url = node.get("url").asText();
        double width = node.get("width").asDouble();
        double height = node.get("height").asDouble();

        Map<Platform, String> classnames = new HashMap<>();
        Map<Platform, List<String>> externalLibraries = new HashMap<>();
        for (JsonNode platform_node: node.get("platforms")) {
            Platform platform = Platform.valueOf(platform_node.get("platform").asText());
            String classname = platform_node.get("classname").asText();
            List<String> externalLibraryList = mapper.readValue(platform_node.get("library_dependency").traverse()
                    , new TypeReference<List<String>>() {});
            classnames.put(platform, classname);
            externalLibraries.put(platform, externalLibraryList);
        }

        DeviceType type = DeviceType.valueOf(node.get("type").asText());
        WiringMethod wiringMethod = null;
        String pioBoardId = "";
        if (type == DeviceType.CONTROLLER) {
            if (node.has("pio_boardid")) {
                pioBoardId = node.get("pio_boardid").asText();
            } else {
                throw new IllegalStateException("Missing field 'pio_boardid' for device id = " + id);
            }
            if (node.has("wiring_method")) {
                wiringMethod = WiringMethod.valueOf(node.get("wiring_method").asText());
            } else {
                throw new IllegalStateException("Missing field 'wiring_method' for device id = " + id);
            }
        }
        FormFactor formFactor = FormFactor.valueOf(node.get("formfactor").asText());
        CloudPlatform cloudPlatform = null;
        if (node.has("cloud_platform")) {
            cloudPlatform = CloudPlatform.valueOf(node.get("cloud_platform").asText());
        }
        List<DevicePort> port = mapper.readValue(node.get("port").traverse()
                , new TypeReference<List<DevicePort>>() {});
        List<Peripheral> connectivity = mapper.readValue(node.get("connectivity").traverse()
                , new TypeReference<List<Peripheral>>() {});
        List<Property> property = mapper.readValue(node.get("property").traverse()
                , new TypeReference<List<Property>>() {});


        Map<GenericDevice, Map<Action, Map<Parameter, Constraint>>> supportedDeviceaction = new HashMap<>();
        Map<GenericDevice, Map<Action, Map<Parameter, Constraint>>> supportedDeviceCondition = new HashMap<>();
        Map<GenericDevice, Map<Value, Constraint>> supportedDeviceValue = new HashMap<>();
        readCompatibilityField(mapper, node, supportedDeviceaction, supportedDeviceCondition, supportedDeviceValue);

        List<IntegratedActualDevice> integratedDevices = new ArrayList<>();
        if (node.has("integrated_device")) {
            for (JsonNode deviceNode : node.get("integrated_device")) {
                String integratedDeviceName = deviceNode.get("name").asText();
                Map<Platform, String> integratedLibrary = new HashMap<>();
                Map<Platform, List<String>> integratedExternalLibrary = new HashMap<>();
                for (JsonNode platform_node: deviceNode.get("platforms")) {
                    Platform platform = Platform.valueOf(platform_node.get("platform").asText());
                    String classname = platform_node.get("classname").asText();
                    List<String> externalLibraryList = mapper.readValue(platform_node.get("library_dependency").traverse()
                            , new TypeReference<List<String>>() {});
                    integratedLibrary.put(platform, classname);
                    integratedExternalLibrary.put(platform, externalLibraryList);
                }

                List<Property> integratedProperty;
                if (deviceNode.has("property")) {
                    integratedProperty = mapper.readValue(deviceNode.get("property").traverse(), new TypeReference<List<Property>>() {});
                } else {
                    integratedProperty = Collections.emptyList();
                }
                List<DevicePort> integratedPort = mapper.readValue(deviceNode.get("port").traverse(),
                        new TypeReference<List<DevicePort>>() {});
                List<Peripheral> integratedConnectivity = mapper.readValue(deviceNode.get("connectivity").traverse(),
                        new TypeReference<List<Peripheral>>() {});

                Map<GenericDevice, Map<Action, Map<Parameter, Constraint>>> integratedSupportedDeviceaction = new HashMap<>();
                Map<GenericDevice, Map<Action, Map<Parameter, Constraint>>> integratedSupportedDeviceCondition = new HashMap<>();
                Map<GenericDevice, Map<Value, Constraint>> integratedSupportedDeviceValue = new HashMap<>();
                readCompatibilityField(mapper, deviceNode, integratedSupportedDeviceaction,
                        integratedSupportedDeviceCondition, integratedSupportedDeviceValue);

                integratedDevices.add(new IntegratedActualDevice(integratedDeviceName, integratedLibrary,
                        integratedExternalLibrary, integratedPort, integratedConnectivity, integratedProperty,
                        integratedSupportedDeviceaction, integratedSupportedDeviceCondition, integratedSupportedDeviceValue));
            }
        }

        Map<CloudPlatform, CloudPlatformLibrary> supportedCloudPlatform = new HashMap<>();
        if (type == DeviceType.CONTROLLER) {
            if (node.has("support_cloudplatform") && node.get("support_cloudplatform").isArray()) {
                for (JsonNode cloudPlatformNode : node.get("support_cloudplatform")) {
                    CloudPlatform cloudPlatformKey = mapper.treeToValue(cloudPlatformNode.get("cloud_platform"), CloudPlatform.class);
                    String className = mapper.treeToValue(cloudPlatformNode.get("classname"), String.class);
                    List<String> cloudPlatformDependency = mapper.readValue(cloudPlatformNode.get("library_dependency").traverse(), new TypeReference<List<String>>() {});
                    CloudPlatformLibrary cloudPlatformLibrary = new CloudPlatformLibrary(className, cloudPlatformDependency);
                    supportedCloudPlatform.put(cloudPlatformKey, cloudPlatformLibrary);
                }
            }
        }

        return new ActualDevice(id, brand, model, url, width, height, type, pioBoardId, wiringMethod, formFactor, classnames, externalLibraries,
                cloudPlatform, port, connectivity, supportedDeviceaction,
                supportedDeviceCondition, supportedDeviceValue, property, supportedCloudPlatform, integratedDevices);
    }

    private void readCompatibilityField(ObjectMapper mapper, JsonNode node, Map<GenericDevice, Map<Action, Map<Parameter, Constraint>>> supportedDeviceaction, Map<GenericDevice, Map<Action, Map<Parameter, Constraint>>> supportedDeviceCondition, Map<GenericDevice, Map<Value, Constraint>> supportedDeviceValue) throws JsonProcessingException {
        for (JsonNode deviceNode : node.get("compatibility")) {
            String deviceName = deviceNode.get("name").asText();
            GenericDevice genericDevice = DeviceLibrary.INSTANCE.getGenericDevice(deviceName);

            Map<Action, Map<Parameter, Constraint>> supportedAction = new HashMap<>();
            for (JsonNode actionNode : deviceNode.get("action")) {
                String actionName = actionNode.get("name").asText();
                Action action = genericDevice.getAction(actionName);
                if (action == null) {
                    continue;
                }
                Map<Parameter, Constraint> supportedParam = new HashMap<>();
                for (JsonNode parameterNode : actionNode.get("parameter")) {
                    String parameterName = parameterNode.get("name").asText();
                    Constraint constraint = mapper.treeToValue(parameterNode.get("constraint"), Constraint.class);
                    Parameter parameter = action.getParameter(parameterName);
                    supportedParam.put(parameter, constraint);
                }
                supportedAction.put(action, supportedParam);
            }
            supportedDeviceaction.put(genericDevice, supportedAction);

            Map<Action, Map<Parameter, Constraint>> supportedCondition = new HashMap<>();
            for (JsonNode actionNode : deviceNode.get("action")) {  // the node name hasn't been changed
                String actionName = actionNode.get("name").asText();
                Action condition = genericDevice.getCondition(actionName);
                if (condition == null) {
                    continue;
                }
                Map<Parameter, Constraint> supportedParam = new HashMap<>();
                for (JsonNode parameterNode : actionNode.get("parameter")) {
                    String parameterName = parameterNode.get("name").asText();
                    Constraint constraint = mapper.treeToValue(parameterNode.get("constraint"), Constraint.class);
                    Parameter parameter = condition.getParameter(parameterName);
                    supportedParam.put(parameter, constraint);
                }
                supportedCondition.put(condition, supportedParam);
            }
            supportedDeviceCondition.put(genericDevice, supportedCondition);

            Map<Value, Constraint> supportedValue = new HashMap<>();
            for (JsonNode valueNode : deviceNode.get("value")) {
                String valueName = valueNode.get("name").asText();
                //System.out.println(actionName);
                Constraint constraint = mapper.treeToValue(valueNode.get("constraint"), Constraint.class);
                //System.out.println(constraint);
                Value value = genericDevice.getValue(valueName);
                supportedValue.put(value, constraint);
            }
            supportedDeviceValue.put(genericDevice, supportedValue);
        }
    }
}