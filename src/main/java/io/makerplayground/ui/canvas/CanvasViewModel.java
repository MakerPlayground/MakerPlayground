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

package io.makerplayground.ui.canvas;

import io.makerplayground.project.*;
import io.makerplayground.ui.canvas.node.BeginSceneViewModel;
import io.makerplayground.ui.canvas.node.ConditionViewModel;
import io.makerplayground.ui.canvas.node.SceneViewModel;
import io.makerplayground.ui.canvas.helper.DynamicViewModelCreator;

/**
 *
 * Created by tanyagorn on 6/13/2017.
 */
public class CanvasViewModel {
    protected final Project project;
    private final DynamicViewModelCreator<Scene, SceneViewModel> paneStateViewModel;
    private final DynamicViewModelCreator<Condition, ConditionViewModel> conditionViewModel;
    private final DynamicViewModelCreator<Line,LineViewModel> lineViewModel;
    private final BeginSceneViewModel beginViewModel;

    public CanvasViewModel(Project project) {
        this.project = project;
        this.paneStateViewModel = new DynamicViewModelCreator<>(project.getScene(), scene -> new SceneViewModel(scene, project));
        this.conditionViewModel = new DynamicViewModelCreator<>(project.getCondition(), condition -> new ConditionViewModel(condition, project));
        this.lineViewModel = new DynamicViewModelCreator<>(project.getLine(), LineViewModel::new);
        this.beginViewModel = new BeginSceneViewModel(project.getBegin(),project);
    }

//    public void connectState(NodeElement nodeElement1, NodeElement nodeElement2) {
//        project.addLine(nodeElement1, nodeElement2);
//    }

    public void connect(NodeElement nodeElement1, NodeElement nodeElement2) {
        project.addLine(nodeElement1, nodeElement2);
    }

    public DynamicViewModelCreator<Line, LineViewModel> getLineViewModel() {
        return lineViewModel;
    }

    public DynamicViewModelCreator<Condition, ConditionViewModel> getConditionViewModel() {
        return conditionViewModel;
    }

    public DynamicViewModelCreator<Scene, SceneViewModel> getPaneStateViewModel() {
        return paneStateViewModel;
    }

    public BeginSceneViewModel getBeginViewModel() {
        return beginViewModel;
    }

    public Project getProject() {
        return project;
    }
}
