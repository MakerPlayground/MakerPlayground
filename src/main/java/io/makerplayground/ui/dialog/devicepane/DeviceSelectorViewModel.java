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

package io.makerplayground.ui.dialog.devicepane;

import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.device.GenericDevice;
import io.makerplayground.project.Project;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

/**
 *
 * Created by Nuntipat Narkthong on 6/7/2017 AD.
 */
public class DeviceSelectorViewModel {
    private final Project project;
    private final ObservableMap<GenericDevice, SimpleIntegerProperty> inputDeviceMap;
    private final ObservableMap<GenericDevice, SimpleIntegerProperty> outputDeviceMap;

    public DeviceSelectorViewModel(Project project) {
        this.project = project;

        this.inputDeviceMap = FXCollections.observableHashMap();
        for (GenericDevice device : DeviceLibrary.INSTANCE.getGenericInputDevice()) {
            inputDeviceMap.put(device, new SimpleIntegerProperty(0));
        }

        this.outputDeviceMap = FXCollections.observableHashMap();
        for (GenericDevice device : DeviceLibrary.INSTANCE.getGenericOutputDevice()) {
            outputDeviceMap.put(device, new SimpleIntegerProperty(0));
        }
    }

    public ObservableMap<GenericDevice, SimpleIntegerProperty> getInputDeviceMap() {
        return FXCollections.unmodifiableObservableMap(inputDeviceMap);
    }

    public ObservableMap<GenericDevice, SimpleIntegerProperty> getOutputDeviceMap() {
        return FXCollections.unmodifiableObservableMap(outputDeviceMap);
    }


    public void importDeviceToProject() {
//        for (Map.Entry<GenericDevice, Integer> entry : inputDeviceMap.entrySet()) {
//            for (int i=0; i<entry.getValue(); i++) {
//                project.addSensor(entry.getKey());
//            }
//        }
//
//        for (Map.Entry<GenericOutputDevice, Integer> entry : outputDeviceMap.entrySet()) {
//            for (int i=0; i<entry.getValue(); i++) {
//                project.addOutputDevice(entry.getKey());
//            }
//        }

        // TODO: clear the map value to 0
    }
}
