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

package io.makerplayground.device.actual;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.makerplayground.device.shared.NumberWithUnit;
import io.makerplayground.device.shared.constraint.Constraint;
import io.makerplayground.device.generic.ControlType;
import io.makerplayground.device.shared.DataType;
import io.makerplayground.device.shared.Unit;

import java.io.IOException;

public class PropertyDeserializer extends StdDeserializer<Property> {

    public PropertyDeserializer() {
        this(null);
    }

    public PropertyDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Property deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        JsonNode node = deserializationContext.readValue(jsonParser, JsonNode.class);

        String name = node.get("name").asText();
        Constraint constraint = mapper.treeToValue(node.get("constraint"), Constraint.class);
        DataType dataType = mapper.treeToValue(node.get("datatype"), DataType.class);
        ControlType controlType = mapper.treeToValue(node.get("controltype"), ControlType.class);

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
            case INTEGER_ENUM:
                defaultValue = node.get("value").asInt();
                break;
            case INTEGER:
                defaultValue = new NumberWithUnit(node.get("value").asInt()
                        , Unit.valueOf(node.get("constraint").get("unit").asText()));
                break;
            case VALUE: // TODO: prove we don't have to do anything
                break;
            default:
                throw(new IllegalStateException("Format error!!!"));
        }

        return new Property(name, defaultValue, constraint, dataType, controlType);
    }
}