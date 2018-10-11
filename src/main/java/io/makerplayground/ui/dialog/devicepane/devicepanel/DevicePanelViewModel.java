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

package io.makerplayground.ui.dialog.devicepane.devicepanel;

import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.device.GenericDevice;
import io.makerplayground.helper.Platform;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectDevice;
import io.makerplayground.ui.canvas.helper.DynamicViewModelCreator;
import io.makerplayground.ui.deprecated.DevicePanelIconViewModel;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.Map;

/**
 *
 * Created by Nuntipat Narkthong on 6/6/2017 AD.
 */
public class DevicePanelViewModel {
    private final Project project;
    private final ObjectProperty<Platform> platformProperty;
    private final DynamicViewModelCreator<ProjectDevice, DevicePanelIconViewModel> inputChildViewModel;
    private final DynamicViewModelCreator<ProjectDevice, DevicePanelIconViewModel> outputChildViewModel;
    private final DynamicViewModelCreator<ProjectDevice, DevicePanelIconViewModel> virtualChildViewModel;

    public DevicePanelViewModel(Project project) {
        this.project = project;
        this.platformProperty = new SimpleObjectProperty<>(project.getPlatform());
        // update ourselves when project's platform has been changed else where
        this.project.platformProperty().addListener((observable, oldValue, newValue) -> platformProperty.set(newValue));
        // write back to project when view changed
        this.platformProperty.addListener((observable, oldValue, newValue) -> project.setPlatform(newValue));
        this.inputChildViewModel = new DynamicViewModelCreator<>(project.getSensor(), projectDevice -> new DevicePanelIconViewModel(projectDevice, project));
        this.outputChildViewModel = new DynamicViewModelCreator<>(project.getActuator(), projectDevice -> new DevicePanelIconViewModel(projectDevice, project));
        this.virtualChildViewModel = new DynamicViewModelCreator<>(project.getVirtual(), projectDevice -> new DevicePanelIconViewModel(projectDevice, project));
    }

    public DynamicViewModelCreator<ProjectDevice, DevicePanelIconViewModel> getInputChildViewModel() {
        return inputChildViewModel;
    }

    public DynamicViewModelCreator<ProjectDevice, DevicePanelIconViewModel> getOutputChildViewModel() {
        return outputChildViewModel;
    }

    public DynamicViewModelCreator<ProjectDevice, DevicePanelIconViewModel> getVirtualChildViewModel() {
        return virtualChildViewModel;
    }

    public boolean removeOutputDevice(DevicePanelIconViewModel device) {
        ProjectDevice deviceToBeRemoved = device.getDevice();
        return project.removeActuator(deviceToBeRemoved);
    }

    public boolean removeInputDevice(DevicePanelIconViewModel device) {
        ProjectDevice deviceToBeRemoved = device.getDevice();
        return project.removeSensor(deviceToBeRemoved);
    }

    public boolean removeConnectivityDevice(DevicePanelIconViewModel device) {
        ProjectDevice deviceToBeRemoved = device.getDevice();
        return project.removeVirtual(deviceToBeRemoved);
    }

    public void addDevice(Map<GenericDevice, Integer> device) {
        for (GenericDevice genericDevice : device.keySet()) {
            if (DeviceLibrary.INSTANCE.getGenericInputDevice().contains(genericDevice)) {
                for (int i = 0; i < device.get(genericDevice); i++) {
                    project.addSensor(genericDevice);
                }
            } else if (DeviceLibrary.INSTANCE.getGenericOutputDevice().contains(genericDevice)) {
                for (int i = 0; i < device.get(genericDevice); i++) {
                    project.addActuator(genericDevice);
                }
            } else if (DeviceLibrary.INSTANCE.getGenericConnectivityDevice().contains(genericDevice)) {
                for (int i = 0; i < device.get(genericDevice); i++) {
                    project.addVirtual(genericDevice);
                }
            } else {
                throw new IllegalStateException("We are in great danger!!!");
            }
        }
    }

    public ObjectProperty<Platform> selectedPlatformProperty() {
        return platformProperty;
    }
}
