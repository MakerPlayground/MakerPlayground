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

package io.makerplayground.generator;

public class Sourcecode {

    public enum Error {
        NONE(""),
        SCENE_ERROR("Missing required parameter in some scenes"),
        MISSING_PROPERTY("Missing required device's property"),
        NOT_FOUND_SCENE_OR_CONDITION("Can't find any scene or condition connect to the begin node"),
        MULT_DIRECT_CONN_TO_SCENE("Found multiple direct connection to the same scene"),
        NEST_CONDITION("Multiple condition are connected together"),
        SHORT_CIRCUIT("Some conditions are not reachable"),
        CONDITION_ERROR("Missing required parameter in some conditions"),
        MORE_THAN_ONE_CLOUD_PLATFORM("Only one cloud platform (e.g. Blynk or NETPIE) is allowed"),
        NOT_SELECT_DEVICE_OR_PORT("Some devices and/or their ports haven't been selected");

        private final String description;

        Error(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    private String code;
    private Error error;
    private String location;

    Sourcecode(String code) {
        this.code = code;
    }

    Sourcecode(Error error, String location) {
        this.error = error;
        this.location = location;
    }

    public String getCode() {
        return code;
    }

    public Error getError() {
        return error;
    }

    public String getLocation() {
        return location;
    }


}
