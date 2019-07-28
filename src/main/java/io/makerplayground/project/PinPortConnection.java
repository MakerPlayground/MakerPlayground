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

@JsonSerialize(using = PinPortConnectionSerializer.class)
@Data
public class PinPortConnection implements Comparable<PinPortConnection> {

    @JsonIgnore public static final PinPortConnection NOT_CONNECTED = new PinPortConnection(null, null);
    @JsonIgnore private static final Comparator<PinPortConnection> comparator = Comparator.comparing(PinPortConnection::getPinPortString);

    /* Note that: the pin that is the elements of port will not be contained in this map */
    private final SortedMap<Pin, Pin> pinMapConsumerProvider;
    private final SortedMap<Port, Port> portMapConsumerProvider;

    public PinPortConnection(SortedMap<Pin, Pin> pinMapFromTo, SortedMap<Port, Port> portMapFromTo) {
        this.pinMapConsumerProvider = Objects.nonNull(pinMapFromTo) ? Collections.unmodifiableSortedMap(pinMapFromTo) : Collections.emptySortedMap();
        this.portMapConsumerProvider = Objects.nonNull(portMapFromTo) ? Collections.unmodifiableSortedMap(portMapFromTo) : Collections.emptySortedMap();
    }

    private String getPinPortString() {
        List<String> pinPortName = new ArrayList<>();
        for(Pin pin : pinMapConsumerProvider.keySet()) {
            pinPortName.add(pin.getName());
            pinPortName.add(pinMapConsumerProvider.get(pin).getName());
        }
        for(Port port : portMapConsumerProvider.keySet()) {
            pinPortName.add(port.getName());
            pinPortName.add(portMapConsumerProvider.get(port).getName());
        }
        return String.join(",", pinPortName);
    }

    @Override
    public int compareTo(PinPortConnection o) {
        return comparator.compare(this, o);
    }
}
