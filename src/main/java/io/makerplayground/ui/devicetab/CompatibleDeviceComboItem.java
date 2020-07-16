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

package io.makerplayground.ui.devicetab;

import io.makerplayground.device.actual.ActualDevice;
import io.makerplayground.device.actual.IntegratedActualDevice;
import io.makerplayground.generator.devicemapping.DeviceMappingResult;
import io.makerplayground.project.ProjectDevice;
import lombok.Value;

@Value
public class CompatibleDeviceComboItem implements Comparable<CompatibleDeviceComboItem> {
    private final CompatibleDevice compatibleDevice;
    private final DeviceMappingResult deviceMappingResult;

    public String getDisplayString() {
        if (compatibleDevice.getActualDevice().isPresent()) {
            ActualDevice actualDevice = compatibleDevice.getActualDevice().get();
            if (actualDevice instanceof IntegratedActualDevice) {
                return actualDevice.getId();
            } else {
                return actualDevice.getDisplayName();
            }
        } else if (compatibleDevice.getProjectDevice().isPresent()) {
            ProjectDevice projectDevice = compatibleDevice.getProjectDevice().get();
            return "Use the same device as " + projectDevice.getName();
        } else {
            throw new IllegalStateException("");
        }
    }

    @Override
    public int compareTo(CompatibleDeviceComboItem that) {
        // devices in the combobox are sorted using the following rule
        // 1. incompatible devices are listed after the last compatible devices and are sorted in alphabetical order
        // 2. identical devices (Using the same device as ...) are listed before the first compatible device and are sorted in alphabetical order
        // 3. compatible devices are sorted in alphabetical order
        if (this.deviceMappingResult != DeviceMappingResult.OK && that.deviceMappingResult == DeviceMappingResult.OK) {
            return 1;
        } else if (this.compatibleDevice.getActualDevice().isEmpty() && that.compatibleDevice.getActualDevice().isPresent()) {
            return -1;
        } else {
            return this.getDisplayString().compareTo(that.getDisplayString());
        }
    }
}