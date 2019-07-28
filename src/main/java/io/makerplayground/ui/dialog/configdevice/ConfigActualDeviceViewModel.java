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
import io.makerplayground.project.PinPortConnection;
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
    private final ObjectProperty<Map<ProjectDevice, Map<ActualDevice, List<PinPortConnection>>>> devicePinPortList;

    private ActualDeviceComboItem selectedController;

    @Setter private Runnable platformChangedCallback;
    @Setter private Runnable controllerChangedCallback;
    @Setter private Runnable deviceConfigChangedCallback;
    @Setter private Runnable configChangedCallback;

    public ConfigActualDeviceViewModel(Project project) {
        this.project = project;
        this.compatibleDeviceMap = new SimpleObjectProperty<>();
        this.devicePinPortList = new SimpleObjectProperty<>();
        applyDeviceMapping();
    }

    private void applyDeviceMapping() {
        compatibleDeviceMap.set(project.getProjectConfiguration().getCompatibleDevicesSelectableMap());
        devicePinPortList.set(project.getProjectConfiguration().getCompatibleDevicePinPortConnectionMap());
    }

    public void clearDeviceConfigChangedCallback() {
        deviceConfigChangedCallback = null;
    }

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
            if (selectedController != null) {
                project.setController(selectedController.getActualDevice());
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

    void setDevice(ProjectDevice projectDevice, CompatibleDevice device) {
        ProjectConfiguration configuration = project.getProjectConfiguration();
        /* same as previous -> do nothing */
        if (device == null) {
            if (configuration.getActualDevice(projectDevice).isPresent() || configuration.getIdenticalDevice(projectDevice).isPresent()) {
                configuration.unsetDevice(projectDevice);
                applyDeviceMapping();
                if (deviceConfigChangedCallback != null) {
                    deviceConfigChangedCallback.run();
                }
                if (configChangedCallback != null) {
                    configChangedCallback.run();
                }
            }
        }
        else if (configuration.getActualDevice(projectDevice) != device.getActualDevice()
                || configuration.getIdenticalDevice(projectDevice) != device.getProjectDevice()) {

            device.getActualDevice().ifPresentOrElse(actualDevice -> configuration.setActualDevice(projectDevice, actualDevice),
                    () -> device.getProjectDevice().ifPresent(identicalDevice -> configuration.setIdenticalDevice(projectDevice, identicalDevice)));
            applyDeviceMapping();
            if (deviceConfigChangedCallback != null) {
                deviceConfigChangedCallback.run();
            }
            if (configChangedCallback != null) {
                configChangedCallback.run();
            }
        }
    }

    void setDevicePinPortConnection(ProjectDevice projectDevice, PinPortConnection connection) {
        if (project.getProjectConfiguration().getDevicePinPortConnection(projectDevice) != connection) {
            if (connection == null) {
                project.getProjectConfiguration().unsetDevicePinPortConnection(projectDevice);
            } else {
                project.getProjectConfiguration().setDevicePinPortConnection(projectDevice, connection);
            }
            applyDeviceMapping();
            if (deviceConfigChangedCallback != null) {
                deviceConfigChangedCallback.run();
            }
            if (configChangedCallback != null) {
                configChangedCallback.run();
            }
        }
    }

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
            return Collections.emptyList();
        }
        return compatibleDeviceMap.get().get(projectDevice).entrySet().stream().map(entry -> new CompatibleDeviceComboItem(entry.getKey(), entry.getValue())).sorted().collect(Collectors.toList());
    }

    public boolean isActualDevicePresent(ProjectDevice projectDevice) {
        return project.getProjectConfiguration().getActualDevice(projectDevice).isPresent();
    }

    public Optional<ActualDevice> getActualDevice(ProjectDevice projectDevice) {
        return project.getProjectConfiguration().getActualDevice(projectDevice);
    }

    public Optional<ProjectDevice> getParentDevice(ProjectDevice projectDevice) {
        return project.getProjectConfiguration().getIdenticalDevice(projectDevice);
    }

    public List<PinPortConnection> getPossiblePinPortConnections(ProjectDevice projectDevice, ActualDevice actualDevice) {
        return devicePinPortList.get().get(projectDevice).get(actualDevice);
    }

    public PinPortConnection getSelectedPinPortConnection(ProjectDevice projectDevice) {
        return project.getProjectConfiguration().getDevicePinPortConnection(projectDevice);
    }

//    ProjectMappingResult autoAssignDevice() {
//        ProjectMappingResult result = ProjectLogic.autoAssignDevices(project);
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
