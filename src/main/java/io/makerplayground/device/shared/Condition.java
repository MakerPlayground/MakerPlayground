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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Data
public class Condition {
    private final String name;
    private final String functionName;
    private final List<Parameter> parameter;

    /**
     * Create a new action. The constructor should only be invoked by the DeviceLibrary in order to
     * rebuild the library from file.
     * @param name name of this action ex. on, off, etc.
     * @param parameter list of parameters of this action ex. brightness, speed, etc.
     */
    @JsonCreator
    Condition(@JsonProperty("name") String name, @JsonProperty("funcname") String functionName, @JsonProperty("parameter") List<Parameter> parameter) {
        this.name = name;
        this.functionName = functionName;
        this.parameter = Collections.unmodifiableList(parameter);
    }

    public Optional<Parameter> getParameter(String name) {
        return parameter.stream().filter(parameter1 -> parameter1.getName().equals(name)).findFirst();
    }
}
