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

package io.makerplayground.ui.dialog.devicepane.input;

import io.makerplayground.project.UserSetting;
import io.makerplayground.ui.canvas.node.ConditionViewModel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import org.controlsfx.control.PopOver;

import java.io.IOException;

/**
 * Created by USER on 05-Jul-17.
 */
public class InputDeviceSelector extends PopOver {
        @FXML FlowPane flowPane;
    public InputDeviceSelector(ConditionViewModel viewModel) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/dialog/devicepane/input/InputDeviceSelector.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        viewModel.getProjectInputDevice().stream().filter(device -> {
            for (UserSetting userSetting : viewModel.getConditionDevice()) {
                if (userSetting.getProjectDevice() == device) {
                    return false;
                }
            }
            return true;
        }).forEachOrdered(device -> {
            InputDeviceIconSelectorView inputIconView = new InputDeviceIconSelectorView(device);
            flowPane.getChildren().add(inputIconView);
            inputIconView.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                viewModel.getCondition().addDevice(device);
                flowPane.getChildren().remove(inputIconView);
            });
        });
    }

}
