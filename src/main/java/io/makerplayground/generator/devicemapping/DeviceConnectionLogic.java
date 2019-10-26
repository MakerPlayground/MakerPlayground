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

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DeviceConnectionLogic {

    private static boolean[][] getConnectionMatchingArray(List<Connection> allConnectionConsume,
                                                          List<Connection> remainingConnectionProvide,
                                                          Map<ProjectDevice, Set<String>> usedRefPin) {
        /* All device must used the same voltage level */
        Map<VoltageLevel, Long> nonPowerPinCountGroupByVoltageLevel = remainingConnectionProvide.stream()
                .map(Connection::getPins)
                .flatMap(Collection::stream)
                .filter(pin -> !pin.getFunction().contains(PinFunction.VCC))
                .filter(pin -> !pin.getFunction().contains(PinFunction.GND))
                .collect(Collectors.toSet())
                .stream()
                .map(Pin::getVoltageLevel)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        Long numNonPowerPinUsed = allConnectionConsume.stream()
                .map(Connection::getPins)
                .flatMap(Collection::stream)
                .filter(pin -> !pin.getFunction().contains(PinFunction.VCC))
                .filter(pin -> !pin.getFunction().contains(PinFunction.GND))
                .count();
        List<VoltageLevel> possibleVoltageLevels = nonPowerPinCountGroupByVoltageLevel.entrySet().stream().filter(entry -> entry.getValue() > numNonPowerPinUsed).map(Map.Entry::getKey).collect(Collectors.toList());

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
                        boolean flag = false;
                        for (PinFunction consumerFunction: connectionConsumer.getPins().get(k).getFunction()) {
                            if (consumerFunction.getPossibleConsume().stream().noneMatch(provideFunctions::contains)) {
                                connectionMatching[i][j] = false;
                                flag = true;
                                break;
                            }
                        }
                        if (flag) break;
                        VoltageLevel consumerVoltageLevel = connectionConsumer.getPins().get(k).getVoltageLevel();
                        VoltageLevel providerVoltageLevel = connectionProvider.getPins().get(k).getVoltageLevel();
                        if (!consumerVoltageLevel.canConsume(providerVoltageLevel) || !possibleVoltageLevels.contains(providerVoltageLevel)) {
                            connectionMatching[i][j] = false;
                            break;
                        }
                    }
                }
            }
        }
        for (int j = 0; j<remainingConnectionProvide.size(); j++) {
            Connection connectionProvide = remainingConnectionProvide.get(j);
            ProjectDevice providerProjectDevice = connectionProvide.getOwnerProjectDevice();
            if (connectionProvide.getPins().stream().anyMatch(pin -> usedRefPin.containsKey(providerProjectDevice) && usedRefPin.get(providerProjectDevice).contains(pin.getRefTo())))
            {
                for (int i=0; i<connectionMatching.length; i++) {
                    connectionMatching[i][j] = false;
                }
            }
        }
        return connectionMatching;
    }

    public static DeviceConnectionResult generatePossibleDeviceConnection(Set<Connection> remainingConnectionProvide,
                                                                          Map<ProjectDevice, Set<String>> usedRefPin,
                                                                          ProjectDevice projectDevice,
                                                                          ActualDevice actualDevice,
                                                                          DeviceConnection currentConnection) {
        List<Connection> allConnectionsProvide = new ArrayList<>(remainingConnectionProvide);
        List<Connection> allConnectionsConsume = actualDevice.getConnectionConsumeByOwnerDevice(projectDevice);
        SortedMap<Connection, List<Connection>> possibleConnections = new TreeMap<>(Connection.NAME_TYPE_COMPARATOR);
        for (int i=0; i<allConnectionsConsume.size(); i++) {
            Connection connectionConsume = allConnectionsConsume.get(i);
            /* In order to check the compatible, the assigned pin/port must be temporary deallocated from the remaining pin/port. */
            Queue<Connection> deallocatingConnections = new ArrayDeque<>();
            Map<ProjectDevice, Set<String>> deallcatingRefTo = new HashMap<>();
            Connection connection = currentConnection.getConsumerProviderConnections().get(connectionConsume);
            if (connection != null) {
                if (connection.getType() != ConnectionType.WIRE) {
                    deallocatingConnections.add(connection);
                    allConnectionsProvide.add(connection);
                } else if (connection.getType() == ConnectionType.WIRE &&
                        connection.getPins().size() == 1 &&
                        currentConnection.getProviderFunction().get(connection).get(0).isSingleUsed())
                {
                    deallocatingConnections.add(connection);
                    allConnectionsProvide.add(connection);
                }
                ProjectDevice providerProjectDevice = connection.getOwnerProjectDevice();
                connection.getPins().forEach(pin -> {
                    if (usedRefPin.containsKey(providerProjectDevice) && usedRefPin.get(providerProjectDevice).contains(pin.getRefTo())) {
                        if (!deallcatingRefTo.containsKey(providerProjectDevice)) {
                            deallcatingRefTo.put(providerProjectDevice, new HashSet<>());
                        }
                        deallcatingRefTo.get(providerProjectDevice).add(pin.getRefTo());
                        usedRefPin.get(providerProjectDevice).remove(pin.getRefTo());
                    }
                });
            }

            boolean[][] connectionMatching = getConnectionMatchingArray(allConnectionsConsume, allConnectionsProvide, usedRefPin);
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
            connectionProvideList.sort(Connection.LESS_PROVIDER_DEPENDENCY);
            possibleConnections.put(connectionConsume, connectionProvideList);

            deallcatingRefTo.forEach((providerProjectDevice, refTo) ->
                    usedRefPin.get(providerProjectDevice).addAll(deallcatingRefTo.get(providerProjectDevice))
            );
            allConnectionsProvide.removeAll(deallocatingConnections);
        }
        return new DeviceConnectionResult(DeviceConnectionResultStatus.OK, possibleConnections);
    }
}
