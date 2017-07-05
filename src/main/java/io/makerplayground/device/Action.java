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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;

/**
 * Represents an action that an output device can perform including all parameters that can be adjusted
 */
public class Action {
    private final String name;
    private final List<Parameter> parameter;

    /**
     * Create a new action. The constructor should only be invoked by the DeviceLibrary in order to
     * rebuild the library from file.
     * @param name name of this action ex. on, off, etc.
     * @param parameter list of parameters of this action ex. brightness, speed, etc.
     */
    @JsonCreator
    Action(@JsonProperty("name") String name, @JsonProperty("parameter") List<Parameter> parameter) {
        this.name = name;
        this.parameter = Collections.unmodifiableList(parameter);
    }

    /**
     * Get the name of this action
     * @return name of this action ex. on, off, blink
     */
    public String getName() {
        return name;
    }

    /**
     * Get the list of parameters of this action
     * @return an unmodifiable list of parameters of this action
     */
    public List<Parameter> getParameter() {
        return parameter;
    }

    @Override
    public String toString() {
        return "Action{" +
                "name='" + name + '\'' +
                ", parameter=" + parameter +
                '}';
    }
}
