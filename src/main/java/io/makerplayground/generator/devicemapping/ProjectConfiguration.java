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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.device.actual.*;
import io.makerplayground.device.shared.Action;
import io.makerplayground.device.shared.Condition;
import io.makerplayground.device.shared.Parameter;
import io.makerplayground.device.shared.constraint.Constraint;
import io.makerplayground.project.DevicePinPortConnection;
import io.makerplayground.project.ProjectDevice;
import io.makerplayground.ui.dialog.configdevice.CompatibleDevice;
import javafx.beans.property.ReadOnlyObjectWrapper;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

@JsonSerialize(using = ProjectConfigurationSerializer.class)
@JsonDeserialize(using = ProjectConfigurationDeserializer.class)
public final class ProjectConfiguration {
    private final ReadOnlyObjectWrapper<Platform> platform;
    private final ReadOnlyObjectWrapper<ActualDevice> controller;

    @Getter(AccessLevel.PACKAGE) private final Map<ProjectDevice, Map<Action, Map<Parameter, Constraint>>> actionCompatibility;
    @Getter(AccessLevel.PACKAGE) private final Map<ProjectDevice, Map<Condition, Map<Parameter, Constraint>>> conditionCompatibility;
    @Getter(AccessLevel.PACKAGE) private final Map<ProjectDevice, Map<Property, Object>> devicePropertyValueMap;

    @Getter(AccessLevel.PACKAGE) private final SortedMap<ProjectDevice, ActualDevice> deviceMap;
    @Getter(AccessLevel.PACKAGE) private final SortedMap<ProjectDevice, ProjectDevice> sameDeviceMap;
    @Getter(AccessLevel.PACKAGE) private final SortedSet<DevicePinPortConnection> devicePinPortConnections;
    @Getter(AccessLevel.PACKAGE) private final SortedMap<CloudPlatform, Map<String, String>> cloudParameterMap;

    @Getter private final SortedMap<ProjectDevice, ActualDevice> unmodifiableDeviceMap;
    @Getter private final SortedMap<ProjectDevice, ProjectDevice> unmodifiableSameDeviceMap;
    @Getter private final SortedSet<DevicePinPortConnection> unmodifiableDevicePinPortConnections;
    @Getter private final SortedMap<CloudPlatform, Map<String, String>> unmodifiableCloudParameterMap;

    @Getter private final SortedMap<ActualDevice, Boolean> controllerSelectableMap;
    @Getter private final Map<ProjectDevice, SortedMap<CompatibleDevice, DeviceMappingResult>> actualDevicesSelectableMap;

    @Builder(access = AccessLevel.PACKAGE)
    public ProjectConfiguration(Platform platform,
                                ActualDevice controller,
                                Map<ProjectDevice, Map<Action, Map<Parameter, Constraint>>> actionCompatibility,
                                Map<ProjectDevice, Map<Condition, Map<Parameter, Constraint>>> conditionCompatibility,
                                Map<ProjectDevice, Map<Property, Object>> devicePropertyValueMap,
                                SortedMap<ProjectDevice, ActualDevice> deviceMap,
                                SortedMap<ProjectDevice, ProjectDevice> sameDeviceMap,
                                SortedSet<DevicePinPortConnection> devicePinPortConnections,
                                SortedMap<CloudPlatform, Map<String, String>> cloudPlatformParameterMap) {
        this.platform = new ReadOnlyObjectWrapper<>(platform);
        this.controller = new ReadOnlyObjectWrapper<>(controller);
        this.actionCompatibility = actionCompatibility;
        this.conditionCompatibility = conditionCompatibility;
        this.devicePropertyValueMap = devicePropertyValueMap;

        this.deviceMap = deviceMap;
        this.sameDeviceMap = sameDeviceMap;
        this.devicePinPortConnections = devicePinPortConnections;
        this.cloudParameterMap = cloudPlatformParameterMap;

        this.unmodifiableDeviceMap = Collections.unmodifiableSortedMap(deviceMap);
        this.unmodifiableSameDeviceMap = Collections.unmodifiableSortedMap(sameDeviceMap);
        this.unmodifiableDevicePinPortConnections = Collections.unmodifiableSortedSet(devicePinPortConnections);
        this.unmodifiableCloudParameterMap = Collections.unmodifiableSortedMap(cloudPlatformParameterMap);

        this.controllerSelectableMap = DeviceLibrary.INSTANCE
                .getActualDevice(getPlatform())
                .stream()
                .filter(actualDevice -> actualDevice.getDeviceType() == DeviceType.CONTROLLER)
                .collect(Collectors.toMap(o -> o, o -> true, (o1, o2)-> { throw new IllegalStateException(""); }, TreeMap::new));
        this.actualDevicesSelectableMap = generateDeviceSelectableMap();
    }

    private Map<ProjectDevice, SortedMap<CompatibleDevice, DeviceMappingResult>> generateDeviceSelectableMap() {
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
                        for (Parameter parameter: action.getParameter()) {
                            if (!parameterConstraintMap.containsKey(parameter)) {
                                selectable.put(compatibleDevice, DeviceMappingResult.NO_SUPPORTING_ACTION_PARAMETER);
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
                        for (Parameter parameter: condition.getParameter()) {
                            if (!parameterConstraintMap.containsKey(parameter)) {
                                selectable.put(compatibleDevice, DeviceMappingResult.NO_SUPPORTING_CONDITION_PARAMETER);
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
            if (unmodifiableDevicePinPortConnections.stream().map(DevicePinPortConnection::getTo).collect(Collectors.toList()).contains(deviceMap.get(device))) {
                continue;
            }
            SortedMap<CompatibleDevice, DeviceMappingResult> selectable = deviceSelectableMap.get(device);
            PinPortLogic pinPortLogic = new PinPortLogic(controller.get(), deviceMap.values(), devicePinPortConnections);
            for (CompatibleDevice compatibleDevice: selectable.keySet()) {
                if (compatibleDevice.getActualDevice().isPresent() && pinPortLogic.checkCompatibilityFor(compatibleDevice.getActualDevice().get()) == DevicePinPortConnectionResultStatus.ERROR) {
                    selectable.put(compatibleDevice, DeviceMappingResult.NO_AVAILABLE_PIN_PORT);
                }
            }
        }

        return deviceSelectableMap;
    }

    public Platform getPlatform() {
        return platform.get();
    }

    public ActualDevice getController() {
        return controller.get();
    }

    public boolean isActualDeviceSelected(ProjectDevice projectDevice) {
        if (unmodifiableDeviceMap.containsKey(projectDevice) && Objects.nonNull(unmodifiableDeviceMap.get(projectDevice))) {
            return true;
        }
        return false;
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
        ActualDevice actualDevice = deviceMap.get(projectDevice);
        devicePinPortConnections.removeAll(devicePinPortConnections.stream()
                .filter(devicePinPortConnection -> devicePinPortConnection.getTo() == actualDevice || devicePinPortConnection.getFrom() == actualDevice)
                .collect(Collectors.toList()));
    }
}
