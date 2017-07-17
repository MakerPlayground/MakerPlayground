/*
 * Copyright 2017 The Maker Playground Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.makerplayground.device;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.makerplayground.helper.Unit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A helper class used by jackson's {@link ObjectMapper} to deserialize a {@link Constraint}
 * from a json file
 */
public class ConstraintDeserializer extends StdDeserializer<Constraint> {
    public ConstraintDeserializer() {
        this(null);
    }

    public ConstraintDeserializer(Class<Constraint> t) {
        super(t);
    }

    @Override
    public Constraint deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        if (node.isArray() && node.size() == 0) {
            return Constraint.NONE;
        } else if (node.isObject()) {
            return Constraint.createNumericConstraint(node.get("min").doubleValue(), node.get("max").doubleValue()
                    , Unit.valueOf(node.get("unit").asText()));
        } else {
            List<String> valueList = new ArrayList<>();
            for (JsonNode jn : node) {
                valueList.add(jn.asText());
            }
            return Constraint.createCategoricalConstraint(valueList);
        }
    }
}
