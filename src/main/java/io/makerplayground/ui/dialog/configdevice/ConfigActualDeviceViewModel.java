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

import io.makerplayground.device.actual.CloudPlatform;
import io.makerplayground.device.actual.ActualDevice;
import io.makerplayground.device.actual.DevicePort;
import io.makerplayground.generator.DeviceMapper;
import io.makerplayground.device.actual.Peripheral;
import io.makerplayground.device.actual.Platform;
import io.makerplayground.generator.DeviceMapperResult;
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
    private DeviceMapperResult deviceMapperResult;
    private Runnable platformChangedCallback;
    private Runnable controllerChangedCallback;
    private Runnable deviceConfigChangedCallback;
    private Runnable configChangedCallback;

    public ConfigActualDeviceViewModel(Project project) {
        this.project = project;
        this.compatibleDeviceList = new SimpleObjectProperty<>();
        this.compatiblePortList = new SimpleObjectProperty<>();
        applyDeviceMapping();
    }

    private void applyDeviceMapping() {
        deviceMapperResult = DeviceMapper.autoAssignDevices(project);
        if (deviceMapperResult == DeviceMapperResult.OK ) {
            compatibleDeviceList.set(DeviceMapper.getSupportedDeviceList(project));
            compatiblePortList.set(DeviceMapper.getDeviceCompatiblePort(project));
        } else {
            compatibleDeviceList.set(null);
            compatiblePortList.set(null);
        }
    }

    public void setPlatformChangedCallback(Runnable callback) {
        platformChangedCallback = callback;
    }

    public void setControllerChangedCallback(Runnable callback) {
        controllerChangedCallback = callback;
    }

    public void setDeviceConfigChangedCallback(Runnable callback) {
        deviceConfigChangedCallback = callback;
    }

    public void clearDeviceConfigChangedCallback() {
        deviceConfigChangedCallback = null;
    }

    public void setConfigChangedCallback(Runnable callback) {
        configChangedCallback = callback;
    }

    DeviceMapperResult getDeviceMapperResult() {
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
            platformChangedCallback.run();
        }
        if (configChangedCallback != null) {
            configChangedCallback.run();
        }
    }

    Platform getSelectedPlatform() {
        return project.getPlatform();
    }

    void setController(ActualDevice device) {
        project.setController(device);
        applyDeviceMapping();
        if (controllerChangedCallback != null) {
            controllerChangedCallback.run();
        }
        if (configChangedCallback != null) {
            configChangedCallback.run();
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
            deviceConfigChangedCallback.run();
        }
        if (configChangedCallback != null) {
            configChangedCallback.run();
        }
    }

    void setPeripheral(ProjectDevice projectDevice, Peripheral peripheral, List<DevicePort> port) {
        // TODO: assume a device only has 1 peripheral
        projectDevice.setDeviceConnection(peripheral, port);
        applyDeviceMapping();
        if (deviceConfigChangedCallback != null) {
            deviceConfigChangedCallback.run();
        }
        if (configChangedCallback != null) {
            configChangedCallback.run();
        }
    }

    void clearPeripheral(ProjectDevice projectDevice, Peripheral peripheral) {
        projectDevice.removeDeviceConnection(peripheral);
        applyDeviceMapping();
        if (deviceConfigChangedCallback != null) {
            deviceConfigChangedCallback.run();
        }
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
    }

    Set<ProjectDevice> getUsedDevice() {
        return project.getAllDeviceUsed();
    }

    Set<ProjectDevice> getUnusedDevice() {
        return  project.getAllDeviceUnused();
    }
}
