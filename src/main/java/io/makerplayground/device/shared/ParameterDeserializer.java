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

package io.makerplayground.device.shared;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.makerplayground.device.shared.constraint.Constraint;
import io.makerplayground.device.generic.ControlType;

import java.io.IOException;
import java.util.List;

/**
 * A helper class used by jackson's {@link ObjectMapper} to deserialize a {@link Parameter}
 * from a json file
 */
public class ParameterDeserializer extends StdDeserializer<Parameter> {

    public ParameterDeserializer() {
        this(null);
    }

    public ParameterDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Parameter deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        String name = node.get("name").asText();
        Constraint constraint = mapper.treeToValue(node.get("constraint"), Constraint.class);
        DataType dataType = mapper.treeToValue(node.get("datatype"), DataType.class);
        List<ControlType> controlType = mapper.readValue(node.get("controltype").traverse(), new TypeReference<List<ControlType>>() {});

        if (controlType.isEmpty()) {
            throw new IllegalStateException("controltype is not specified.");
        }

        Object defaultValue = null;
        switch (dataType) {
            case STRING:
                defaultValue = node.get("value").asText();
                break;
            case DOUBLE:
                defaultValue = new NumberWithUnit(node.get("value").asDouble()
                        , Unit.valueOf(node.get("constraint").get("unit").asText()));
                break;
            case ENUM:
                defaultValue = node.get("value").asText();
                break;
            case INTEGER:
                defaultValue = new NumberWithUnit(node.get("value").asInt()
                        , Unit.valueOf(node.get("constraint").get("unit").asText()));
                break;
            case INTEGER_ENUM:
                defaultValue = node.get("value").asInt();
                break;
            case DATETIME:
                defaultValue = RealTimeClock.getDefault();
                break;
            case IMAGE:
                defaultValue = null;
                break;
            case RECORD:
                defaultValue = new Record();
                break;
            default:
                throw new IllegalStateException("Error: found unknown datatype!!!");
        }

        return new Parameter(name, defaultValue, constraint, dataType, controlType);
    }
}
