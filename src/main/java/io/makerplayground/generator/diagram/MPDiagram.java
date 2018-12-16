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
import io.makerplayground.device.actual.ActualDevice;
import io.makerplayground.device.actual.DevicePort;
import io.makerplayground.device.actual.FormFactor;
import io.makerplayground.device.actual.Peripheral;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectDevice;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

class MPDiagram extends Pane {
    private enum Side {
        TOP, RIGHT, BOTTOM, LEFT
    }

    private static final double DEVICE_TO_BOARD_SPACING = 100;
    private static final double DEVICE_SPACING = 20;
    private static final double PIN_PITCH = 11.34;   // 144 pixel per inch / 25.4 mm per inch * 2 mm (pitch of maker playground connector)
    private static final double WIRE_WIDTH = 6;
    private static final List<Color> wireColor = List.of(Color.web("#FEE600"), Color.WHITE, Color.web("#EB2427"), Color.BLACK); // yellow, white, red, black
    private static final String deviceDirectoryPath = DeviceLibrary.INSTANCE.getLibraryPath().get() + File.separator + "devices";

    private Map<Side, List<ProjectDevice>> deviceSideMap = new EnumMap<>(Side.class);
    private Map<ProjectDevice, DevicePort> devicePortMap = new HashMap<>();
    private Map<ProjectDevice, DevicePort> deviceControllerPortMap = new HashMap<>();
    private double controllerOffsetX, controllerOffsetY;

    public MPDiagram(Project project) {
        setPrefSize(1000, 1000);

        for (Side s :  Side.values()) {
            deviceSideMap.put(s, new ArrayList<>());
        }

        ActualDevice controller = project.getController();
        if (controller == null) {
            throw new IllegalStateException();
        }

        // create list of devices that are needed to be drawn
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

            List<DevicePort> devicePort = projectDevice.getActualDevice().getPort(deviceConnectivity.get(0));
            if (devicePort.size() != 1) {
                throw new IllegalStateException();
            }

            String controllerPortName = controllerPort.get(0).getName();
            if (controllerPortName.equals("Internal")) {    // TODO: replace with integrated device json syntax
                continue;
            }

            deviceSideMap.get(getPortSide(controller, controllerPort.get(0))).add(projectDevice);
            devicePortMap.put(projectDevice, devicePort.get(0));
            deviceControllerPortMap.put(projectDevice, controllerPort.get(0));
        }

        // draw controller
        Path controllerImagePath = Paths.get(deviceDirectoryPath,project.getController().getId(), "asset", "device.png");
        try (InputStream controllerImageStream = Files.newInputStream(controllerImagePath)) {
            // left offset is equal to the max height of all devices on the left hand side not the max width because the image
            // need to be rotate CW by 90 degree
            controllerOffsetX = deviceSideMap.get(Side.LEFT).stream().map(ProjectDevice::getActualDevice)
                    .mapToDouble(ActualDevice::getHeight).max().orElse(0) + DEVICE_TO_BOARD_SPACING;
            controllerOffsetY = deviceSideMap.get(Side.TOP).stream().map(ProjectDevice::getActualDevice)
                    .mapToDouble(ActualDevice::getHeight).max().orElse(0) + DEVICE_TO_BOARD_SPACING;
            Image controllerImage = new Image(controllerImageStream);
            ImageView controllerImageView = new ImageView(controllerImage);
            controllerImageView.setLayoutX(controllerOffsetX);
            controllerImageView.setLayoutY(controllerOffsetY);
            getChildren().add(controllerImageView);
        } catch (IOException e) {
            throw new IllegalStateException("Image not found : " + deviceDirectoryPath + project.getController().getId() + "asset/MPcontroller.png");
        }

        double left = 0, right = 0, top = 0, bottom = 0;

        // draw top device
        left = deviceSideMap.get(Side.LEFT).stream().map(ProjectDevice::getActualDevice)   // left offset is equal to the max height of all devices on the left hand side
                .mapToDouble(ActualDevice::getHeight).max().orElse(0);               // not the max width because the image need to be rotate CW by 90 degree
        bottom = deviceSideMap.get(Side.TOP).stream().map(ProjectDevice::getActualDevice)
                .mapToDouble(ActualDevice::getHeight).max().orElse(0);
        List<ProjectDevice> topDevice = deviceSideMap.get(Side.TOP);
        topDevice.sort(Comparator.comparingDouble(o -> deviceControllerPortMap.get(o).getX()));
        for (ProjectDevice device : topDevice) {
            drawDevice(device, left, bottom, 0);
            left += device.getActualDevice().getWidth() + DEVICE_SPACING;
        }

        // draw left device
        right = deviceSideMap.get(Side.LEFT).stream().map(ProjectDevice::getActualDevice)   // left offset is equal to the max height of all devices on the left hand side
                .mapToDouble(ActualDevice::getHeight).max().orElse(0);               // not the max width because the image need to be rotate CW by 90 degree
        top = deviceSideMap.get(Side.TOP).stream().map(ProjectDevice::getActualDevice)
                .mapToDouble(ActualDevice::getHeight).max().orElse(0) + DEVICE_TO_BOARD_SPACING;
        List<ProjectDevice> leftDevice = deviceSideMap.get(Side.LEFT);
        leftDevice.sort(Comparator.comparingDouble(o -> deviceControllerPortMap.get(o).getY()));
        for (ProjectDevice device : leftDevice) {
            drawDevice(device, right, top, -90);
            top += device.getActualDevice().getWidth() + DEVICE_SPACING;
        }

        // draw bottom device
        left = deviceSideMap.get(Side.LEFT).stream().map(ProjectDevice::getActualDevice)   // left offset is equal to the max height of all devices on the left hand side
                .mapToDouble(ActualDevice::getHeight).max().orElse(0);               // not the max width because the image need to be rotate CW by 90 degree
        top = deviceSideMap.get(Side.TOP).stream().map(ProjectDevice::getActualDevice)
                .mapToDouble(ActualDevice::getHeight).max().orElse(0) + DEVICE_TO_BOARD_SPACING
                + controller.getHeight() + DEVICE_TO_BOARD_SPACING;
        List<ProjectDevice> bottomDevice = deviceSideMap.get(Side.BOTTOM);
        bottomDevice.sort(Comparator.comparingDouble(o -> deviceControllerPortMap.get(o).getX()));
        for (ProjectDevice device : bottomDevice) {
            drawDevice(device, left, top, 180);
            left += device.getActualDevice().getWidth() + DEVICE_SPACING;
        }

        // draw right device
        left = deviceSideMap.get(Side.LEFT).stream().map(ProjectDevice::getActualDevice)   // left offset is equal to the max height of all devices on the left hand side
                .mapToDouble(ActualDevice::getHeight).max().orElse(0)                // not the max width because the image need to be rotate CW by 90 degree
                + DEVICE_TO_BOARD_SPACING + controller.getWidth() + DEVICE_TO_BOARD_SPACING;
        top = deviceSideMap.get(Side.TOP).stream().map(ProjectDevice::getActualDevice)
                .mapToDouble(ActualDevice::getHeight).max().orElse(0) + DEVICE_TO_BOARD_SPACING;
        List<ProjectDevice> rightDevice = deviceSideMap.get(Side.RIGHT);
        rightDevice.sort(Comparator.comparingDouble(o -> deviceControllerPortMap.get(o).getY()));
        for (ProjectDevice device : deviceSideMap.get(Side.RIGHT)) {
            drawDevice(device, left, top, 90);
            top += device.getActualDevice().getWidth() + DEVICE_SPACING;
        }
    }

    private Side getPortSide(ActualDevice controller, DevicePort port) {
        Side side = Side.TOP;
        double distance = port.getY();
        if (port.getX() < distance) {
            side = Side.LEFT;
            distance = port.getX();
        }
        if (controller.getWidth() - port.getX() < distance) {
            side = Side.RIGHT;
            distance = controller.getWidth() - port.getX();
        }
        if (controller.getHeight() - port.getY() < distance) {
            side = Side.BOTTOM;
            distance = controller.getHeight() - port.getY();
        }
        return side;
    }

    private void drawDevice(ProjectDevice device, double left, double top, double angle) {
//        drawCircle(left, top, Color.RED);

        Path deviceImagePath = Paths.get(deviceDirectoryPath,device.getActualDevice().getId(), "asset", "device.png");
        try (InputStream deviceImageStream = Files.newInputStream(deviceImagePath)){
            // draw device
            Image deviceImage = new Image(deviceImageStream);
            ImageView deviceImageView = new ImageView(deviceImage);
            deviceImageView.setRotate(angle);
            if (Double.compare(angle, 0) == 0) {
                deviceImageView.setLayoutX(left);
                deviceImageView.setLayoutY(top - deviceImage.getHeight());
            } else if (Double.compare(angle, 180) == 0) {
                deviceImageView.setLayoutX(left);
                deviceImageView.setLayoutY(top);
            } else if (Double.compare(angle, 90) == 0) {
                deviceImageView.setLayoutX(left - (deviceImage.getWidth() / 2.0) + (deviceImage.getHeight() / 2.0));
                deviceImageView.setLayoutY(top - (deviceImage.getHeight() / 2.0) + (deviceImage.getWidth() / 2.0));
            } else if (Double.compare(angle, -90) == 0) {
                deviceImageView.setLayoutX(left - (deviceImage.getWidth() / 2.0) - (deviceImage.getHeight() / 2.0));
                deviceImageView.setLayoutY(top - (deviceImage.getHeight() / 2.0) + (deviceImage.getWidth() / 2.0));
            } else {
                throw new IllegalArgumentException();
            }
            getChildren().addAll(deviceImageView);

            // draw wire
            double wireStartX, wireStartY;
            if (Double.compare(angle, 0) == 0 || Double.compare(angle, 180) == 0) {
                wireStartX = left + deviceImage.getWidth() / 2.0;
                wireStartY = top;
            } else { // 90, -90
                wireStartX = left;
                wireStartY = top + deviceImage.getWidth() / 2.0;
            }

            DevicePort controllerPort = deviceControllerPortMap.get(device);
            DevicePort devicePort = devicePortMap.get(device);
            drawWire(wireStartX, wireStartY, angle, devicePort.getSubType()
                    , controllerOffsetX + controllerPort.getX(), controllerOffsetY + controllerPort.getY()
                    , controllerPort.getAngle(), controllerPort.getSubType());

//            drawCircle(wireStartX, wireStartY, Color.RED);
//            drawCircle(controllerOffsetX + controllerPort.getX(), controllerOffsetY + controllerPort.getY(), Color.RED);
        } catch (IOException e) {
            throw new IllegalStateException("Image not found : " + deviceDirectoryPath + device.getActualDevice().getId() + "/asset/device.png");
        }
    }

    private void drawWire(double startX, double startY, double startAngle, DevicePort.SubType startType
            , double endX, double endY, double endAngle, DevicePort.SubType endType) {
        for (int i=0; i<4; i++) {
            double centerOffset = PIN_PITCH * (i - 1.5);
            double startFlip = (startType == DevicePort.SubType.RIGHTANGLE_BOTTOM) ? 1 : -1;
            double sx = startX + (centerOffset * Math.cos(Math.toRadians(startAngle)) * startFlip);
            double sy = startY + (centerOffset * Math.sin(Math.toRadians(startAngle)) * startFlip);
//            drawCircle(sx, sy, wireColor.get(i));

            double endFlip = (endType == DevicePort.SubType.RIGHTANGLE_TOP) ? 1 : -1;
            double ex = endX + (centerOffset * Math.sin(Math.toRadians(endAngle)) * endFlip);
            double ey = endY + (centerOffset * Math.cos(Math.toRadians(endAngle)) * endFlip);
//            drawCircle(ex, ey, wireColor.get(i));

            Polyline line1 = new Polyline();
            line1.setStroke(wireColor.get(i));
            line1.setStrokeLineCap(StrokeLineCap.ROUND);
            line1.setStrokeLineJoin(StrokeLineJoin.ROUND);
            line1.setStrokeWidth(WIRE_WIDTH);
            Polyline line2 = new Polyline();
            line2.setStroke(wireColor.get(i));
            line2.setStrokeLineCap(StrokeLineCap.ROUND);
            line2.setStrokeLineJoin(StrokeLineJoin.ROUND);
            line2.setStrokeWidth(WIRE_WIDTH);
            if (Double.compare(startAngle, 0) == 0 || Double.compare(startAngle, 180) == 0) {
                line1.getPoints().addAll(sx, sy, sx, (ey-sy)/2+sy, (ex-sx)/2+sx, (ey-sy)/2+sy);
                line2.getPoints().addAll((ex-sx)/2+sx, (ey-sy)/2+sy, ex, (ey-sy)/2+sy, ex, ey);
            } else {
                line1.getPoints().addAll(sx, sy, (ex-sx)/2+sx, sy, (ex-sx)/2+sx, (ey-sy)/2+sy);
                line2.getPoints().addAll((ex-sx)/2+sx, (ey-sy)/2+sy, (ex-sx)/2+sx, ey, ex, ey);
            }
            getChildren().addAll(line1, line2);
        }
    }

//    private void drawCircle(double left, double top, Color color) {
//        Circle c = new Circle();
//        c.setCenterX(left);
//        c.setCenterY(top);
//        c.setRadius(2);
//        c.setFill(color);
//        getChildren().add(c);
//    }
}
