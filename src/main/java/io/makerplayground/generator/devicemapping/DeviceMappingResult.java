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

package io.makerplayground.generator.devicemapping;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public enum DeviceMappingResult {
    OK(""),
    NO_SUPPORTING_ACTION("device doesn't support action used in the diagram"),
    NO_SUPPORTING_CONDITION("device doesn't support condition used in the diagram"),
    NO_SUPPORTING_VALUE("device doesn't provide value used in the diagram"),
    CONDITION_PARAMETER_NOT_COMPATIBLE("device doesn't support every configurable parameter of the condition used"),
    ACTION_PARAMETER_NOT_COMPATIBLE("device doesn't support every configurable parameter of the action used"),
    CONSTRAINT_NOT_COMPATIBLE("device doesn't meet the capability required by the diagram"),
    // TODO: edit the description when device can be connected to other device rather than the controller
    NO_AVAILABLE_PIN_PORT("controller doesn't have enough port or the available port doesn't compatible with the device"),
    // TODO: edit the description in case that cloud platform can be provided by other device than the controller
    NO_AVAILABLE_CLOUD_PLATFORM("controller doesn't support this cloud platform");

    @Getter
    private final String errorMessage;

    @Getter @Setter
    private String detailMessage;
}
