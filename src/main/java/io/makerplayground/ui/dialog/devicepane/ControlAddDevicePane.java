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

package io.makerplayground.ui.dialog.devicepane;

import io.makerplayground.device.GenericDevice;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by tanyagorn on 6/20/2017.
 */
public class ControlAddDevicePane extends VBox {
    private GenericDevice genericDevice;
    private int count;
    @FXML private VBox controllAddDevicePane;
    @FXML private ImageView imageView;
    @FXML private Label nameLabel;
    @FXML private Button incBtn;
    @FXML private Button decBtn;
    @FXML private TextField numberTextField;

    public ControlAddDevicePane(GenericDevice genericDevice) {
        this.genericDevice = genericDevice;
        this.count = 0;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/dialog/devicepane/ControlAddDevicePane.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        InputStream imageStream = getClass().getResourceAsStream("/icons/colorIcons-3/" + genericDevice.getName() + ".png");
        if (imageStream == null) {
            throw new IllegalStateException("Missing icon of " + genericDevice.getName());
        }

        imageView.setImage(new Image(imageStream));
        nameLabel.setText(genericDevice.getName());

        String text = genericDevice.getDescription();
        text = text.replaceAll("\\\\n", "\r\n");
        text = text.replaceAll("\\\\t", "\t");
        Tooltip tooltip = new Tooltip(text);
        tooltip.setShowDelay(Duration.millis(100));
        Tooltip.install(imageView, tooltip);

        numberTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                count = Integer.parseInt(numberTextField.getText());
            }
        });

        decBtn.setOnAction(event -> {
            if (count > 0) {
                count = count - 1;
            }
            numberTextField.setText(String.valueOf(count));
        });

        incBtn.setOnAction(event -> {
            count = count + 1;
            numberTextField.setText(String.valueOf(count));
        });
    }

    public GenericDevice getGenericDevice() {
        return genericDevice;
    }

    public int getCount() {
        return count;
    }
}