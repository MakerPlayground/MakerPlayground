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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.device.actual.*;
import io.makerplayground.device.shared.Action;
import io.makerplayground.device.shared.Condition;
import io.makerplayground.device.shared.Parameter;
import io.makerplayground.device.shared.constraint.Constraint;
import io.makerplayground.generator.devicemapping.DeviceMappingResult;
import io.makerplayground.generator.devicemapping.DevicePinPortConnectionResultStatus;
import io.makerplayground.generator.devicemapping.DevicePinPortLogic;
import io.makerplayground.ui.dialog.configdevice.CompatibleDevice;
import lombok.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@JsonSerialize(using = ProjectConfigurationSerializer.class)
@JsonDeserialize(using = ProjectConfigurationDeserializer.class)
public final class ProjectConfiguration {
    @Setter(AccessLevel.PACKAGE) private Platform platform;

    private Map<ProjectDevice, Map<Action, Map<Parameter, Constraint>>> actionCompatibility;
    private Map<ProjectDevice, Map<Condition, Map<Parameter, Constraint>>> conditionCompatibility;

    @Getter(AccessLevel.PACKAGE) private final Map<ProjectDevice, Map<Property, Object>> devicePropertyValueMap;
    @Getter(AccessLevel.PACKAGE) private final SortedMap<ProjectDevice, ActualDevice> deviceMap;
    @Getter(AccessLevel.PACKAGE) private final SortedMap<ProjectDevice, ProjectDevice> sameDeviceMap;
    @Getter(AccessLevel.PACKAGE) private final SortedSet<DevicePinPortConnection> devicePinPortConnections;
    @Getter(AccessLevel.PACKAGE) private final SortedMap<CloudPlatform, Map<String, String>> cloudParameterMap;

    @Getter private final Map<ProjectDevice, Map<Property, Object>> unmodifiableDevicePropertyValueMap;
    @Getter private final SortedMap<ProjectDevice, ActualDevice> unmodifiableDeviceMap;
    @Getter private final SortedMap<ProjectDevice, ProjectDevice> unmodifiableSameDeviceMap;
    @Getter private final SortedSet<DevicePinPortConnection> unmodifiableDevicePinPortConnections;
    @Getter private final SortedMap<CloudPlatform, Map<String, String>> unmodifiableCloudParameterMap;

    @Getter private SortedMap<ActualDevice, DeviceMappingResult> controllerSelectableMap;
    @Getter private Map<ProjectDevice, SortedMap<CompatibleDevice, DeviceMappingResult>> actualDevicesSelectableMap;

    @Builder
    ProjectConfiguration(@NonNull Platform platform,
                                ActualDevice controller,
                                @NonNull Map<ProjectDevice, Map<Property, Object>> devicePropertyValueMap,
                                @NonNull SortedMap<ProjectDevice, ActualDevice> deviceMap,
                                @NonNull SortedMap<ProjectDevice, ProjectDevice> sameDeviceMap,
                                @NonNull SortedSet<DevicePinPortConnection> devicePinPortConnections,
                                @NonNull SortedMap<CloudPlatform, Map<String, String>> cloudPlatformParameterMap) {
        this.platform = platform;
        this.devicePropertyValueMap = devicePropertyValueMap;

        this.deviceMap = deviceMap;
        this.deviceMap.put(ProjectDevice.CONTROLLER, controller);
        this.sameDeviceMap = sameDeviceMap;
        this.devicePinPortConnections = devicePinPortConnections;
        this.cloudParameterMap = cloudPlatformParameterMap;

        this.unmodifiableDevicePropertyValueMap = Collections.unmodifiableMap(devicePropertyValueMap);
        this.unmodifiableDeviceMap = Collections.unmodifiableSortedMap(deviceMap);
        this.unmodifiableSameDeviceMap = Collections.unmodifiableSortedMap(sameDeviceMap);
        this.unmodifiableDevicePinPortConnections = Collections.unmodifiableSortedSet(devicePinPortConnections);
        this.unmodifiableCloudParameterMap = Collections.unmodifiableSortedMap(cloudPlatformParameterMap);

        this.controllerSelectableMap = DeviceLibrary.INSTANCE
                .getActualDevice(getPlatform())
                .stream()
                .filter(actualDevice -> actualDevice.getDeviceType() == DeviceType.CONTROLLER)
                .collect(Collectors.toMap(o -> o, o -> DeviceMappingResult.OK, (o1, o2)-> { throw new IllegalStateException(""); }, TreeMap::new));
    }

    void updateCompatibility(Map<ProjectDevice, Map<Action, Map<Parameter, Constraint>>> actionCompatibility, Map<ProjectDevice, Map<Condition, Map<Parameter, Constraint>>> conditionCompatibility) {
        this.actionCompatibility = actionCompatibility;
        this.conditionCompatibility = conditionCompatibility;

        List<ProjectDevice> devices = Stream.concat(actionCompatibility.keySet().stream(), conditionCompatibility.keySet().stream()).collect(Collectors.toList());
        devices.add(ProjectDevice.CONTROLLER);

        deviceMap.entrySet().removeIf(entry -> !devices.contains(entry.getKey()));
        devicePropertyValueMap.entrySet().removeIf(entry -> !devices.contains(entry.getKey()));
        sameDeviceMap.entrySet().removeIf(entry -> !devices.contains(entry.getKey()) && !devices.contains(entry.getValue()));
        devicePinPortConnections.removeIf(connection -> !devices.contains(connection.getFrom()) && !devices.contains(connection.getTo()));

        generateDeviceSelectableMap();
    }

    private void generateDeviceSelectableMap() {
        Map<ProjectDevice, SortedMap<CompatibleDevice, DeviceMappingResult>> deviceSelectableMap = new HashMap<>();
        Set<ProjectDevice> usedDevice = new HashSet<>();
        usedDevice.addAll(this.actionCompatibility.keySet());
        usedDevice.addAll(this.conditionCompatibility.keySet());
        /* add all device that is the same generic with "OK" mapping result */
        for (ProjectDevice device: usedDevice) {
            SortedMap<CompatibleDevice, DeviceMappingResult> selectable = DeviceLibrary.INSTANCE.getActualDevice(getPlatform())
                    .stream()
                    .filter(actualDevice -> actualDevice.getCompatibilityMap().containsKey(device.getGenericDevice()))
                    .collect(Collectors.toMap(CompatibleDevice::new, o->DeviceMappingResult.OK, (o1, o2)->{throw new IllegalStateException("");}, TreeMap::new));
            deviceSelectableMap.put(device, selectable);
        }
        /* set reason for incompatible actual device */
        for (ProjectDevice device: this.actionCompatibility.keySet()) {
            Map<Action, Map<Parameter, Constraint>> compatibility = this.actionCompatibility.get(device);
            SortedMap<CompatibleDevice, DeviceMappingResult> selectable = deviceSelectableMap.get(device);
            for (CompatibleDevice compatibleDevice: selectable.keySet()) {
                if (compatibleDevice.getActualDevice().isPresent()) {
                    for (Action action: compatibility.keySet()) {
                        if (!compatibleDevice.getActualDevice().get().getCompatibilityMap().get(device.getGenericDevice()).getDeviceAction().containsKey(action)) {
                            selectable.put(compatibleDevice, DeviceMappingResult.NO_SUPPORTING_ACTION);
                            continue;
                        }
                        Map<Parameter, Constraint> parameterConstraintMap = compatibleDevice.getActualDevice().get().getCompatibilityMap().get(device.getGenericDevice()).getDeviceAction().get(action);
                        for (Parameter parameter: parameterConstraintMap.keySet()) {
                            if (!action.getParameter().contains(parameter)) {
                                selectable.put(compatibleDevice, DeviceMappingResult.ACTION_PARAMETER_NOT_COMPATIBLE);
                                break;
                            }
                        }
                        for (Parameter parameter: action.getParameter()) {
                            if (!parameterConstraintMap.containsKey(parameter)) {
                                selectable.put(compatibleDevice, DeviceMappingResult.ACTION_PARAMETER_NOT_COMPATIBLE);
                                break;
                            }
                            Constraint constraint = parameterConstraintMap.get(parameter);
                            if (!constraint.isCompatible(compatibility.get(action).get(parameter))) {
                                selectable.put(compatibleDevice, DeviceMappingResult.CONSTRAINT_NOT_COMPATIBLE);
                            }
                        }
                    }
                }
            }
        }
        /* set reason for incompatible actual device */
        for (ProjectDevice device: this.conditionCompatibility.keySet()) {
            Map<Condition, Map<Parameter, Constraint>> compatibility = this.conditionCompatibility.get(device);
            SortedMap<CompatibleDevice, DeviceMappingResult> selectable = deviceSelectableMap.get(device);
            for (CompatibleDevice compatibleDevice: selectable.keySet()) {
                for (Condition condition: compatibility.keySet()) {
                    if (compatibleDevice.getActualDevice().isPresent()) {
                        if (!compatibleDevice.getActualDevice().get().getCompatibilityMap().get(device.getGenericDevice()).getDeviceCondition().containsKey(condition)) {
                            selectable.put(compatibleDevice, DeviceMappingResult.NO_SUPPORTING_CONDITION);
                            continue;
                        }
                        Map<Parameter, Constraint> parameterConstraintMap = compatibleDevice.getActualDevice().get().getCompatibilityMap().get(device.getGenericDevice()).getDeviceCondition().get(condition);
                        for (Parameter parameter: parameterConstraintMap.keySet()) {
                            if (!condition.getParameter().contains(parameter)) {
                                selectable.put(compatibleDevice, DeviceMappingResult.CONDITION_PARAMETER_NOT_COMPATIBLE);
                                break;
                            }
                        }
                        for (Parameter parameter: condition.getParameter()) {
                            if (!parameterConstraintMap.containsKey(parameter)) {
                                selectable.put(compatibleDevice, DeviceMappingResult.CONDITION_PARAMETER_NOT_COMPATIBLE);
                                break;
                            }
                            Constraint constraint = parameterConstraintMap.get(parameter);
                            if (!constraint.isCompatible(compatibility.get(condition).get(parameter))) {
                                selectable.put(compatibleDevice, DeviceMappingResult.CONSTRAINT_NOT_COMPATIBLE);
                            }
                        }
                    }
                }
            }
        }
        /* set reason for circuit incompatibility */
        for (ProjectDevice device: usedDevice) {
            /* device already connect: no problem */
            if (unmodifiableDevicePinPortConnections.stream().map(DevicePinPortConnection::getTo).collect(Collectors.toList()).contains(device)) {
                continue;
            }
            SortedMap<CompatibleDevice, DeviceMappingResult> selectable = deviceSelectableMap.get(device);
            DevicePinPortLogic pinPortLogic = new DevicePinPortLogic(deviceMap, devicePinPortConnections);
            for (CompatibleDevice compatibleDevice: selectable.keySet()) {
                if (compatibleDevice.getActualDevice().isPresent() && pinPortLogic.checkCompatibilityFor(device, compatibleDevice.getActualDevice().get()) == DevicePinPortConnectionResultStatus.ERROR) {
                    selectable.put(compatibleDevice, DeviceMappingResult.NO_AVAILABLE_PIN_PORT);
                }
            }
        }
        this.actualDevicesSelectableMap = deviceSelectableMap;
    }

    public Platform getPlatform() {
        return platform;
    }

    public void setController(ActualDevice controller) {
        unsetAllDevices();
        deviceMap.put(ProjectDevice.CONTROLLER, controller);
        generateDeviceSelectableMap();
    }

    public ActualDevice getController() {
        return deviceMap.get(ProjectDevice.CONTROLLER);
    }

    public boolean isActualDeviceSelected(ProjectDevice projectDevice) {
        return unmodifiableDeviceMap.containsKey(projectDevice) && Objects.nonNull(unmodifiableDeviceMap.get(projectDevice));
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
        else if (unmodifiableSameDeviceMap.containsKey(projectDevice)) {
            return getCloudConsume(unmodifiableSameDeviceMap.get(projectDevice));
        }
        return Optional.empty();
    }

    public Optional<Object> getPropertyValue(ProjectDevice device, Property p) {
        if (devicePropertyValueMap.containsKey(device) && devicePropertyValueMap.get(device).containsKey(p)) {
            return Optional.of(devicePropertyValueMap.get(device).get(p));
        }
        return Optional.empty();
    }

    public boolean isUsedSameDevice(ProjectDevice projectDevice) {
        if (unmodifiableSameDeviceMap.containsKey(projectDevice) && Objects.nonNull(unmodifiableSameDeviceMap.get(projectDevice))) {
            return getActualDevice(unmodifiableSameDeviceMap.get(projectDevice)).isPresent();
        }
        return false;
    }

    public Optional<ProjectDevice> getParentDevice(ProjectDevice projectDevice) {
        if (!unmodifiableSameDeviceMap.containsKey(projectDevice)) {
            return Optional.empty();
        }
        ProjectDevice candidate = unmodifiableSameDeviceMap.get(projectDevice);
        if (getParentDevice(candidate).isEmpty()) {
            return Optional.of(candidate);
        }
        return getParentDevice(candidate);
    }

    public void setActualDevice(ProjectDevice projectDevice, ActualDevice actualDevice) {
        this.deviceMap.put(projectDevice, actualDevice);
    }

    public void setParentDevice(ProjectDevice projectDevice, ProjectDevice parentDevice) {
        this.sameDeviceMap.put(projectDevice, parentDevice);
    }

    public void setPropertyValue(ProjectDevice projectDevice, Property p, Object value) {
        if(!this.devicePropertyValueMap.containsKey(projectDevice)) {
            this.devicePropertyValueMap.put(projectDevice, new HashMap<>());
        }
        this.devicePropertyValueMap.get(projectDevice).put(p, value);
    }

    public void removeAllDeviceConnection(ProjectDevice projectDevice) {
        devicePinPortConnections.removeAll(devicePinPortConnections.stream()
                .filter(devicePinPortConnection -> devicePinPortConnection.getTo() == projectDevice || devicePinPortConnection.getFrom() == projectDevice)
                .collect(Collectors.toList()));
    }

    public void unsetDevice(ProjectDevice projectDevice) {
        deviceMap.entrySet().removeIf(entry -> projectDevice == entry.getKey());
        devicePropertyValueMap.entrySet().removeIf(entry -> projectDevice == entry.getKey());
        sameDeviceMap.entrySet().removeIf(entry -> projectDevice == entry.getKey() || projectDevice == entry.getValue());
        devicePinPortConnections.removeIf(connection -> projectDevice == connection.getFrom() || projectDevice == connection.getTo());
    }

    void unsetAllDevices() {
        deviceMap.clear();
        devicePropertyValueMap.clear();
        sameDeviceMap.clear();
        devicePinPortConnections.clear();
    }
}
