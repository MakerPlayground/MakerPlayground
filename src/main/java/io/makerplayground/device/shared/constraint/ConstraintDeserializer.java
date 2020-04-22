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

package io.makerplayground.device.shared.constraint;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.makerplayground.device.shared.DataType;
import io.makerplayground.device.shared.Unit;

import java.io.IOException;
import java.util.*;

/**
 * A helper class used by jackson's {@link ObjectMapper} to deserialize a {@link Constraint}
 * from a json file
 */
public class ConstraintDeserializer extends JsonDeserializer<Constraint> {

    private DataType dataType;

    public ConstraintDeserializer(DataType type) {
        dataType = type;
    }

    @Override
    public Constraint deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {

        JsonNode node = deserializationContext.readValue(jsonParser, JsonNode.class);

        if (node.isArray() && node.size() == 0) {
            return Constraint.NONE;
        } else if (node.isObject()) {
            double min;
            if (node.get("min").isNumber()) {
                min = node.get("min").doubleValue();
            } else if ("MIN_DOUBLE".equals(node.get("min").asText())) {
                min = -Double.MAX_VALUE;
            } else if ("MIN_INTEGER".equals(node.get("min").asText())) {
                min = Integer.MIN_VALUE;
            } else {
                throw new IllegalArgumentException("min should be double or the reversed words only.");
            }
            double max;
            if (node.get("max").isNumber()) {
                max = node.get("max").doubleValue();
            } else if ("MAX_DOUBLE".equals(node.get("max").asText())) {
                max = Double.MAX_VALUE;
            } else if ("MAX_INTEGER".equals(node.get("max").asText())) {
                max = Integer.MAX_VALUE;
            } else {
                throw new IllegalArgumentException("max should be double or the reversed words only.");
            }
            return Constraint.createNumericConstraint(min, max, Unit.valueOf(node.get("unit").asText()));
        } else if (node.isArray() && dataType == DataType.STRING_INT_ENUM) {
            LinkedHashMap<String, Integer> values = new LinkedHashMap<>();
            for (JsonNode jn: node) {
                values.put(jn.get("key").asText(), jn.get("value").asInt());
            }
            return Constraint.createStringIntegerCategoricalConstraint(values);
        } else {
            if (dataType == DataType.INTEGER_ENUM) {
                List<Integer> valueList = new ArrayList<>();
                for (JsonNode jn : node) {
                    valueList.add(jn.asInt());
                }
                return Constraint.createIntegerCategoricalConstraint(valueList);
            } else if (dataType == DataType.BOOLEAN_ENUM){
                List<String> valueList = new ArrayList<>();
                for (JsonNode jn : node) {
                    valueList.add(jn.asText());
                }
                return Constraint.createCategoricalConstraint(valueList);
            } else {
                throw new IllegalArgumentException("The dataType of the array is " + dataType);
            }
        }
    }
}
