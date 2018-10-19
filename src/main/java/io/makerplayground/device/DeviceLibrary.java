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
import io.makerplayground.device.actual.ActualDevice;
import io.makerplayground.device.actual.Platform;
import io.makerplayground.device.generic.GenericDevice;

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

    private List<GenericDevice> genericInputDevice;
    private List<GenericDevice> genericOutputDevice;
    private List<GenericDevice> genericVirtualDevice;
    private List<ActualDevice> actualDevice;

    DeviceLibrary() {
    }

    public void loadDeviceFromJSON() {
        this.genericInputDevice = loadGenericDeviceFromJSON("/json/genericinputdevice.json");
        this.genericOutputDevice = loadGenericDeviceFromJSON("/json/genericoutputdevice.json");
        this.genericVirtualDevice = loadGenericDeviceFromJSON("/json/genericvirtualdevice.json");
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
            temp = Collections.emptyList();
        }
        return temp;
    }

    private List<ActualDevice> loadActualDeviceList(){
        List<ActualDevice> temp = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get("library/devices"))){
            for (Path deviceDirectory : directoryStream) {
                if(Files.isDirectory(deviceDirectory)){
                    try(DirectoryStream<Path> dev = Files.newDirectoryStream(deviceDirectory,"device.json")){
                        for(Path deviceDefinitionFile: dev){
                            temp.add(mapper.readValue(deviceDefinitionFile.toFile(),new TypeReference<ActualDevice>() {}));
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
        for (GenericDevice genericDevice : genericVirtualDevice) {
            if (genericDevice.getName().equals(name)) {
                return genericDevice;
            }
        }
        throw new IllegalArgumentException("No generic device named " + name);
    }

    public List<GenericDevice> getGenericInputDevice() {
        return genericInputDevice;
    }

    public List<GenericDevice> getGenericOutputDevice() {
        return genericOutputDevice;
    }

    public List<GenericDevice> getGenericVirtualDevice() {
        return genericVirtualDevice;
    }

    public List<ActualDevice> getActualDevice() {
        return actualDevice;
    }

    public List<ActualDevice> getActualDevice(Platform platform) {
        return actualDevice.stream().filter(device -> device.getSupportedPlatform().contains(platform))
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    public ActualDevice getActualDevice(String id) {
        for (ActualDevice device : actualDevice) {
            if (device.getId().equals(id)) {
                return device;
            }
        }
        return null;
    }
}
