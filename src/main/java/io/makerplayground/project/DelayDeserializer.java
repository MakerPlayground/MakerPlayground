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
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.makerplayground.device.shared.DelayUnit;

import java.io.IOException;

public class DelayDeserializer extends JsonDeserializer<Delay> {
    private final Project project;
    private ObjectMapper mapper = new ObjectMapper();

    DelayDeserializer(Project project) {
        this.project = project;
    }

    @Override
    public Delay deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode node = mapper.readTree(jsonParser);
        String name = node.get("name").asText();

        double delay = node.get("delay").asDouble();
        DelayUnit delayUnit = DelayUnit.valueOf(node.get("delayUnit").asText());

        JsonNode positionNode = node.get("position");
        double top = positionNode.get("top").asDouble();
        double left = positionNode.get("left").asDouble();
        double width = positionNode.get("width").asDouble();
        double height = positionNode.get("height").asDouble();

        return new Delay(top, left, width, height, name, delay, delayUnit, project);
    }
}
