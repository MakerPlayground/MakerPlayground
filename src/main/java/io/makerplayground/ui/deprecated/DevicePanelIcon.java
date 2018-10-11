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

package io.makerplayground.ui.deprecated;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

/**
 *
 * Created by Nuntipat Narkthong on 6/8/2017 AD.
 */
public class DevicePanelIcon extends AnchorPane {

    private final DevicePanelIconViewModel viewModel;

    @FXML private ImageView imageView;
    @FXML private TextField nameTextField;
    @FXML private Button removeButton;

    public DevicePanelIcon(DevicePanelIconViewModel viewModel) {
        this.viewModel = viewModel;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/deprecated/DevicePanelIcon.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        imageView.setImage(new Image(getClass().getResourceAsStream("/icons/colorIcons-3/" + viewModel.getDeviceName() + ".png")));
        //nameTextField.textProperty().bindBidirectional(viewModel.nameProperty());
        nameTextField.setText(viewModel.getDevice().getName());

        nameTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                // Do not allow device's name to be duplicated or empty
                if (nameTextField.getText().isEmpty() || viewModel.isNameDuplicate(nameTextField.getText())) {
                    nameTextField.setText(viewModel.getDevice().getName());
                } else { // write change to model
                    viewModel.getDevice().setName(nameTextField.getText());
                }
            }
        });
    }

    public void setOnAction(EventHandler<ActionEvent> event) {
        removeButton.setOnAction(event);
    }
}
