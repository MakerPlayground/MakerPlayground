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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.makerplayground.device.actual.*;
import io.makerplayground.device.shared.NumberWithUnit;
import io.makerplayground.util.AzureCognitiveServices;
import io.makerplayground.util.AzureIoTHubDevice;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ProjectConfigurationSerializer extends JsonSerializer<ProjectConfiguration> {
    @Override
    public void serialize(ProjectConfiguration configuration, JsonGenerator jsonGenerator, SerializerProvider serializers) throws IOException {

        jsonGenerator.writeStartObject();
        /* platform */
        jsonGenerator.writeObjectField("platform", configuration.getPlatform());


//        /* controller */
//        if (configuration.getController() != null) {
//            jsonGenerator.writeObjectField("controller", configuration.getController().getId());
//        }

//        /* actionCompatibility */
//        var projectDeviceActionCompatibility = configuration.getActionCompatibility();
//        jsonGenerator.writeArrayFieldStart("projectDeviceActionCompatibility");
//        for (ProjectDevice projectDevice: projectDeviceActionCompatibility.keySet()) {
//            jsonGenerator.writeStartObject();
//            jsonGenerator.writeStringField("projectDevice", projectDevice.getDisplayName());
//
//            var actionCompatibility = projectDeviceActionCompatibility.get(projectDevice);
//            jsonGenerator.writeArrayFieldStart("actionCompatibility");
//            for (Action action: actionCompatibility.keySet()) {
//                jsonGenerator.writeStartObject();
//                jsonGenerator.writeStringField("action", action.getDisplayName());
//
//                var compatibility = actionCompatibility.get(action);
//                jsonGenerator.writeArrayFieldStart("compatibility");
//                for (Parameter parameter: compatibility.keySet()) {
//                    jsonGenerator.writeStringField("parameter", parameter.getDisplayName());
//                    jsonGenerator.writeObjectField("constraint", compatibility.get(parameter));
//                }
//                jsonGenerator.writeEndArray();
//                jsonGenerator.writeEndObject();
//            }
//            jsonGenerator.writeEndArray();
//            jsonGenerator.writeEndObject();
//        }
//        jsonGenerator.writeEndArray();

//        /* conditionCompatibility */
//        var projectDeviceConditionCompatibility = configuration.getConditionCompatibility();
//        jsonGenerator.writeArrayFieldStart("projectDeviceConditionCompatibility");
//        for (ProjectDevice projectDevice: projectDeviceConditionCompatibility.keySet()) {
//            jsonGenerator.writeStartObject();
//            jsonGenerator.writeStringField("projectDevice", projectDevice.getDisplayName());
//
//            var conditionCompatibility = projectDeviceConditionCompatibility.get(projectDevice);
//            jsonGenerator.writeArrayFieldStart("conditionCompatibility");
//            for (Condition condition: conditionCompatibility.keySet()) {
//                jsonGenerator.writeStartObject();
//                jsonGenerator.writeStringField("condition", condition.getDisplayName());
//
//                var compatibility = conditionCompatibility.get(condition);
//                jsonGenerator.writeArrayFieldStart("compatibility");
//                for (Parameter parameter: compatibility.keySet()) {
//                    jsonGenerator.writeStringField("parameter", parameter.getDisplayName());
//                    jsonGenerator.writeObjectField("constraint", compatibility.get(parameter));
//                }
//                jsonGenerator.writeEndArray();
//                jsonGenerator.writeEndObject();
//            }
//            jsonGenerator.writeEndArray();
//            jsonGenerator.writeEndObject();
//        }
//        jsonGenerator.writeEndArray();

        /* deviceMap */
        var deviceMap = configuration.getDeviceMap();
        jsonGenerator.writeArrayFieldStart("deviceMap");
        for (ProjectDevice projectDevice : deviceMap.keySet()) {
            ActualDevice actualDevice = deviceMap.get(projectDevice);
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("projectDevice", projectDevice.getName());
            jsonGenerator.writeStringField("actualDevice", actualDevice != null ? actualDevice.getId() : "");
            jsonGenerator.writeEndObject();
        }
        jsonGenerator.writeEndArray();

        /* devicePropertyValueMap */
        var devicePropertyValueMap = configuration.getDevicePropertyValueMap();
        jsonGenerator.writeArrayFieldStart("devicePropertyValueMap");
        for (ProjectDevice projectDevice: devicePropertyValueMap.keySet()) {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("projectDevice", projectDevice.getName());

            jsonGenerator.writeArrayFieldStart("propertyValue");
            var propertyValue = devicePropertyValueMap.get(projectDevice);
            for (Property property: propertyValue.keySet()) {
                Object value = propertyValue.get(property);
                jsonGenerator.writeStartObject();
                jsonGenerator.writeStringField("property", property.getName());
                switch (property.getDataType()) {
                    case STRING:
                    case ENUM:
                        jsonGenerator.writeStringField("value", (String) value);
                        break;
                    case INTEGER:
                    case DOUBLE:
                        NumberWithUnit numberWithUnit = (NumberWithUnit) value;
                        jsonGenerator.writeObjectFieldStart("value");
                        jsonGenerator.writeStringField("value", String.valueOf(numberWithUnit.getValue()));
                        jsonGenerator.writeObjectField("unit", numberWithUnit.getUnit());
                        jsonGenerator.writeEndObject();
                        break;
                    case INTEGER_ENUM:
                        jsonGenerator.writeNumberField("value", (Integer) value);
                        break;
                    case BOOLEAN_ENUM:
                        jsonGenerator.writeBooleanField("value", (Boolean) value);
                        break;
                    case AZURE_COGNITIVE_KEY:
                        if (value == null) {
                            jsonGenerator.writeStringField("value", "");
                        } else {
                            AzureCognitiveServices acs = (AzureCognitiveServices) value;
                            jsonGenerator.writeObjectFieldStart("value");
                            jsonGenerator.writeStringField("displayName", acs.getName());
                            jsonGenerator.writeStringField("location", acs.getLocation());
                            jsonGenerator.writeStringField("key1", acs.getKey1());
                            jsonGenerator.writeStringField("key2", acs.getKey2());
                            jsonGenerator.writeEndObject();
                        }
                        break;
                    case AZURE_IOTHUB_KEY:
                        if (value == null) {
                            jsonGenerator.writeStringField("value", "");
                        } else {
                            AzureIoTHubDevice azureIoTHubDevice = (AzureIoTHubDevice) value;
                            jsonGenerator.writeObjectFieldStart("value");
                            jsonGenerator.writeStringField("deviceId", azureIoTHubDevice.getName());
                            jsonGenerator.writeStringField("connectionString", azureIoTHubDevice.getConnectionString());
                            jsonGenerator.writeEndObject();
                        }
                        break;
                    default:
                        throw new IllegalStateException("Found invalid datatype while deserialize property");
                }
                jsonGenerator.writeEndObject();
            }
            jsonGenerator.writeEndArray();
            jsonGenerator.writeEndObject();
        }
        jsonGenerator.writeEndArray();

        /* identicalDeviceMap */
        var identicalDeviceMap = configuration.getIdenticalDeviceMap();
        jsonGenerator.writeArrayFieldStart("identicalDeviceMap");
        for (ProjectDevice projectDevice : identicalDeviceMap.keySet()) {
            ProjectDevice identicalDevice = identicalDeviceMap.get(projectDevice);
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("projectDevice", projectDevice.getName());
            jsonGenerator.writeStringField("identicalDevice", identicalDevice.getName());
            jsonGenerator.writeEndObject();
        }
        jsonGenerator.writeEndArray();

        /* devicePinPortConnections */
        jsonGenerator.writeArrayFieldStart("deviceConnection");
        for (ProjectDevice projectDevice: configuration.getDeviceConnections().keySet()) {
            DeviceConnection deviceConnection = configuration.getDeviceConnections().get(projectDevice);
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("projectDevice", projectDevice.getName());
            jsonGenerator.writeArrayFieldStart("consumerProviderConnection");
            for (Connection consumerConnection : deviceConnection.getConsumerProviderConnections().keySet()) {
                Connection providerConnection = deviceConnection.getConsumerProviderConnections().get(consumerConnection);
                jsonGenerator.writeStartObject();
                jsonGenerator.writeStringField("consumeConnectionName", consumerConnection.getName());
                jsonGenerator.writeStringField("consumeConnectionOwner", consumerConnection.getOwnerProjectDevice().getName());
                jsonGenerator.writeStringField("provideConnectionName", providerConnection.getName());
                jsonGenerator.writeStringField("provideConnectionOwner", providerConnection.getOwnerProjectDevice().getName());
                jsonGenerator.writeEndObject();
            }
            jsonGenerator.writeEndArray();
            jsonGenerator.writeArrayFieldStart("providerFunction");
            for (Connection providerConnection : deviceConnection.getProviderFunction().keySet()) {
                List<PinFunction> pinFunctions = deviceConnection.getProviderFunction().get(providerConnection);
                jsonGenerator.writeStartObject();
                jsonGenerator.writeStringField("provideConnectionName", providerConnection.getName());
                jsonGenerator.writeStringField("provideConnectionOwner", providerConnection.getOwnerProjectDevice().getName());
                jsonGenerator.writeArrayFieldStart("pinFunctions");
                for (PinFunction function: pinFunctions) {
                    jsonGenerator.writeObject(function);
                }
                jsonGenerator.writeEndArray();
                jsonGenerator.writeEndObject();
            }
            jsonGenerator.writeEndArray();
            jsonGenerator.writeEndObject();
        }
        jsonGenerator.writeEndArray();

        /* cloudParameterMap */
        var cloudParameterMap = configuration.getCloudParameterMap();
        jsonGenerator.writeArrayFieldStart("cloudParameterMap");
        for (CloudPlatform cloudPlatform : cloudParameterMap.keySet()) {
            Map<String, String> parameterMap = cloudParameterMap.get(cloudPlatform);
            jsonGenerator.writeStartObject();
            jsonGenerator.writeObjectField("cloudPlatform", cloudPlatform);
            jsonGenerator.writeArrayFieldStart("parameterMap");
            for (String parameter: parameterMap.keySet()) {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeStringField("parameter", parameter);
                jsonGenerator.writeStringField("value", parameterMap.get(parameter));
                jsonGenerator.writeEndObject();
            }
            jsonGenerator.writeEndArray();
            jsonGenerator.writeEndObject();
        }
        jsonGenerator.writeEndArray();
        jsonGenerator.writeEndObject();
    }
}
