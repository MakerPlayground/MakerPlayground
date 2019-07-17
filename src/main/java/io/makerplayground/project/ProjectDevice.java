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

package io.makerplayground.project;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.makerplayground.device.actual.ActualDevice;
import io.makerplayground.device.actual.Property;
import io.makerplayground.device.generic.GenericDevice;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Getter;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

@JsonSerialize (using = ProjectDeviceSerializer.class)
public class ProjectDevice implements Comparable<ProjectDevice>{
    public static final ProjectDevice CONTROLLER = new ProjectDevice("Controller", null);

    private final StringProperty name;
    @Getter private final GenericDevice genericDevice;

    public ProjectDevice(String name, GenericDevice genericDevice, Project project) {
        this.name = new SimpleStringProperty(name);
        this.genericDevice = genericDevice;
    }

    public String getName() {
        return name.get();
    }

    public boolean setName(String name) {
        // name should contain only letters, digits and underscores and cannot start with a digit (we follow the c
        // identifier rule which should be fine for code generate for most platform )
        this.name.set(name);
        return true;
    }

    public StringProperty nameProperty() {
        return name;
    }

    @Override
    public int compareTo(ProjectDevice o) {
        return getName().compareTo(o.getName());
    }
}
