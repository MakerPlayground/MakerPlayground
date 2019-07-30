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

import io.makerplayground.device.generic.GenericDevice;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class IntegratedActualDevice extends ActualDevice {

    @Getter protected ActualDevice parent;

    public void setParent(ActualDevice parent) {
        if (this.parent != null) {
            throw new UnsupportedOperationException("parent couldn't be set multiple time.");
        }
        this.parent = parent;
    }

    @Builder(builderMethodName = "IntegratedActualDeviceBuilder")
    IntegratedActualDevice(String id, String brand, String model, String url, double width, double height,
                           String pioBoardId, DeviceType deviceType, List<Pin> pinProvide, List<Pin> pinConsume,
                           List<Pin> pinUnused, List<Property> property, List<Port> portProvide, List<Port> portConsume,
                           Map<String, List<String>> samePinMap, CloudPlatform cloudConsume,
                           Map<GenericDevice, Compatibility> compatibilityMap,
                           Map<CloudPlatform, SourceCodeLibrary> cloudPlatformSourceCodeLibrary,
                           Map<Platform, SourceCodeLibrary> platformSourceCodeLibrary,
                           List<IntegratedActualDevice> integratedDevices) {
        super(id, brand, model, url, width, height, pioBoardId, deviceType, pinProvide, pinConsume, pinUnused,
                portConsume, portProvide, samePinMap, property, cloudConsume, compatibilityMap,
                cloudPlatformSourceCodeLibrary, platformSourceCodeLibrary, integratedDevices);
        this.parent = null;
    }
}
