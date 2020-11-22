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
                                                          Map<ProjectDevice, Set<String>> usedRefPin,
                                                          VoltageLevel operatingVoltageLevel) {
        boolean[][] connectionMatching = new boolean[allConnectionConsume.size()][];
        for (int i=0; i<connectionMatching.length; i++) {
            connectionMatching[i] = new boolean[remainingConnectionProvide.size()];
            for (int j = 0; j< remainingConnectionProvide.size(); j++) {
                // Initially set to true
                connectionMatching[i][j] = true;
                Connection connectionConsumer = allConnectionConsume.get(i);
                Connection connectionProvider = remainingConnectionProvide.get(j);

                // Check Connection Type
                if (!connectionConsumer.getType().canConsume(connectionProvider.getType())) {
                    connectionMatching[i][j] = false;
                    continue;
                }

                // Check name of the connection consume of the integrated device
                if (connectionConsumer.getType() == ConnectionType.INTEGRATED) {
                    if (!connectionConsumer.getName().equals(connectionProvider.getName())) {
                        connectionMatching[i][j] = false;
                        continue;
                    }
                }

                // Check the number of pins of the connection.
                if (connectionProvider.getPins().size() != connectionConsumer.getPins().size()) {
                    connectionMatching[i][j] = false;
                    continue;
                }

                ProjectDevice providerProjectDevice = connectionProvider.getOwnerProjectDevice();
                for (int k = 0; k< connectionProvider.getPins().size(); k++) {
                    Pin pin = connectionProvider.getPins().get(k);
                    // Check whether the provider already used that pin refto
                    if (usedRefPin.containsKey(providerProjectDevice)
                            && usedRefPin.get(providerProjectDevice).contains(pin.getRefTo())
                            && !connectionConsumer.getPins().get(k).getFunction().contains(PinFunction.NO_FUNCTION))
                    {
                        connectionMatching[i][j] = false;
                        break;
                    }

                    if (connectionConsumer.getType() == ConnectionType.INTEGRATED) {
                        continue;
                    }

                    // Check whether the provider can provide the function for consumer
                    List<PinFunction> provideFunctions = pin.getFunction();
                    boolean flag = false;
                    for (PinFunction consumerFunction: connectionConsumer.getPins().get(k).getFunction()) {
                        if (consumerFunction.getPossibleConsume().stream().noneMatch(provideFunctions::contains)) {
                            connectionMatching[i][j] = false;
                            flag = true;
                            break;
                        }
                    }
                    if (flag) break;

                    Pin consumerPin = connectionConsumer.getPins().get(k);
                    if (!consumerPin.getFunction().contains(PinFunction.GND)) {
                        // Check whether the operating voltage of consumer is compatible to the voltage level of provider.
                        VoltageLevel providerVoltageLevel = connectionProvider.getPins().get(k).getVoltageLevel();
                        double providerPinVoltage = connectionProvider.getPins().get(k).getVoltageLevel().getVoltage();
                        double minConsumerVoltage = consumerPin.getMinVoltage();
                        double maxConsumerVoltage = consumerPin.getMaxVoltage();
                        if (minConsumerVoltage > providerPinVoltage
                                || maxConsumerVoltage < providerPinVoltage
                                || (operatingVoltageLevel != VoltageLevel.NOT_SPECIFIED && providerVoltageLevel != operatingVoltageLevel)) {
                            connectionMatching[i][j] = false;
                            break;
                        }
                    }
                }
            }
        }
//        for (int j = 0; j<remainingConnectionProvide.size(); j++) {
//            Connection connectionProvide = remainingConnectionProvide.get(j);
//            ProjectDevice providerProjectDevice = connectionProvide.getOwnerProjectDevice();
//            if (connectionProvide.getPins().stream().anyMatch(pin -> usedRefPin.containsKey(providerProjectDevice) && usedRefPin.get(providerProjectDevice).contains(pin.getRefTo())))
//            {
//                for (int i=0; i<connectionMatching.length; i++) {
//                    connectionMatching[i][j] = false;
//                }
//            }
//        }
        return connectionMatching;
    }

    public static DeviceConnectionResult generatePossibleDeviceConnection(Set<Connection> remainingConnectionProvide,
                                                                          Map<ProjectDevice, Set<String>> usedRefPin,
                                                                          ProjectDevice projectDevice,
                                                                          ActualDevice actualDevice,
                                                                          DeviceConnection currentConnection,
                                                                          boolean stopOnFirstUnmatched) {
        DeviceConnectionResultStatus resultStatus = DeviceConnectionResultStatus.OK;
        List<Connection> allConnectionsProvide = new ArrayList<>(remainingConnectionProvide);
        List<Connection> allConnectionsConsume = actualDevice.getConnectionConsumeByOwnerDevice(projectDevice);
        SortedMap<Connection, List<Connection>> possibleConnections = new TreeMap<>(Connection.NAME_TYPE_COMPARATOR);
        for (int i=0; i<allConnectionsConsume.size(); i++) {
            Connection connectionConsume = allConnectionsConsume.get(i);
            /* In order to check the compatible connection, the assigned connection must be temporary deallocated from the remaining connection. */
            Queue<Connection> deallocatingConnections = new ArrayDeque<>();
            Map<ProjectDevice, Set<String>> deallocatingRefTo = new HashMap<>();
            Connection currentConnectionProvide = currentConnection.getConsumerProviderConnections().get(connectionConsume);
            DeviceConnection deallocatingDeviceConnection = new DeviceConnection(currentConnection);

            if (currentConnectionProvide != null) {
                if (currentConnectionProvide.getType() != ConnectionType.WIRE) {
                    deallocatingConnections.add(currentConnectionProvide);
                    allConnectionsProvide.add(currentConnectionProvide);
                } else if (currentConnectionProvide.getType() == ConnectionType.WIRE &&
                        currentConnectionProvide.getPins().size() == 1 &&
                        currentConnection.getProviderFunction().get(currentConnectionProvide).get(0).isSingleUsed())
                {
                    deallocatingConnections.add(currentConnectionProvide);
                    allConnectionsProvide.add(currentConnectionProvide);
                }
                ProjectDevice providerProjectDevice = currentConnectionProvide.getOwnerProjectDevice();
                currentConnectionProvide.getPins().forEach(pin -> {
                    if (usedRefPin.containsKey(providerProjectDevice) && usedRefPin.get(providerProjectDevice).contains(pin.getRefTo())) {
                        if (!deallocatingRefTo.containsKey(providerProjectDevice)) {
                            deallocatingRefTo.put(providerProjectDevice, new HashSet<>());
                        }
                        deallocatingRefTo.get(providerProjectDevice).add(pin.getRefTo());
                        usedRefPin.get(providerProjectDevice).remove(pin.getRefTo());
                    }
                });
                deallocatingDeviceConnection.getProviderFunction().remove(currentConnectionProvide);
            }
            VoltageLevel consumerOperatingVoltageLevel = deallocatingDeviceConnection.getOperatingVoltageLevel();

            // Try matching the connection consume and provide.
            boolean[][] connectionMatching = getConnectionMatchingArray(allConnectionsConsume, allConnectionsProvide, usedRefPin, consumerOperatingVoltageLevel);

            // Create list of matched connection provide.
            List<Connection> connectionProvideList = new ArrayList<>();
            for (int j=0; j<allConnectionsProvide.size(); j++) {
                if (connectionMatching[i][j]) {
                    Connection connectionProvide = allConnectionsProvide.get(j);
                    connectionProvideList.add(connectionProvide);
                }
            }

            // If there is no matched connection.
            if (connectionProvideList.isEmpty()) {
                if (stopOnFirstUnmatched) {
                    return DeviceConnectionResult.ERROR;
                } else {
                    resultStatus = DeviceConnectionResultStatus.ERROR;
                }
            }

            // Sort the matched list
            VoltageLevel operatingVoltage = connectionProvideList.stream()
                    .flatMap(connection -> connection.getPins().stream())
                    .map(Pin::getVoltageLevel)
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                    .entrySet()
                    .stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);

            connectionProvideList.sort((connection1, connection2) -> {
                long countCorrect1 = connection1.getPins().stream().filter(pin -> pin.getVoltageLevel() == operatingVoltage).count();
                long countCorrect2 = connection2.getPins().stream().filter(pin -> pin.getVoltageLevel() == operatingVoltage).count();
                if (countCorrect1 != countCorrect2) {
                    return (int) (countCorrect2 - countCorrect1);
                }
                return Connection.LESS_PROVIDER_DEPENDENCY.compare(connection1, connection2);
            });

            // Keep the result in the possibleConnection map.
            possibleConnections.put(connectionConsume, connectionProvideList);

            // Reallocation the deallocated connection.
            deallocatingRefTo.forEach((providerProjectDevice, refTo) ->
                    usedRefPin.get(providerProjectDevice).addAll(deallocatingRefTo.get(providerProjectDevice))
            );
            allConnectionsProvide.removeAll(deallocatingConnections);
        }
        return new DeviceConnectionResult(resultStatus, possibleConnections);
    }
}
