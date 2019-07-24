/*
 * Copyright (c) 2019. The Maker Playground Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed providerDevice in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.makerplayground.project;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.makerplayground.device.actual.Pin;
import io.makerplayground.device.actual.Port;
import lombok.*;

import java.util.*;

@JsonSerialize(using = DevicePinPortConnectionSerializer.class)
@Data
public class DevicePinPortConnection implements Comparable<DevicePinPortConnection> {

    @JsonIgnore public static final DevicePinPortConnection NOT_CONNECTED = new DevicePinPortConnection();

    @JsonIgnore private static final SortedMap<Pin, Pin> dummyPinMap = new TreeMap<>();
    @JsonIgnore private static final SortedMap<Port, Port> dummyPortMap = new TreeMap<>();
    @JsonIgnore private static final Comparator<DevicePinPortConnection> comparator = Comparator
            .comparing(DevicePinPortConnection::getConsumerDevice)
            .thenComparing(DevicePinPortConnection::getProviderDevice)
            .thenComparing(DevicePinPortConnection::getPinPortString);

    private final ProjectDevice consumerDevice;
    private final ProjectDevice providerDevice;
    private final SortedMap<Pin, Pin> pinMapConsumerProvider;
    private final SortedMap<Port, Port> portMapConsumerProvider;

    private DevicePinPortConnection() {
        consumerDevice = null;
        providerDevice = null;
        pinMapConsumerProvider = dummyPinMap;
        portMapConsumerProvider = dummyPortMap;
    }

    public DevicePinPortConnection(@NonNull ProjectDevice from,
                                   @NonNull ProjectDevice to,
                                   SortedMap<Pin, Pin> pinMapFromTo,
                                   SortedMap<Port, Port> portMapFromTo) {
        this.consumerDevice = from;
        this.providerDevice = to;
        this.pinMapConsumerProvider = Objects.nonNull(pinMapFromTo) ? Collections.unmodifiableSortedMap(pinMapFromTo) : dummyPinMap;
        this.portMapConsumerProvider = Objects.nonNull(portMapFromTo) ? Collections.unmodifiableSortedMap(portMapFromTo) : dummyPortMap;
    }

    private String getPinPortString() {
        List<String> pinPortName = new ArrayList<>();
        if (Objects.nonNull(pinMapConsumerProvider)) {
            for(Pin pin : pinMapConsumerProvider.keySet()) {
                pinPortName.add(pin.getName());
                pinPortName.add(pinMapConsumerProvider.get(pin).getName());
            }
        }
        if (Objects.nonNull(portMapConsumerProvider)) {
            for(Port port : portMapConsumerProvider.keySet()) {
                pinPortName.add(port.getName());
                pinPortName.add(portMapConsumerProvider.get(port).getName());
            }
        }
        return String.join(",", pinPortName);
    }

    @Override
    public int compareTo(DevicePinPortConnection o) {
        return comparator.compare(this, o);
    }
}
