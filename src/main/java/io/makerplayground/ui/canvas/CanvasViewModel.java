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

package io.makerplayground.ui.canvas;

import io.makerplayground.project.*;
import io.makerplayground.ui.canvas.node.BeginViewModel;
import io.makerplayground.ui.canvas.node.ConditionViewModel;
import io.makerplayground.ui.canvas.node.DelayViewModel;
import io.makerplayground.ui.canvas.node.SceneViewModel;
import io.makerplayground.ui.canvas.helper.DynamicViewModelCreator;

/**
 *
 * Created by tanyagorn on 6/13/2017.
 */
public class CanvasViewModel {
    protected final Project project;
    private final DynamicViewModelCreator<Begin, BeginViewModel> beginViewModel;
    private final DynamicViewModelCreator<Scene, SceneViewModel> SceneViewModel;
    private final DynamicViewModelCreator<Condition, ConditionViewModel> conditionViewModel;
    private final DynamicViewModelCreator<Delay, DelayViewModel> delayViewModel;
    private final DynamicViewModelCreator<Line, LineViewModel> lineViewModel;

    public CanvasViewModel(Project project) {
        this.project = project;
        this.beginViewModel = new DynamicViewModelCreator<>(project.getBegin(), begin -> new BeginViewModel(begin, project));
        this.SceneViewModel = new DynamicViewModelCreator<>(project.getUnmodifiableScene(), scene -> new SceneViewModel(scene, project));
        this.conditionViewModel = new DynamicViewModelCreator<>(project.getUnmodifiableCondition(), condition -> new ConditionViewModel(condition, project));
        this.delayViewModel = new DynamicViewModelCreator<>(project.getUnmodifiableDelay(), delay -> new DelayViewModel(delay, project));
        this.lineViewModel = new DynamicViewModelCreator<>(project.getUnmodifiableLine(), LineViewModel::new);
    }

    public void connect(NodeElement nodeElement1, NodeElement nodeElement2) {
        project.addLine(nodeElement1, nodeElement2);
    }

    public DynamicViewModelCreator<Begin, BeginViewModel> getBeginViewModelCreator() {
        return beginViewModel;
    }

    public DynamicViewModelCreator<Line, LineViewModel> getLineViewModelCreator() {
        return lineViewModel;
    }

    public DynamicViewModelCreator<Condition, ConditionViewModel> getConditionViewModelCreator() {
        return conditionViewModel;
    }

    public DynamicViewModelCreator<Delay, DelayViewModel> getDelayViewModelCreator() {
        return delayViewModel;
    }

    public DynamicViewModelCreator<Scene, SceneViewModel> getSceneViewModelCreator() {
        return SceneViewModel;
    }

    public Project getProject() {
        return project;
    }
}
