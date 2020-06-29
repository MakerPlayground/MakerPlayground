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

package io.makerplayground.project;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.makerplayground.device.generic.GenericDevice;
import javafx.beans.property.SimpleStringProperty;

import java.util.Comparator;

@JsonSerialize (using = ProjectDeviceSerializer.class)
@JsonDeserialize (using = ProjectDeviceDeserializer.class)
public class ProjectDevice {

    public static final Comparator<ProjectDevice> NAME_COMPARATOR = Comparator.comparing(ProjectDevice::getName);

    @JsonIgnore
    public static final ProjectDevice CONTROLLER = new ProjectDevice("Controller", null);

    @JsonIgnore
    private final SimpleStringProperty nameProperty = new SimpleStringProperty();

    private final GenericDevice genericDevice;

    public ProjectDevice(String name, GenericDevice genericDevice) {
        this.nameProperty.set(name);
        this.genericDevice = genericDevice;
    }

    public SimpleStringProperty NameProperty() {
        return nameProperty;
    }

    public String getName() {
        return nameProperty.get();
    }

    void setName(String name) {
        nameProperty.set(name);
    }

    public GenericDevice getGenericDevice() {
        return this.genericDevice;
    }

    public String toString() {
        return "ProjectDevice(nameProperty=" + this.getName() + ", genericDevice=" + this.getGenericDevice() + ")";
    }
}
