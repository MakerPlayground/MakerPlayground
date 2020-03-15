/*
 * Copyright (c) 2020. The Maker Playground Authors.
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

package io.makerplayground.ui.canvas.node.usersetting.output;

import io.makerplayground.ui.canvas.node.SceneViewModel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import org.controlsfx.control.PopOver;

import java.io.IOException;

/**
 * Created by tanyagorn on 6/26/2017.
 */
public class OutputDeviceSelector extends PopOver {
    @FXML FlowPane flowPane;
    public OutputDeviceSelector(SceneViewModel viewModel) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/canvas/node/usersetting/output/OutputDeviceSelector.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        viewModel.getProjectOutputDevice().forEach(device -> {
            OutputDeviceIconSelectorView outputIconView = new OutputDeviceIconSelectorView(device);
            flowPane.getChildren().add(outputIconView);
            outputIconView.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                viewModel.getScene().addDevice(device);
            });
        });
    }
}
