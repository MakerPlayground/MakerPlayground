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

package io.makerplayground.device.shared;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.makerplayground.device.generic.ControlType;
import io.makerplayground.device.shared.constraint.Constraint;
import io.makerplayground.device.shared.constraint.ConstraintDeserializer;

import java.io.IOException;

/**
 * A helper class used by jackson's {@link ObjectMapper} to deserialize a {@link Parameter}
 * from a json file
 */
public class ParameterDeserializer extends JsonDeserializer<Parameter> {

    @Override
    public Parameter deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        String name = node.get("name").asText();
        DataType dataType = mapper.treeToValue(node.get("datatype"), DataType.class);

        SimpleModule module = new SimpleModule();
        module.addDeserializer(Constraint.class, new ConstraintDeserializer(dataType));
        mapper.registerModule(module);
        Constraint constraint = mapper.readValue(node.get("constraint").traverse(), Constraint.class);
        ControlType controlType = mapper.treeToValue(node.get("controltype"), ControlType.class);

        Object defaultValue = null;
        switch (dataType) {
            case STRING:
            case ENUM:
            case STRING_INT_ENUM:
                defaultValue = node.get("value").asText();
                break;
            case DOUBLE:
                defaultValue = new NumberWithUnit(node.get("value").asDouble()
                        , Unit.valueOf(node.get("constraint").get("unit").asText()));
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
                throw new IllegalStateException("Error: found unknown datatype: " + dataType.name());
        }

        return new Parameter(name, dataType, defaultValue, constraint, controlType);
    }
}
