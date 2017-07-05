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

    private final Map<GenericDevice, List<Device>> inputDevice;
    private final Map<GenericDevice, List<Device>> outputDevice;

    DeviceLibrary() {
        Map<GenericDevice, List<Device>> tmpInputDevice = new HashMap<>();
        Map<GenericDevice, List<Device>> tmpOutputDevice = new HashMap<>();

        ObjectMapper mapper = new ObjectMapper();
        List<GenericDevice> temp = null;

        try {
            temp = mapper.readValue(getClass().getResourceAsStream("/json/genericoutputdevice.json")
                    , new TypeReference<List<GenericDevice>>() {});
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (GenericDevice device : temp) {
            tmpOutputDevice.put(device, Collections.emptyList());
        }

        this.inputDevice = Collections.unmodifiableMap(tmpInputDevice);
        this.outputDevice = Collections.unmodifiableMap(tmpOutputDevice);
    }

    public Set<GenericDevice> getInputDevice() {
        return inputDevice.keySet();
    }

    public Set<GenericDevice> getOutputDevice() {
        return outputDevice.keySet();
    }
}

//        Device d = new Device("Sparkfun", "Sparkdun Redboard", "http://www.ss"
//                , Collections.singletonMap(new GenericDevice("led",
//                Arrays.asList(new Action("on", Arrays.asList(new Parameter("brightness", 5, Constraint.NONE, DataType.INTEGER, ControlType.SLIDER)))), Collections.emptyList())
//                , Collections.singletonMap(new Action("on", Arrays.asList(new Parameter("brightness", 5, Constraint.NONE, DataType.INTEGER, ControlType.SLIDER)))
//                    , Collections.singletonMap(new Parameter("brightness", 5, Constraint.NONE, DataType.INTEGER, ControlType.SLIDER), Constraint.NONE)))
//                , Collections.emptyMap());
//        try {
//            mapper.writeValue(new File("device.json"), d);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        System.out.println(temp);
