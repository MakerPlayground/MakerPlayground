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
import io.makerplayground.project.ProjectDevice;
import lombok.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
public class IntegratedActualDevice extends ActualDevice {

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Getter protected ActualDevice parent;

    public void setParent(ActualDevice parent) {
        if (this.parent != null) {
            throw new UnsupportedOperationException("parent couldn't be set multiple time.");
        }
        this.parent = parent;
    }

    IntegratedActualDevice(String name,
                           List<Property> property,
                           String pinTemplate,
                           List<Connection> integratedConnection,
                           Map<GenericDevice, Compatibility> compatibilityMap,
                           Map<Platform, SourceCodeLibrary> platformSourceCodeLibrary) {
        super(name, "", "", "", 0.0, 0.0, "", DeviceType.MODULE, false, pinTemplate,
                Collections.emptyList(), integratedConnection, property, null, compatibilityMap,
                Collections.emptyMap(), platformSourceCodeLibrary, Collections.emptyList(), null);
        this.parent = null;
    }
}
