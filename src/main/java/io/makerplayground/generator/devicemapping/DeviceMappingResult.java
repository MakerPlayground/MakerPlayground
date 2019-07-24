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
    NO_SUPPORTING_ACTION("device not provide a required action"),
    NO_SUPPORTING_CONDITION("device not provide a required condition"),
    CONDITION_PARAMETER_NOT_COMPATIBLE("some condition contains incompatible parameters"),
    ACTION_PARAMETER_NOT_COMPATIBLE("some action contains incompatible parameters"),
    CONSTRAINT_NOT_COMPATIBLE("project requires more capability than the device can provide"),
    NO_AVAILABLE_PIN_PORT("no available pin or port"),
    NO_AVAILABLE_CLOUD_PLATFORM("no available cloud platform in this circuit");

    @Getter
    private final String errorMessage;

    @Getter @Setter
    private String detailMessage;
}
