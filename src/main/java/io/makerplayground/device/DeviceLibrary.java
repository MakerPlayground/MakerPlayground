/*
 * Copyright 2017 The Maker Playground Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.makerplayground.device;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.*;

/**
 *
 */
public enum DeviceLibrary {
    INSTANCE;

//    private List<Microcontroller> microcontroller;
    private List<GenericDevice> genericInputDevice;
    private List<GenericDevice> genericOutputDevice;
    private List<Device> actualDevice;

    DeviceLibrary() {
    }

    public void loadDeviceFromJSON() {
//        List<Microcontroller> temp3;
        List<GenericDevice> temp;
        List<Device> temp2;

        ObjectMapper mapper = new ObjectMapper();

        try {
//            temp3 = mapper.readValue(getClass().getResourceAsStream("/json/processor.json")
//                    , new TypeReference<List<Microcontroller>>() {});
//            this.microcontroller = Collections.unmodifiableList(temp3);

            temp = mapper.readValue(getClass().getResourceAsStream("/json/genericinputdevice.json")
                    , new TypeReference<List<GenericDevice>>() {});
            this.genericInputDevice = Collections.unmodifiableList(temp);

            temp = mapper.readValue(getClass().getResourceAsStream("/json/genericoutputdevice.json")
                    , new TypeReference<List<GenericDevice>>() {});
            this.genericOutputDevice = Collections.unmodifiableList(temp);

            temp2 = mapper.readValue(getClass().getResourceAsStream("/json/actualdevice.json")
                    , new TypeReference<List<Device>>() {});
            this.actualDevice = Collections.unmodifiableList(temp2);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(this.actualDevice);
    }

//    public List<Microcontroller> getMicrocontroller() {
//        return microcontroller;
//    }

    public GenericDevice getGenericDevice(String name) {
        for (GenericDevice genericDevice : genericInputDevice) {
            if (genericDevice.getName().equals(name)) {
                return genericDevice;
            }
        }
        for (GenericDevice genericDevice : genericOutputDevice) {
            if (genericDevice.getName().equals(name)) {
                return genericDevice;
            }
        }
        return null;
    }

    public List<GenericDevice> getGenericInputDevice() {
        return genericInputDevice;
    }

    public List<GenericDevice> getGenericOutputDevice() {
        return genericOutputDevice;
    }

    public List<Device> getActualDevice() {
        return actualDevice;
    }

    public Device getActualDevice(String id) {
        for (Device device : actualDevice) {
            if (device.getId().equals(id)) {
                return device;
            }
        }
        return null;
    }
}

//        try {
//            temp = mapper.readValue(getClass().getResourceAsStream("/json/genericinputdevice.json")
//                    , new TypeReference<List<GenericDevice>>() {});
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        for (GenericDevice actualDevice : temp) {
//            tmpInputDevice.put(actualDevice, Collections.emptyList());
//        }

//        Device d = new Device("Sparkfun", "Sparkdun Redboard", "http://www.ss"
//                , Collections.singletonMap(new GenericDevice("led",
//                Arrays.asList(new Action("on", Arrays.asList(new Parameter("brightness", 5, Constraint.NONE, DataType.INTEGER, ControlType.SLIDER)))), Collections.emptyList())
//                , Collections.singletonMap(new Action("on", Arrays.asList(new Parameter("brightness", 5, Constraint.NONE, DataType.INTEGER, ControlType.SLIDER)))
//                    , Collections.singletonMap(new Parameter("brightness", 5, Constraint.NONE, DataType.INTEGER, ControlType.SLIDER), Constraint.NONE)))
//                , Collections.emptyMap());
//        try {
//            mapper.writeValue(new File("actualDevice.json"), d);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        System.out.println(temp);
