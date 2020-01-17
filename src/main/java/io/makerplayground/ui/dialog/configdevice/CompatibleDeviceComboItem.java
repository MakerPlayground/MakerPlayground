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

package io.makerplayground.ui.dialog.configdevice;

import io.makerplayground.generator.devicemapping.DeviceMappingResult;
import lombok.Value;

@Value
public class CompatibleDeviceComboItem implements Comparable<CompatibleDeviceComboItem> {
    private final CompatibleDevice compatibleDevice;
    private final DeviceMappingResult deviceMappingResult;

    @Override
    public int compareTo(CompatibleDeviceComboItem that) {
        if (this.deviceMappingResult == DeviceMappingResult.OK && that.deviceMappingResult != DeviceMappingResult.OK) {
            return -1;
        } else if (this.deviceMappingResult != DeviceMappingResult.OK && that.deviceMappingResult == DeviceMappingResult.OK) {
            return 1;
        }
        return this.compatibleDevice.compareTo(that.compatibleDevice);
    }
}