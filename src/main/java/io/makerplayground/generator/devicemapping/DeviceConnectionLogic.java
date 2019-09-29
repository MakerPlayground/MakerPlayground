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

import io.makerplayground.device.actual.*;
import io.makerplayground.project.ProjectDevice;
import lombok.NonNull;

import java.util.*;

public class DeviceConnectionLogic {

    private static boolean[][] getConnectionMatchingArray(List<Connection> allConnectionConsume, List<Connection> remainingConnectionProvide, Map<ProjectDevice, Set<String>> usedRefPin) {
        boolean[][] connectionMatching = new boolean[allConnectionConsume.size()][];
        for (int i=0; i<connectionMatching.length; i++) {
            connectionMatching[i] = new boolean[remainingConnectionProvide.size()];
            for (int j = 0; j< remainingConnectionProvide.size(); j++) {
                connectionMatching[i][j] = true;
                Connection connectionConsumer = allConnectionConsume.get(i);
                Connection connectionProvider = remainingConnectionProvide.get(j);
                if (!connectionConsumer.getType().canConsume(connectionProvider.getType())) {
                    connectionMatching[i][j] = false;
                    continue;
                }
                if (connectionConsumer.getType() == ConnectionType.INTEGRATED
                        && !connectionConsumer.getName().equals(connectionProvider.getName())) {
                    connectionMatching[i][j] = false;
                    continue;
                }
                if (connectionProvider.getPins().size() != connectionConsumer.getPins().size()) {
                    connectionMatching[i][j] = false;
                    continue;
                }
                if (connectionConsumer.getType() != ConnectionType.INTEGRATED) {
                    for (int k = 0; k< connectionProvider.getPins().size(); k++) {
                        List<PinFunction> provideFunctions = connectionProvider.getPins().get(k).getFunction();
                        if (connectionConsumer.getPins().get(k).getFunction().get(0).getPossibleConsume().stream().noneMatch(provideFunctions::contains)) {
                            connectionMatching[i][j] = false;
                            break;
                        }
                        VoltageLevel consumerVoltageLevel = connectionConsumer.getPins().get(k).getVoltageLevel();
                        VoltageLevel providerVoltageLevel = connectionProvider.getPins().get(k).getVoltageLevel();
                        if (!consumerVoltageLevel.canConsume(providerVoltageLevel)) {
                            connectionMatching[i][j] = false;
                            break;
                        }
                    }
                }
            }
        }
        for (int j = 0; j<remainingConnectionProvide.size(); j++) {
            Connection connectionProvider = remainingConnectionProvide.get(j);
            ProjectDevice providerProjectDevice = connectionProvider.getOwnerProjectDevice();
            if (connectionProvider.getPins().stream().anyMatch(pin -> usedRefPin.containsKey(providerProjectDevice) && usedRefPin.get(providerProjectDevice).contains(pin.getRefTo())))
            {
                for (int i=0; i<connectionMatching.length; i++) {
                    connectionMatching[i][j] = false;
                }
            }
        }
        return connectionMatching;
    }

    private static boolean[][] deepCopy(@NonNull boolean[][] original) {
        final boolean[][] result = new boolean[original.length][];
        for (int i = 0; i < original.length; i++) {
            result[i] = Arrays.copyOf(original[i], original[i].length);
        }
        return result;
    }

    private static final Comparator<Connection> LESS_PROVIDER_DEPENDENCY = Comparator
            .comparingInt((Connection connection) -> connection.getPins().stream().map(Pin::getFunction).mapToInt(List::size).sum())
            .thenComparing(Connection::getName);

    private static final Comparator<Connection> CONNECTION_NAME_ASCENDING = Comparator.comparing(Connection::getName);

    public static DeviceConnectionResult generatePossibleDeviceConnection(Set<Connection> remainingConnectionProvide, Map<ProjectDevice, Set<String>> usedRefPin, ProjectDevice projectDevice, ActualDevice actualDevice) {
        List<Connection> allConnectionsProvide = new ArrayList<>(remainingConnectionProvide);
        List<Connection> allConnectionsConsume = actualDevice.getConnectionConsumeByOwnerDevice(projectDevice);
        boolean[][] connectionMatching = getConnectionMatchingArray(allConnectionsConsume, allConnectionsProvide, usedRefPin);
        SortedMap<Connection, List<Connection>> deviceConnections = new TreeMap<>(CONNECTION_NAME_ASCENDING);
        for (int i=0; i<allConnectionsConsume.size(); i++) {
            Connection connectionConsume = allConnectionsConsume.get(i);
            List<Connection> connectionProvideList = new ArrayList<>();
            for (int j=0; j<allConnectionsProvide.size(); j++) {
                if (connectionMatching[i][j]) {
                    Connection connectionProvide = allConnectionsProvide.get(j);
                    connectionProvideList.add(connectionProvide);
                }
            }
            if (connectionProvideList.isEmpty()) {
                return DeviceConnectionResult.ERROR;
            }
            connectionProvideList.sort(LESS_PROVIDER_DEPENDENCY);
            deviceConnections.put(connectionConsume, connectionProvideList);
        }
        return new DeviceConnectionResult(DeviceConnectionResultStatus.OK, deviceConnections);
    }
}
