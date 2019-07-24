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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.makerplayground.device.actual.ActualDevice;
import io.makerplayground.device.actual.Property;
import io.makerplayground.device.generic.GenericDevice;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.*;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

@JsonSerialize (using = ProjectDeviceSerializer.class)
@Data @AllArgsConstructor
public class ProjectDevice implements Comparable<ProjectDevice>{
    @JsonIgnore
    public static final ProjectDevice CONTROLLER = new ProjectDevice("Controller", null);

    private String name;
    @Getter @Setter(AccessLevel.NONE) private final GenericDevice genericDevice;

//    public ProjectDevice(String name, GenericDevice genericDevice) {
//        this.name = name;
//        this.genericDevice = genericDevice;
//    }

    @Override
    public int compareTo(ProjectDevice o) {
        return getName().compareTo(o.getName());
    }
}
