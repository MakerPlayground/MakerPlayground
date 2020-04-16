/*
 * Copyright (c) 2019. The Maker Playground Authors.
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

package io.makerplayground.device;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.makerplayground.device.actual.*;
import io.makerplayground.device.generic.GenericDevice;
import io.makerplayground.util.PathUtility;
import org.apache.commons.io.FilenameUtils;

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
    private List<ActualDevice> actualAndIntegratedDevice;

    DeviceLibrary() {}

    public void loadDeviceFromFiles() {
        this.genericSensorDevice = loadGenericDeviceFromFile("/yaml/genericsensordevice.yaml", GenericDeviceType.SENSOR);
        this.genericActuatorDevice = loadGenericDeviceFromFile("/yaml/genericactuatordevice.yaml", GenericDeviceType.ACTUATOR);
        this.genericUtilityDevice = loadGenericDeviceFromFile("/yaml/genericutilitydevice.yaml", GenericDeviceType.UTILITY);
        this.genericCloudDevice = loadGenericDeviceFromFile("/yaml/genericclouddevice.yaml", GenericDeviceType.CLOUD);
        this.genericInterfaceDevice = loadGenericDeviceFromFile("/yaml/genericinterfacedevice.yaml", GenericDeviceType.INTERFACE);
        this.allGenericDevice = new ArrayList<>();
        this.allGenericDevice.addAll(genericSensorDevice);
        this.allGenericDevice.addAll(genericActuatorDevice);
        this.allGenericDevice.addAll(genericUtilityDevice);
        this.allGenericDevice.addAll(genericCloudDevice);
        this.allGenericDevice.addAll(genericInterfaceDevice);
        Map<String, Map<String, PinTemplate>> pinTemplate = loadPinTemplateList();
        this.actualDevice = loadActualDeviceList(pinTemplate);
        this.actualAndIntegratedDevice = Stream.concat(actualDevice.stream(), actualDevice.stream()
                .flatMap(actualDevice1 -> actualDevice1.getIntegratedDevices().stream()))
                .collect(Collectors.toList());
    }

    private Map<String, Map<String, PinTemplate>> loadPinTemplateList() {
        YAMLMapper mapper = new YAMLMapper();
        Optional<String> libraryPath = getLibraryPath();
        Map<String, Map<String, PinTemplate>> pinTemplateMap = new HashMap<>();
        if (libraryPath.isPresent()) {
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(libraryPath.get(), "pin_templates"))) {
                for (Path pinTemplatePath : directoryStream) {
                    if (Files.exists(pinTemplatePath)) {
                        try {
                            JsonNode rootNode = mapper.readValue(pinTemplatePath.toFile(), JsonNode.class);
                            String filename = FilenameUtils.removeExtension(pinTemplatePath.getFileName().toString());
                            if (!rootNode.has("pins") && !rootNode.get("pins").isArray()) {
                                throw new IllegalStateException("pin template file must has 'pins' and it must be an array");
                            }
                            pinTemplateMap.put(filename, new HashMap<>());
                            List<PinTemplate> pinTemplateList = mapper.readValue(rootNode.get("pins").traverse(), new TypeReference<List<PinTemplate>>() {});
                            pinTemplateList.forEach(pinTemplate -> pinTemplateMap.get(filename).put(pinTemplate.getName(), pinTemplate));
                        } catch (JsonParseException e) {
                            System.err.println("Found some errors when reading pin_template at " + pinTemplatePath.toAbsolutePath());
                        } catch (NullPointerException e) {
                            System.err.println("Found some errors when reading pin_template at " + pinTemplatePath.toAbsolutePath());
                            throw new IllegalStateException(e);
                        }
                    }
                }
                return Collections.unmodifiableMap(pinTemplateMap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return Collections.emptyMap();
    }

    private List<GenericDevice> loadGenericDeviceFromFile(String resourceName, GenericDeviceType type){
        ObjectMapper mapper = new YAMLMapper();
        mapper.setInjectableValues(new InjectableValues.Std().addValue(GenericDeviceType.class, type));
        List<GenericDevice> temp;
        try {
            temp = mapper.readValue(getClass().getResourceAsStream(resourceName), new TypeReference<List<GenericDevice>>() {});
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Can't load generic devices from " + resourceName);
        }
        temp.sort((device1, device2) -> device1.getName().compareToIgnoreCase(device2.getName()));
        return temp;
    }

    private static final List<String> libraryPaths = List.of(
            "library",   // default path when running from the IDE which should override installer path to aid in development
            PathUtility.MP_WORKSPACE + File.separator + "library",  // updated library for each user in user's machine
            PathUtility.MP_INSTALLDIR + File.separator + "dependencies" + File.separator + "library",    // default path for Windows installer (fallback)
            "/Library/Application Support/MakerPlayground/library"   // default path for macOS installer (fallback)
    );

    public static Optional<String> getLibraryPath() {
        return libraryPaths.stream()
                .filter(s -> Files.exists(Path.of(s, "lib")) && Files.exists(Path.of(s, "lib_ext"))
                        && Files.exists(Path.of(s, "pin_templates")))
                .findFirst();
    }

    public static String getDeviceDirectoryPath() {
        if (getLibraryPath().isEmpty()) {
            throw new IllegalStateException("Library Path is missing");
        }
        return getLibraryPath().get() + File.separator + "devices";
    }

    public static Path getDeviceImagePath(ActualDevice actualDevice) {
        String id;
        if (actualDevice instanceof IntegratedActualDevice) {
            id = ((IntegratedActualDevice) actualDevice).getParent().getId();
        } else {
            id = actualDevice.getId();
        }
        // TODO: Should we handle case that the image is missing or let the caller check for path existence?
        return Path.of(DeviceLibrary.getDeviceDirectoryPath(), id, "asset", "device.png");
    }

    public static Path getDeviceThumbnailPath(ActualDevice actualDevice) {
        String id;
        if (actualDevice instanceof IntegratedActualDevice) {
            id = ((IntegratedActualDevice) actualDevice).getParent().getId();
        } else {
            id = actualDevice.getId();
        }
        Path thumbnailPath = Path.of(DeviceLibrary.getDeviceDirectoryPath(), id, "asset", "device_thumbnail.png");
        if (Files.exists(thumbnailPath)) {
            return thumbnailPath;
        } else {
            return getDeviceImagePath(actualDevice);
        }
    }

    private List<ActualDevice> loadActualDeviceList(Map<String, Map<String, PinTemplate>> pinTemplate){
        List<ActualDevice> temp = new ArrayList<>();
        YAMLMapper mapper = new YAMLMapper();
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(ActualDevice.class, new ActualDeviceDeserializer(pinTemplate));
        mapper.registerModule(simpleModule);
        Optional<String> libraryPath = getLibraryPath();
        if (libraryPath.isPresent()) {
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(libraryPath.get(), "devices"))) {
                for (Path deviceDirectory : directoryStream) {
                    Path deviceDefinitionPath = deviceDirectory.resolve("device.yaml");
                    if (Files.exists(deviceDefinitionPath)) {
                        try {
                            ActualDevice actualDevice = mapper.readValue(deviceDefinitionPath.toFile(), ActualDevice.class);
                            temp.add(actualDevice);
                        } catch (JsonParseException e) {
                            System.err.println("Found some errors when reading device at " + deviceDefinitionPath.toAbsolutePath());
                        } catch (NullPointerException e) {
                            System.err.println("Found some errors when reading device at " + deviceDefinitionPath.toAbsolutePath());
                            throw new IllegalStateException(e);
                        }
                    }
                }
                return Collections.unmodifiableList(temp);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return Collections.emptyList();
    }

    public List<GenericDevice> getGenericDevice() {
        return allGenericDevice;
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
        return actualDevice.stream().filter(device -> device.getPlatformSourceCodeLibrary().keySet().contains(platform))
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

    public List<ActualDevice> getActualDevice(DeviceType deviceType) {
        return actualDevice.stream().filter(device -> device.getDeviceType() == deviceType)
                .collect(Collectors.toUnmodifiableList());
    }

    public List<ActualDevice> getActualDevice(GenericDevice genericDevice) {
        return actualDevice.stream().filter(device -> device.getCompatibilityMap().containsKey(genericDevice))
                .collect(Collectors.toUnmodifiableList());
    }

    public List<ActualDevice> getActualAndIntegratedDevice() {
        return actualAndIntegratedDevice;
    }

    public List<ActualDevice> getActualAndIntegratedDevice(GenericDevice genericDevice) {
        return actualAndIntegratedDevice.stream().filter(device -> device.getCompatibilityMap().containsKey(genericDevice))
                .collect(Collectors.toUnmodifiableList());
    }
}
