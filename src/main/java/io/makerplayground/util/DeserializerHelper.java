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

package io.makerplayground.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;

public class DeserializerHelper {
    public static void throwIfMissingField(JsonNode node, String fieldname, String... tags) {
        if (!node.has(fieldname)) {
            throw new IllegalStateException("Missing field '" + fieldname + "' for (" + String.join("->" ,tags) + ")");
        }
    }

    public static void throwIfFieldIsNotArray(JsonNode node, String fieldname, String... tags) {
        if (!node.has(fieldname) || !node.get(fieldname).isArray()) {
            throw new IllegalStateException("Field '" + fieldname + "' for (" + String.join("->", tags) + ") must be an array.");
        }
    }

    public static void createArrayNodeIfMissing(JsonNode node, String... fieldNames) {
        for (String fieldName: fieldNames) {
            if (!node.has(fieldName)) {
                ((ObjectNode) node).putArray(fieldName);
            }
        }
    }

    public static void throwIfOneOfTheseFieldsNotExist(JsonNode node, List<String> fields, String... tags) {
        for (String field: fields) {
            if (node.has(field))
                return;
        }
        throw new IllegalStateException("One of these fields {" + String.join(",", fields) + "} must be existed. (" + String.join("->", tags) + ")");
    }
}
