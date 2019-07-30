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
import com.fasterxml.jackson.databind.ObjectMapper;
import io.makerplayground.device.actual.Pin;
import io.makerplayground.device.actual.Port;

import java.io.IOException;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class PinPortConnectionDeserializer extends JsonDeserializer<PinPortConnection> {

    ObjectMapper mapper = new ObjectMapper();
    private final List<ProjectDevice> deviceList;

    PinPortConnectionDeserializer(Project project) {
        this.deviceList = project.getUnmodifiableProjectDevice();
    }

    @Override
    public PinPortConnection deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        SortedMap<Pin, Pin> pinMapConsumerProvider = new TreeMap<>();
        SortedMap<Port, Port> portMapConsumerProvider = new TreeMap<>();



        return new PinPortConnection(pinMapConsumerProvider, portMapConsumerProvider);
    }
}
