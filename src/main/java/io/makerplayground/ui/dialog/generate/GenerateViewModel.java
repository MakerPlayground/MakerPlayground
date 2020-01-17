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

package io.makerplayground.ui.dialog.generate;

import io.makerplayground.device.actual.ActualDevice;
import io.makerplayground.device.actual.Connection;
import io.makerplayground.project.DeviceConnection;
import io.makerplayground.project.ProjectConfiguration;
import io.makerplayground.generator.source.SourceCodeResult;
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
            var connections = configuration.getUnmodifiableDeviceConnections();
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
    private static final String NEW_LINE = "\n";

    private String generateDescription(DeviceConnection connection) {
        StringBuilder stringBuilder = new StringBuilder();

        Map<Connection, Connection> connectionMap = connection.getConsumerProviderConnections();
        for(Connection consumerConnection : connectionMap.keySet()) {
            Connection providerConnection = connectionMap.get(consumerConnection);
            if (Objects.isNull(consumerConnection) || Objects.isNull(providerConnection)) {
                continue;
            }
            stringBuilder.append("(");
            stringBuilder.append(consumerConnection.getOwnerProjectDevice().getName()).append("'s ").append(consumerConnection.getName());
            stringBuilder.append(" -> ");
            stringBuilder.append(providerConnection.getOwnerProjectDevice().getName()).append("'s ").append(providerConnection.getName());
            stringBuilder.append(")");
            stringBuilder.append(NEW_LINE);
        }
        return stringBuilder.toString().strip();
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
