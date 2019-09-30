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
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.device.actual.*;
import io.makerplayground.device.generic.GenericDevice;
import io.makerplayground.device.shared.Action;
import io.makerplayground.device.shared.Condition;
import io.makerplayground.device.shared.Parameter;
import io.makerplayground.device.shared.constraint.Constraint;
import io.makerplayground.generator.devicemapping.*;
import io.makerplayground.ui.dialog.configdevice.CompatibleDevice;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import lombok.*;

import java.util.*;
import java.util.stream.Collectors;

import static io.makerplayground.project.ProjectDevice.CONTROLLER;

@JsonSerialize(using = ProjectConfigurationSerializer.class)
public final class ProjectConfiguration {

    /* input variables: the compatibilities data from the project instance. These variables must be set before calculation */
    @JsonIgnore private Map<ProjectDevice, Map<Action, Map<Parameter, Constraint>>> actionCompatibility;
    @JsonIgnore private Map<ProjectDevice, Map<Condition, Map<Parameter, Constraint>>> conditionCompatibility;

    /* state variables: the variable used in calculation and cached the sub-solution */
    @JsonIgnore private ObservableList<ProjectDevice> usedDevices = FXCollections.observableArrayList();
    @JsonIgnore private FilteredList<ProjectDevice> nonControllerDevices = new FilteredList<>(usedDevices, projectDevice -> projectDevice != CONTROLLER);
    @JsonIgnore private Set<Connection> remainingConnectionProvide = new HashSet<>();
    @JsonIgnore private Map<ProjectDevice, Set<String>> usedRefPin = new HashMap<>();
    @JsonIgnore private List<CloudPlatform> remainingCloudPlatform = new ArrayList<>();

    /* output variables: compatible devices and pin/port connections */
    @JsonIgnore @Getter private Map<ProjectDevice, SortedMap<CompatibleDevice, DeviceMappingResult>> compatibleDevicesSelectableMap;
    @JsonIgnore @Getter private Map<ProjectDevice, Map<ActualDevice, SortedMap<Connection, List<Connection>>>> compatibleConnectionMap;

    /* data structure: user's input that would be stored in file */
    @Setter(AccessLevel.PACKAGE) @Getter private Platform platform;
    @Getter(AccessLevel.PACKAGE) private final Map<ProjectDevice, Map<Property, Object>> devicePropertyValueMap;
    @Getter(AccessLevel.PACKAGE) private final SortedMap<ProjectDevice, ActualDevice> deviceMap;
    @Getter(AccessLevel.PACKAGE) private final SortedMap<ProjectDevice, ProjectDevice> identicalDeviceMap;
    @Getter(AccessLevel.PACKAGE) private final SortedMap<ProjectDevice, DeviceConnection> deviceConnections;
    @Getter(AccessLevel.PACKAGE) private final SortedMap<CloudPlatform, Map<String, String>> cloudParameterMap;

    /* unmodifiable variables: the unmodifiable view of data structure to protect the data change from outside class */
    @Getter private final Map<ProjectDevice, Map<Property, Object>> unmodifiableDevicePropertyValueMap;
    @Getter private final SortedMap<ProjectDevice, ActualDevice> unmodifiableDeviceMap;
    @Getter private final SortedMap<ProjectDevice, ProjectDevice> unmodifiableIdenticalDeviceMap;
    @Getter private final SortedMap<ProjectDevice, DeviceConnection> unmodifiableDeviceConnections;
    @Getter private final SortedMap<CloudPlatform, Map<String, String>> unmodifiableCloudParameterMap;

    ProjectConfiguration(@NonNull Platform platform) {
        this.platform = platform;
        this.devicePropertyValueMap = new HashMap<>();
        this.deviceMap = new TreeMap<>();
        this.identicalDeviceMap = new TreeMap<>();
        this.deviceConnections = new TreeMap<>();
        this.cloudParameterMap = new TreeMap<>();

        this.unmodifiableDevicePropertyValueMap = Collections.unmodifiableMap(devicePropertyValueMap);
        this.unmodifiableDeviceMap = Collections.unmodifiableSortedMap(deviceMap);
        this.unmodifiableIdenticalDeviceMap = Collections.unmodifiableSortedMap(identicalDeviceMap);
        this.unmodifiableDeviceConnections = Collections.unmodifiableSortedMap(deviceConnections);
        this.unmodifiableCloudParameterMap = Collections.unmodifiableSortedMap(cloudParameterMap);
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

        generateDeviceSelectableMapAndConnection();
    }

    private void generateDeviceSelectableMapAndConnection() {
        Map<ProjectDevice, SortedMap<CompatibleDevice, DeviceMappingResult>> deviceSelectableMap = new HashMap<>();
        Map<ProjectDevice, Map<ActualDevice, SortedMap<Connection, List<Connection>>>> deviceConnectionMap = new HashMap<>();

        /* add all device that is the same generic with "OK" mapping result */
        for (ProjectDevice device: nonControllerDevices) {
            SortedMap<CompatibleDevice, DeviceMappingResult> selectable;
            if (getController() != null) {
                selectable = DeviceLibrary.INSTANCE.getActualDevice(getPlatform())
                        .stream()
                        .filter(actualDevice -> actualDevice.getCompatibilityMap().containsKey(device.getGenericDevice()))
                        .collect(Collectors.toMap(CompatibleDevice::new, o->DeviceMappingResult.OK, (o1, o2)->{throw new IllegalStateException("");}, TreeMap::new));
                getController().getIntegratedDevices().forEach(integratedActualDevice -> {
                    if (integratedActualDevice.getCompatibilityMap().containsKey(device.getGenericDevice())) {
                        selectable.put(new CompatibleDevice(integratedActualDevice), DeviceMappingResult.OK);
                    }
                });
            } else {
                selectable = new TreeMap<>();
            }
            deviceSelectableMap.put(device, selectable);
        }
        setFlagToDeviceIfActionConditionIsIncompatible(deviceSelectableMap);
        setFlagToDeviceIfCloudIsNotSupport(deviceSelectableMap);
        setFlagToDeviceIfConnectionIsIncompatible(deviceSelectableMap, deviceConnectionMap);

        /* add identical device to selectable list */
        Map<ProjectDevice, List<CompatibleDevice>> possibleIdenticalDeviceMap = calculatePossibleIdenticalDeviceMap();
        for (ProjectDevice device: possibleIdenticalDeviceMap.keySet()) {
            SortedMap<CompatibleDevice, DeviceMappingResult> selectable = deviceSelectableMap.get(device);
            for (CompatibleDevice identicalDevice: possibleIdenticalDeviceMap.get(device)) {
                selectable.put(identicalDevice, DeviceMappingResult.OK);
            }
        }

        this.compatibleDevicesSelectableMap = deviceSelectableMap;
        this.compatibleConnectionMap = deviceConnectionMap;
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

    private void setFlagToDeviceIfConnectionIsIncompatible(Map<ProjectDevice, SortedMap<CompatibleDevice, DeviceMappingResult>> deviceSelectableMap,
                                                           Map<ProjectDevice, Map<ActualDevice, SortedMap<Connection, List<Connection>>>> deviceConnectionMap) {
        /* set reason for circuit incompatibility */
        for (ProjectDevice projectDevice: nonControllerDevices) {
            SortedMap<CompatibleDevice, DeviceMappingResult> selectable = deviceSelectableMap.get(projectDevice);
            Map<ActualDevice, SortedMap<Connection, List<Connection>>> actualDeviceConnectionMap = new HashMap<>();
            for (CompatibleDevice compatibleDevice: selectable.keySet()) {
                if (compatibleDevice.getActualDevice().isPresent() && selectable.get(compatibleDevice) == DeviceMappingResult.OK) {
                    ActualDevice actualDevice = compatibleDevice.getActualDevice().get();
                    DeviceConnection currentConnection = deviceConnections.getOrDefault(projectDevice, DeviceConnection.NOT_CONNECTED);
                    DeviceConnectionResult result = DeviceConnectionLogic.generatePossibleDeviceConnection(remainingConnectionProvide, usedRefPin, projectDevice, actualDevice, currentConnection);
                    if (result.getStatus() == DeviceConnectionResultStatus.ERROR) {
                        selectable.put(compatibleDevice, DeviceMappingResult.NO_AVAILABLE_PIN_PORT);
                    }
                    else {
                        actualDeviceConnectionMap.put(actualDevice, result.getConnections());
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
            remainingConnectionProvide.clear();
            remainingCloudPlatform.clear();
            remainingConnectionProvide.addAll(controller.getConnectionProvideByOwnerDevice(CONTROLLER));
            remainingCloudPlatform.addAll(controller.getCloudPlatformSourceCodeLibrary().keySet());
            usedRefPin.clear();

            generateDeviceSelectableMapAndConnection();
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

    public Object getPropertyValue(ProjectDevice device, Property p) {
        if (devicePropertyValueMap.containsKey(device) && devicePropertyValueMap.get(device).containsKey(p)) {
            return devicePropertyValueMap.get(device).get(p);
        }
        return null;
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
            generateDeviceSelectableMapAndConnection();
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
            generateDeviceSelectableMapAndConnection();
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
        if (deviceMap.containsKey(projectDevice) && deviceConnections.containsKey(projectDevice)) {
            unsetDeviceConnection(projectDevice);
        }
        deviceMap.remove(projectDevice);
        devicePropertyValueMap.remove(projectDevice);
        identicalDeviceMap.entrySet().removeIf(entry -> projectDevice == entry.getKey() || projectDevice == entry.getValue());

        compatibleDevicesSelectableMap.remove(projectDevice);
        compatibleConnectionMap.remove(projectDevice);
        generateDeviceSelectableMapAndConnection();
    }

    public void unsetAllDevices() {
        deviceMap.keySet().removeIf(projectDevice -> projectDevice != CONTROLLER);
        devicePropertyValueMap.clear();
        identicalDeviceMap.clear();
        deviceConnections.clear();
        remainingConnectionProvide.clear();
        usedRefPin.clear();

        if (deviceMap.get(CONTROLLER) != null) {
            ActualDevice controller = deviceMap.get(CONTROLLER);
            remainingConnectionProvide.addAll(controller.getConnectionProvideByOwnerDevice(CONTROLLER));
        }
        generateDeviceSelectableMapAndConnection();
    }

    public void setConnection(ProjectDevice projectDevice, Connection consumerConnection, Connection providerConnection) {
        // If the project device is not selected
        if (!deviceMap.containsKey(projectDevice)) {
            throw new UnsupportedOperationException("cannot set connection of the device that is not in the deviceMap");
        }

        // If providerConnection == null, use unsetConnection instead
        if (providerConnection == null) {
            unsetConnection(projectDevice, consumerConnection);
            return;
        }

        // If the connection is never assigned, do lazy initiation
        if (!deviceConnections.containsKey(projectDevice)) {
            /* initial DeviceConnection instance and define map's key */
            SortedMap<Connection, Connection> consumerProviderConnectionMap = new TreeMap<>();
            SortedMap<Connection, List<PinFunction>> providerFunctionUsed = new TreeMap<>();
            ActualDevice actualDevice = deviceMap.get(projectDevice);
            for (Connection connectionConsume: actualDevice.getConnectionConsumeByOwnerDevice(projectDevice)) {
                consumerProviderConnectionMap.put(connectionConsume, null);
                providerFunctionUsed.put(providerConnection, new ArrayList<>());
            }
            deviceConnections.put(projectDevice, new DeviceConnection(consumerProviderConnectionMap, providerFunctionUsed));
        }

        // If the value changed, remove the old first
        if (deviceConnections.get(projectDevice).getConsumerProviderConnections().get(consumerConnection) != providerConnection) {
            unsetConnection(projectDevice, consumerConnection);
        }

        // Set value of connection to the deviceConnections mapping.
        deviceConnections.get(projectDevice).setConnection(consumerConnection, providerConnection);

        // Reserve the connection by remove the providerConnection from the remainingConnectionProvide
        if (providerConnection.getType() != ConnectionType.WIRE) {
            remainingConnectionProvide.remove(providerConnection);
        }
        else if (providerConnection.getType() == ConnectionType.WIRE &&
                providerConnection.getPins().size() == 1 &&
                deviceConnections.get(projectDevice).getProviderFunction().get(providerConnection).get(0).isSingleUsed())
        {
            remainingConnectionProvide.remove(providerConnection);
        }

        // Add used pins to list
        ProjectDevice providerProjectDevice = providerConnection.getOwnerProjectDevice();
        for (int i=0; i<consumerConnection.getPins().size(); i++) {
            Pin consumerPin = consumerConnection.getPins().get(i);
            Pin providerPin = providerConnection.getPins().get(i);
            List<PinFunction> provideFunctions = providerPin.getFunction();
            if (consumerPin.getFunction().get(0) == PinFunction.NO_FUNCTION) {
                continue;
            }
            PinFunction function = consumerPin.getFunction().get(0).getPossibleConsume().stream().filter(provideFunctions::contains).findFirst().orElseThrow();
            if (function.isSingleUsed()) {
                if (!usedRefPin.containsKey(providerProjectDevice)) {
                    usedRefPin.put(providerProjectDevice, new HashSet<>());
                }
                usedRefPin.get(providerProjectDevice).add(providerPin.getRefTo());
            }
        }

        generateDeviceSelectableMapAndConnection();
    }

    public void unsetConnection(ProjectDevice projectDevice, Connection connectionConsume) {
        DeviceConnection deviceConnection = deviceConnections.get(projectDevice);
        if (deviceConnection != null && deviceConnection.getConsumerProviderConnections().containsKey(connectionConsume)) {
            Connection connectionProvide = deviceConnection.getConsumerProviderConnections().get(connectionConsume);
            if (connectionProvide == null) {
                return;
            }
            ProjectDevice providerProjectDevice = connectionProvide.getOwnerProjectDevice();
            remainingConnectionProvide.add(connectionProvide);
            connectionProvide.getPins().forEach(providerPin -> {
                if (usedRefPin.containsKey(providerProjectDevice)) {
                    usedRefPin.get(providerProjectDevice).remove(providerPin.getRefTo());
                }
            });
            deviceConnection.getConsumerProviderConnections().put(connectionConsume, null);
            generateDeviceSelectableMapAndConnection();
        }
    }

    private Connection getConnection(ProjectDevice projectDevice, Connection consumerConnection) {
        if (!deviceMap.containsKey(projectDevice)) {
            return null;
        }
        if (!deviceConnections.containsKey(projectDevice)) {
            return null;
        }
        return deviceConnections.get(projectDevice).getConsumerProviderConnections().get(consumerConnection);
    }

    void setDeviceConnection(ProjectDevice projectDevice, DeviceConnection connection) {
        if (getActualDevice(projectDevice).isPresent()) {
            DeviceConnection previousConnection = getDeviceConnection(projectDevice);
            if (previousConnection != connection) {
                unsetDeviceConnection(projectDevice);
                deviceConnections.put(projectDevice, connection);
                connection.getConsumerProviderConnections().forEach((consumerConnection, providerConnection) -> {
                    if (providerConnection == null) {
                        return;
                    }
                    ProjectDevice providerProjectDevice = providerConnection.getOwnerProjectDevice();
                    if (providerConnection.getType() != ConnectionType.WIRE) {
                        remainingConnectionProvide.remove(providerConnection);
                    }
                    for (int i = 0; i<consumerConnection.getPins().size(); i++) {
                        Pin consumerPin = consumerConnection.getPins().get(i);
                        Pin providerPin = providerConnection.getPins().get(i);

                        List<PinFunction> provideFunctions = providerPin.getFunction();
                        if (consumerPin.getFunction().get(0) == PinFunction.NO_FUNCTION) {
                            continue;
                        }
                        PinFunction function = consumerPin.getFunction().get(0).getPossibleConsume().stream().filter(provideFunctions::contains).findFirst().orElseThrow();
                        if (function.isSingleUsed()) {
                            if (!usedRefPin.containsKey(providerProjectDevice)) {
                                usedRefPin.put(providerProjectDevice, new HashSet<>());
                            }
                            usedRefPin.get(providerProjectDevice).add(providerPin.getRefTo());
                        }
                    }
                });
                generateDeviceSelectableMapAndConnection();
            }
        }
    }

    private void unsetDeviceConnection(ProjectDevice projectDevice) {
        DeviceConnection connection = deviceConnections.remove(projectDevice);
        if (connection != null) {
            connection.getConsumerProviderConnections().forEach((consumerPort, providerConnection) -> {
                ProjectDevice providerProjectDevice = providerConnection.getOwnerProjectDevice();
                remainingConnectionProvide.add(providerConnection);
                providerConnection.getPins().forEach(providerPin -> {
                    if (usedRefPin.containsKey(providerProjectDevice)) {
                        usedRefPin.get(providerProjectDevice).remove(providerPin.getRefTo());
                    }
                });
            });
        }
    }

    public DeviceConnection getDeviceConnection(ProjectDevice projectDevice) {
        return deviceConnections.getOrDefault(projectDevice, DeviceConnection.NOT_CONNECTED);
    }

    public void setCloudPlatformParameter(CloudPlatform cloudPlatform, String parameterName, String value) {
        if (!cloudParameterMap.containsKey(cloudPlatform)) {
            cloudParameterMap.put(cloudPlatform, new HashMap<>());
        }
        this.cloudParameterMap.get(cloudPlatform).put(parameterName, value);
    }

    private Comparator<Connection> getLessChoiceOfConnectionComparator(ProjectDevice projectDevice) {
        return Comparator.comparingInt((Connection connection) -> {
                    if (getConnection(projectDevice, connection) != null) {
                        return 0;
                    } else {
                        ActualDevice actualDevice = deviceMap.get(projectDevice);
                        Map<ActualDevice, SortedMap<Connection, List<Connection>>> actualDeviceListMap = compatibleConnectionMap.get(projectDevice);
                        if (!actualDeviceListMap.containsKey(actualDevice)) {
                            return 0;
                        }
                        Map<Connection, List<Connection>> possibleDeviceConnection = actualDeviceListMap.get(actualDevice);
                        return possibleDeviceConnection.get(connection).size();
                    }
                })
                .thenComparing(Connection::getName);
    }


    public ProjectMappingResult autoAssignDevices() {
        if (getController() == null) {
            return ProjectMappingResult.NO_MCU_SELECTED;
        }

        List<ProjectDevice> unassignedDevices = new ArrayList<>(usedDevices);
        unassignedDevices.remove(CONTROLLER);
        unassignedDevices.removeAll(identicalDeviceMap.keySet());
        deviceConnections.forEach((projectDevice, deviceConnection) -> {
            if (deviceConnection != DeviceConnection.NOT_CONNECTED &&
                    deviceConnection.getConsumerProviderConnections().values().stream().noneMatch(Objects::isNull)) {
                unassignedDevices.remove(projectDevice);
            }
        });
        while (!unassignedDevices.isEmpty()) {
            // assign the identical device if possible.
            Map<ProjectDevice, List<ProjectDevice>> identicalDeviceMap = compatibleDevicesSelectableMap.entrySet().stream()
                    .filter(entry -> unassignedDevices.contains(entry.getKey()))    // unassigned device only
                    .filter(entry -> !deviceMap.containsKey(entry.getKey()) || deviceMap.get(entry.getKey()) == null)
                    .collect(Collectors.toMap(Map.Entry::getKey,
                            entry -> entry.getValue().keySet().stream()
                                    .filter(compatibleDevice -> compatibleDevice.getProjectDevice().isPresent())
                                    .map(compatibleDevice -> compatibleDevice.getProjectDevice().get())
                                    .collect(Collectors.toList())));
            identicalDeviceMap.forEach((projectDevice, projectDevices) -> {
                if (!projectDevices.isEmpty()) {
                    setIdenticalDevice(projectDevice, projectDevices.get(0));
                    unassignedDevices.remove(projectDevice);
                }
            });

            // assign the connection to the selected actual device.
            List<ProjectDevice> tempMap = new ArrayList<>(unassignedDevices);
            for (ProjectDevice projectDevice: tempMap) {
                if (deviceMap.containsKey(projectDevice)
                        && deviceMap.get(projectDevice) != null
                        && (!deviceConnections.containsKey(projectDevice)
                                || deviceConnections.get(projectDevice) == DeviceConnection.NOT_CONNECTED
                                || deviceConnections.get(projectDevice).getConsumerProviderConnections().values().stream().anyMatch(Objects::isNull)
                        )
                ) {
                    if (!compatibleDevicesSelectableMap.containsKey(projectDevice)) {
                        throw new IllegalStateException("Cannot have project device that is not in the compatibility map");
                    }
                    ActualDevice actualDevice = deviceMap.get(projectDevice);
                    List<Connection> allConnectionConsume = new ArrayList<>(actualDevice.getConnectionConsumeByOwnerDevice(projectDevice));
                    while (!allConnectionConsume.isEmpty()) {
                        allConnectionConsume.sort(getLessChoiceOfConnectionComparator(projectDevice));
                        Connection connectionConsume = allConnectionConsume.get(0);
                        // Do not replace the connection that is already assigned by user.
                        if (getConnection(projectDevice, connectionConsume) != null) {
                            allConnectionConsume.remove(connectionConsume);
                            continue;
                        }
                        Map<ActualDevice, SortedMap<Connection, List<Connection>>> actualDeviceListMap = compatibleConnectionMap.get(projectDevice);
                        if (!actualDeviceListMap.containsKey(actualDevice)) {
                            throw new IllegalStateException("Cannot have selected actual device that is not in the compatibility map");
                        }
                        Map<Connection, List<Connection>> possibleDeviceConnection = actualDeviceListMap.get(actualDevice);
                        if (possibleDeviceConnection.get(connectionConsume).isEmpty()) {
                            return ProjectMappingResult.CANT_ASSIGN_PORT;
                        } else {
                            setConnection(projectDevice, connectionConsume, possibleDeviceConnection.get(connectionConsume).get(0));
                            allConnectionConsume.remove(connectionConsume);
                        }
                    }
                    unassignedDevices.remove(projectDevice);
                }
            }

            // get the unassigned device that has minimum remaining value.
            Optional<ProjectDevice> mrvProjectDevice = unassignedDevices.stream().min((projectDevice1, projectDevice2) -> {
                SortedMap<CompatibleDevice, DeviceMappingResult> possibleChoice1 = compatibleDevicesSelectableMap.get(projectDevice1);
                int possibleChoiceCount = 0;
                for (CompatibleDevice compatibleDevice: possibleChoice1.keySet()) {
                    if (possibleChoice1.get(compatibleDevice) == DeviceMappingResult.OK && compatibleDevice.getActualDevice().isPresent()) {
                        possibleChoiceCount += 1;
                    }
                }
                SortedMap<CompatibleDevice, DeviceMappingResult> possibleChoice2 = compatibleDevicesSelectableMap.get(projectDevice2);
                for (CompatibleDevice compatibleDevice: possibleChoice2.keySet()) {
                    if (possibleChoice1.get(compatibleDevice) == DeviceMappingResult.OK && compatibleDevice.getActualDevice().isPresent()) {
                        possibleChoiceCount -= 1;
                    }
                }
                return possibleChoiceCount;
            });
            mrvProjectDevice.ifPresent(projectDevice -> setActualDevice(projectDevice, compatibleDevicesSelectableMap.get(projectDevice).firstKey().getActualDevice().orElseThrow()));
        }
        return ProjectMappingResult.OK;
    }
}