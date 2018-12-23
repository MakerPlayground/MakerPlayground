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
import com.fasterxml.jackson.databind.*;
import io.makerplayground.device.actual.ActualDevice;
import io.makerplayground.device.actual.Platform;
import io.makerplayground.device.generic.GenericDevice;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 */
public enum DeviceLibrary {
    INSTANCE;

    private List<GenericDevice> genericSensorDevice;
    private List<GenericDevice> genericActuatorDevice;
    private List<GenericDevice> genericUtilityDevice;
    private List<GenericDevice> genericCloudDevice;
    private List<GenericDevice> genericInterfaceDevice;
    private List<GenericDevice> allGenericDevice;
    private List<ActualDevice> actualDevice;

    DeviceLibrary() {}

    public void loadDeviceFromJSON() {
        this.genericSensorDevice = loadGenericDeviceFromJSON("/json/genericsensordevice.json", GenericDeviceType.SENSOR);
        this.genericActuatorDevice = loadGenericDeviceFromJSON("/json/genericactuatordevice.json", GenericDeviceType.ACTUATOR);
        this.genericUtilityDevice = loadGenericDeviceFromJSON("/json/genericutilitydevice.json", GenericDeviceType.UTILITY);
        this.genericCloudDevice = loadGenericDeviceFromJSON("/json/genericclouddevice.json", GenericDeviceType.CLOUD);
        this.genericInterfaceDevice = loadGenericDeviceFromJSON("/json/genericinterfacedevice.json", GenericDeviceType.INTERFACE);
        this.allGenericDevice = new ArrayList<>();
        this.allGenericDevice.addAll(genericSensorDevice);
        this.allGenericDevice.addAll(genericActuatorDevice);
        this.allGenericDevice.addAll(genericUtilityDevice);
        this.allGenericDevice.addAll(genericCloudDevice);
        this.allGenericDevice.addAll(genericInterfaceDevice);
        this.actualDevice = loadActualDeviceList();
    }

    private List<GenericDevice> loadGenericDeviceFromJSON(String resourceName, GenericDeviceType type){
        ObjectMapper mapper = new ObjectMapper();
        mapper.setInjectableValues(new InjectableValues.Std().addValue(GenericDeviceType.class, type));
        List<GenericDevice> temp;
        try {
            temp = mapper.readValue(getClass().getResourceAsStream(resourceName), new TypeReference<List<GenericDevice>>() {});
        } catch (IOException e) {
            throw new IllegalStateException("Can't load generic devices from " + resourceName);
        }
        return temp;
    }

    private final List<String> libraryPaths = List.of(
            "library",               // default path for Windows installer and when running from the IDE
            "../Resources/library"   // default path for macOS installer
    );

    public Optional<String> getLibraryPath() {
        return libraryPaths.stream().filter(s -> new File(s).exists()).findFirst();
    }

    private List<ActualDevice> loadActualDeviceList(){
        List<ActualDevice> temp = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        Optional<String> libraryPath = getLibraryPath();
        if (libraryPath.isPresent()) {
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(libraryPath.get(), "devices"))) {
                for (Path deviceDirectory : directoryStream) {
                    Path deviceDefinitionPath = deviceDirectory.resolve("device.json");
                    if (Files.exists(deviceDefinitionPath)) {
                        temp.add(mapper.readValue(deviceDefinitionPath.toFile(), new TypeReference<ActualDevice>() {}));
                    }
                }
                return Collections.unmodifiableList(temp);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.err.println("Found some errors when loading the device library!!!");
        return Collections.emptyList();
    }

    public GenericDevice getGenericDevice(String name) {
        for (GenericDevice genericDevice : allGenericDevice) {
            if (genericDevice.getName().equals(name)) {
                return genericDevice;
            }
        }
        throw new IllegalArgumentException("No generic device named " + name);
    }

    public List<GenericDevice> getGenericSensorDevice() {
        return genericSensorDevice;
    }

    public List<GenericDevice> getGenericActuatorDevice() {
        return genericActuatorDevice;
    }

    public List<GenericDevice> getGenericUtilityDevice() {
        return genericUtilityDevice;
    }

    public List<GenericDevice> getGenericCloudDevice() {
        return genericCloudDevice;
    }

    public List<GenericDevice> getGenericInterfaceDevice() {
        return genericInterfaceDevice;
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
