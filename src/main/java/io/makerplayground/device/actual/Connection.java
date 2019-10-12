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

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.makerplayground.project.ProjectDevice;
import lombok.Data;

import java.util.Comparator;
import java.util.List;

@Data
public class Connection implements Comparable<Connection> {

    @JsonIgnore
    public static final Comparator<Connection> NAME_TYPE_COMPARATOR = Comparator.comparing(Connection::getName).thenComparing(Connection::getType);

    @JsonIgnore
    public static final Comparator<Connection> LESS_PROVIDER_DEPENDENCY = Comparator
            .comparingInt((Connection connection) -> connection.getPins().stream()
                    .map(Pin::getFunction)
                    .mapToInt(pinFunctions -> pinFunctions.stream()
                            // Give penalty value to HW Serial, we not recommend using it
                            .mapToInt(pinFunction -> pinFunction.isHWSerial() ? 3 : 1)
                            .sum())
                    .sum())
            .thenComparing(Connection::getName);

    private final String name;
    private final ConnectionType type;
    private final List<Pin> pins;

    private final ProjectDevice ownerProjectDevice;

    @Override
    public int compareTo(Connection o) {
        return NAME_TYPE_COMPARATOR.compare(this, o);
    }
}
