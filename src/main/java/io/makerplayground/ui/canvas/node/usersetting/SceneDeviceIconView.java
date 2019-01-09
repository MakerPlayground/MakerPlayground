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

package io.makerplayground.ui.canvas.node.usersetting;

import io.makerplayground.ui.control.AutoResizeTextField;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.controlsfx.control.PopOver;


import java.io.IOException;

/**
 * Created by tanyagorn on 6/12/2017.
 */
public class SceneDeviceIconView extends TitledPane {

    private final SceneDeviceIconViewModel viewModel;
    private static SceneDevicePropertyWindow devicePropertyWindow;

    @FXML private ImageView deviceIcon;
    @FXML private AutoResizeTextField deviceName;
    @FXML private ImageView removeButton;

    public SceneDeviceIconView(SceneDeviceIconViewModel viewModel) {
        this.viewModel = viewModel;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/canvas/node/usersetting/SceneDeviceIcon.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        deviceName.textProperty().bindBidirectional(viewModel.nameProperty());
//        action.setText(viewModel.getAction().getName());
//        viewModel.actionProperty().addListener((observable, oldValue, newValue) -> action.setText(newValue.getName()));
        deviceIcon.setImage(new Image(getClass().getResourceAsStream("/icons/colorIcons-3/" + viewModel.getImageName() + ".png" )));

        SceneDevicePropertyWindow propertyPane = new SceneDevicePropertyWindow(viewModel);
        // save space by don't take into account size of the content when the titlepane is collapsed
        propertyPane.managedProperty().bind(expandedProperty());

        setContent(propertyPane);
    }

    public void setOnRemoved(Runnable r) {
        removeButton.setOnMouseClicked(event -> r.run());
    }
}
