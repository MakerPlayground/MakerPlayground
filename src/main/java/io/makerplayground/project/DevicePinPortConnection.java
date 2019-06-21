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

package io.makerplayground.project;

import io.makerplayground.device.actual.ActualDevice;
import io.makerplayground.device.actual.Pin;
import io.makerplayground.device.actual.Port;
import lombok.*;

import java.util.*;

@Data
public class DevicePinPortConnection implements Comparable<DevicePinPortConnection> {
    private final ActualDevice from;
    private final ActualDevice to;
    private final Map<Pin, Pin> pinMapFromTo;
    private final Map<Port, Port> portMapFromTo;
    @Getter(lazy = true) private final String description = generateDescription();

    public DevicePinPortConnection(@NonNull ActualDevice from,
                                   @NonNull ActualDevice to,
                                   Map<Pin, Pin> pinMapFromTo,
                                   Map<Port, Port> portMapFromTo) {
        this.from = from;
        this.to = to;
        this.pinMapFromTo = Objects.nonNull(pinMapFromTo) ? Collections.unmodifiableMap(pinMapFromTo) : null;
        this.portMapFromTo = Objects.nonNull(portMapFromTo) ? Collections.unmodifiableMap(portMapFromTo) : null;
    }

    private String generateDescription() {
        List<String> pinPortNameFrom = new ArrayList<>();
        List<String> pinPortNameTo = new ArrayList<>();
        if (Objects.nonNull(pinMapFromTo)) {
            for(Pin pin : pinMapFromTo.keySet()) {
                pinPortNameFrom.add(pin.getName());
                pinPortNameTo.add(pinMapFromTo.get(pin).getName());
            }
        }
        if (Objects.nonNull(getPortMapFromTo())) {
            for(Port port : portMapFromTo.keySet()) {
                pinPortNameFrom.add(port.getName());
                pinPortNameTo.add(portMapFromTo.get(port).getName());
            }
        }
        return from.getModel() + "-"+ from.getModel() + "(" + String.join(",", pinPortNameFrom) + ") -> "+ to.getBrand() + "-" + to.getModel() + "(" + String.join(", ", pinPortNameTo);
    }

    @Override
    public int compareTo(DevicePinPortConnection o) {
        return generateDescription().compareTo(o.generateDescription());
    }
}
