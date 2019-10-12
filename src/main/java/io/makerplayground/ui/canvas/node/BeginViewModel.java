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

package io.makerplayground.ui.canvas.node;

import io.makerplayground.project.Begin;
import io.makerplayground.project.NodeElement;
import io.makerplayground.project.Project;
import javafx.beans.property.DoubleProperty;

/**
 * Created by Mai.Manju on 13-Jul-17.
 */
public class BeginViewModel {
    private final Begin begin;
    private final Project project;

    public BeginViewModel(Begin begin, Project project) {
        this.begin = begin;
        this.project = project;

    }

    public Begin getBegin() {
        return begin;
    }

    public double getX() {
        return begin.getLeft();
    }

    public DoubleProperty xProperty() {
        return begin.leftProperty();
    }

    public double getY() {
        return begin.getTop();
    }

    public DoubleProperty yProperty() {
        return begin.topProperty();
    }

    public boolean hasConnectionTo(NodeElement other) {
        return project.hasLine(begin, other);
    }

    public boolean isError() {
        return false;
        // TODO: begin node doesn't get update when other disconnect it
        //return !project.hasLineFrom(begin);
    }
}
