/*
 * Copyright (c) 2018. The Maker Playground Authors.
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

package io.makerplayground.ui.dialog.configdevice;

import io.makerplayground.device.actual.ActualDevice;
import io.makerplayground.device.actual.CloudPlatform;
import io.makerplayground.device.actual.Platform;
import io.makerplayground.device.actual.Property;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectDevice;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by tanyagorn on 7/11/2017.
 */
public class ConfigActualDeviceViewModel {
    private final Project project;
//    private final ObjectProperty<Map<ProjectDevice, List<ActualDevice>>> compatibleDeviceList;
//    private final ObjectProperty<Map<ProjectDevice, List<ProjectDevice>>> compatibleShareDeviceList;
//    private final ObjectProperty<Map<ProjectDevice, Map<Peripheral, List<List<DevicePort>>>>> compatiblePortList;

    private ActualDeviceComboItem selectedController;

    @Setter private Runnable platformChangedCallback;
    @Setter private Runnable controllerChangedCallback;
    @Setter private Runnable deviceConfigChangedCallback;
    @Setter private Runnable configChangedCallback;

    public ConfigActualDeviceViewModel(Project project) {
        this.project = project;
    }

    private void applyDeviceMapping() {
        /* TODO: uncomment this */
//        compatibleDeviceList.set(ProjectConfigurationLogic.getSupportedDeviceList(project));
//        compatibleShareDeviceList.set(ProjectConfigurationLogic.getShareableDeviceList(project));
//        compatiblePortList.set(ProjectConfigurationLogic.getDeviceCompatiblePort(project));
    }

    public void clearDeviceConfigChangedCallback() {
        deviceConfigChangedCallback = null;
    }

//    List<CompatibleDevice> getCompatibleDevice(ProjectDevice projectDevice) {
//        List<CompatibleDevice> compatibleDevices = new ArrayList<>();
//        compatibleDevices.addAll(compatibleShareDeviceList.get().get(projectDevice).stream().map(CompatibleDevice::new).collect(Collectors.toList()));
//        compatibleDevices.addAll(compatibleDeviceList.get().get(projectDevice).stream().map(CompatibleDevice::new).collect(Collectors.toList()));
//        return compatibleDevices;
//    }
//
//    Map<Peripheral, List<List<DevicePort>>> getCompatiblePort(ProjectDevice projectDevice) {
//        return compatiblePortList.get().get(projectDevice);
//    }
//
//    List<ActualDeviceComboItem> getCompatibleControllerDevice() {
//        return ProjectConfigurationLogic.getControllerComboItemList(project);
//    }
//
    void setPlatform(Platform platform) {
        project.setPlatform(platform);
        if (platformChangedCallback != null) {
            platformChangedCallback.run();
        }
        if (configChangedCallback != null) {
            configChangedCallback.run();
        }
    }

    Platform getSelectedPlatform() {
        return project.getSelectedPlatform();
    }

    ActualDevice getController() {
        return project.getSelectedController();
    }

    void setController(ActualDeviceComboItem device) {
        selectedController = device;
        project.setController(device.getActualDevice());
        if (controllerChangedCallback != null) {
            controllerChangedCallback.run();
        }
        if (configChangedCallback != null) {
            configChangedCallback.run();
        }
    }

    ActualDevice getSelectedController() {
        return project.getSelectedController();
    }

////    ObjectProperty<Map<ProjectDevice, List<ActualDevice>>> compatibleDeviceListProperty() {
////        return compatibleDeviceList;
////    }
////
////    ObjectProperty<Map<ProjectDevice, Map<Peripheral, List<List<DevicePort>>>>> compatiblePortListProperty() {
////        return compatiblePortList;
////    }

    void setDevice(ProjectDevice projectDevice, CompatibleDevice device) {
        if (project.isActualDeviceSelected(projectDevice)) {
            project.getProjectConfiguration().removeAllDeviceConnection(projectDevice);
        }
        if (device.getActualDevice().isPresent()) {
            projectDevice.setActualDevice(device.getActualDevice().get());
        } else {
            project.getProjectConfiguration().setParentDevice(projectDevice, device.getProjectDevice().orElseThrow());
        }
        applyDeviceMapping();
        if (deviceConfigChangedCallback != null) {
            deviceConfigChangedCallback.run();
        }
        if (configChangedCallback != null) {
            configChangedCallback.run();
        }
    }

//    void setPeripheral(ProjectDevice projectDevice, Peripheral peripheral, List<DevicePort> port) {
//        // TODO: assume a device only has 1 peripheral
//        projectDevice.setDeviceConnection(peripheral, port);
//        applyDeviceMapping();
//        if (deviceConfigChangedCallback != null) {
//            deviceConfigChangedCallback.run();
//        }
//        if (configChangedCallback != null) {
//            configChangedCallback.run();
//        }
//    }
//
//    void clearPeripheral(ProjectDevice projectDevice, Peripheral peripheral) {
//        projectDevice.removeDeviceConnection(peripheral);
//        applyDeviceMapping();
//        if (deviceConfigChangedCallback != null) {
//            deviceConfigChangedCallback.run();
//        }
//        if (configChangedCallback != null) {
//            configChangedCallback.run();
//        }
//    }

    Object getPropertyValue(ProjectDevice projectDevice, Property p) {
        return project.getPropertyValue(projectDevice, p);
    }

    void setPropertyValue(ProjectDevice projectDevice, Property p, Object value) {
        project.getProjectConfiguration().setPropertyValue(projectDevice, p, value);
        if (configChangedCallback != null) {
            configChangedCallback.run();
        }
    }

    Set<CloudPlatform> getCloudPlatformUsed() {
        return project.getCloudPlatformUsed();
    }

    String getCloudPlatfromParameterValue(CloudPlatform cloudPlatform, String name) {
        return project.getCloudPlatformParameter(cloudPlatform, name);
    }

    void setCloudPlatformParameter(CloudPlatform cloudPlatform, String parameterName, String value) {
        project.setCloudPlatformParameter(cloudPlatform, parameterName, value);
        if (configChangedCallback != null) {
            configChangedCallback.run();
        }
    }

    Set<ProjectDevice> getUsedDevice() {
        return project.getAllDeviceUsed();
    }

    Set<ProjectDevice> getUnusedDevice() {
        return  project.getAllDeviceUnused();
    }

    public List<ActualDeviceComboItem> getControllerComboItemList() {
        return project.getProjectConfiguration().getControllerSelectableMap().entrySet().stream()
                .map(entry -> new ActualDeviceComboItem(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    public List<CompatibleDeviceComboItem> getCompatibleDevice(ProjectDevice projectDevice) {
        /* TODO: uncomment this */
        return new ArrayList<>();
    }

    public boolean isActualDeviceSelected(ProjectDevice projectDevice) {
        return project.isActualDeviceSelected(projectDevice);
    }

    public Optional<ActualDevice> getActualDeviceComboItem(ProjectDevice projectDevice) {
        return project.getActualDevice(projectDevice);
    }

    public Optional<ActualDevice> getActualDevice(ProjectDevice projectDevice) {
        return project.getActualDevice(projectDevice);
    }

    public Optional<ProjectDevice> getParentDevice(ProjectDevice projectDevice) {
        return project.getParentDevice(projectDevice);
    }

//    DeviceMapperResult autoAssignDevice() {
//        DeviceMapperResult result = ProjectConfigurationLogic.autoAssignDevices(project);
//        applyDeviceMapping();
//        if (deviceConfigChangedCallback != null) {
//            deviceConfigChangedCallback.run();
//        }
//        if (configChangedCallback != null) {
//            configChangedCallback.run();
//        }
//        return result;
//    }
}
