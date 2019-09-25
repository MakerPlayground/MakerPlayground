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
import io.makerplayground.project.DeviceConnection;
import io.makerplayground.project.ProjectDevice;
import lombok.NonNull;

import java.util.*;

public class DeviceConnectionLogic {

    private static final Comparator<DeviceConnection> LESS_PROVIDER_DEPENDENCY = Comparator
            .comparingInt((DeviceConnection deviceConnection) -> deviceConnection.getConsumerProviderConnections().size())
            .thenComparingInt((DeviceConnection deviceConnection) -> deviceConnection
                    .getConsumerProviderConnections()
                    .values()
                    .stream()
                    .flatMap(providerConnection -> providerConnection.getPins().stream())
                    .map(Pin::getFunction)
                    .mapToInt(List::size)
                    .sum())
            .thenComparing(DeviceConnection::toString);

    public static DeviceConnectionResult generateOneStepPossibleDeviceConnection(Set<Connection> remainingConnectionProvide,
                                                                                 Map<ProjectDevice, Set<String>> usedRefPin,
                                                                                 ProjectDevice projectDevice,
                                                                                 ActualDevice actualDevice)
    {
        List<Connection> allConnectionsProvide = new ArrayList<>(remainingConnectionProvide);
        List<Connection> allConnectionsConsume = actualDevice.getConnectionConsumeByOwnerDevice(projectDevice);
        boolean[][] connectionMatching = getConnectionMatchingArray(allConnectionsConsume, allConnectionsProvide, usedRefPin);
        List<DeviceConnection> deviceConnections = generateAllPossibleDeviceConnection(connectionMatching, allConnectionsProvide, allConnectionsConsume);
        deviceConnections.sort(LESS_PROVIDER_DEPENDENCY);
        if (deviceConnections.isEmpty()) {
            return DeviceConnectionResult.ERROR;
        }
        return new DeviceConnectionResult(DeviceConnectionResultStatus.OK, deviceConnections);
    }

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

    private static List<DeviceConnection> generateAllPossibleDeviceConnection(boolean[][] matching,
                                                                              List<Connection> allConnectionsProvide,
                                                                              List<Connection> allConnectionsConsume)
    {
        boolean[][] selected = new boolean[matching.length][];
        for (int i=0; i<matching.length; i++) {
            selected[i] = new boolean[matching[i].length];
        }
        return generateAllPossibleDeviceConnectionRecursive(matching, selected, allConnectionsProvide, allConnectionsConsume, 0);
    }

    private static List<DeviceConnection> generateAllPossibleDeviceConnectionRecursive(boolean[][] matching,
                                                                                       boolean[][] selected,
                                                                                       List<Connection> allConnectionsProvide,
                                                                                       List<Connection> allConnectionsConsume,
                                                                                       int currentRow)
    {
        if (currentRow >= selected.length) {
            List<DeviceConnection> result = new ArrayList<>();
            SortedMap<Connection, Connection> connectionMap = new TreeMap<>();
            SortedMap<Connection, List<PinFunction>> functionUsed = new TreeMap<>();
            for (int i=0; i<selected.length; i++) {
                int trueIndex = -1;
                for (int j=0; j<selected[i].length; j++) {
                    if (selected[i][j]) {
                        trueIndex = j;
                    }
                }
                Connection connectionConsume = allConnectionsConsume.get(i);
                Connection connectionProvide = allConnectionsProvide.get(trueIndex);
                List<Pin> consumerPinList = connectionConsume.getPins();
                List<Pin> providerPinList = connectionProvide.getPins();
                if (consumerPinList.size() != providerPinList.size()) {
                    return Collections.emptyList();
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
                    return Collections.emptyList();
                }
                connectionMap.put(connectionConsume, connectionProvide);
                functionUsed.put(connectionProvide, providerPinFunction);
            }
            result.add(new DeviceConnection(connectionMap, functionUsed));
            return result;
        }
        List<DeviceConnection> result = new ArrayList<>();
        boolean rowHasTrue = false;
        for (int j=0; j<matching[currentRow].length; j++) {
            if (matching[currentRow][j]) {
                rowHasTrue = true;
                boolean[][] temp = deepCopy(selected);
                boolean repeat = false;
                for (int i=0; i<currentRow; i++) {
                    if (selected[i][j]) {
                        repeat = true;
                        break;
                    }
                }
                if (!repeat) {
                    temp[currentRow][j] = true;
                    result.addAll(generateAllPossibleDeviceConnectionRecursive(matching, temp, allConnectionsProvide, allConnectionsConsume, currentRow+1));
                    temp[currentRow][j] = false;
                }
            }
        }
        if (!rowHasTrue) {
            return Collections.emptyList();
        }
        return result;
    }

    private static boolean[][] deepCopy(@NonNull boolean[][] original) {
        final boolean[][] result = new boolean[original.length][];
        for (int i = 0; i < original.length; i++) {
            result[i] = Arrays.copyOf(original[i], original[i].length);
        }
        return result;
    }

}
