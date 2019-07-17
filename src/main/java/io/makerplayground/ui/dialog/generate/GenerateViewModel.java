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

package io.makerplayground.ui.dialog.generate;

import io.makerplayground.device.actual.ActualDevice;
import io.makerplayground.device.actual.Pin;
import io.makerplayground.device.actual.Port;
import io.makerplayground.project.ProjectConfiguration;
import io.makerplayground.generator.source.SourceCodeResult;
import io.makerplayground.project.DevicePinPortConnection;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectDevice;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.*;

/**
 * Created by tanyagorn on 7/19/2017.
 */
public class GenerateViewModel {
    private final Project project;
    private final SourceCodeResult code;
    private final ObservableList<TableDataList> observableTableList;

    public GenerateViewModel(Project project, SourceCodeResult code) {
        this.project = project;
        this.code = code;
        this.observableTableList = FXCollections.observableArrayList();

        if (!code.hasError()) {
            ProjectConfiguration configuration = project.getProjectConfiguration();
            var connections = configuration.getUnmodifiableDevicePinPortConnections();
            var deviceMap = configuration.getUnmodifiableDeviceMap();
            for (ProjectDevice device : deviceMap.keySet()) {
                ActualDevice actualDevice = deviceMap.get(device);
                if (connections.containsKey(device)) {
                    var connection = connections.get(device);
                    observableTableList.add(new TableDataList(device.getName(),
                            actualDevice.getBrand(),
                            actualDevice.getModel(),
                            actualDevice.getId(),
                            generateDescription(connection),
                            actualDevice.getUrl())
                    );
                }
            }
        }
    }

    private String generateDescription(DevicePinPortConnection connection) {
        List<String> pinPortNameFrom = new ArrayList<>();
        List<String> pinPortNameTo = new ArrayList<>();
        ProjectConfiguration configuration = project.getProjectConfiguration();
        Optional<ActualDevice> from = configuration.getActualDevice(connection.getFrom());
        Optional<ActualDevice> to = configuration.getActualDevice(connection.getTo());
        if (from.isPresent() && to.isPresent()) {
            Map<Pin, Pin> pinMapFromTo = connection.getPinMapFromTo();
            Map<Port, Port> portMapFromTo = connection.getPortMapFromTo();
            if (Objects.nonNull(pinMapFromTo)) {
                for(Pin pin : pinMapFromTo.keySet()) {
                    pinPortNameFrom.add(pin.getName());
                    pinPortNameTo.add(pinMapFromTo.get(pin).getName());
                }
            }
            if (Objects.nonNull(portMapFromTo)) {
                for(Port port : portMapFromTo.keySet()) {
                    pinPortNameFrom.add(port.getName());
                    pinPortNameTo.add(portMapFromTo.get(port).getName());
                }
            }
            return from.get().getBrand() + "-"+ from.get().getModel() + "(" + String.join(",", pinPortNameFrom) + ") -> "+ to.get().getBrand() + "-" + to.get().getModel() + "(" + String.join(", ", pinPortNameTo);
        }
        return "";
    }

    public Project getProject() {
        return project;
    }

    public String getCode() {
        return code.getCode();
    }

    public boolean hasError() {
        return code.hasError();
    }

    public ObservableList<TableDataList> getObservableTableList() { return observableTableList; }
}
