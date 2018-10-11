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

package io.makerplayground.generator.diagram;

import io.makerplayground.device.DevicePort;
import io.makerplayground.helper.FormFactor;
import io.makerplayground.helper.Peripheral;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectDevice;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

class MPHexagonDiagram extends Pane {
    private static final Point2D BASEBOARD_CENTER_POSITION = new Point2D(500, 400);
    private static final Map<String, Point2D> WIRE_POSITION = Map.ofEntries(
            Map.entry("D1", new Point2D(248, 433)),
            Map.entry("D2", new Point2D(248, 380)),
            Map.entry("D3", new Point2D(254, 274)),
            Map.entry("D4", new Point2D(250, 170)),
            Map.entry("D5/A1", new Point2D(255, 54)),
            Map.entry("D6/A2", new Point2D(510, 54)),
            Map.entry("I2C (#1)", new Point2D(590, 170)),
            Map.entry("I2C (#2)", new Point2D(620, 274)),
            Map.entry("I2C (#3)", new Point2D(620, 380)),
            Map.entry("I2C (#4)", new Point2D(590, 433)),
            Map.entry("D7/A3", new Point2D(590, 170)),
            Map.entry("D8/I2C1", new Point2D(620, 274)),
            Map.entry("I2C0 (#1)", new Point2D(620, 380)),
            Map.entry("I2C0 (#2)", new Point2D(590, 433))
    );
    private static final Map<String, Point2D> DEVICE_POSITION = Map.ofEntries(
            Map.entry("D1", new Point2D(270, 521)),
            Map.entry("D2", new Point2D(270, 418)),
            Map.entry("D3", new Point2D(270, 292)),
            Map.entry("D4", new Point2D(270, 189)),
            Map.entry("D5/A1", new Point2D(270, 72)),
            Map.entry("D6/A2", new Point2D(730, 72)),
            Map.entry("I2C (#1)", new Point2D(730, 189)),
            Map.entry("I2C (#2)", new Point2D(730, 292)),
            Map.entry("I2C (#3)", new Point2D(730, 418)),
            Map.entry("I2C (#4)", new Point2D(730, 521)),
            Map.entry("D7/A3", new Point2D(730, 189)),
            Map.entry("D8/I2C1", new Point2D(730, 292)),
            Map.entry("I2C0 (#1)", new Point2D(730, 418)),
            Map.entry("I2C0 (#2)", new Point2D(730, 521))
    );
    private static final List<String> LEFT_PORT_NAME = List.of("D1", "D2", "D3", "D4", "D5/A1");
    private static final List<String> RIGHT_PORT_NAME = List.of("D6/A2", "I2C (#1)", "I2C (#2)", "I2C (#3)", "I2C (#4)", "D7/A3", "D8/I2C1", "I2C0 (#1)", "I2C0 (#2)");

    public MPHexagonDiagram(Project project) {
        setPrefSize(1000, 600);

        // draw all devices
        for (ProjectDevice projectDevice : project.getAllDeviceUsed()) {
            if (projectDevice.getActualDevice().getFormFactor() == FormFactor.NONE) {
                continue;
            }
            List<Peripheral> deviceConnectivity = projectDevice.getActualDevice().getConnectivity();
            if (deviceConnectivity.size() != 1) {
                throw new IllegalStateException();
            }

            Map<Peripheral, List<DevicePort>> deviceConnection = projectDevice.getDeviceConnection();
            if (deviceConnection.keySet().size() != 1) {
                throw new IllegalStateException();
            }

            List<DevicePort> controllerPort = deviceConnection.get(deviceConnectivity.get(0));
            if (controllerPort.size() != 1) {
                throw new IllegalStateException();
            }

            String controllerPortName = controllerPort.get(0).getName();

            // draw wire
            if (!controllerPortName.equals("Internal")) {
                String wireImageFileName = "/wiring/MP_HEXAGON/MP_WIRE_" + controllerPortName.replace('/', '-') + ".png";
                InputStream wireImageStream = getClass().getResourceAsStream(wireImageFileName);
                if (wireImageStream == null) {
                    throw new IllegalStateException("Image not found: " + wireImageFileName);
                }
                ImageView wireImageView = new ImageView(new Image(wireImageStream));
                wireImageView.setLayoutX(WIRE_POSITION.get(controllerPortName).getX());
                wireImageView.setLayoutY(WIRE_POSITION.get(controllerPortName).getY());

                getChildren().add(wireImageView);


                // draw device
                Point2D devicePosition = DEVICE_POSITION.get(controllerPortName);
                Path deviceImagePath = Paths.get("library/devices",projectDevice.getActualDevice().getId(),"asset","MPdevice.png");
                try (InputStream deviceImageStream = Files.newInputStream(deviceImagePath)){
                    Image deviceImage = new Image(deviceImageStream);
                    ImageView deviceImageView = new ImageView(deviceImage);
                    if (LEFT_PORT_NAME.contains(controllerPortName)) {
                        deviceImageView.setRotate(90);
                        deviceImageView.setLayoutX(devicePosition.getX() + (deviceImage.getHeight() / 2 - deviceImage.getWidth() / 2)
                                - deviceImage.getHeight());
                    } else if (RIGHT_PORT_NAME.contains(controllerPortName)) {
                        deviceImageView.setRotate(-90);
                        deviceImageView.setLayoutX(devicePosition.getX() + (deviceImage.getHeight() / 2 - deviceImage.getWidth() / 2));
                    } else {
//                        throw new IllegalStateException("Invalid port");
                    }
                    deviceImageView.setLayoutY(devicePosition.getY() - (deviceImage.getHeight() / 2.0 - deviceImage.getWidth() / 2.0)
                            - deviceImage.getWidth() / 2.0);

                    getChildren().addAll(deviceImageView);
                } catch (IOException e) {
                    throw new IllegalStateException("Image not found : " + "library/devices/" + projectDevice.getActualDevice().getId() + "/asset/MPdevice.png");
                }
            }
        }

        // draw controller
        Path controllerImagePath = Paths.get("library/devices",project.getController().getId(),"asset","MPcontroller.png");
        try (InputStream controllerImageStream = Files.newInputStream(controllerImagePath)) {
            Image controllerImage = new Image(controllerImageStream);
            ImageView controllerImageView = new ImageView(controllerImage);
            controllerImageView.setLayoutX(BASEBOARD_CENTER_POSITION.getX() - (controllerImage.getWidth() / 2.0));
            controllerImageView.setLayoutY(BASEBOARD_CENTER_POSITION.getY() - (controllerImage.getHeight() / 2.0));
            getChildren().add(controllerImageView);
        } catch (IOException e) {
            throw new IllegalStateException("Image not found : " + "library/devices/" + project.getController().getId() + "asset/MPcontroller.png");
        }
    }
}