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

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;

/**
 *
 */
@JsonSerialize(using = LineSerializer.class)
public class Line {
    private final Project project;
    private final NodeElement source;
    private final NodeElement destination;
    private final ReadOnlyObjectWrapper<DiagramError> error;

    public Line(NodeElement source, NodeElement destination, Project project) {
        this.project = project;
        this.source = source;
        this.destination = destination;
        this.error = new ReadOnlyObjectWrapper<>(DiagramError.NONE);
    }

    public NodeElement getSource() {
        return source;
    }

    public NodeElement getDestination() {
        return destination;
    }

    public final DiagramError getError() {
        return error.get();
    }

    public final ReadOnlyObjectProperty<DiagramError> errorProperty() {
        return error.getReadOnlyProperty();
    }

    protected DiagramError checkError() {
        return project.getDiagramConnectionStatus().getOrDefault(this, DiagramError.NONE);
    }

    public final void invalidate() {
        error.set(checkError());
    }
}
