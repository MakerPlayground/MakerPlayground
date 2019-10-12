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

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.makerplayground.device.actual.Connection;
import io.makerplayground.device.actual.Pin;
import io.makerplayground.device.actual.PinFunction;
import lombok.*;

import java.util.*;

@Data
public class DeviceConnection {

    @JsonIgnore public static final DeviceConnection NOT_CONNECTED = new DeviceConnection(null, null);

    /* Note that: the pin that is the elements of port will not be contained in this map */
    private final SortedMap<Connection, Connection> consumerProviderConnections;
    private final SortedMap<Connection, List<PinFunction>> providerFunction;

    public DeviceConnection(SortedMap<Connection, Connection> consumerProviderConnection, SortedMap<Connection, List<PinFunction>> providerFunction)
    {
        this.consumerProviderConnections = Objects.nonNull(consumerProviderConnection)
                ? consumerProviderConnection
                : Collections.emptySortedMap();
        this.providerFunction = Objects.nonNull(providerFunction)
                ? providerFunction
                : Collections.emptySortedMap();
    }

    void setConnection(Connection consumerConnection, Connection providerConnection) {
        if (consumerProviderConnections.containsKey(consumerConnection) && consumerProviderConnections.get(consumerConnection) != null) {
            providerFunction.remove(consumerProviderConnections.get(consumerConnection));
        }
        if (providerConnection == null) {
            return;
        }
        List<Pin> consumerPinList = consumerConnection.getPins();
        List<Pin> providerPinList = providerConnection.getPins();
        if (consumerPinList.size() != providerPinList.size()) {
            throw new IllegalStateException("Two connections have difference size of pin list.");
        }
        List<PinFunction> providerPinFunction = new ArrayList<>();
        for (int k=0; k<providerPinList.size(); k++) {
            Pin consumerPin = consumerPinList.get(k);
            Pin providerPin = providerPinList.get(k);
            for (PinFunction function: consumerPin.getFunction().get(0).getPossibleConsume()) {
                if (providerPin.getFunction().contains(function)) {
                    providerPinFunction.add(function);
                    break;
                }
            }
        }
        if (providerPinFunction.size() != providerPinList.size()) {
            throw new IllegalStateException("Pin functions of connections are not matched.");
        }
        consumerProviderConnections.put(consumerConnection, providerConnection);
        providerFunction.put(providerConnection, providerPinFunction);
    }
}
