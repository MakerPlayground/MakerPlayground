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
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

/**
 * Created by USER on 13-Jul-17.
 */
public class NodeElementSerializer extends StdSerializer<NodeElement> {
    public NodeElementSerializer() { this(null); }

    public NodeElementSerializer(Class<NodeElement> t) { super(t); }

    @Override
    public void serialize(NodeElement nodeElement, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();

        jsonGenerator.writeObjectField("top",nodeElement.getTop());
        jsonGenerator.writeObjectField("left",nodeElement.getLeft());
        jsonGenerator.writeObjectField("width",nodeElement.getWidth());
        jsonGenerator.writeObjectField("height",nodeElement.getHeight());

        jsonGenerator.writeEndObject();
    }
}

