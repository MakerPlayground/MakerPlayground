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

public enum DeviceMapperResult {
    OK(""),
    NOT_ENOUGH_PORT("Can't find support device or the controller selected doesn't have enough port"),
    NO_SUPPORT_DEVICE("Can't find support device"),
    NO_MCU_SELECTED("Controller hasn't been selected");

    private final String errorMessage;

    DeviceMapperResult(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
