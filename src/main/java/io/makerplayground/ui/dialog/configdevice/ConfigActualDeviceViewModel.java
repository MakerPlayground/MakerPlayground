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

import io.makerplayground.device.CloudPlatform;
import io.makerplayground.device.ActualDevice;
import io.makerplayground.device.DevicePort;
import io.makerplayground.generator.DeviceMapper;
import io.makerplayground.helper.Peripheral;
import io.makerplayground.helper.Platform;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectDevice;
import io.makerplayground.ui.dialog.devicepane.devicepanel.Callback;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.*;

/**
 * Created by tanyagorn on 7/11/2017.
 */
public class ConfigActualDeviceViewModel {
    private final Project project;
    private final ObjectProperty<Map<ProjectDevice, List<ActualDevice>>> compatibleDeviceList;
    private final ObjectProperty<Map<ProjectDevice, Map<Peripheral, List<List<DevicePort>>>>> compatiblePortList;
    private DeviceMapper.DeviceMapperResult deviceMapperResult;
    private Callback platformChangedCallback;
    private Callback controllerChangedCallback;
    private Callback deviceConfigChangedCallback;

    public ConfigActualDeviceViewModel(Project project) {
        this.project = project;
        this.compatibleDeviceList = new SimpleObjectProperty<>();
        this.compatiblePortList = new SimpleObjectProperty<>();
        applyDeviceMapping();
    }

    private void applyDeviceMapping() {
        deviceMapperResult = DeviceMapper.autoAssignDevices(project);
        if (deviceMapperResult == DeviceMapper.DeviceMapperResult.OK ) {
            compatibleDeviceList.set(DeviceMapper.getSupportedDeviceList(project));
            compatiblePortList.set(DeviceMapper.getDeviceCompatiblePort(project));
        } else {
            compatibleDeviceList.set(null);
            compatiblePortList.set(null);
        }
    }

    void setPlatformChangedCallback(Callback platformChangedCallback) {
        this.platformChangedCallback = platformChangedCallback;
    }

    void setControllerChangedCallback(Callback controllerChangedCallback) {
        this.controllerChangedCallback = controllerChangedCallback;
    }

    void setDeviceConfigChangedCallback(Callback deviceConfigChangedCallback) {
        this.deviceConfigChangedCallback = deviceConfigChangedCallback;
    }

    void removeDeviceConfigChangedCallback() {
        this.deviceConfigChangedCallback = null;
    }

    DeviceMapper.DeviceMapperResult getDeviceMapperResult() {
        return deviceMapperResult;
    }

    List<ActualDevice> getCompatibleDevice(ProjectDevice projectDevice) {
        return compatibleDeviceList.get().get(projectDevice);
    }

    Map<Peripheral, List<List<DevicePort>>> getCompatiblePort(ProjectDevice projectDevice) {
        return compatiblePortList.get().get(projectDevice);
    }

    List<ActualDevice> getCompatibleControllerDevice() {
        return DeviceMapper.getSupportedController(project);
    }

    void setPlatform(Platform platform) {
        project.setPlatform(platform);
        applyDeviceMapping();
        if (platformChangedCallback != null) {
            platformChangedCallback.call();
        }
    }

    Platform getSelectedPlatform() {
        return project.getPlatform();
    }

    void setController(ActualDevice device) {
        project.setController(device);
        applyDeviceMapping();
        if (controllerChangedCallback != null) {
            controllerChangedCallback.call();
        }
    }

    ActualDevice getSelectedController() {
        return project.getController();
    }

//    ObjectProperty<Map<ProjectDevice, List<ActualDevice>>> compatibleDeviceListProperty() {
//        return compatibleDeviceList;
//    }
//
//    ObjectProperty<Map<ProjectDevice, Map<Peripheral, List<List<DevicePort>>>>> compatiblePortListProperty() {
//        return compatiblePortList;
//    }

    void setDevice(ProjectDevice projectDevice, ActualDevice device) {
        if (projectDevice.getActualDevice() != null) {
            projectDevice.removeAllDeviceConnection();
        }
        projectDevice.setActualDevice(device);
        applyDeviceMapping();
        if (deviceConfigChangedCallback != null) {
            deviceConfigChangedCallback.call();
        }
    }

    void setPeripheral(ProjectDevice projectDevice, Peripheral peripheral, List<DevicePort> port) {
        // TODO: assume a device only has 1 peripheral
        projectDevice.setDeviceConnection(peripheral, port);
        applyDeviceMapping();
        if (deviceConfigChangedCallback != null) {
            deviceConfigChangedCallback.call();
        }
    }

    void clearPeripheral(ProjectDevice projectDevice, Peripheral peripheral) {
        projectDevice.removeDeviceConnection(peripheral);
        applyDeviceMapping();
        if (deviceConfigChangedCallback != null) {
            deviceConfigChangedCallback.call();
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
    }

    Set<ProjectDevice> getUsedDevice() {
        return project.getAllDeviceUsed();
    }

    Set<ProjectDevice> getUnusedDevice() {
        return  project.getAllDeviceUnused();
    }
}
