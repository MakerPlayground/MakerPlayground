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

package io.makerplayground.generator.source;

public enum SourceCodeError {
    NONE(""),
    DIAGRAM_ERROR("Found some errors in the diagram"),
    MISSING_PROPERTY("Missing required device's property"),
    MORE_THAN_ONE_CLOUD_PLATFORM("Only one cloud platform (e.g. Blynk or NETPIE) is allowed"),
    NOT_SELECT_DEVICE_OR_PORT("Some devices and/or their ports haven't been selected");

    private final String description;

    SourceCodeError(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
