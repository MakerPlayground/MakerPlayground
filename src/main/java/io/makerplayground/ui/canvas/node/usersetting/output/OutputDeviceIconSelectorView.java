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

import io.makerplayground.project.ProjectDevice;
import io.makerplayground.util.PathUtility;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.io.IOException;

/**
 * Created by tanyagorn on 6/26/2017.
 */
public class OutputDeviceIconSelectorView extends VBox {
    ProjectDevice projectDevice;
    @FXML private ImageView imv;
    @FXML private Label name;
    public OutputDeviceIconSelectorView(ProjectDevice projectDevice) {
        this.projectDevice = projectDevice;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/canvas/node/usersetting/output/OutputDeviceIconSelector.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Image image = new Image(PathUtility.getGenericDeviceIconAsStream(projectDevice.getGenericDevice()));
        imv.setImage(image);

        name.setText(projectDevice.getName());
    }
}
