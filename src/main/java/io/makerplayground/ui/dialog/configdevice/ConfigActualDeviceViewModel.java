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
import io.makerplayground.generator.devicemapping.DeviceMappingResult;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectConfiguration;
import io.makerplayground.project.ProjectDevice;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by tanyagorn on 7/11/2017.
 */
public class ConfigActualDeviceViewModel {

    private final Project project;
    private final ObjectProperty<Map<ProjectDevice, SortedMap<CompatibleDevice, DeviceMappingResult>>> compatibleDeviceMap;
//    private final ObjectProperty<Map<ProjectDevice, List<CompatibleDevice>>> compatibleDeviceList;
//    private final ObjectProperty<Map<ProjectDevice, List<ProjectDevice>>> compatibleShareDeviceList;
//    private final ObjectProperty<Map<ProjectDevice, Map<Peripheral, List<List<DevicePort>>>>> compatiblePortList;

    private ActualDeviceComboItem selectedController;

    @Setter private Runnable platformChangedCallback;
    @Setter private Runnable controllerChangedCallback;
    @Setter private Runnable deviceConfigChangedCallback;
    @Setter private Runnable configChangedCallback;

    public ConfigActualDeviceViewModel(Project project) {
        this.project = project;
        this.compatibleDeviceMap = new SimpleObjectProperty<>();
        applyDeviceMapping();
    }

    private void applyDeviceMapping() {
        compatibleDeviceMap.set(project.getProjectConfiguration().getActualDevicesSelectableMap());
        /* TODO: uncomment this */
//        compatibleDeviceList.set(ProjectConfigurationLogic.getSupportedDeviceList(project));
//        compatibleShareDeviceList.set(ProjectConfigurationLogic.getShareableDeviceList(project));
//        compatiblePortList.set(ProjectConfigurationLogic.getDeviceCompatiblePort(project));
    }

    public void clearDeviceConfigChangedCallback() {
        deviceConfigChangedCallback = null;
    }

//    List<CompatibleDevice> getCompatibleDeviceComboItem(ProjectDevice projectDevice) {
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
        if (project.getSelectedPlatform() != platform) {
            project.setPlatform(platform);
            project.getProjectConfiguration().unsetDevice(ProjectDevice.CONTROLLER);
            applyDeviceMapping();
            if (platformChangedCallback != null) {
                platformChangedCallback.run();
            }
            if (configChangedCallback != null) {
                configChangedCallback.run();
            }
        }
    }

    Platform getSelectedPlatform() {
        return project.getSelectedPlatform();
    }

    ActualDevice getController() {
        return project.getSelectedController();
    }

    void setController(ActualDeviceComboItem device) {
        if (selectedController != device) {
            selectedController = device;
            if (device != null) {
                project.setController(device.getActualDevice());
            }
            applyDeviceMapping();
            if (controllerChangedCallback != null) {
                controllerChangedCallback.run();
            }
            if (configChangedCallback != null) {
                configChangedCallback.run();
            }
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
        ProjectConfiguration configuration = project.getProjectConfiguration();
        if (configuration.isActualDeviceSelected(projectDevice)) {
            project.getProjectConfiguration().removeAllDeviceConnection(projectDevice);
        }
        configuration.unsetDevice(projectDevice);
        if (device.getActualDevice().isPresent()) {
            configuration.setActualDevice(projectDevice, device.getActualDevice().get());
        } else if (device.getProjectDevice().isPresent()){
            configuration.setParentDevice(projectDevice, device.getProjectDevice().orElseThrow());
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
        return project.getProjectConfiguration().getPropertyValue(projectDevice, p);
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

    String getCloudPlatformParameterValue(CloudPlatform cloudPlatform, String name) {
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

    public List<ActualDeviceComboItem> getControllerComboItemList(Platform platform) {
        return project.getProjectConfiguration().getControllerSelectableMap().entrySet().stream()
                .filter(entry -> entry.getKey().getPlatformSourceCodeLibrary().containsKey(platform))
                .map(entry -> new ActualDeviceComboItem(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    public List<CompatibleDeviceComboItem> getCompatibleDeviceComboItem(ProjectDevice projectDevice) {
        if (!compatibleDeviceMap.get().containsKey(projectDevice)) {
            return new ArrayList<>();
        }
        return compatibleDeviceMap.get().get(projectDevice).entrySet().stream().map(entry -> new CompatibleDeviceComboItem(entry.getKey(), entry.getValue())).sorted().collect(Collectors.toList());
    }

    public boolean isActualDeviceSelected(ProjectDevice projectDevice) {
        return project.getProjectConfiguration().isActualDeviceSelected(projectDevice);
    }

//    public Optional<ActualDevice> getActualDeviceComboItem(ProjectDevice projectDevice) {
//        return project.getActualDevice(projectDevice);
//    }

    public Optional<ActualDevice> getActualDevice(ProjectDevice projectDevice) {
        return project.getProjectConfiguration().getActualDevice(projectDevice);
    }

    public Optional<ProjectDevice> getParentDevice(ProjectDevice projectDevice) {
        return project.getProjectConfiguration().getParentDevice(projectDevice);
    }

//    ProjectMappingResult autoAssignDevice() {
//        ProjectMappingResult result = ProjectConfigurationLogic.autoAssignDevices(project);
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
