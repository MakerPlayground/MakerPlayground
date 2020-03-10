/*
 * Copyright (c) 2020. The Maker Playground Authors.
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

package io.makerplayground.ui.devicetab;

import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.device.actual.*;
import io.makerplayground.generator.devicemapping.DeviceMappingResult;
import io.makerplayground.generator.devicemapping.ProjectMappingResult;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectConfiguration;
import io.makerplayground.project.ProjectDevice;
import io.makerplayground.ui.devicetab.ActualDeviceComboItem;
import io.makerplayground.ui.devicetab.CompatibleDevice;
import io.makerplayground.ui.devicetab.CompatibleDeviceComboItem;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by tanyagorn on 7/11/2017.
 */
public class ConfigActualDeviceViewModel {

    private final Project project;
    private final ObjectProperty<Map<ProjectDevice, SortedMap<CompatibleDevice, DeviceMappingResult>>> compatibleDeviceMap;
    private final ObjectProperty<Map<ProjectDevice, Map<ActualDevice, SortedMap<Connection, List<Connection>>>>> deviceConnectionList;

    private ActualDeviceComboItem selectedController;

    @Setter private Runnable platformChangedCallback;
    @Setter private Runnable controllerChangedCallback;
    @Setter private Runnable deviceConfigChangedCallback;
    @Setter private Runnable configChangedCallback;

    public ConfigActualDeviceViewModel(Project project) {
        this.project = project;
        this.compatibleDeviceMap = new SimpleObjectProperty<>();
        this.deviceConnectionList = new SimpleObjectProperty<>();
        retrieveDeviceMapping();
    }

    private void retrieveDeviceMapping() {
        compatibleDeviceMap.set(project.getProjectConfiguration().getCompatibleDevicesSelectableMap());
        deviceConnectionList.set(project.getProjectConfiguration().getCompatibleConnectionMap());
    }

    public void clearDeviceConfigChangedCallback() {
        deviceConfigChangedCallback = null;
    }

    void setPlatform(Platform platform) {
        if (project.getSelectedPlatform() != platform) {
            project.setPlatform(platform);
            project.getProjectConfiguration().unsetDevice(ProjectDevice.CONTROLLER);
            project.getProjectConfiguration().unsetAllDevices();
            retrieveDeviceMapping();
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
            retrieveDeviceMapping();
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
                retrieveDeviceMapping();
                if (deviceConfigChangedCallback != null) {
                    deviceConfigChangedCallback.run();
                }
                if (configChangedCallback != null) {
                    configChangedCallback.run();
                }
            }
        } else if (configuration.getActualDevice(projectDevice) != device.getActualDevice()
                || configuration.getIdenticalDevice(projectDevice) != device.getProjectDevice()) {

            device.getActualDevice().ifPresentOrElse(actualDevice -> configuration.setActualDevice(projectDevice, actualDevice),
                    () -> device.getProjectDevice().ifPresent(identicalDevice -> configuration.setIdenticalDevice(projectDevice, identicalDevice)));
            retrieveDeviceMapping();
            if (deviceConfigChangedCallback != null) {
                deviceConfigChangedCallback.run();
            }
            if (configChangedCallback != null) {
                configChangedCallback.run();
            }
        }
    }

    Project.SetNameResult setProjectDeviceName(ProjectDevice projectDevice, String name) {
        return project.setProjectDeviceName(projectDevice, name);
    }

    void removeDevice(ProjectDevice projectDevice) {
        project.removeDevice(projectDevice);

        retrieveDeviceMapping();
        if (deviceConfigChangedCallback != null) {
            deviceConfigChangedCallback.run();
        }
        if (configChangedCallback != null) {
            configChangedCallback.run();
        }
    }

    void setConnection(ProjectDevice projectDevice, Connection connectionConsume, Connection connectionProvide) {
        if (project.getProjectConfiguration().getDeviceConnection(projectDevice).getConsumerProviderConnections().get(connectionConsume) != connectionProvide) {
            if (connectionProvide == null) {
                project.getProjectConfiguration().unsetConnection(projectDevice, connectionConsume);
            } else {
                project.getProjectConfiguration().setConnection(projectDevice, connectionConsume, connectionProvide);
            }
            retrieveDeviceMapping();
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

    Set<CloudPlatform> getAllCloudPlatforms() {
        return project.getAllCloudPlatforms();
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
        return DeviceLibrary.INSTANCE
                .getActualDevice(platform)
                .stream()
                .filter(actualDevice -> actualDevice.getDeviceType() == DeviceType.CONTROLLER)
                .map(actualDevice -> new ActualDeviceComboItem(actualDevice, DeviceMappingResult.OK))
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

    public Map<Connection, List<Connection>> getPossibleDeviceConnections(ProjectDevice projectDevice, ActualDevice actualDevice) {
        return deviceConnectionList.get().get(projectDevice).get(actualDevice);
    }

    public Connection getSelectedConnection(ProjectDevice projectDevice, Connection connectionConsume) {
        return project.getProjectConfiguration().getDeviceConnection(projectDevice).getConsumerProviderConnections().get(connectionConsume);
    }

    ProjectMappingResult autoAssignDevice() {
        ProjectMappingResult result = project.getProjectConfiguration().autoAssignDevices();
        retrieveDeviceMapping();
        if (deviceConfigChangedCallback != null) {
            deviceConfigChangedCallback.run();
        }
        if (configChangedCallback != null) {
            configChangedCallback.run();
        }
        return result;
    }

    public ObservableList<ProjectDevice> getAllDevices() {
        return project.getUnmodifiableProjectDevice();
    }
}
