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

package io.makerplayground.version;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ProjectVersionControl {

    public static final String CURRENT_VERSION = "0.3.0";

    public static boolean isConvertibleToCurrentVersion(String projectVersion) {
        List<String> incompatibleList = List.of("0.2", "0.2.3");
        if (CURRENT_VERSION.equals(projectVersion)) {
            return true;
        }
        else if (incompatibleList.contains(projectVersion)) {
            return false;
        }
        return false;
    }

    public static String readProjectVersion(File selectedFile) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode node = mapper.readTree(selectedFile);
            if (node.has("projectVersion")) {
                return node.get("projectVersion").asText("0.2");
            }
            else {
                return "0.2";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void convertToCurrentVersion(File selectedFile) {
        throw new UnsupportedOperationException("Implementation needed");
    }
}
