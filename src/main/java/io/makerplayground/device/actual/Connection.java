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
import lombok.Builder;
import lombok.Data;

import java.util.Comparator;
import java.util.List;

@Data @Builder
public class Connection implements Comparable<Connection> {

    @JsonIgnore
    private static final Comparator<Connection> comparator = Comparator.comparing(Connection::getName).thenComparing(Connection::getType);

    private final String name;
    private final ConnectionType type;
    private final List<Pin> pins;

    private final ProjectDevice ownerProjectDevice;

    @Override
    public int compareTo(Connection o) {
        return comparator.compare(this, o);
    }
}