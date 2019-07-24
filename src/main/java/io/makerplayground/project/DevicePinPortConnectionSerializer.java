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
import io.makerplayground.device.actual.Pin;
import io.makerplayground.device.actual.Port;

import java.io.IOException;

public class DevicePinPortConnectionSerializer extends JsonSerializer<DevicePinPortConnection> {

    @Override
    public void serialize(DevicePinPortConnection devicePinPortConnection, JsonGenerator jsonGenerator, SerializerProvider serializers) throws IOException {
        jsonGenerator.writeStartObject();

        /* from */
        jsonGenerator.writeStringField("from", devicePinPortConnection.getConsumerDevice().getName());

        /* to */
        jsonGenerator.writeStringField("to", devicePinPortConnection.getProviderDevice().getName());

        /* pinMapFromTo */
        var pinMap = devicePinPortConnection.getPinMapConsumerProvider();
        jsonGenerator.writeArrayFieldStart("pinMap");
        for (Pin pinFrom: pinMap.keySet()) {
            Pin pinTo = pinMap.get(pinFrom);
            jsonGenerator.writeStringField("pinFrom", pinFrom.getName());
            jsonGenerator.writeStringField("pinTo", pinTo.getName());
        }
        jsonGenerator.writeEndArray();

        /* portMapFromTo */
        var portMap = devicePinPortConnection.getPortMapConsumerProvider();
        jsonGenerator.writeArrayFieldStart("portMap");
        for (Port portFrom: portMap.keySet()) {
            Port portTo = portMap.get(portFrom);
            jsonGenerator.writeStringField("portFrom", portFrom.getName());
            jsonGenerator.writeStringField("portTo", portTo.getName());
        }
        jsonGenerator.writeEndArray();

        jsonGenerator.writeEndObject();
    }
}
