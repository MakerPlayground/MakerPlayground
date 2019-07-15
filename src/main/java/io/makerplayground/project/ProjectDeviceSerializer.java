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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.makerplayground.device.actual.IntegratedActualDevice;
import io.makerplayground.device.actual.Property;
import io.makerplayground.device.shared.NumberWithUnit;
import io.makerplayground.util.AzureCognitiveServices;
import io.makerplayground.util.AzureIoTHubDevice;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ProjectDeviceSerializer extends StdSerializer<ProjectDevice> {
    public ProjectDeviceSerializer() { this(null); }

    public ProjectDeviceSerializer(Class<ProjectDevice> t) { super(t); }

    @Override
    public void serialize(ProjectDevice projectDevice, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringField("name", projectDevice.getName());
        jsonGenerator.writeStringField("genericDevice", projectDevice.getGenericDevice().getName());

        /* TODO: uncomment this */
//        if (projectDevice.getDependentDevice() != null) {
//            jsonGenerator.writeStringField("dependentDevice", projectDevice.getDependentDevice().getId());
//        } else {
//            jsonGenerator.writeStringField("dependentDevice", "");
//        }
//
//        jsonGenerator.writeArrayFieldStart("dependentDeviceConnection");
//        for (Map.Entry<Peripheral, List<DevicePort>> connection : projectDevice.getDependentDeviceConnection().entrySet()) {
//            jsonGenerator.writeStartObject();
//            jsonGenerator.writeStringField("devicePeripheral", connection.getKey().name());
//            //jsonGenerator.writeStringField("controllerPeripheral", connection.getValue().getDeviceId());
//            jsonGenerator.writeArrayFieldStart("controllerPeripheral");
//            for (DevicePort devicePort : connection.getValue()) {
//                mapper.writeValue(jsonGenerator, devicePort.getName());
//            }
//            jsonGenerator.writeEndArray();
//            jsonGenerator.writeEndObject();
//        }
//        jsonGenerator.writeEndArray();

        /* TODO: uncomment this */
//        jsonGenerator.writeArrayFieldStart("property");
//        if (projectDevice.isActualDeviceSelected()) {
//            for (Property property : projectDevice.getCompatibleDeviceComboItem().getProperty()) {
//                Object value = projectDevice.getPropertyValue(property);
//                jsonGenerator.writeStartObject();
//                jsonGenerator.writeStringField("name", property.getName());
//                switch (property.getDataType()) {
//                    case STRING:
//                    case ENUM:
//                        jsonGenerator.writeStringField("value", (String) value);
//                        break;
//                    case INTEGER:
//                    case DOUBLE:
//                        NumberWithUnit numberWithUnit = (NumberWithUnit) value;
//                        jsonGenerator.writeObjectFieldStart("value");
//                        jsonGenerator.writeStringField("value", String.valueOf(numberWithUnit.getValue()));
//                        jsonGenerator.writeStringField("unit", numberWithUnit.getUnit().name());
//                        jsonGenerator.writeEndObject();
//                        break;
//                    case INTEGER_ENUM:
//                        jsonGenerator.writeNumberField("value", (Integer) value);
//                        break;
//                    case BOOLEAN_ENUM:
//                        jsonGenerator.writeBooleanField("value", (Boolean) value);
//                        break;
//                    case AZURE_COGNITIVE_KEY:
//                        if (value == null) {
//                            jsonGenerator.writeStringField("value", "");
//                        } else {
//                            AzureCognitiveServices acs = (AzureCognitiveServices) value;
//                            jsonGenerator.writeObjectFieldStart("value");
//                            jsonGenerator.writeStringField("name", acs.getName());
//                            jsonGenerator.writeStringField("location", acs.getLocation());
//                            jsonGenerator.writeStringField("key1", acs.getKey1());
//                            jsonGenerator.writeStringField("key2", acs.getKey2());
//                            jsonGenerator.writeEndObject();
//                        }
//                        break;
//                    case AZURE_IOTHUB_KEY:
//                        if (value == null) {
//                            jsonGenerator.writeStringField("value", "");
//                        } else {
//                            AzureIoTHubDevice azureIoTHubDevice = (AzureIoTHubDevice) value;
//                            jsonGenerator.writeObjectFieldStart("value");
//                            jsonGenerator.writeStringField("deviceId", azureIoTHubDevice.getName());
//                            jsonGenerator.writeStringField("connectionString", azureIoTHubDevice.getConnectionString());
//                            jsonGenerator.writeEndObject();
//                        }
//                        break;
//                    default:
//                        throw new IllegalStateException("Found invalid datatype while deserialize property");
//                }
//                jsonGenerator.writeEndObject();
//            }
//        }
//        jsonGenerator.writeEndArray();

        jsonGenerator.writeEndObject();
    }
}
