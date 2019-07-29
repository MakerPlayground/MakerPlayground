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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.device.actual.*;
import io.makerplayground.device.generic.GenericDevice;
import io.makerplayground.device.shared.Action;
import io.makerplayground.device.shared.Condition;
import io.makerplayground.device.shared.Parameter;
import io.makerplayground.device.shared.constraint.Constraint;
import io.makerplayground.generator.devicemapping.DeviceMappingResult;
import io.makerplayground.generator.devicemapping.PinPortConnectionResult;
import io.makerplayground.generator.devicemapping.PinPortConnectionResultStatus;
import io.makerplayground.generator.devicemapping.DevicePinPortLogic;
import io.makerplayground.ui.dialog.configdevice.CompatibleDevice;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import lombok.*;

import java.util.*;
import java.util.stream.Collectors;

import static io.makerplayground.project.ProjectDevice.CONTROLLER;

@JsonSerialize(using = ProjectConfigurationSerializer.class)
@JsonDeserialize(using = ProjectConfigurationDeserializer.class)
public final class ProjectConfiguration {

    /* input variables: the compatibilities data from the project instance. These variables must be set before calculation */
    @JsonIgnore private Map<ProjectDevice, Map<Action, Map<Parameter, Constraint>>> actionCompatibility;
    @JsonIgnore private Map<ProjectDevice, Map<Condition, Map<Parameter, Constraint>>> conditionCompatibility;

    /* state variables: the variable used in calculation and cached the sub-solution */
    @JsonIgnore private ObservableList<ProjectDevice> usedDevices = FXCollections.observableArrayList();
    @JsonIgnore private FilteredList<ProjectDevice> nonControllerDevices = new FilteredList<>(usedDevices, projectDevice -> projectDevice != CONTROLLER);
    @JsonIgnore private Set<Pin> remainingPinProvide = new HashSet<>();
    @JsonIgnore private Set<Port> remainingPortProvide = new HashSet<>();
    @JsonIgnore private List<CloudPlatform> remainingCloudPlatform = new ArrayList<>();

    /* output variables: compatible devices and pin/port connections */
    @JsonIgnore @Getter private SortedMap<ActualDevice, DeviceMappingResult> controllerSelectableMap;
    @JsonIgnore @Getter private Map<ProjectDevice, SortedMap<CompatibleDevice, DeviceMappingResult>> compatibleDevicesSelectableMap;
    @JsonIgnore @Getter private Map<ProjectDevice, Map<ActualDevice, List<PinPortConnection>>> compatibleDevicePinPortConnectionMap;

    /* data structure: user's input that would be stored in file */
    @Setter(AccessLevel.PACKAGE) @Getter private Platform platform;
    @Getter(AccessLevel.PACKAGE) private final Map<ProjectDevice, Map<Property, Object>> devicePropertyValueMap;
    @Getter(AccessLevel.PACKAGE) private final SortedMap<ProjectDevice, ActualDevice> deviceMap;
    @Getter(AccessLevel.PACKAGE) private final SortedMap<ProjectDevice, ProjectDevice> identicalDeviceMap;
    @Getter(AccessLevel.PACKAGE) private final SortedMap<ProjectDevice, PinPortConnection> devicePinPortConnections;
    @Getter(AccessLevel.PACKAGE) private final SortedMap<CloudPlatform, Map<String, String>> cloudParameterMap;

    /* unmodifiable variables: the unmodifiable view of data structure to protect the data change from outside class */
    @Getter private final Map<ProjectDevice, Map<Property, Object>> unmodifiableDevicePropertyValueMap;
    @Getter private final SortedMap<ProjectDevice, ActualDevice> unmodifiableDeviceMap;
    @Getter private final SortedMap<ProjectDevice, ProjectDevice> unmodifiableIdenticalDeviceMap;
    @Getter private final SortedMap<ProjectDevice, PinPortConnection> unmodifiableDevicePinPortConnections;
    @Getter private final SortedMap<CloudPlatform, Map<String, String>> unmodifiableCloudParameterMap;

    @Builder
    ProjectConfiguration(@NonNull Platform platform,
                                ActualDevice controller,
                                @NonNull Map<ProjectDevice, Map<Property, Object>> devicePropertyValueMap,
                                @NonNull SortedMap<ProjectDevice, ActualDevice> deviceMap,
                                @NonNull SortedMap<ProjectDevice, ProjectDevice> identicalDeviceMap,
                                @NonNull SortedMap<ProjectDevice, PinPortConnection> devicePinPortConnections,
                                @NonNull SortedMap<CloudPlatform, Map<String, String>> cloudPlatformParameterMap) {
        this.platform = platform;
        this.devicePropertyValueMap = devicePropertyValueMap;

        this.deviceMap = deviceMap;
        this.deviceMap.put(CONTROLLER, controller);
        this.identicalDeviceMap = identicalDeviceMap;
        this.devicePinPortConnections = devicePinPortConnections;
        this.cloudParameterMap = cloudPlatformParameterMap;

        this.unmodifiableDevicePropertyValueMap = Collections.unmodifiableMap(devicePropertyValueMap);
        this.unmodifiableDeviceMap = Collections.unmodifiableSortedMap(deviceMap);
        this.unmodifiableIdenticalDeviceMap = Collections.unmodifiableSortedMap(identicalDeviceMap);
        this.unmodifiableDevicePinPortConnections = Collections.unmodifiableSortedMap(devicePinPortConnections);
        this.unmodifiableCloudParameterMap = Collections.unmodifiableSortedMap(cloudPlatformParameterMap);

        this.controllerSelectableMap = DeviceLibrary.INSTANCE
                .getActualDevice(getPlatform())
                .stream()
                .filter(actualDevice -> actualDevice.getDeviceType() == DeviceType.CONTROLLER)
                .collect(Collectors.toMap(o -> o, o -> DeviceMappingResult.OK, (o1, o2)-> { throw new IllegalStateException(""); }, TreeMap::new));
    }

    void updateCompatibility(Map<ProjectDevice, Map<Action, Map<Parameter, Constraint>>> actionCompatibility,
                             Map<ProjectDevice, Map<Condition, Map<Parameter, Constraint>>> conditionCompatibility) {
        this.actionCompatibility = actionCompatibility;
        this.conditionCompatibility = conditionCompatibility;

        this.usedDevices.clear();
        this.usedDevices.addAll(this.actionCompatibility.keySet());
        this.usedDevices.addAll(this.conditionCompatibility.keySet());
        this.usedDevices.add(CONTROLLER);

        /* remove the unused device from the data structure */
        Set<ProjectDevice> unusedDevices = deviceMap.keySet().stream().filter(projectDevice -> !usedDevices.contains(projectDevice)).collect(Collectors.toSet());
        for (ProjectDevice removingDevice: unusedDevices) {
            unsetDevice(removingDevice);
        }

        generateDeviceSelectableMapAndPinPortConnection();
    }

    private void generateDeviceSelectableMapAndPinPortConnection() {
        Map<ProjectDevice, SortedMap<CompatibleDevice, DeviceMappingResult>> deviceSelectableMap = new HashMap<>();
        Map<ProjectDevice, Map<ActualDevice, List<PinPortConnection>>> deviceConnectionMap = new HashMap<>();
        /* add all device that is the same generic with "OK" mapping result */
        for (ProjectDevice device: nonControllerDevices) {
            SortedMap<CompatibleDevice, DeviceMappingResult> selectable = DeviceLibrary.INSTANCE.getActualDevice(getPlatform())
                    .stream()
                    .filter(actualDevice -> actualDevice.getCompatibilityMap().containsKey(device.getGenericDevice()))
                    .collect(Collectors.toMap(CompatibleDevice::new, o->DeviceMappingResult.OK, (o1, o2)->{throw new IllegalStateException("");}, TreeMap::new));
            if (getController() != null) {
                getController().getIntegratedDevices().forEach(integratedActualDevice -> {
                    if (integratedActualDevice.getCompatibilityMap().containsKey(device.getGenericDevice())) {
                        selectable.put(new CompatibleDevice(integratedActualDevice), DeviceMappingResult.OK);
                    }
                });
            }
            deviceSelectableMap.put(device, selectable);
        }
        setFlagToDeviceIfActionConditionIsIncompatible(deviceSelectableMap);
        setFlagToDeviceIfCloudIsNotSupport(deviceSelectableMap);
        setFlagToDeviceIfPinPortIsIncompatible(deviceSelectableMap, deviceConnectionMap);

        /* add identical device to selectable list */
        Map<ProjectDevice, List<CompatibleDevice>> possibleIdenticalDeviceMap = calculatePossibleIdenticalDeviceMap();
        for (ProjectDevice device: possibleIdenticalDeviceMap.keySet()) {
            SortedMap<CompatibleDevice, DeviceMappingResult> selectable = deviceSelectableMap.get(device);
            for (CompatibleDevice identicalDevice: possibleIdenticalDeviceMap.get(device)) {
                selectable.put(identicalDevice, DeviceMappingResult.OK);
            }
        }

        this.compatibleDevicesSelectableMap = deviceSelectableMap;
        this.compatibleDevicePinPortConnectionMap = deviceConnectionMap;
    }

    private Map<ProjectDevice, List<CompatibleDevice>> calculatePossibleIdenticalDeviceMap() {
        // build a map with list of generic device type that each project device has been used for example if humidity1 and altimeter1
        // are selected to share device with temperature1. map will contain mapping for from temperature1 (project device) to list of
        // humidity (generic device) and altimeter (generic device)
        Map<ProjectDevice, List<GenericDevice>> previousIdenticalUsedMap = new HashMap<>();
        for (ProjectDevice device : nonControllerDevices) {
            previousIdenticalUsedMap.put(device, new ArrayList<>());
        }
        for (ProjectDevice device : nonControllerDevices) {
            if (identicalDeviceMap.containsKey(device)) {
                previousIdenticalUsedMap.get(identicalDeviceMap.get(device)).add(device.getGenericDevice());
            }
        }

        Map<ProjectDevice, List<CompatibleDevice>> result = new HashMap<>();
        for (ProjectDevice device : nonControllerDevices) {
            result.put(device, new ArrayList<>());
        }
        if (getController() != null) {
            for (ProjectDevice projectDevice : nonControllerDevices) {
                for (ProjectDevice parentDevice : nonControllerDevices) {
                    if (getIdenticalDevice(projectDevice).isPresent() && getIdenticalDevice(projectDevice).get() == parentDevice) {
                        result.get(projectDevice).add(new CompatibleDevice(parentDevice));
                        continue;
                    }
                    if (projectDevice == parentDevice   // prevent sharing with ourselves
                            || getActualDevice(parentDevice).isEmpty()   // skip if the actual device hasn't been selected or if this device shares an actual device with other device
                            || projectDevice.getGenericDevice() == parentDevice.getGenericDevice()  // skip if the device has identical generic type e.g. temperature1 can't be merged with temperature2
                            || previousIdenticalUsedMap.get(parentDevice).contains(projectDevice.getGenericDevice())) {    // skip if this device has been selected by other device with the same generic type
                        continue;
                    }
                    ActualDevice actualDevice = getActualDevice(parentDevice).get();
                    if (actualDevice.getCompatibilityMap().containsKey(projectDevice.getGenericDevice())) {
                        boolean actionCompatible = checkActionCompatibility(projectDevice, actualDevice) == DeviceMappingResult.OK;
                        boolean conditionCompatible = checkConditionCompatibility(projectDevice, actualDevice) == DeviceMappingResult.OK;

                        if (actionCompatible && conditionCompatible) {
                            if (actualDevice.getCloudConsume() != null) {
                                // if this device uses a cloud platform and the controller has been selected, we accept this device
                                // if and only if the selected controller supports the cloud platform that this device uses
                                if (remainingCloudPlatform.contains(actualDevice.getCloudConsume())) {
                                    result.get(projectDevice).add(new CompatibleDevice(parentDevice));
                                }
                            } else {
                                result.get(projectDevice).add(new CompatibleDevice(parentDevice));
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    private void setFlagToDeviceIfCloudIsNotSupport(Map<ProjectDevice, SortedMap<CompatibleDevice, DeviceMappingResult>> deviceSelectableMap) {
        /* set reason for incompatible actual device */
        for (ProjectDevice device: this.nonControllerDevices) {
            var selectable = deviceSelectableMap.get(device);
            for (CompatibleDevice compatibleDevice: selectable.keySet()) {
                if (compatibleDevice.getActualDevice().isPresent() && selectable.get(compatibleDevice) == DeviceMappingResult.OK) {
                    ActualDevice actualDevice = compatibleDevice.getActualDevice().get();
                    if (actualDevice.getCloudConsume() != null && !remainingCloudPlatform.contains(actualDevice.getCloudConsume())) {
                        selectable.put(compatibleDevice, DeviceMappingResult.NO_AVAILABLE_CLOUD_PLATFORM);
                    }
                }
            }
        }
    }

    private void setFlagToDeviceIfPinPortIsIncompatible(Map<ProjectDevice, SortedMap<CompatibleDevice, DeviceMappingResult>> deviceSelectableMap, Map<ProjectDevice, Map<ActualDevice, List<PinPortConnection>>> deviceConnectionMap) {
        /* set reason for circuit incompatibility */
        for (ProjectDevice projectDevice: nonControllerDevices) {
            SortedMap<CompatibleDevice, DeviceMappingResult> selectable = deviceSelectableMap.get(projectDevice);
            Map<ActualDevice, List<PinPortConnection>> actualDeviceConnectionMap = new HashMap<>();
            for (CompatibleDevice compatibleDevice: selectable.keySet()) {
                if (compatibleDevice.getActualDevice().isPresent() && selectable.get(compatibleDevice) == DeviceMappingResult.OK) {
                    ActualDevice actualDevice = compatibleDevice.getActualDevice().get();

                    /* In order to check the compatible, the assigned pin/port must be temporary deallocated from the remaining pin/port. */
                    PinPortConnection connectionFromProjectDevice = devicePinPortConnections.getOrDefault(projectDevice, PinPortConnection.NOT_CONNECTED);
                    Queue<PinPortConnection> allDeallocatedConnections = new ArrayDeque<>();
                    Queue<PinPortConnection> connectionsToBeDeallocated = new ArrayDeque<>();
                    connectionsToBeDeallocated.add(connectionFromProjectDevice);
                    while(connectionsToBeDeallocated.size() > 0) {
                        PinPortConnection connection = connectionsToBeDeallocated.poll();
                        allDeallocatedConnections.add(connection);
                        remainingPinProvide.addAll(connection.getPinMapConsumerProvider().values().stream()
                                .filter(pin -> !pin.getFunction().get(0).isMultipleUsed())
                                .collect(Collectors.toList()));
                        remainingPinProvide.addAll(connection.getPortMapConsumerProvider().values().stream()
                                .flatMap(port -> port.getElements().stream())
                                .filter(pin -> !pin.getFunction().get(0).isMultipleUsed())
                                .collect(Collectors.toList()));
                        remainingPortProvide.addAll(connection.getPortMapConsumerProvider().values());
                        // TODO: add connection to be deallocated in case that there is the dependent device
                    }

                    PinPortConnectionResult result = DevicePinPortLogic.generateOneStepPossiblePinPortConnection(remainingPinProvide, remainingPortProvide, projectDevice, actualDevice);
                    if (result.getStatus() == PinPortConnectionResultStatus.ERROR) {
                        selectable.put(compatibleDevice, DeviceMappingResult.NO_AVAILABLE_PIN_PORT);
                    }
                    else {
                        actualDeviceConnectionMap.put(actualDevice, result.getConnections());
                    }

                    while(allDeallocatedConnections.size() > 0) {
                        PinPortConnection connection = allDeallocatedConnections.poll();
                        remainingPinProvide.removeAll(connection.getPinMapConsumerProvider().values().stream()
                                .filter(pin -> !pin.getFunction().get(0).isMultipleUsed())
                                .collect(Collectors.toList()));
                        remainingPinProvide.removeAll(connection.getPortMapConsumerProvider().values().stream()
                                .flatMap(port -> port.getElements().stream())
                                .filter(pin -> !pin.getFunction().get(0).isMultipleUsed())
                                .collect(Collectors.toList()));
                        remainingPortProvide.removeAll(connection.getPortMapConsumerProvider().values());
                        // TODO: add pin/port to be removed in case that there is the dependent device
                    }
                }
            }
            deviceConnectionMap.put(projectDevice, actualDeviceConnectionMap);
        }
    }

    private DeviceMappingResult checkConditionCompatibility(ProjectDevice device, ActualDevice actualDevice) {
        if (conditionCompatibility.containsKey(device)) {
            for (Condition condition : conditionCompatibility.get(device).keySet()) {
                if (!actualDevice.getCompatibilityMap().get(device.getGenericDevice()).getDeviceCondition().containsKey(condition)) {
                    return DeviceMappingResult.NO_SUPPORTING_CONDITION;
                }
                Map<Parameter, Constraint> parameterConstraintMap = actualDevice.getCompatibilityMap().get(device.getGenericDevice()).getDeviceCondition().get(condition);
                for (Parameter parameter : parameterConstraintMap.keySet()) {
                    if (!condition.getParameter().contains(parameter)) {
                        return DeviceMappingResult.CONDITION_PARAMETER_NOT_COMPATIBLE;
                    }
                }
                for (Parameter parameter : condition.getParameter()) {
                    if (!parameterConstraintMap.containsKey(parameter)) {
                        return DeviceMappingResult.CONDITION_PARAMETER_NOT_COMPATIBLE;
                    }
                    Constraint constraint = parameterConstraintMap.get(parameter);
                    if (!constraint.isCompatible(conditionCompatibility.get(device).get(condition).get(parameter))) {
                        return DeviceMappingResult.CONSTRAINT_NOT_COMPATIBLE;
                    }
                }
            }
        }
        return DeviceMappingResult.OK;
    }

    private DeviceMappingResult checkActionCompatibility(ProjectDevice device, ActualDevice actualDevice) {
        if (actionCompatibility.containsKey(device)) {
            for (Action action: actionCompatibility.get(device).keySet()) {
                if (!actualDevice.getCompatibilityMap().get(device.getGenericDevice()).getDeviceAction().containsKey(action)) {
                    return DeviceMappingResult.NO_SUPPORTING_ACTION;
                }
                Map<Parameter, Constraint> parameterConstraintMap = actualDevice.getCompatibilityMap().get(device.getGenericDevice()).getDeviceAction().get(action);
                for (Parameter parameter: parameterConstraintMap.keySet()) {
                    if (!action.getParameter().contains(parameter)) {
                        return DeviceMappingResult.ACTION_PARAMETER_NOT_COMPATIBLE;
                    }
                }
                for (Parameter parameter: action.getParameter()) {
                    if (!parameterConstraintMap.containsKey(parameter)) {
                        return DeviceMappingResult.ACTION_PARAMETER_NOT_COMPATIBLE;
                    }
                    Constraint constraint = parameterConstraintMap.get(parameter);
                    if (!constraint.isCompatible(actionCompatibility.get(device).get(action).get(parameter))) {
                        return DeviceMappingResult.CONSTRAINT_NOT_COMPATIBLE;
                    }
                }
            }
        }
        return DeviceMappingResult.OK;
    }

    private void setFlagToDeviceIfActionConditionIsIncompatible(Map<ProjectDevice, SortedMap<CompatibleDevice, DeviceMappingResult>> deviceSelectableMap) {
        /* set reason for incompatible actual device */
        for (ProjectDevice device: this.actionCompatibility.keySet()) {
            var selectable = deviceSelectableMap.get(device);
            for (CompatibleDevice compatibleDevice: selectable.keySet()) {
                if (selectable.get(compatibleDevice) == DeviceMappingResult.OK && compatibleDevice.getActualDevice().isPresent()) {
                    ActualDevice actualDevice = compatibleDevice.getActualDevice().get();
                    DeviceMappingResult result = checkActionCompatibility(device, actualDevice);
                    if (result!= DeviceMappingResult.OK) {
                        selectable.put(compatibleDevice, result);
                    }
                }
            }
        }
        for (ProjectDevice device: this.conditionCompatibility.keySet()) {
            var selectable = deviceSelectableMap.get(device);
            for (CompatibleDevice compatibleDevice: selectable.keySet()) {
                if (selectable.get(compatibleDevice) == DeviceMappingResult.OK && compatibleDevice.getActualDevice().isPresent()) {
                    ActualDevice actualDevice = compatibleDevice.getActualDevice().get();
                    DeviceMappingResult result = checkConditionCompatibility(device, actualDevice);
                    if (result!= DeviceMappingResult.OK) {
                        selectable.put(compatibleDevice, result);
                    }
                }
            }
        }
    }

    public void setController(ActualDevice controller) {
        if (this.deviceMap.get(CONTROLLER) != controller) {
            unsetAllDevices();

            /* set new controller and reset pin/port remaining */
            deviceMap.put(CONTROLLER, controller);
            remainingPinProvide.clear();
            remainingPortProvide.clear();
            remainingCloudPlatform.clear();
            remainingPinProvide.addAll(controller.getPinProvideByOwnerDevice(CONTROLLER));
            remainingPortProvide.addAll(controller.getPortProvideByOwnerDevice(CONTROLLER));
            remainingCloudPlatform.addAll(controller.getCloudPlatformSourceCodeLibrary().keySet());

            generateDeviceSelectableMapAndPinPortConnection();
        }
    }

    public ActualDevice getController() {
        return deviceMap.get(CONTROLLER);
    }

    public Optional<ActualDevice> getActualDevice(ProjectDevice projectDevice) {
        if (unmodifiableDeviceMap.containsKey(projectDevice)) {
            return Optional.of(unmodifiableDeviceMap.get(projectDevice));
        }
        return Optional.empty();
    }

    public Optional<CloudPlatform> getCloudConsume(ProjectDevice projectDevice) {
        if (unmodifiableDeviceMap.containsKey(projectDevice) && Objects.nonNull(unmodifiableDeviceMap.get(projectDevice).getCloudConsume())) {
            return Optional.of(unmodifiableDeviceMap.get(projectDevice).getCloudConsume());
        }
        else if (unmodifiableIdenticalDeviceMap.containsKey(projectDevice)) {
            return getCloudConsume(unmodifiableIdenticalDeviceMap.get(projectDevice));
        }
        return Optional.empty();
    }

    public Optional<Object> getPropertyValue(ProjectDevice device, Property p) {
        if (devicePropertyValueMap.containsKey(device) && devicePropertyValueMap.get(device).containsKey(p)) {
            return Optional.of(devicePropertyValueMap.get(device).get(p));
        }
        return Optional.empty();
    }

    public Optional<ProjectDevice> getIdenticalDevice(ProjectDevice projectDevice) {
        if (!unmodifiableIdenticalDeviceMap.containsKey(projectDevice)) {
            return Optional.empty();
        }
        ProjectDevice candidate = unmodifiableIdenticalDeviceMap.get(projectDevice);
        if (getIdenticalDevice(candidate).isEmpty()) {
            return Optional.of(candidate);
        }
        return getIdenticalDevice(candidate);
    }

    public void setActualDevice(ProjectDevice projectDevice, ActualDevice actualDevice) {
        if (actualDevice != null) {
            boolean differToPreviousActual = getActualDevice(projectDevice).isPresent() && getActualDevice(projectDevice).get() != actualDevice;
            boolean identicalDeviceIsSet = getIdenticalDevice(projectDevice).isPresent();
            if (differToPreviousActual || identicalDeviceIsSet) {
                unsetDevice(projectDevice);
            }
            deviceMap.put(projectDevice, actualDevice);
            generateDeviceSelectableMapAndPinPortConnection();
        } else {
            unsetDevice(projectDevice);
        }
    }

    public void setIdenticalDevice(ProjectDevice projectDevice, ProjectDevice parentDevice) {
        if (parentDevice != null) {
            boolean differToPreviousIdentical = getIdenticalDevice(projectDevice).isPresent() && getIdenticalDevice(projectDevice).get() != parentDevice;
            boolean actualDeviceIsSet = getActualDevice(projectDevice).isPresent();
            if (differToPreviousIdentical || actualDeviceIsSet) {
                unsetDevice(projectDevice);
            }
            identicalDeviceMap.put(projectDevice, parentDevice);
            generateDeviceSelectableMapAndPinPortConnection();
        } else {
            unsetDevice(projectDevice);
        }
    }

    public void setPropertyValue(ProjectDevice projectDevice, Property p, Object value) {
        if(!this.devicePropertyValueMap.containsKey(projectDevice)) {
            this.devicePropertyValueMap.put(projectDevice, new HashMap<>());
        }
        this.devicePropertyValueMap.get(projectDevice).put(p, value);
    }

    public void unsetDevice(ProjectDevice projectDevice) {
        if (deviceMap.containsKey(projectDevice) && devicePinPortConnections.containsKey(projectDevice)) {
            unsetDevicePinPortConnection(projectDevice);
        }
        deviceMap.remove(projectDevice);
        devicePropertyValueMap.remove(projectDevice);
        identicalDeviceMap.entrySet().removeIf(entry -> projectDevice == entry.getKey() || projectDevice == entry.getValue());

        compatibleDevicesSelectableMap.remove(projectDevice);
        compatibleDevicePinPortConnectionMap.remove(projectDevice);
    }

    private void unsetAllDevices() {
        deviceMap.keySet().removeIf(projectDevice -> projectDevice != CONTROLLER);
        devicePropertyValueMap.clear();
        identicalDeviceMap.clear();
        devicePinPortConnections.clear();
        remainingPinProvide.clear();
        remainingPortProvide.clear();

        if (deviceMap.get(CONTROLLER) != null) {
            ActualDevice controller = deviceMap.get(CONTROLLER);
            remainingPinProvide.addAll(controller.getPinProvideByOwnerDevice(CONTROLLER));
            remainingPortProvide.addAll(controller.getPortProvideByOwnerDevice(CONTROLLER));
        }
    }

    public void setDevicePinPortConnection(ProjectDevice projectDevice, PinPortConnection connection) {
        if (getActualDevice(projectDevice).isPresent()) {
            PinPortConnection previousConnection = getDevicePinPortConnection(projectDevice);
            if (previousConnection != connection) {
                unsetDevicePinPortConnection(projectDevice);
                devicePinPortConnections.put(projectDevice, connection);
                connection.getPinMapConsumerProvider().forEach((consumerPin, providerPin) -> {
                    if (!consumerPin.getFunction().get(0).getOpposite().isMultipleUsed()) {
                        remainingPinProvide.remove(providerPin);
                    }
                });
                connection.getPortMapConsumerProvider().forEach((consumerPort, providerPort) -> {
                    remainingPortProvide.remove(providerPort);
                    for (int i=0; i<consumerPort.getElements().size(); i++) {
                        Pin consumerPin = consumerPort.getElements().get(i);
                        Pin providerPin = providerPort.getElements().get(i);
                        if (!consumerPin.getFunction().get(0).getOpposite().isMultipleUsed()) {
                            remainingPinProvide.remove(providerPin);
                        }
                    }
                });
                generateDeviceSelectableMapAndPinPortConnection();
            }
        }
    }

    public void unsetDevicePinPortConnection(ProjectDevice projectDevice) {
        PinPortConnection connection = devicePinPortConnections.remove(projectDevice);
        if (connection != null) {
            connection.getPinMapConsumerProvider().forEach((consumerPin, providerPin) -> remainingPinProvide.add(providerPin));
            connection.getPortMapConsumerProvider().forEach((consumerPort, providerPort) -> {
                remainingPortProvide.add(providerPort);
                remainingPinProvide.addAll(providerPort.getElements());
            });
        }
//        devicePinPortConnections.entrySet().removeAll(devicePinPortConnections.entrySet().stream()
//                .filter(entry -> entry.getValue().getProviderDevice() == projectDevice || entry.getValue().getConsumerDevice() == projectDevice)
//                .collect(Collectors.toList()));
    }

    public PinPortConnection getDevicePinPortConnection(ProjectDevice projectDevice) {
        return devicePinPortConnections.getOrDefault(projectDevice, PinPortConnection.NOT_CONNECTED);
    }

    public void setCloudPlatformParameter(CloudPlatform cloudPlatform, String parameterName, String value) {
        if (!cloudParameterMap.containsKey(cloudPlatform)) {
            cloudParameterMap.put(cloudPlatform, new HashMap<>());
        }
        this.cloudParameterMap.get(cloudPlatform).put(parameterName, value);
    }
}
