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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * Created by USER on 13-Jul-17.
 */
public class ConditionSerializer extends JsonSerializer<Condition> {
    @Override
    public void serialize(Condition condition, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringField("name", condition.getName());

        jsonGenerator.writeArrayFieldStart("setting");
        for (UserSetting setting : condition.getSetting()) {
            mapper.writeValue(jsonGenerator,setting);
        }
        jsonGenerator.writeEndArray();

        jsonGenerator.writeObjectFieldStart("position");
        jsonGenerator.writeNumberField("top",condition.getTop());
        jsonGenerator.writeNumberField("left",condition.getLeft());
        jsonGenerator.writeNumberField("width",condition.getWidth());
        jsonGenerator.writeNumberField("height",condition.getHeight());
        jsonGenerator.writeEndObject();

        jsonGenerator.writeEndObject();
    }
}
