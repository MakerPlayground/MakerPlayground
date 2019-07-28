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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.makerplayground.project.ProjectDevice;
import lombok.*;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Data @Builder
@JsonDeserialize(using = PinDeserializer.class)
public class Pin implements Comparable<Pin>{

    @JsonIgnore
    private static final Comparator<Pin> comparator = Comparator.comparing(Pin::getName).thenComparing(Pin::getVoltageLevel).thenComparing(Pin::getX).thenComparing(Pin::getY);

    private final String name;
    private final VoltageLevel voltageLevel;
    private final List<PinFunction> function;
    private final PinConnectionType connectionType;
    @ToString.Exclude private final double x;
    @ToString.Exclude private final double y;

    private final ProjectDevice ownerProjectDevice;

    @Override
    public int compareTo(Pin o) {
        return comparator.compare(this, o);
    }
}
