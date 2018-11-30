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

import io.makerplayground.device.generic.GenericDevice;
import io.makerplayground.device.actual.Platform;
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
    private final DynamicViewModelCreator<ProjectDevice, DevicePanelIconViewModel> sensorChildViewModel;
    private final DynamicViewModelCreator<ProjectDevice, DevicePanelIconViewModel> actuatorChildViewModel;
    private final DynamicViewModelCreator<ProjectDevice, DevicePanelIconViewModel> utilityChildViewModel;
    private final DynamicViewModelCreator<ProjectDevice, DevicePanelIconViewModel> cloudChildViewModel;
    private final DynamicViewModelCreator<ProjectDevice, DevicePanelIconViewModel> interfaceChildViewModel;

    public DevicePanelViewModel(Project project) {
        this.project = project;
        this.platformProperty = new SimpleObjectProperty<>(project.getPlatform());
        // update ourselves when project's platform has been changed else where
        this.project.platformProperty().addListener((observable, oldValue, newValue) -> platformProperty.set(newValue));
        // write back to project when view changed
        this.platformProperty.addListener((observable, oldValue, newValue) -> project.setPlatform(newValue));
        this.sensorChildViewModel = new DynamicViewModelCreator<>(project.getSensorDevice(), projectDevice -> new DevicePanelIconViewModel(projectDevice, project));
        this.actuatorChildViewModel = new DynamicViewModelCreator<>(project.getActuatorDevice(), projectDevice -> new DevicePanelIconViewModel(projectDevice, project));
        this.utilityChildViewModel = new DynamicViewModelCreator<>(project.getUtilityDevice(), projectDevice -> new DevicePanelIconViewModel(projectDevice, project));
        this.cloudChildViewModel = new DynamicViewModelCreator<>(project.getCloudDevice(), projectDevice -> new DevicePanelIconViewModel(projectDevice, project));
        this.interfaceChildViewModel = new DynamicViewModelCreator<>(project.getInterfaceDevice(), projectDevice -> new DevicePanelIconViewModel(projectDevice, project));
    }

    public DynamicViewModelCreator<ProjectDevice, DevicePanelIconViewModel> getSensorChildViewModel() {
        return sensorChildViewModel;
    }

    public DynamicViewModelCreator<ProjectDevice, DevicePanelIconViewModel> getActuatorChildViewModel() {
        return actuatorChildViewModel;
    }

    public DynamicViewModelCreator<ProjectDevice, DevicePanelIconViewModel> getUtilityChildViewModel() {
        return utilityChildViewModel;
    }

    public DynamicViewModelCreator<ProjectDevice, DevicePanelIconViewModel> getCloudChildViewModel() {
        return cloudChildViewModel;
    }

    public DynamicViewModelCreator<ProjectDevice, DevicePanelIconViewModel> getInterfaceChildViewModel() {
        return interfaceChildViewModel;
    }

    public boolean removeDevice(DevicePanelIconViewModel device) {
        ProjectDevice deviceToBeRemoved = device.getDevice();
        return project.removeDevice(deviceToBeRemoved);
    }

    public void addDevice(Map<GenericDevice, Integer> device) {
        device.forEach((genericDevice, count) -> {
            for (int i=0; i<count; i++) {
                project.addDevice(genericDevice);
            }
        });
    }

    public ObjectProperty<Platform> selectedPlatformProperty() {
        return platformProperty;
    }
}
