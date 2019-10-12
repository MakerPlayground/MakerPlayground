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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.device.actual.*;
import io.makerplayground.device.shared.NumberWithUnit;
import io.makerplayground.device.shared.Unit;
import io.makerplayground.util.AzureCognitiveServices;
import io.makerplayground.util.AzureIoTHubDevice;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

public class ProjectConfigurationDeserializer extends JsonDeserializer<ProjectConfiguration> {

    private final List<ProjectDevice> deviceList;
    private final Project project;
    ObjectMapper mapper = new ObjectMapper();

    public ProjectConfigurationDeserializer(Project project) {
        this.project = project;
        this.deviceList = project.getUnmodifiableProjectDevice();
    }

    private ProjectDevice searchProjectDevice(String name) {
        if (ProjectDevice.CONTROLLER.getName().equals(name)) {
            return ProjectDevice.CONTROLLER;
        }
        Optional<ProjectDevice> projectDeviceOptional = deviceList.stream().filter(projectDevice -> projectDevice.getName().equals(name)).findFirst();
        if (projectDeviceOptional.isEmpty()) {
            throw new IllegalStateException("Couldn't find project device named: " + name);
        }
        return projectDeviceOptional.get();
    }

    @Override
    public ProjectConfiguration deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = mapper.readTree(jsonParser);
        Platform platform = Platform.valueOf(node.get("platform").asText());

        SortedMap<ProjectDevice, String> tempIntegratedDeviceMap = new TreeMap<>();

        /* deviceMap */
        SortedMap<ProjectDevice, ActualDevice> deviceMap = new TreeMap<>();
        for (JsonNode deviceMapNode: node.get("deviceMap")) {
            String projectDeviceName = deviceMapNode.get("projectDevice").asText();
            String actualDeviceId = deviceMapNode.get("actualDevice").asText();
            ProjectDevice projectDevice = searchProjectDevice(projectDeviceName);
            if (deviceMapNode.get("isIntegrated").asBoolean()) {
                tempIntegratedDeviceMap.put(projectDevice, actualDeviceId);
            } else {
                ActualDevice actualDevice = DeviceLibrary.INSTANCE.getActualDevice(actualDeviceId);
                deviceMap.put(projectDevice, actualDevice);
            }
        }

        /* controller */
        ActualDevice controller = deviceMap.getOrDefault(ProjectDevice.CONTROLLER, null);

        if (controller != null) {
            for (ProjectDevice projectDevice: tempIntegratedDeviceMap.keySet()) {
                Optional<IntegratedActualDevice> deviceOptional = controller.getIntegratedDevices(tempIntegratedDeviceMap.get(projectDevice));
                deviceOptional.ifPresent(a -> deviceMap.put(projectDevice, a));
            }
        }

        /* devicePropertyValueMap */
        Map<ProjectDevice, Map<Property, Object>> devicePropertyValueMap = new HashMap<>();
        JsonNode devicePropertyValueMapNode = node.get("devicePropertyValueMap");
        for (JsonNode devicePropertyValueEntryNode: devicePropertyValueMapNode) {
            String projectDeviceName = devicePropertyValueEntryNode.get("projectDevice").asText();
            ProjectDevice projectDevice = searchProjectDevice(projectDeviceName);
            Map<Property, Object> propertyValueMap = new HashMap<>();
            for (JsonNode propertyValueNode: devicePropertyValueEntryNode.get("propertyValue")) {
                String propertyName = propertyValueNode.get("property").asText();
                ActualDevice actualDevice = deviceMap.get(projectDevice);
                Optional<Property> p = actualDevice.getProperty(propertyName);
                if (p.isEmpty()) {
                    continue;
                }
                Object value;
                switch (p.get().getDataType()) {
                    case STRING:
                    case ENUM:
                        value = propertyValueNode.get("value").asText();
                        break;
                    case INTEGER:
                    case DOUBLE:
                        double num = propertyValueNode.get("value").get("value").asDouble();
                        Unit unit = Unit.valueOf(propertyValueNode.get("value").get("unit").asText());
                        value = new NumberWithUnit(num, unit);
                        break;
                    case INTEGER_ENUM:
                        value = propertyValueNode.get("value").asInt();
                        break;
                    case BOOLEAN_ENUM:
                        value = propertyValueNode.get("value").asBoolean();
                        break;
                    case AZURE_COGNITIVE_KEY:
                        if (propertyValueNode.get("value").has("name")) {
                            value = new AzureCognitiveServices(propertyValueNode.get("value").get("name").asText()
                                    , propertyValueNode.get("value").get("location").asText()
                                    , propertyValueNode.get("value").get("key1").asText()
                                    , propertyValueNode.get("value").get("key2").asText());
                        } else {
                            value = null;
                        }
                        break;
                    case AZURE_IOTHUB_KEY:
                        if (propertyValueNode.get("value").has("deviceId")) {
                            value = new AzureIoTHubDevice(propertyValueNode.get("value").get("deviceId").asText()
                                    , propertyValueNode.get("value").get("connectionString").asText());
                        } else {
                            value = null;
                        }
                        break;
                    default:
                        throw new IllegalStateException("Found invalid datatype while deserialize property");
                }
                propertyValueMap.put(p.get(), value);
            }
            devicePropertyValueMap.put(projectDevice, propertyValueMap);
        }

        /* identicalDeviceMap */
        SortedMap<ProjectDevice, ProjectDevice> identicalDeviceMap = new TreeMap<>();
        for (JsonNode identicalDeviceMapNode: node.get("identicalDeviceMap")) {
            String projectDeviceName = identicalDeviceMapNode.get("projectDevice").asText();
            String identicalDeviceName = identicalDeviceMapNode.get("identicalDevice").asText();
            ProjectDevice projectDevice = searchProjectDevice(projectDeviceName);
            ProjectDevice identicalDevice = searchProjectDevice(identicalDeviceName);
            identicalDeviceMap.put(projectDevice, identicalDevice);
        }

        /* devicePinPortConnections */
        SortedMap<ProjectDevice, DeviceConnection> deviceConnectionMap = new TreeMap<>();
        for (JsonNode deviceConnectionNode: node.get("deviceConnection")) {
            String projectDeviceName = deviceConnectionNode.get("projectDevice").asText();
            ProjectDevice projectDevice = searchProjectDevice(projectDeviceName);

            SortedMap<Connection, Connection> consumerProviderConnection = new TreeMap<>();
            for (JsonNode connectionConsumerProviderNode: deviceConnectionNode.get("consumerProviderConnection")) {
                String consumerProjectDeviceName = connectionConsumerProviderNode.get("consumeConnectionOwner").asText();
                String consumePortName = connectionConsumerProviderNode.get("consumeConnectionName").asText();
                ProjectDevice consumerProjectDevice = searchProjectDevice(consumerProjectDeviceName);
                ActualDevice consumerActualDevice = deviceMap.get(consumerProjectDevice);
                Optional<Connection> connectionConsume = consumerActualDevice.getConnectionConsumeByOwnerDevice(consumerProjectDevice, consumePortName);

                if (connectionConsumerProviderNode.has("provideConnectionOwner") && connectionConsumerProviderNode.has("provideConnectionName")) {
                    String providerProjectDeviceName = connectionConsumerProviderNode.get("provideConnectionOwner").asText();
                    String providerPinName = connectionConsumerProviderNode.get("provideConnectionName").asText();
                    ProjectDevice providerProjectDevice = searchProjectDevice(providerProjectDeviceName);
                    ActualDevice providerActualDevice = deviceMap.get(providerProjectDevice);
                    Optional<Connection> connectionProvide = providerActualDevice.getConnectionProvideByOwnerDevice(providerProjectDevice, providerPinName);

                    if (connectionConsume.isEmpty() || connectionProvide.isEmpty()) {
                        throw new IllegalStateException("The required connection is not in the project");
                    }
                    consumerProviderConnection.put(connectionConsume.get(), connectionProvide.get());
                } else {
                    consumerProviderConnection.put(connectionConsume.get(), null);
                }
            }

            SortedMap<Connection, List<PinFunction>> providerFunction = new TreeMap<>();
            for (JsonNode providerFunctionNode : deviceConnectionNode.get("providerFunction")) {
                List<PinFunction> pinFunctions = new ArrayList<>();

                String providerProjectDeviceName = providerFunctionNode.get("provideConnectionOwner").asText();
                String providerPinName = providerFunctionNode.get("provideConnectionName").asText();
                ProjectDevice providerProjectDevice = searchProjectDevice(providerProjectDeviceName);
                ActualDevice providerActualDevice = deviceMap.get(providerProjectDevice);
                Optional<Connection> connectionProvide = providerActualDevice.getConnectionProvideByOwnerDevice(providerProjectDevice, providerPinName);
                if (connectionProvide.isEmpty()) {
                    throw new IllegalStateException("The required connection is not in the project");
                }
                for (JsonNode pinFunctionNode : providerFunctionNode.get("pinFunctions")) {
                    pinFunctions.add(PinFunction.valueOf(pinFunctionNode.asText()));
                }
                providerFunction.put(connectionProvide.get(), pinFunctions);
            }
            deviceConnectionMap.put(projectDevice, new DeviceConnection(consumerProviderConnection, providerFunction));
        }

        SortedMap<CloudPlatform, Map<String, String>> cloudParameterMap = new TreeMap<>();
        for (JsonNode cloudParameterNode: node.get("cloudParameterMap")) {
            Map<String, String> parameterMap = new HashMap<>();
            CloudPlatform cloudPlatform = CloudPlatform.valueOf(cloudParameterNode.get("cloudPlatform").asText());
            for (JsonNode parameterNode: cloudParameterNode.get("parameterMap")) {
                String parameter = parameterNode.get("parameter").asText();
                String value = parameterNode.get("value").asText();
                parameterMap.put(parameter, value);
            }
            cloudParameterMap.put(cloudPlatform, parameterMap);
        }

        ProjectConfiguration configuration = new ProjectConfiguration(platform);
        project.setProjectConfiguration(configuration);
        project.calculateCompatibility();

        configuration.setController(controller);

        for (ProjectDevice projectDevice: deviceMap.keySet()) {
            ActualDevice actualDevice = deviceMap.get(projectDevice);
            configuration.setActualDevice(projectDevice, actualDevice);
        }

        for (ProjectDevice projectDevice: identicalDeviceMap.keySet()) {
            ProjectDevice identicalDevice = identicalDeviceMap.get(projectDevice);
            configuration.setIdenticalDevice(projectDevice, identicalDevice);
        }

        for (ProjectDevice projectDevice: deviceConnectionMap.keySet()) {
            DeviceConnection connection = deviceConnectionMap.get(projectDevice);
            configuration.setDeviceConnection(projectDevice, connection);
        }

        for (ProjectDevice projectDevice: devicePropertyValueMap.keySet()) {
            Map<Property, Object> propertyMap = devicePropertyValueMap.get(projectDevice);
            for (Property p: propertyMap.keySet()) {
                configuration.setPropertyValue(projectDevice, p, propertyMap.get(p));
            }
        }

        for (CloudPlatform cloudPlatform: cloudParameterMap.keySet()) {
            Map<String, String> parameterMap = cloudParameterMap.get(cloudPlatform);
            for (String p: parameterMap.keySet()) {
                configuration.setCloudPlatformParameter(cloudPlatform, p, parameterMap.get(p));
            }
        }

        return configuration;
    }
}
