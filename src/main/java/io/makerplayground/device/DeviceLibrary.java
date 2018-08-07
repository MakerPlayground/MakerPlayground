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
import io.makerplayground.helper.Platform;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
public enum DeviceLibrary {
    INSTANCE;

//    private List<Microcontroller> microcontroller;
    private List<GenericDevice> genericInputDevice;
    private List<GenericDevice> genericOutputDevice;
    private List<GenericDevice> genericConnectivityDevice;
    private List<Device> actualDevice;

    DeviceLibrary() {
    }

    public void loadDeviceFromJSON() {
        this.genericInputDevice = loadGenericDeviceFromJSON("/json/genericinputdevice.json");
        this.genericOutputDevice = loadGenericDeviceFromJSON("/json/genericoutputdevice.json");
        this.genericConnectivityDevice = loadGenericDeviceFromJSON("/json/genericconnectivitydevice.json");
        this.actualDevice = loadActualDeviceList();
    }

    private List<GenericDevice> loadGenericDeviceFromJSON(String resourceName){
        ObjectMapper mapper = new ObjectMapper();
        List<GenericDevice> temp;
        try {
            temp = mapper.readValue(getClass().getResourceAsStream(resourceName), new TypeReference<List<GenericDevice>>() {});
            temp = Collections.unmodifiableList(temp);
        } catch (IOException e) {
            e.printStackTrace();
            temp = Collections.EMPTY_LIST;
        }
        return temp;
    }

    private List<Device> loadActualDeviceList(){
        List<Device> temp = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get("devices"))){
            for (Path deviceDirectory : directoryStream) {
                if(Files.isDirectory(deviceDirectory)){
                    try(DirectoryStream<Path> dev = Files.newDirectoryStream(deviceDirectory,"device.json")){
                        for(Path deviceDefinitionFile: dev){
                            temp.add(mapper.readValue(deviceDefinitionFile.toFile(),new TypeReference<Device>() {}));
                        }
                    }
                }
            }
            temp = Collections.unmodifiableList(temp);
        }catch (IOException e){
            e.printStackTrace();
            temp = Collections.emptyList();
        }
        return temp;
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
        for (GenericDevice genericDevice : genericConnectivityDevice) {
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

    public List<GenericDevice> getGenericConnectivityDevice() {
        return genericConnectivityDevice;
    }

    public List<Device> getActualDevice() {
        return actualDevice;
    }

    public List<Device> getActualDevice(Platform platform) {
        return actualDevice.stream().filter(device -> device.getSupportedPlatform().contains(platform))
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
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
