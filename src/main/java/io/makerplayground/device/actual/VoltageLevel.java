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

package io.makerplayground.device.actual;

public enum VoltageLevel {
    LEVEL_3v3, LEVEL_5, LEVEL_3v3_5;

    public boolean canConsume(VoltageLevel provideVoltageLevel) {
        if (this == provideVoltageLevel) {
            return true;
        }
        return (this == LEVEL_3v3_5) && (provideVoltageLevel == LEVEL_5 || provideVoltageLevel == LEVEL_3v3);
    }
}
