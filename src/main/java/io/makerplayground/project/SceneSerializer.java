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
public class SceneSerializer extends JsonSerializer<Scene> {

    @Override
    public void serialize(Scene scene, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
       ObjectMapper mapper = new ObjectMapper();

       jsonGenerator.writeStartObject();

       jsonGenerator.writeStringField("name",scene.getName());

       jsonGenerator.writeArrayFieldStart("setting");
       for (UserSetting setting : scene.getVirtualDeviceSetting()) {
          mapper.writeValue(jsonGenerator, setting);
       }
       for (UserSetting setting : scene.getSetting()) {
          mapper.writeValue(jsonGenerator, setting);
       }
       jsonGenerator.writeEndArray();

       jsonGenerator.writeObjectFieldStart("position");
       jsonGenerator.writeNumberField("top",scene.getTop());
       jsonGenerator.writeNumberField("left",scene.getLeft());
       jsonGenerator.writeNumberField("width",scene.getWidth());
       jsonGenerator.writeNumberField("height",scene.getHeight());
       jsonGenerator.writeEndObject();

       jsonGenerator.writeEndObject();
   }
}
