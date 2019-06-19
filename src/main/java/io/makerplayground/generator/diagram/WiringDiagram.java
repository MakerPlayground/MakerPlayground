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

package io.makerplayground.generator.diagram;

import io.makerplayground.project.Project;
import javafx.scene.layout.Pane;

public class WiringDiagram {

    // TODO: this method and the wiring_method json field are deprecated in flavor of the unified connection diagram in the next release
    public static Pane make(Project project) {
        Pane wiringDiagram;
        switch(project.getController().getWiringMethod()) {
            case WIRE_AND_BREADBOARD:
            case KIDBRIGHT:
                wiringDiagram = new WireAndBreadboardDiagram(project);
                break;
            case MP:
            case INEX:
            case GROVE:
                wiringDiagram = new JSTDiagram(project);
                break;
            default:
                throw new IllegalStateException("Wiring method not found");
        }
        return wiringDiagram;
    }
}
