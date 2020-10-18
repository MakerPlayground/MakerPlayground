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

import io.makerplayground.device.DeviceLibrary;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import org.controlsfx.control.PopOver;

import java.io.IOException;

public class SceneDeviceIconView extends HBox {

    private final SceneDeviceIconViewModel viewModel;
    private static SceneDevicePropertyWindow devicePropertyWindow;

    @FXML private Label nameIconImageView;
    @FXML private ImageView iconImageView;
    @FXML private Label action;
    @FXML private Button removeStateDeviceBtn;

    public SceneDeviceIconView(SceneDeviceIconViewModel viewModel) {
        this.viewModel = viewModel;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/canvas/node/usersetting/StateDeviceIcon2View.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        nameIconImageView.textProperty().bindBidirectional(viewModel.nameProperty());
        action.setText(viewModel.getAction().getName());
        viewModel.actionProperty().addListener((observable, oldValue, newValue) -> action.setText(newValue.getName()));
        iconImageView.setImage(new Image(DeviceLibrary.getGenericDeviceIconAsStream(viewModel.getGenericDevice())));

        setOnMouseClicked(e -> {
            if (devicePropertyWindow != null && devicePropertyWindow.isShowing()) {
                devicePropertyWindow.hide();
                devicePropertyWindow = null;
            }
            devicePropertyWindow = new SceneDevicePropertyWindow(viewModel);
            devicePropertyWindow.setArrowLocation(PopOver.ArrowLocation.TOP_LEFT);
            devicePropertyWindow.setOnHiding(event -> viewModel.getProject().invalidateDiagram());
            devicePropertyWindow.show(SceneDeviceIconView.this);
        });
    }

    public void setOnRemoved(EventHandler<ActionEvent> e) {
        removeStateDeviceBtn.setOnAction(e);
    }
}
