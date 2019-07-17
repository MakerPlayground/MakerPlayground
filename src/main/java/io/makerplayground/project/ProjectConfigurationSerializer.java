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
import io.makerplayground.device.actual.ActualDevice;
import io.makerplayground.device.actual.CloudPlatform;
import io.makerplayground.device.actual.Property;
import io.makerplayground.device.shared.Action;
import io.makerplayground.device.shared.Condition;
import io.makerplayground.device.shared.Parameter;

import java.io.IOException;
import java.util.Map;

public class ProjectConfigurationSerializer extends JsonSerializer<ProjectConfiguration> {
    @Override
    public void serialize(ProjectConfiguration configuration, JsonGenerator jsonGenerator, SerializerProvider serializers) throws IOException {

        jsonGenerator.writeStartObject();
        /* platform */
        jsonGenerator.writeObjectField("platform", configuration.getPlatform());

        /* controller */
        if (configuration.getController() != null) {
            jsonGenerator.writeObjectField("controller", configuration.getController().getId());
        }

//        /* actionCompatibility */
//        var projectDeviceActionCompatibility = configuration.getActionCompatibility();
//        jsonGenerator.writeArrayFieldStart("projectDeviceActionCompatibility");
//        for (ProjectDevice projectDevice: projectDeviceActionCompatibility.keySet()) {
//            jsonGenerator.writeStartObject();
//            jsonGenerator.writeStringField("projectDevice", projectDevice.getName());
//
//            var actionCompatibility = projectDeviceActionCompatibility.get(projectDevice);
//            jsonGenerator.writeArrayFieldStart("actionCompatibility");
//            for (Action action: actionCompatibility.keySet()) {
//                jsonGenerator.writeStartObject();
//                jsonGenerator.writeStringField("action", action.getName());
//
//                var compatibility = actionCompatibility.get(action);
//                jsonGenerator.writeArrayFieldStart("compatibility");
//                for (Parameter parameter: compatibility.keySet()) {
//                    jsonGenerator.writeStringField("parameter", parameter.getName());
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
//            jsonGenerator.writeStringField("projectDevice", projectDevice.getName());
//
//            var conditionCompatibility = projectDeviceConditionCompatibility.get(projectDevice);
//            jsonGenerator.writeArrayFieldStart("conditionCompatibility");
//            for (Condition condition: conditionCompatibility.keySet()) {
//                jsonGenerator.writeStartObject();
//                jsonGenerator.writeStringField("condition", condition.getName());
//
//                var compatibility = conditionCompatibility.get(condition);
//                jsonGenerator.writeArrayFieldStart("compatibility");
//                for (Parameter parameter: compatibility.keySet()) {
//                    jsonGenerator.writeStringField("parameter", parameter.getName());
//                    jsonGenerator.writeObjectField("constraint", compatibility.get(parameter));
//                }
//                jsonGenerator.writeEndArray();
//                jsonGenerator.writeEndObject();
//            }
//            jsonGenerator.writeEndArray();
//            jsonGenerator.writeEndObject();
//        }
//        jsonGenerator.writeEndArray();

        /* devicePropertyValueMap */
        var devicePropertyValueMap = configuration.getDevicePropertyValueMap();
        jsonGenerator.writeArrayFieldStart("devicePropertyValueMap");
        for (ProjectDevice projectDevice: devicePropertyValueMap.keySet()) {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("projectdevice", projectDevice.getName());

            jsonGenerator.writeArrayFieldStart("propertyValue");
            var propertyValue = devicePropertyValueMap.get(projectDevice);
            for (Property property: propertyValue.keySet()) {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeStringField("property", property.getName());
                jsonGenerator.writeObjectField("value", propertyValue.get(property));
                jsonGenerator.writeEndObject();
            }
            jsonGenerator.writeEndArray();
            jsonGenerator.writeEndObject();
        }
        jsonGenerator.writeEndArray();

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

        /* sameDeviceMap */
        var sameDeviceMap = configuration.getSameDeviceMap();
        jsonGenerator.writeArrayFieldStart("sameDeviceMap");
        for (ProjectDevice projectDevice : sameDeviceMap.keySet()) {
            ProjectDevice sameDevice = sameDeviceMap.get(projectDevice);
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("projectDevice", projectDevice.getName());
            jsonGenerator.writeStringField("sameDevice", sameDevice.getName());
            jsonGenerator.writeEndObject();
        }
        jsonGenerator.writeEndArray();

        /* devicePinPortConnections */
        jsonGenerator.writeArrayFieldStart("devicePinPortConnection");
        for (ProjectDevice projectDevice: configuration.getDevicePinPortConnections().keySet()) {
            DevicePinPortConnection devicePinPortConnection = configuration.getDevicePinPortConnections().get(projectDevice);
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("projectDevice", projectDevice.getName());
            jsonGenerator.writeObjectField("pinPortConnection", devicePinPortConnection);
            jsonGenerator.writeEndObject();
        }
        jsonGenerator.writeEndArray();

        /* cloudParameterMap */
        var cloudParameterMap = configuration.getCloudParameterMap();
        jsonGenerator.writeArrayFieldStart("cloudParameterMap");
        for (CloudPlatform cloudPlatform : cloudParameterMap.keySet()) {
            Map<String, String> parameterMap = cloudParameterMap.get(cloudPlatform);
            jsonGenerator.writeStartObject();
            jsonGenerator.writeObjectField("cloudplatform", cloudPlatform);
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
