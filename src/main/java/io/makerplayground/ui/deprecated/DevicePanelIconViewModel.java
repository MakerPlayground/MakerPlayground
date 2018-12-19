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

package io.makerplayground.ui.deprecated;

import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectDevice;
import javafx.beans.property.StringProperty;

/**
 *
 * Created by Nuntipat Narkthong on 6/8/2017 AD.
 */
public class DevicePanelIconViewModel {

    private final ProjectDevice device;
    private final Project project;
    //private final StringProperty name;

    public DevicePanelIconViewModel(ProjectDevice device, Project project) {
        this.device = device;
        this.project = project;
        //this.name = new SimpleStringProperty(device.getName());
        //this.name.addListener((observable, oldValue, newValue) -> this.device.setName(newValue));
    }

    public String getDeviceName() {
        return device.getGenericDevice().getName();
    }

    public String getName() {
        return device.getName();
    }

    public void setName(String name) {
        device.setName(name);
    }

    public StringProperty nameProperty() {
        return device.nameProperty();
    }

    public ProjectDevice getDevice() {
        return device;
    }

    // Check if current scene's name is duplicated with other scenes
    // return true when this name cannot be used
    public boolean isNameDuplicate(String newName) {
        for (ProjectDevice projectDevice : project.getDevice()) {
            if (projectDevice.getName().equals(newName)) {
                return true;
            }
        }
        return false;
    }
}
