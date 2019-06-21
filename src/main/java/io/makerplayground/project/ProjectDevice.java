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

package io.makerplayground.project;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.makerplayground.device.actual.ActualDevice;
import io.makerplayground.device.actual.Property;
import io.makerplayground.device.generic.GenericDevice;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Getter;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

@JsonSerialize (using = ProjectDeviceSerializer.class)
public class ProjectDevice implements Comparable<ProjectDevice>{
    private final StringProperty name;
    @Getter private final GenericDevice genericDevice;
//    @Getter private ActualDevice actualDevice;
//    @Getter private ProjectDevice parentDevice;     // this device may share the actual device with other project device in the project
                                            // in this case actualDevice will be null and parentDevice will contain the reference
                                            // to the other project device (only one field can have value at the same time)
//    private Map<Peripheral, List<DevicePort>> deviceConnection; // connection from this device (key) to the processor (value)
//    @Getter private ActualDevice dependentDevice;
//    @Getter private Map<Peripheral, List<DevicePort>> dependentDeviceConnection; // connection from this device (key) to the processor (value)

    public ProjectDevice(String name, GenericDevice genericDevice, Project project) {
        this.name = new SimpleStringProperty(name);
        this.genericDevice = genericDevice;
//        this.deviceConnection = new HashMap<>();
//        this.dependentDeviceConnection = new HashMap<>();
//        this.propertyValue = new HashMap<>();
    }

    ProjectDevice(String name, GenericDevice genericDevice, ActualDevice actualDevice,
//                  Map<Peripheral, List<DevicePort>> deviceConnection,
                  ActualDevice dependentDevice,
//                  Map<Peripheral, List<DevicePort>> dependentDeviceConnection,
                  Map<Property, Object> propertyValue) {
        this.name = new SimpleStringProperty(name);
        this.genericDevice = genericDevice;
//        this.actualDevice = actualDevice;
//        this.deviceConnection = deviceConnection;
//        this.dependentDevice = dependentDevice;
//        this.dependentDeviceConnection = dependentDeviceConnection;
    }

    public String getName() {
        return name.get();
    }

    public boolean setName(String name) {
        // name should contain only letters, digits and underscores and cannot start with a digit (we follow the c
        // identifier rule which should be fine for code generate for most platform )
        this.name.set(name);
        return true;
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setActualDevice(ActualDevice actualDevice) {
//        this.actualDevice = actualDevice;
//        this.parentDevice = null;
        // initialize device properties with their default value (we don't clear the map so that it looks like we save
        // the old property value when user switch back and forth between device)
//        if (actualDevice != null) {
//            for (Property property : actualDevice.getProperty()) {
//                propertyValue.put(property, property.getDefaultValue());
//            }
//        }
    }

    @Override
    public int compareTo(ProjectDevice o) {
        return getName().compareTo(o.getName());
    }

//    public boolean isActualDeviceSelected() {
//        return actualDevice != null;
//    }

//    public void setParentDevice(ProjectDevice parentDevice) {
//        this.actualDevice = null;
//        this.parentDevice = parentDevice;
//    }

//    public boolean isMergeToOtherDevice() {
//        return parentDevice != null;
//    }

//    public Map<Peripheral, List<DevicePort>> getDeviceConnection() {
//        // TODO: we should return an immutable copy for safety
//        return deviceConnection;
//    }
//
//    public void setDeviceConnection(Peripheral device, List<DevicePort> processor) {
//        this.deviceConnection.put(device, processor);
//    }

//    public void removeDeviceConnection(Peripheral device) {
//        this.deviceConnection.remove(device);
//    }
//
//    public void removeAllDeviceConnection() {
//        this.deviceConnection.clear();
//    }


}
