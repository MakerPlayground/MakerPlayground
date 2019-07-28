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
import io.makerplayground.project.PinPortConnection;
import io.makerplayground.project.ProjectDevice;
import lombok.NonNull;

import java.util.*;

public class DevicePinPortLogic {

    public static PinPortConnectionResult generateOneStepPossiblePinPortConnection(Set<Pin> remainingPinProvide,
                                                                                   Set<Port> remainingPortProvide,
                                                                                   ProjectDevice projectDevice,
                                                                                   ActualDevice actualDevice)
    {
        List<Port> allPortsProvide = new ArrayList<>(remainingPortProvide);
        List<Pin> allPinsProvide = new ArrayList<>(remainingPinProvide);

        List<Port> allPortsConsume = actualDevice.getPortConsumeByOwnerDevice(projectDevice);
        List<Pin> allPinsConsume = actualDevice.getPinConsumeByOwnerDevice(projectDevice);
        allPortsConsume.forEach(port -> allPinsConsume.addAll(port.getElements()));

        boolean[][] portMatching = getPortMatchingArray(allPortsConsume, allPortsProvide);
        boolean[][] pinMatching = getPinMatchingArray(allPinsConsume, allPinsProvide);

        List<PinPortConnection> portConnections = generateAllPossibleDevicePortConnection(portMatching, allPortsProvide, allPortsConsume);
        List<PinPortConnection> pinConnections = generateAllPossibleDevicePinConnection(pinMatching, allPinsProvide, allPinsConsume);
        List<PinPortConnection> mergeConnection = mergeAllPossiblePinAndPortConnection(portConnections, pinConnections);
        if (mergeConnection.isEmpty()) {
            return PinPortConnectionResult.ERROR;
        }
        return new PinPortConnectionResult(PinPortConnectionResultStatus.OK, mergeConnection);
    }

    private static boolean[][] getPinMatchingArray(List<Pin> allPinsConsume, List<Pin> remainingPinProvide) {
        boolean[][] pinMatching = new boolean[allPinsConsume.size()][];
        for (int i=0; i<pinMatching.length; i++) {
            pinMatching[i] = new boolean[remainingPinProvide.size()];
            for (int j=0; j<remainingPinProvide.size(); j++) {
                pinMatching[i][j] = true;
                Pin pinConsume = allPinsConsume.get(i);
                Pin pinProvide = remainingPinProvide.get(j);
                if (!pinProvide.getFunction().contains(pinConsume.getFunction().get(0).getOpposite())) {
                    pinMatching[i][j] = false;
                    continue;
                }
                VoltageLevel consumeVoltageLevel = pinConsume.getVoltageLevel();
                VoltageLevel provideVoltageLevel = pinProvide.getVoltageLevel();
                if (consumeVoltageLevel == VoltageLevel.LEVEL_3v3 && provideVoltageLevel == VoltageLevel.LEVEL_5) {
                    pinMatching[i][j] = false;
                    break;
                }
                if (consumeVoltageLevel == VoltageLevel.LEVEL_5 && provideVoltageLevel == VoltageLevel.LEVEL_3v3) {
                    pinMatching[i][j] = false;
                    break;
                }
            }
        }
        return pinMatching;
    }

    private static boolean[][] getPortMatchingArray(List<Port> allPortsConsume, List<Port> remainingPortProvide) {
        boolean[][] portMatching = new boolean[allPortsConsume.size()][];
        for (int i=0; i<portMatching.length; i++) {
            portMatching[i] = new boolean[remainingPortProvide.size()];
            for (int j=0; j<remainingPortProvide.size(); j++) {
                portMatching[i][j] = true;
                Port portConsume = allPortsConsume.get(i);
                Port portProvide = remainingPortProvide.get(j);
                if (portProvide.getType() != portConsume.getType()) {
                    portMatching[i][j] = false;
                    continue;
                }
                if (portProvide.getElements().size() != portConsume.getElements().size()) {
                    portMatching[i][j] = false;
                    continue;
                }
                for (int k=0; k<portProvide.getElements().size(); k++) {
                    if (!portProvide.getElements().get(k).getFunction().contains(portConsume.getElements().get(k).getFunction().get(0).getOpposite())) {
                        portMatching[i][j] = false;
                        break;
                    }
                    VoltageLevel consumeVoltageLevel = portConsume.getElements().get(i).getVoltageLevel();
                    VoltageLevel provideVoltageLevel = portProvide.getElements().get(j).getVoltageLevel();
                    if (consumeVoltageLevel == VoltageLevel.LEVEL_3v3 && provideVoltageLevel == VoltageLevel.LEVEL_5) {
                        portMatching[i][j] = false;
                        break;
                    }
                    if (consumeVoltageLevel == VoltageLevel.LEVEL_5 && provideVoltageLevel == VoltageLevel.LEVEL_3v3) {
                        portMatching[i][j] = false;
                        break;
                    }
                }
            }
        }
        return portMatching;
    }

    private static List<PinPortConnection> mergeAllPossiblePinAndPortConnection(List<PinPortConnection> portConnections,
                                                                                List<PinPortConnection> pinConnections)
    {
        List<PinPortConnection> result = new ArrayList<>();
        for (PinPortConnection portConnection: portConnections) {
            for (PinPortConnection pinConnection: pinConnections) {
                result.add(new PinPortConnection(pinConnection.getPinMapConsumerProvider(),
                        portConnection.getPortMapConsumerProvider()));
            }
        }
        return result;
    }

    private static List<PinPortConnection> generateAllPossibleDevicePinConnection(boolean[][] pinMatching,
                                                                                 List<Pin> allPinsProvide,
                                                                                 List<Pin> allPinsConsume)
    {
        boolean[][] selected = new boolean[pinMatching.length][];
        for (int i=0; i<pinMatching.length; i++) {
            selected[i] = new boolean[pinMatching[i].length];
            for (int j=0; j<selected[i].length; j++) {
                selected[i][j] = false;
            }
        }
        return generateAllPossibleDevicePinConnectionRecursive(pinMatching, selected, allPinsProvide, allPinsConsume, 0);
    }

    private static List<PinPortConnection> generateAllPossibleDevicePinConnectionRecursive(boolean[][] pinMatching,
                                                                                          boolean[][] selected,
                                                                                          List<Pin> allPinsProvide,
                                                                                          List<Pin> allPinsConsume,
                                                                                          int currentRow)
    {
        if (currentRow < 0 || currentRow > pinMatching.length) {
            throw new IllegalStateException("illegal row value");
        }
        // base case
        if (currentRow == selected.length) {
            List<PinPortConnection> result = new ArrayList<>();
            SortedMap<Pin, Pin> pinMap = new TreeMap<>();
            for (int i=0; i<selected.length; i++) {
                int trueIndex = -1;
                for (int j=0; j<selected[i].length; j++) {
                    if (selected[i][j]) {
                        trueIndex = j;
                    }
                }
                pinMap.put(allPinsConsume.get(i), allPinsProvide.get(trueIndex));
            }
            result.add(new PinPortConnection(pinMap, null));
            return result;
        }
        // recursive case
        List<PinPortConnection> result = new ArrayList<>();
        boolean rowHasTrue = false;
        for (int j=0; j<pinMatching[currentRow].length; j++) {
            if (pinMatching[currentRow][j]) {
                rowHasTrue = true;
                boolean[][] temp = deepCopy(selected);
                boolean repeat = false;
                for (int i=0; i<currentRow; i++) {
                    if (selected[i][j]) {
                        repeat = true;
                    }
                }
                if (!repeat) {
                    temp[currentRow][j] = true;
                    result.addAll(generateAllPossibleDevicePinConnectionRecursive(pinMatching, temp, allPinsProvide, allPinsConsume, currentRow + 1));
                    temp[currentRow][j] = false;
                }
            }
        }
        if (!rowHasTrue) {
            return Collections.emptyList();
        }
        return result;
    }

    private static List<PinPortConnection> generateAllPossibleDevicePortConnection(boolean[][] portMatching,
                                                                                   List<Port> allPortsProvide,
                                                                                   List<Port> allPortsConsume)
    {
        boolean[][] selected = new boolean[portMatching.length][];
        for (int i=0; i<portMatching.length; i++) {
            selected[i] = new boolean[portMatching[i].length];
            for (int j=0; j<selected[i].length; j++) {
                selected[i][j] = false;
            }
        }
        return generateAllPossibleDevicePortConnectionRecursive(portMatching, selected, allPortsProvide, allPortsConsume, 0);
    }

    private static List<PinPortConnection> generateAllPossibleDevicePortConnectionRecursive(boolean[][] portMatching,
                                                                                     boolean[][] selected,
                                                                                     List<Port> allPortsProvide,
                                                                                     List<Port> allPortsConsume,
                                                                                     int currentRow)
    {
        if (currentRow >= selected.length) {
            List<PinPortConnection> result = new ArrayList<>();
            SortedMap<Port, Port> portMap = new TreeMap<>();
            for (int i=0; i<selected.length; i++) {
                int trueIndex = -1;
                for (int j=0; j<selected[i].length; j++) {
                    if (selected[i][j]) {
                        trueIndex = j;
                    }
                }
                portMap.put(allPortsConsume.get(i), allPortsProvide.get(trueIndex));
            }
            result.add(new PinPortConnection(null, portMap));
            return result;
        }
        List<PinPortConnection> result = new ArrayList<>();
        boolean rowHasTrue = false;
        for (int j=0; j<portMatching[currentRow].length; j++) {
            if (portMatching[currentRow][j]) {
                rowHasTrue = true;
                boolean[][] temp = deepCopy(selected);
                boolean repeat = false;
                for (int i=0; i<currentRow; i++) {
                    if (selected[i][j]) {
                        repeat = true;
                    }
                }
                if (!repeat) {
                    temp[currentRow][j] = true;
                    result.addAll(generateAllPossibleDevicePortConnectionRecursive(portMatching, temp, allPortsProvide, allPortsConsume, currentRow+1));
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
