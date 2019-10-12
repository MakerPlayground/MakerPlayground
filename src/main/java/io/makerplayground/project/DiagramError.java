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

public enum DiagramError {
    NONE(""),
    BEGIN_INVALID_NAME("name shouldn't be empty and should contain only A-Z, a-z, 0-9 and _"),
    DIAGRAM_MULTIPLE_SCENE("there shouldn't be connection from the same node to multiple scene"),
    DIAGRAM_CHAIN_CONDITION("condition can't be connected together"),
    DIAGRAM_CONDITION_IGNORE("this condition will always be ignored"),
    DIAGRAM_MULTIPLE_BEGIN("the scene/condition shouldn't be originated from different begins and tasks."),
    SCENE_INVALID_NAME("name shouldn't be empty and should contain only A-Z, a-z, 0-9 and _"),
    SCENE_DUPLICATE_NAME("duplicate scene name"),
    SCENE_INVALID_PARAM("some parameters are invalid"),
    CONDITION_EMPTY("there isn't any condition"),
    CONDITION_INVALID_PARAM("some parameters are invalid"),
    CONDITION_INVALID_EXPRESSION("some expression are invalid"),
    CONDITION_NO_ENABLE_EXPRESSION("at least one expression should be enabled for each devices"),
    TASK_INVALID_NAME("name shouldn't be empty and should contain only A-Z, a-z, 0-9 and _");

    private String tooltip;

    DiagramError(String tooltip) {
        this.tooltip = tooltip;
    }

    @Override
    public String toString() {
        return tooltip;
    }
}
