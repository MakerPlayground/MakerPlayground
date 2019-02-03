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
import io.makerplayground.device.actual.DevicePort;
import io.makerplayground.device.generic.GenericDevice;
import io.makerplayground.device.actual.Property;
import io.makerplayground.device.actual.Peripheral;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * Created by Nuntipat Narkthong on 6/2/2017 AD.
 */
@JsonSerialize (using = ProjectDeviceSerializer.class)
public class ProjectDevice {
    private StringProperty name;
    private final GenericDevice genericDevice;
    private ActualDevice actualDevice;
    private Map<Peripheral, List<DevicePort>> deviceConnection; // connection from this device (key) to the processor (value)
    private ActualDevice dependentDevice;
    private Map<Peripheral, List<DevicePort>> dependentDeviceConnection; // connection from this device (key) to the processor (value)
    private Map<Property, Object> propertyValue;  // property needed by the actual device

    public ProjectDevice(String name, GenericDevice genericDevice) {
        this.name = new SimpleStringProperty(name);
        this.genericDevice = genericDevice;
        this.actualDevice = null;
        this.deviceConnection = new HashMap<>();
        this.dependentDevice = null;
        this.dependentDeviceConnection = new HashMap<>();
        this.propertyValue = new HashMap<>();
    }

    ProjectDevice(String name, GenericDevice genericDevice, ActualDevice actualDevice, Map<Peripheral
            , List<DevicePort>> deviceConnection, ActualDevice dependentDevice, Map<Peripheral
            , List<DevicePort>> dependentDeviceConnection, Map<Property, Object> propertyValue) {
        this.name = new SimpleStringProperty(name);
        this.genericDevice = genericDevice;
        this.actualDevice = actualDevice;
        this.deviceConnection = deviceConnection;
        this.dependentDevice = dependentDevice;
        this.dependentDeviceConnection = dependentDeviceConnection;
        this.propertyValue = propertyValue;
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public StringProperty nameProperty() {
        return name;
    }

    public GenericDevice getGenericDevice() {
        return genericDevice;
    }

    public ActualDevice getActualDevice() {
        return actualDevice;
    }

    public void setActualDevice(ActualDevice actualDevice) {
        this.actualDevice = actualDevice;
        // initialize device properties with their default value (we don't clear the map so that it looks like we save
        // the old property value when user switch back and forth between device)
        if (actualDevice != null) {
            for (Property property : actualDevice.getProperty()) {
                propertyValue.put(property, property.getDefaultValue());
            }
        }
    }

    public Map<Peripheral, List<DevicePort>> getDeviceConnection() {
        // TODO: we should return an immutable copy for safety
        return deviceConnection;
    }

//    public void setDeviceConnection(Map<Peripheral, Peripheral> deviceConnection) {
//        this.deviceConnection = deviceConnection;
//    }
    public void setDeviceConnection(Peripheral device, List<DevicePort> processor) {
        this.deviceConnection.put(device, processor);
    }

    public void removeDeviceConnection(Peripheral device) {
        this.deviceConnection.remove(device);
    }

    public void removeAllDeviceConnection() {
        this.deviceConnection.clear();
    }

    public ActualDevice getDependentDevice() {
        return dependentDevice;
    }

//    public void setDependentDevice(ActualDevice dependentDevice) {
//        this.dependentDevice = dependentDevice;
//    }

    public Map<Peripheral, List<DevicePort>> getDependentDeviceConnection() {
        return dependentDeviceConnection;
    }

//    public void setDependentDeviceConnection(Peripheral device, List<DevicePort> processor) {
//        this.dependentDeviceConnection.put(device, processor);
//    }

    public Object getPropertyValue(Property p) {
        return propertyValue.get(p);
    }

    public void setPropertyValue(Property p, Object value) {
        propertyValue.put(p, value);
    }

//    public void setDependentDeviceConnection(Map<Peripheral, Peripheral> dependentDeviceConnection) {
//        this.dependentDeviceConnection = dependentDeviceConnection;
//    }
}
