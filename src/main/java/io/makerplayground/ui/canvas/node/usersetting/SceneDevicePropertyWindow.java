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

package io.makerplayground.ui.canvas.node.usersetting;


import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import org.controlsfx.control.PopOver;

public class SceneDevicePropertyWindow extends PopOver {

    public SceneDevicePropertyWindow(SceneDeviceIconViewModel viewModel) {
        SceneDevicePropertyPane devicePropertyPane = new SceneDevicePropertyPane(viewModel.getUserSetting(), viewModel.getProject());
        devicePropertyPane.setPadding(new Insets(20, 20, 20, 20));

        setDetachable(false);
        setContentNode(devicePropertyPane);
    }

}
