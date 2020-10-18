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

package io.makerplayground.ui;

import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.device.generic.GenericDevice;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class DeviceLibraryPanel extends TabPane {

    @FXML private TextField searchTextField;
    @FXML private Accordion deviceLibraryAccordian;
    @FXML private TitledPane actuatorPane;
    @FXML private VBox actuatorVBox;
    @FXML private TitledPane sensorPane;
    @FXML private VBox sensorVBox;
    @FXML private TitledPane utilityPane;
    @FXML private VBox utilityVBox;
    @FXML private TitledPane cloudPane;
    @FXML private VBox cloudVBox;
    @FXML private TitledPane interfacePane;
    @FXML private VBox interfaceVBox;

    private Consumer<GenericDevice> devicePressHandler;

    public DeviceLibraryPanel() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/DeviceLibraryPanel.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (GenericDevice genericDevice : DeviceLibrary.INSTANCE.getGenericActuatorDevice()) {
            DeviceLibraryListCell deviceLibraryListCell = new DeviceLibraryListCell(genericDevice);
            deviceLibraryListCell.setOnAddButtonPressed(event -> {
                if (devicePressHandler != null) {
                    devicePressHandler.accept(genericDevice);
                }
            });
            actuatorVBox.getChildren().add(deviceLibraryListCell);
        }

        for (GenericDevice genericDevice : DeviceLibrary.INSTANCE.getGenericSensorDevice()) {
            DeviceLibraryListCell deviceLibraryListCell = new DeviceLibraryListCell(genericDevice);
            deviceLibraryListCell.setOnAddButtonPressed(event -> {
                if (devicePressHandler != null) {
                    devicePressHandler.accept(genericDevice);
                }
            });
            sensorVBox.getChildren().add(deviceLibraryListCell);
        }

        for (GenericDevice genericDevice : DeviceLibrary.INSTANCE.getGenericUtilityDevice()) {
            DeviceLibraryListCell deviceLibraryListCell = new DeviceLibraryListCell(genericDevice);
            deviceLibraryListCell.setOnAddButtonPressed(event -> {
                if (devicePressHandler != null) {
                    devicePressHandler.accept(genericDevice);
                }
            });
            utilityVBox.getChildren().add(deviceLibraryListCell);
        }

        for (GenericDevice genericDevice : DeviceLibrary.INSTANCE.getGenericCloudDevice()) {
            DeviceLibraryListCell deviceLibraryListCell = new DeviceLibraryListCell(genericDevice);
            deviceLibraryListCell.setOnAddButtonPressed(event -> {
                if (devicePressHandler != null) {
                    devicePressHandler.accept(genericDevice);
                }
            });
            cloudVBox.getChildren().add(deviceLibraryListCell);
        }

        for (GenericDevice genericDevice : DeviceLibrary.INSTANCE.getGenericInterfaceDevice()) {
            DeviceLibraryListCell deviceLibraryListCell = new DeviceLibraryListCell(genericDevice);
            deviceLibraryListCell.setOnAddButtonPressed(event -> {
                if (devicePressHandler != null) {
                    devicePressHandler.accept(genericDevice);
                }
            });
            interfaceVBox.getChildren().add(deviceLibraryListCell);
        }

        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            Predicate<Node> cellPredicate = deviceLibraryListCell -> {
                if (((DeviceLibraryListCell) deviceLibraryListCell).getName().toLowerCase().contains(newValue.toLowerCase())) {
                    deviceLibraryListCell.setVisible(true);
                    deviceLibraryListCell.setManaged(true);
                    return true;
                } else {
                    deviceLibraryListCell.setVisible(false);
                    deviceLibraryListCell.setManaged(false);
                    return false;
                }
            };
            // Implementation note:
            // 1. filter and count can't be replaced by anyMatch as we want the side effect of the predicate
            // 2. the order should be reversed from the display order so that the top-most TitledPane with matched content is expanded
            deviceLibraryAccordian.setExpandedPane(null);
            if (interfaceVBox.getChildren().stream().filter(cellPredicate).count() != 0) {
                interfacePane.setExpanded(true);
            }
            if (cloudVBox.getChildren().stream().filter(cellPredicate).count() != 0) {
                cloudPane.setExpanded(true);
            }
            if (utilityVBox.getChildren().stream().filter(cellPredicate).count() != 0) {
                utilityPane.setExpanded(true);
            }
            if (sensorVBox.getChildren().stream().filter(cellPredicate).count() != 0) {
                sensorPane.setExpanded(true);
            }
            if (actuatorVBox.getChildren().stream().filter(cellPredicate).count() != 0) {
                actuatorPane.setExpanded(true);
            }
        });
    }

    public void setOnDevicePressed(Consumer<GenericDevice> consumer) {
        devicePressHandler = consumer;
    }

    private static class DeviceLibraryListCell extends HBox {
        @FXML private ImageView imageView;
        @FXML private Label nameLabel;
        @FXML private Button addButton;

        public DeviceLibraryListCell(GenericDevice genericDevice) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/DeviceLibraryListCell.fxml"));
            fxmlLoader.setRoot(this);
            fxmlLoader.setController(this);
            try {
                fxmlLoader.load();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            try (InputStream imageStream = DeviceLibrary.getGenericDeviceIconAsStream(genericDevice)) {
                imageView.setImage(new Image(imageStream));
            } catch (NullPointerException | IOException e) {
                throw new IllegalStateException("Missing icon of " + genericDevice.getName());
            }
            nameLabel.setText(genericDevice.getName());
        }

        public void setOnAddButtonPressed(EventHandler<ActionEvent> event) {
            addButton.setOnAction(event);
        }

        public String getName() {
            return nameLabel.getText();
        }
    }
}
