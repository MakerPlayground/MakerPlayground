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
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.device.actual.*;
import io.makerplayground.device.shared.DataType;
import io.makerplayground.device.shared.NumberWithUnit;
import io.makerplayground.device.shared.Unit;
import io.makerplayground.util.AzureCognitiveServices;
import io.makerplayground.util.AzureIoTHubDevice;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.util.*;

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

        /* deviceMap */
        SortedMap<ProjectDevice, ActualDevice> deviceMap = new TreeMap<>();
        for (JsonNode deviceMapNode: node.get("deviceMap")) {
            String projectDeviceName = deviceMapNode.get("projectDevice").asText();
            String actualDeviceId = deviceMapNode.get("actualDevice").asText();
            ProjectDevice projectDevice = searchProjectDevice(projectDeviceName);
            ActualDevice actualDevice = DeviceLibrary.INSTANCE.getActualDevice(actualDeviceId);
            deviceMap.put(projectDevice, actualDevice);
        }

        /* controller */
        ActualDevice controller = deviceMap.getOrDefault(ProjectDevice.CONTROLLER, null);

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
        SortedMap<ProjectDevice, PinPortConnection> devicePinPortConnectionMap = new TreeMap<>();
        for (JsonNode devicePinPortConnectionNode: node.get("devicePinPortConnection")) {
            String projectDeviceName = devicePinPortConnectionNode.get("projectDevice").asText();
            ProjectDevice projectDevice = searchProjectDevice(projectDeviceName);

            SortedMap<Pin, Pin> pinMapConsumerProvider = new TreeMap<>();
            for (JsonNode pinMapConsumerProviderNode: devicePinPortConnectionNode.get("pinMapConsumerProvider")) {
                String consumerProjectDeviceName = pinMapConsumerProviderNode.get("pinConsumeOwner").asText();
                String consumerPinName = pinMapConsumerProviderNode.get("pinConsumeName").asText();
                ProjectDevice consumerProjectDevice = searchProjectDevice(consumerProjectDeviceName);
                ActualDevice consumerActualDevice = deviceMap.get(consumerProjectDevice);
                Optional<Pin> pinConsume = consumerActualDevice.getPinConsumeByOwnerDevice(consumerProjectDevice, consumerPinName);

                String providerProjectDeviceName = pinMapConsumerProviderNode.get("pinProvideOwner").asText();
                String providerPinName = pinMapConsumerProviderNode.get("pinProvideName").asText();
                ProjectDevice providerProjectDevice = searchProjectDevice(providerProjectDeviceName);
                ActualDevice providerActualDevice = deviceMap.get(providerProjectDevice);
                Optional<Pin> pinProvide = providerActualDevice.getPinProvideByOwnerDevice(providerProjectDevice, providerPinName);

                if (pinConsume.isEmpty() || pinProvide.isEmpty()) {
                    throw new IllegalStateException("The required pin is not in the project");
                }
                pinMapConsumerProvider.put(pinConsume.get(), pinProvide.get());
            }

            SortedMap<Port, Port> portMapConsumerProvider = new TreeMap<>();
            for (JsonNode portMapConsumerProviderNode: devicePinPortConnectionNode.get("portMapConsumerProvider")) {
                String consumerProjectDeviceName = portMapConsumerProviderNode.get("portConsumeOwner").asText();
                String consumePortName = portMapConsumerProviderNode.get("portConsumerName").asText();
                ProjectDevice consumerProjectDevice = searchProjectDevice(consumerProjectDeviceName);
                ActualDevice consumerActualDevice = deviceMap.get(consumerProjectDevice);
                Optional<Port> portConsume = consumerActualDevice.getPortConsumeByOwnerDevice(consumerProjectDevice, consumePortName);

                String providerProjectDeviceName = portMapConsumerProviderNode.get("portProvideOwner").asText();
                String providerPinName = portMapConsumerProviderNode.get("portProvideName").asText();
                ProjectDevice providerProjectDevice = searchProjectDevice(providerProjectDeviceName);
                ActualDevice providerActualDevice = deviceMap.get(providerProjectDevice);
                Optional<Port> portProvide = providerActualDevice.getPortProvideByOwnerDevice(providerProjectDevice, providerPinName);

                if (portConsume.isEmpty() || portProvide.isEmpty()) {
                    throw new IllegalStateException("The required pin is not in the project");
                }
                portMapConsumerProvider.put(portConsume.get(), portConsume.get());
            }

            devicePinPortConnectionMap.put(projectDevice, new PinPortConnection(pinMapConsumerProvider, portMapConsumerProvider));
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

        for (ProjectDevice projectDevice: devicePinPortConnectionMap.keySet()) {
            PinPortConnection connection = devicePinPortConnectionMap.get(projectDevice);
            configuration.setDevicePinPortConnection(projectDevice, connection);
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
