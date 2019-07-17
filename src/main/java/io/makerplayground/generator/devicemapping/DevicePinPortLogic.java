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
import io.makerplayground.project.DevicePinPortConnection;
import io.makerplayground.project.ProjectDevice;
import lombok.NonNull;

import java.util.*;
import java.util.stream.Stream;

public class DevicePinPortLogic {

    private final ActualDevice controller;

    /* Status of the connection (OK or ERROR)
     */
    private DevicePinPortConnectionResultStatus status;

    /* DevicePinPortConnection keeps the connection between 2 devices.
     * Set<DevicePinPortConnection> should contain device connection for every pair of connectible actual devices.
     * Set<Set<DevicePinPortConnection>> is all connection patterns that could be generated.
     */
    private Set<Set<DevicePinPortConnection>> possibleDevicePinPortConnection;

    private Map<Set<DevicePinPortConnection>, List<Pin>> remainingPinProvide;

    private Map<Set<DevicePinPortConnection>, List<Port>> remainingPortProvide;


    /* The assigned pin and port connection is used as constraint to limit the generated possible answers.
     */
    private Collection<DevicePinPortConnection> assignedDevicePinPortConnections;

    public DevicePinPortLogic() {
        this(Collections.emptyMap(), Collections.emptySortedSet());
    }

    public DevicePinPortLogic(@NonNull Map<ProjectDevice, ActualDevice> deviceMap) {
        this(deviceMap, Collections.emptySortedSet());
    }

    public DevicePinPortLogic(@NonNull Map<ProjectDevice, ActualDevice> deviceMap, @NonNull Collection<DevicePinPortConnection> assignedDevicePinPortConnections) {
        this(deviceMap, assignedDevicePinPortConnections, null, null);
    }

    private DevicePinPortLogic(@NonNull Map<ProjectDevice, ActualDevice> deviceMap,
                               @NonNull Collection<DevicePinPortConnection> assignedDevicePinPortConnections,
                               Map<Set<DevicePinPortConnection>, List<Pin>> remainingPinProvide,
                               Map<Set<DevicePinPortConnection>, List<Port>> remainingPortProvide)
    {
        this.controller = deviceMap.get(ProjectDevice.CONTROLLER);
        this.status = DevicePinPortConnectionResultStatus.OK;
        this.assignedDevicePinPortConnections = assignedDevicePinPortConnections;

        Set<DevicePinPortConnection> emptyInitial = new HashSet<>();

        /* Only controller device in the project also has the valid pin/port connection which is an empty set */
        this.possibleDevicePinPortConnection = new HashSet<>();
        this.possibleDevicePinPortConnection.add(emptyInitial);

        if (remainingPinProvide == null) {
            this.remainingPinProvide = new HashMap<>();
            this.remainingPinProvide.put(emptyInitial, Objects.nonNull(controller) && controller.getPinProvide() != null ? controller.getPinProvide() : Collections.emptyList());
        } else {
            this.remainingPinProvide = remainingPinProvide;
        }

        if (remainingPortProvide == null) {
            this.remainingPortProvide = new HashMap<>();
            this.remainingPortProvide.put(emptyInitial, Objects.nonNull(controller) && controller.getPortProvide() != null ? controller.getPortProvide() : Collections.emptyList());
        } else {
            this.remainingPortProvide = remainingPortProvide;
        }

        /* Suppose that the actual device can have dependent device so the controller cannot provide the appropriate pin or port.
         * So the order is not important.
         */
        Map<ProjectDevice, ActualDevice> allDeviceMap = new HashMap<>(deviceMap);
        while(!allDeviceMap.isEmpty()) {
            ProjectDevice removingItem = null;
            for (ProjectDevice projectDevice: allDeviceMap.keySet()) {
                ActualDevice actualDevice = allDeviceMap.get(projectDevice);
                if (actualDevice == null || projectDevice == ProjectDevice.CONTROLLER) {
                    continue;
                }
                DevicePinPortConnectionResult result = this.withNewActualDevice(projectDevice, actualDevice);
                if (result.getStatus() == DevicePinPortConnectionResultStatus.OK) {
                    this.possibleDevicePinPortConnection = result.getConnectionSet();
                    this.remainingPortProvide = result.getRemainingPortProvide();
                    this.remainingPinProvide = result.getRemainingPinProvide();
                    removingItem = projectDevice;
                    break;
                }
            }
            if (removingItem == null) {
                status = DevicePinPortConnectionResultStatus.ERROR;
                return;
            }
            else {
                allDeviceMap.remove(removingItem);
            }
        }
    }

    public DevicePinPortConnectionResultStatus checkCompatibilityFor(ProjectDevice projectDevice, ActualDevice actualDevice) {
        DevicePinPortConnectionResult result = this.withNewActualDevice(projectDevice, actualDevice);
        return result.getStatus();
    }

    /* Caution!!! This method implementation assumes that the pin/port provider is controller only
     * If there is the change in assumption, the code implementation must be changed.
     */
    public DevicePinPortConnectionResult withNewActualDevice(ProjectDevice target, ActualDevice actualDevice) {
        if (actualDevice.getDeviceType() == DeviceType.CONTROLLER) {
            throw new UnsupportedOperationException("Not support for controller");
        }
        if (actualDevice.isPortProviderDevice()) {
            throw new UnsupportedOperationException("Not support for port expander device yet");
        }
        if (actualDevice.isPinProviderDevice()) {
            throw new UnsupportedOperationException("Not support for pin expander device yet");
        }

        boolean hasPortConsume = Objects.nonNull(actualDevice.getPortConsume());
        boolean hasPinConsume = Objects.nonNull(actualDevice.getPinConsume());
        if (hasPinConsume && hasPortConsume) {
            throw new UnsupportedOperationException("Not support for device that has both pinConsume and portConsume. " +
                    "Implementation is finished but testing is needed. " +
                    "Please uncomment this throw exception!!");
        }

        /* device has no connection (e.g. virtual device) */
        if (!hasPortConsume && !hasPinConsume) {
            return new DevicePinPortConnectionResult(DevicePinPortConnectionResultStatus.OK, possibleDevicePinPortConnection, remainingPinProvide, remainingPortProvide);
        }

        Set<Set<DevicePinPortConnection>> possibleOutput = new HashSet<>();
        Map<Set<DevicePinPortConnection>, List<Pin>> possibleRemainingPinProvide = new HashMap<>(this.remainingPinProvide);
        Map<Set<DevicePinPortConnection>, List<Port>> possibleRemainingPortProvide = new HashMap<>(this.remainingPortProvide);
        /* for each solution */
        for (Set<DevicePinPortConnection> connections: possibleDevicePinPortConnection) {
            /* generate list of pinProvide and portProvide */
            List<Pin> allPinProvide = new ArrayList<>();
            List<Port> allPortProvide = new ArrayList<>();
            if (possibleRemainingPinProvide.containsKey(connections)) {
                allPinProvide.addAll(possibleRemainingPinProvide.get(connections));
            }
            if (possibleRemainingPortProvide.containsKey(connections)) {
                allPortProvide.addAll(possibleRemainingPortProvide.get(connections));
            }

            /* if the device connection is not assigned yet */
            if (assignedDevicePinPortConnections.stream().noneMatch(devicePinPortConnection -> devicePinPortConnection.getTo() == target)) {
                /* try consume pin and port from pinProvide and portProvide */
                boolean[][] portMatching = new boolean[hasPortConsume ? actualDevice.getPortConsume().size() : 0][];
                for (int i=0; i<portMatching.length; i++) {
                    portMatching[i] = new boolean[allPortProvide.size()];
                    for (int j=0; j<allPortProvide.size(); j++) {
                        portMatching[i][j] = true;
                        Port portConsume = actualDevice.getPortConsume().get(i);
                        Port portProvide = allPortProvide.get(j);
                        if (portProvide.getType() != portConsume.getType()) {
                            portMatching[i][j] = false;
                            continue;
                        }
                        if (portProvide.getElements().size() != portConsume.getElements().size()) {
                            portMatching[i][j] = false;
                            continue;
                        }
                        for (int k=0; k<portProvide.getElements().size(); k++) {
                            if (!portProvide.getElements().get(k).getFunction().contains(portConsume.getElements().get(k).getFunction().get(0))) {
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

                boolean[][] pinMatching = new boolean[hasPinConsume ? actualDevice.getPinConsume().size() : 0][];
                for (int i=0; i<pinMatching.length; i++) {
                    pinMatching[i] = new boolean[allPinProvide.size()];
                    for (int j=0; j<allPinProvide.size(); j++) {
                        pinMatching[i][j] = true;
                        Pin pinConsume = actualDevice.getPinConsume().get(i);
                        Pin pinProvide = allPinProvide.get(j);
                        if (!pinProvide.getFunction().contains(pinConsume.getFunction().get(0))) {
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

                if (hasPortConsume && hasPinConsume) {
                    Set<DevicePinPortConnection> portConnections = generateAllPossibleDevicePortConnection(portMatching, target, ProjectDevice.CONTROLLER, actualDevice, controller, actualDevice.getPortConsume(), allPortProvide);
                    Set<DevicePinPortConnection> pinConnections = generateAllPossibleDevicePinConnection(pinMatching, target, ProjectDevice.CONTROLLER, actualDevice, controller, actualDevice.getPinConsume(), allPinProvide);
                    Set<DevicePinPortConnection> mergeConnection = mergeAllPossiblePinAndPortConnection(portConnections, pinConnections);
                    if (mergeConnection.isEmpty()) {
                        return DevicePinPortConnectionResult.ERROR;
                    }
                    for (DevicePinPortConnection pinPortConnection : mergeConnection) {
                        Set<DevicePinPortConnection> temp = new HashSet<>(connections);
                        temp.add(pinPortConnection);
                        List<Port> portProvide = new ArrayList<>(allPortProvide);
                        List<Pin> pinProvide = new ArrayList<>(allPinProvide);
                        portProvide.removeAll(pinPortConnection.getPortMapFromTo().keySet());
                        pinPortConnection.getPortMapFromTo().forEach((portFrom, portTo) -> {
                            for (int i=0; i<portFrom.getElements().size(); i++) {
                                if (!portTo.getElements().get(i).getFunction().get(0).getOpposite().isMultipleUsed()) {
                                    pinProvide.remove(portFrom.getElements().get(i));
                                }
                            }
                        });
                        pinPortConnection.getPinMapFromTo().forEach((pinFrom, pinTo) -> {
                            if (!pinTo.getFunction().get(0).getOpposite().isMultipleUsed()) {
                                pinProvide.remove(pinFrom);
                            }
                        });
                        possibleOutput.add(temp);
                        possibleRemainingPinProvide.put(temp, pinProvide);
                        possibleRemainingPortProvide.put(temp, portProvide);
                    }
                    possibleRemainingPortProvide.remove(connections);
                    possibleRemainingPinProvide.remove(connections);
                }
                /* has portConsume only */
                else if (hasPortConsume) {
                    Set<DevicePinPortConnection> portConnections = generateAllPossibleDevicePortConnection(portMatching, target, ProjectDevice.CONTROLLER , actualDevice, controller, actualDevice.getPortConsume(), allPortProvide);
                    if (portConnections.isEmpty()) {
                        return DevicePinPortConnectionResult.ERROR;
                    }
                    for (DevicePinPortConnection portConnection : portConnections) {
                        Set<DevicePinPortConnection> temp = new HashSet<>(connections);
                        temp.add(portConnection);
                        List<Port> portProvide = new ArrayList<>(allPortProvide);
                        List<Pin> pinProvide = new ArrayList<>(allPinProvide);
                        portProvide.removeAll(portConnection.getPortMapFromTo().keySet());
                        portConnection.getPortMapFromTo().forEach((portFrom, portTo) -> {
                            for (int i=0; i<portFrom.getElements().size(); i++) {
                                if (!portTo.getElements().get(i).getFunction().get(0).getOpposite().isMultipleUsed()) {
                                    pinProvide.remove(portFrom.getElements().get(i));
                                }
                            }
                        });
                        possibleOutput.add(temp);
                        possibleRemainingPinProvide.put(temp, pinProvide);
                        possibleRemainingPortProvide.put(temp, portProvide);
                    }
                    possibleRemainingPortProvide.remove(connections);
                    possibleRemainingPinProvide.remove(connections);
                }
                /* has pinConsume only */
                else {
                    Set<DevicePinPortConnection> pinConnections = generateAllPossibleDevicePinConnection(pinMatching, target, ProjectDevice.CONTROLLER, actualDevice, controller, actualDevice.getPinConsume(), allPinProvide);
                    if (pinConnections.isEmpty()) {
                        return DevicePinPortConnectionResult.ERROR;
                    }
                    for (DevicePinPortConnection pinConnection : pinConnections) {
                        Set<DevicePinPortConnection> temp = new HashSet<>(connections);
                        temp.add(pinConnection);
                        List<Pin> pinProvide = new ArrayList<>(allPinProvide);
                        pinConnection.getPinMapFromTo().forEach((pinFrom, pinTo) -> {
                            if (!pinTo.getFunction().get(0).getOpposite().isMultipleUsed()) {
                                pinProvide.remove(pinFrom);
                            }
                        });
                        possibleOutput.add(temp);
                        possibleRemainingPinProvide.put(temp, pinProvide);
                    }
                    possibleRemainingPinProvide.remove(connections);
                }
            }
            /* if the device connection is already assigned */
            else {
                Stream<DevicePinPortConnection> stream = assignedDevicePinPortConnections.stream()
                        .filter(devicePinPortConnection -> devicePinPortConnection.getTo() == target);
                if (stream.count() > 1) {
                    throw new UnsupportedOperationException("Not support the case that one device has two or more devices to connected");
                }
                DevicePinPortConnection assignedConnection = stream.findFirst().orElseThrow();

                Set<DevicePinPortConnection> temp = new HashSet<>(connections);
                temp.add(assignedConnection);
                List<Port> portProvide = new ArrayList<>(allPortProvide);
                List<Pin> pinProvide = new ArrayList<>(allPinProvide);
                portProvide.removeAll(assignedConnection.getPortMapFromTo().keySet());
                assignedConnection.getPortMapFromTo().forEach((portFrom, portTo) -> {
                    for (int i=0; i<portFrom.getElements().size(); i++) {
                        if (!portTo.getElements().get(i).getFunction().get(0).getOpposite().isMultipleUsed()) {
                            pinProvide.remove(portFrom.getElements().get(i));
                        }
                    }
                });
                assignedConnection.getPinMapFromTo().forEach((pinFrom, pinTo) -> {
                    if (!pinTo.getFunction().get(0).getOpposite().isMultipleUsed()) {
                        pinProvide.remove(pinFrom);
                    }
                });
                possibleOutput.add(temp);
                possibleRemainingPinProvide.put(temp, pinProvide);
                possibleRemainingPortProvide.put(temp, portProvide);
                possibleRemainingPortProvide.remove(connections);
                possibleRemainingPinProvide.remove(connections);
            }
        }
        return new DevicePinPortConnectionResult(DevicePinPortConnectionResultStatus.OK, possibleOutput, possibleRemainingPinProvide, possibleRemainingPortProvide);
    }

    private Set<DevicePinPortConnection> mergeAllPossiblePinAndPortConnection(Set<DevicePinPortConnection> portConnections, Set<DevicePinPortConnection> pinConnections) {
        Set<DevicePinPortConnection> result = new HashSet<>();
        for (DevicePinPortConnection portConnection: portConnections) {
            for (DevicePinPortConnection pinConnection: pinConnections) {
                result.add(new DevicePinPortConnection(portConnection.getFrom(), portConnection.getTo(), pinConnection.getPinMapFromTo(), portConnection.getPortMapFromTo()));
            }
        }
        return result;
    }

    private Set<DevicePinPortConnection> generateAllPossibleDevicePinConnection(boolean[][] pinMatching, ProjectDevice projectDeviceConsumer, ProjectDevice projectDeviceProvider, ActualDevice consumer, ActualDevice provider, List<Pin> pinConsume, List<Pin> allPinProvide) {
        boolean[][] selected = new boolean[pinMatching.length][];
        for (int i=0; i<pinMatching.length; i++) {
            selected[i] = new boolean[pinMatching[i].length];
            for (int j=0; j<selected[i].length; j++) {
                selected[i][j] = false;
            }
        }
        return generateAllPossibleDevicePinConnectionRecursive(pinMatching, selected, projectDeviceConsumer, projectDeviceProvider, consumer, provider, pinConsume, allPinProvide, 0);
    }

    private Set<DevicePinPortConnection> generateAllPossibleDevicePinConnectionRecursive(boolean[][] pinMatching, boolean[][] selected, ProjectDevice projectDeviceConsumer, ProjectDevice projectDeviceProvider, ActualDevice consumer, ActualDevice provider, List<Pin> pinConsume, List<Pin> allPinProvide, int currentRow) {
        if (currentRow < 0 || currentRow > pinMatching.length) {
            throw new IllegalStateException("illegal row value");
        }
        // base case
        if (currentRow == selected.length) {
            Set<DevicePinPortConnection> result = new HashSet<>();
            Map<Pin, Pin> pinMap = new HashMap<>();
            for (int i=0; i<selected.length; i++) {
                int trueIndex = -1;
                for (int j=0; j<selected[i].length; j++) {
                    if (selected[i][j]) {
                        trueIndex = j;
                    }
                }
                pinMap.put(allPinProvide.get(trueIndex), pinConsume.get(i));
            }
            result.add(new DevicePinPortConnection(projectDeviceProvider, projectDeviceConsumer, pinMap, null));
            return result;
        }
        // recursive case
        Set<DevicePinPortConnection> result = new HashSet<>();
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
                    result.addAll(generateAllPossibleDevicePinConnectionRecursive(pinMatching, temp, projectDeviceConsumer, projectDeviceProvider, consumer, provider, pinConsume, allPinProvide, currentRow + 1));
                    temp[currentRow][j] = false;
                }
            }
        }
        if (!rowHasTrue) {
            return new HashSet<>();
        }
        return result;
    }

    private Set<DevicePinPortConnection> generateAllPossibleDevicePortConnection(boolean[][] portMatching, ProjectDevice projectDeviceConsumer, ProjectDevice projectDeviceProvider, ActualDevice consumer, ActualDevice provider, List<Port> portConsume, List<Port> allPortProvide) {
        boolean[][] selected = new boolean[portMatching.length][];
        for (int i=0; i<portMatching.length; i++) {
            selected[i] = new boolean[portMatching[i].length];
            for (int j=0; j<selected[i].length; j++) {
                selected[i][j] = false;
            }
        }
        return generateAllPossibleDevicePortConnectionRecursive(portMatching, selected, projectDeviceConsumer, projectDeviceProvider, consumer, provider, portConsume, allPortProvide, 0);
    }

    private Set<DevicePinPortConnection> generateAllPossibleDevicePortConnectionRecursive(boolean[][] portMatching, boolean[][] selected, ProjectDevice projectDeviceConsumer, ProjectDevice projectDeviceProvider, ActualDevice consumer, ActualDevice provider, List<Port> portConsume, List<Port> allPortProvide, int currentRow) {
        if (currentRow >= selected.length) {
            Set<DevicePinPortConnection> result = new HashSet<>();
            Map<Port, Port> portMap = new HashMap<>();
            for (int i=0; i<selected.length; i++) {
                int trueIndex = -1;
                for (int j=0; j<selected[i].length; j++) {
                    if (selected[i][j]) {
                        trueIndex = j;
                    }
                }
                portMap.put(allPortProvide.get(trueIndex), portConsume.get(i));
            }
            result.add(new DevicePinPortConnection(projectDeviceProvider, projectDeviceConsumer, null, portMap));
            return result;
        }
        Set<DevicePinPortConnection> result = new HashSet<>();
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
                    result.addAll(generateAllPossibleDevicePortConnectionRecursive(portMatching, temp, projectDeviceConsumer, projectDeviceProvider, consumer, provider, portConsume, allPortProvide, currentRow+1));
                    temp[currentRow][j] = false;
                }
            }
        }
        if (!rowHasTrue) {
            return new HashSet<>();
        }
        return result;
    }

    private boolean[][] deepCopy(boolean[][] original) {
        if (original == null) {
            return null;
        }
        final boolean[][] result = new boolean[original.length][];
        for (int i = 0; i < original.length; i++) {
            result[i] = Arrays.copyOf(original[i], original[i].length);
        }
        return result;
    }

}
