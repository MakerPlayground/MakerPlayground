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
import io.makerplayground.device.generic.GenericDevice;

import java.io.IOException;

public class ProjectDeviceDeserializer extends JsonDeserializer<ProjectDevice> {
    @Override
    public ProjectDevice deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(jsonParser);

        String name = node.get("name").asText();
        if (ProjectDevice.CONTROLLER.getName().equals(name)) {
            return ProjectDevice.CONTROLLER;
        }

        GenericDevice genericDevice = DeviceLibrary.INSTANCE.getGenericDevice(node.get("genericDevice").asText());
        return new ProjectDevice(name, genericDevice);
    }
}
