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

import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.device.actual.DevicePort;
import io.makerplayground.device.actual.FormFactor;
import io.makerplayground.device.actual.Peripheral;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectDevice;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

class MPRectangleTinyDiagram extends Pane {
    private static final Point2D BASEBOARD_CENTER_POSITION = new Point2D(400, 300);

    private static final String TOP = "D2";
    private static final String SECOND_TOP = "D1";
    private static final String SECOND_BUTTOM = "A1";
    private static final String BUTTOM = "I2C/D3";

    private static final Map<String, Point2D> WIRE_POSITION = Map.ofEntries(
            Map.entry(TOP, new Point2D(131, 14)),
            Map.entry(SECOND_TOP, new Point2D(131, 41)),
            Map.entry(SECOND_BUTTOM, new Point2D(131, 69)),
            Map.entry(BUTTOM, new Point2D(131, 94))
    );
    private static final Map<String, Point2D> DEVICE_POSITION = Map.ofEntries(
            Map.entry(TOP, new Point2D(0, -130)),
            Map.entry(SECOND_TOP, new Point2D(150, -80)),
            Map.entry(SECOND_BUTTOM, new Point2D(150, 80)),
            Map.entry(BUTTOM, new Point2D(0, 130))
    );

    public MPRectangleTinyDiagram(Project project) {
        setPrefSize(1000, 800);

        String deviceDirectoryPath = DeviceLibrary.INSTANCE.getLibraryPath().get() + File.separator + "devices";

        // draw controller
        Path controllerImagePath = Paths.get(deviceDirectoryPath,project.getController().getId(),"asset","MPcontroller.png");
        try (InputStream controllerImageStream = Files.newInputStream(controllerImagePath)) {
            Image controllerImage = new Image(controllerImageStream);
            ImageView controllerImageView = new ImageView(controllerImage);
            controllerImageView.setLayoutX(BASEBOARD_CENTER_POSITION.getX() - (controllerImage.getWidth() / 2.0));
            controllerImageView.setLayoutY(BASEBOARD_CENTER_POSITION.getY() - (controllerImage.getHeight() / 2.0));
            getChildren().add(controllerImageView);

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

                    double startX = BASEBOARD_CENTER_POSITION.getX() + WIRE_POSITION.get(controllerPortName).getX() - (controllerImage.getWidth() / 2.0);
                    double startY = BASEBOARD_CENTER_POSITION.getY() + WIRE_POSITION.get(controllerPortName).getY() - (controllerImage.getHeight() / 2.0);

                    // draw device
                    Point2D devicePosition = DEVICE_POSITION.get(controllerPortName);
                    Path deviceImagePath = Paths.get(deviceDirectoryPath,projectDevice.getActualDevice().getId(),"asset","MPdevice.png");
                    try (InputStream deviceImageStream = Files.newInputStream(deviceImagePath)){
                        Image deviceImage = new Image(deviceImageStream);
                        ImageView deviceImageView = new ImageView(deviceImage);

                        if (TOP.equals(controllerPortName)) {
                            deviceImageView.setRotate(180);
                            double endX = BASEBOARD_CENTER_POSITION.getX() + DEVICE_POSITION.get(controllerPortName).getX() + (deviceImage.getWidth() / 2.0);
                            double endY = BASEBOARD_CENTER_POSITION.getY() + DEVICE_POSITION.get(controllerPortName).getY() + (deviceImage.getHeight() / 2.0);

                            Line line = new Line(startX, startY, endX, endY);
                            line.setFill(Color.BLUE);
                            line.setStrokeWidth(3);
                            getChildren().add(line);
                        }
                        else if (SECOND_TOP.equals(controllerPortName)) {
                            deviceImageView.setRotate(-90);
                            double endX = BASEBOARD_CENTER_POSITION.getX() + DEVICE_POSITION.get(controllerPortName).getX();
                            double endY = BASEBOARD_CENTER_POSITION.getY() + DEVICE_POSITION.get(controllerPortName).getY();

                            Line line = new Line(startX, startY, endX, endY);
                            line.setFill(Color.BLUE);
                            line.setStrokeWidth(3);
                            getChildren().add(line);
                        }
                        else if (SECOND_BUTTOM.equals(controllerPortName)) {
                            deviceImageView.setRotate(-90);
                            double endX = BASEBOARD_CENTER_POSITION.getX() + DEVICE_POSITION.get(controllerPortName).getX();
                            double endY = BASEBOARD_CENTER_POSITION.getY() + DEVICE_POSITION.get(controllerPortName).getY();

                            Line line = new Line(startX, startY, endX, endY);
                            line.setFill(Color.BLUE);
                            line.setStrokeWidth(3);
                            getChildren().add(line);
                        }
                        else if (BUTTOM.equals(controllerPortName)) {
                            deviceImageView.setRotate(0);
                            double endX = BASEBOARD_CENTER_POSITION.getX() + DEVICE_POSITION.get(controllerPortName).getX() + (deviceImage.getWidth() / 2.0);
                            double endY = BASEBOARD_CENTER_POSITION.getY() + DEVICE_POSITION.get(controllerPortName).getY() - (deviceImage.getHeight() / 2.0);

                            Line line = new Line(startX, startY, endX, endY);
                            line.setFill(Color.BLUE);
                            line.setStrokeWidth(3);
                            getChildren().add(line);
                        }
                        else {
                            throw new IllegalStateException("Can't draw line for controller port name: " + controllerPortName);
                        }
                        deviceImageView.setLayoutX(BASEBOARD_CENTER_POSITION.getX() + devicePosition.getX() + (deviceImage.getHeight() / 2.0 - deviceImage.getWidth() / 2.0));
                        deviceImageView.setLayoutY(BASEBOARD_CENTER_POSITION.getY() + devicePosition.getY() - (deviceImage.getHeight() / 2.0 - deviceImage.getWidth() / 2.0) - deviceImage.getWidth() / 2.0);

                        getChildren().addAll(deviceImageView);
                    } catch (IOException e) {
                        throw new IllegalStateException("Image not found : " + deviceDirectoryPath + projectDevice.getActualDevice().getId() + "/asset/MPdevice.png");
                    }
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Image not found : " + deviceDirectoryPath + project.getController().getId() + "asset/MPcontroller.png");
        }
    }
}
