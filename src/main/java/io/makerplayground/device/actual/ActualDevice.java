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

package io.makerplayground.device.actual;

import io.makerplayground.device.generic.GenericDevice;
import io.makerplayground.project.ProjectDevice;
import lombok.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data @Builder
public class ActualDevice implements Comparable<ActualDevice> {

    public static final Comparator<ActualDevice> NAME_COMPARATOR = Comparator.comparing(ActualDevice::getBrand).thenComparing(ActualDevice::getModel).thenComparing(ActualDevice::getId);

    protected final String id;
    @Setter(AccessLevel.NONE) protected String brand;
    @Setter(AccessLevel.NONE) protected String model;
    protected final String url;
    protected final double width;
    protected final double height;
    protected final String pioBoardId;
    protected final DeviceType deviceType;
    protected final boolean needBreadboard;
    protected final String pinTemplate;
    @Getter(AccessLevel.NONE) protected final List<Connection> connectionProvide;
    @Getter(AccessLevel.NONE) protected final List<Connection> connectionConsume;
    protected final List<Property> property;
    protected final CloudPlatform cloudConsume;

    /* compatibility */
    protected final Map<GenericDevice, Compatibility> compatibilityMap;

    /* cloud that provided to the system by the device */
    protected final Map<CloudPlatform, SourceCodeLibrary> cloudPlatformSourceCodeLibrary;

    /* name of the device implementation class */
    protected final Map<Platform, SourceCodeLibrary> platformSourceCodeLibrary;

    private final Map<Platform, List<String>> firmwarePath;

    protected final List<IntegratedActualDevice> integratedDevices;
    protected final BreadboardPlacement breadboardPlacement;

    public String getDisplayName() {
        return brand + " " + model;
    }

    public Optional<Property> getProperty(String name) {
        return property.stream().filter(property1 -> property1.getName().equals(name)).findFirst();
    }

    public Set<GenericDevice> getSupportedGenericDevice() {
        return compatibilityMap.keySet();
    }

    public Optional<IntegratedActualDevice> getIntegratedDevices(String name) {
        return integratedDevices.stream().filter(integratedActualDevice -> integratedActualDevice.getId().equals(name)).findFirst();
    }

    public String getMpLibrary(Platform platform) {
        SourceCodeLibrary sourceCodeLibrary = platformSourceCodeLibrary.get(platform);
        if (sourceCodeLibrary != null) {
            return sourceCodeLibrary.getClassName();
        }
        throw new IllegalStateException("The actual device [" + String.join(", ", List.of(id, brand, model)) + "] not support for platform [" + platform.getDisplayName() + "]");
    }

    public List<String> getExternalLibrary(Platform platform) {
        SourceCodeLibrary sourceCodeLibrary = platformSourceCodeLibrary.get(platform);
        if (sourceCodeLibrary != null) {
            return sourceCodeLibrary.getDependency();
        }
        throw new IllegalStateException("The actual device [" + String.join(", ", List.of(id, brand, model)) + "] not support for platform [" + platform.getDisplayName() + "]");
    }

    public String getCloudPlatformLibraryName(CloudPlatform cloudPlatform) {
        SourceCodeLibrary sourceCodeLibrary = cloudPlatformSourceCodeLibrary.get(cloudPlatform);
        if (sourceCodeLibrary != null) {
            return sourceCodeLibrary.getClassName();
        }
        throw new IllegalStateException("The actual device [" + String.join(", ", List.of(id, brand, model)) + "] not support for cloudplatform [" + cloudPlatform.getDisplayName() + "]");
    }

    public List<String> getCloudPlatformLibraryDependency(CloudPlatform cloudPlatform) {
        SourceCodeLibrary sourceCodeLibrary = cloudPlatformSourceCodeLibrary.get(cloudPlatform);
        if (sourceCodeLibrary != null) {
            return sourceCodeLibrary.getDependency();
        }
        throw new IllegalStateException("The actual device [" + String.join(", ", List.of(id, brand, model)) + "] not support for cloudplatform [" + cloudPlatform.getDisplayName() + "]");
    }

    private Stream<Connection> queryConnection(List<Connection> connectionList, ProjectDevice projectDevice, String portName) {
        List<Connection> retVal = connectionList.stream()
                                    .map(port -> {
                                        Connection conn = new Connection(port.getName(), port.getType(), port.getPins(), projectDevice);
                                        conn.setFriendConnections(port.getFriendConnections());
                                        return conn;
                                    })
                                    .collect(Collectors.toList());
        for (Connection conn: retVal) {
            List<Connection> newFriendConnection = conn.getFriendConnections().stream()
                .map(connection -> {
                    Optional<Connection> foundConnection = retVal.stream().filter(connection1 -> Connection.NAME_TYPE_COMPARATOR.compare(connection, connection1) == 0).findFirst();
                    if (foundConnection.isEmpty()) {
                        throw new IllegalStateException("");
                    } else {
                        return foundConnection.get();
                    }
                })
                .collect(Collectors.toUnmodifiableList());
            conn.setFriendConnections(newFriendConnection);
        }
        return retVal.stream().filter(port -> portName == null || port.getName().equals(portName));
    }

    public List<Connection> getConnectionProvideByOwnerDevice(ProjectDevice projectDevice) {
        return queryConnection(connectionProvide, projectDevice, null).collect(Collectors.toList());
    }

    public Optional<Connection> getConnectionProvideByOwnerDevice(ProjectDevice projectDevice, String portName) {
        return queryConnection(connectionProvide, projectDevice, portName).findFirst();
    }

    public List<Connection> getConnectionConsumeByOwnerDevice(ProjectDevice projectDevice) {
        return queryConnection(connectionConsume, projectDevice, null).collect(Collectors.toList());
    }

    public Optional<Connection> getConnectionConsumeByOwnerDevice(ProjectDevice projectDevice, String portName) {
        return queryConnection(connectionConsume, projectDevice, portName).findFirst();
    }

    @Override
    public int compareTo(ActualDevice o) {
        return (getBrand() + getModel()).compareTo(o.getBrand() + o.getModel());
    }
}