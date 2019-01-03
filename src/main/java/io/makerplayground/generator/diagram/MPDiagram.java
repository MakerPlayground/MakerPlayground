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
import javafx.geometry.Point2D;
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

    private static final double PADDING_X = 20;
    private static final double PADDING_Y = 20;
    private static final double DEVICE_TO_BOARD_SPACING = 100;
    private static final double DEVICE_SPACING = 20;
    private static final double PIN_PITCH = 11.34;   // 144 pixel per inch / 25.4 mm per inch * 2 mm (pitch of maker playground connector)
    private static final double WIRE_WIDTH = 6;
    private static final List<Color> WIRE_COLOR = List.of(Color.web("#FEE600"), Color.WHITE, Color.web("#EB2427"), Color.BLACK); // yellow, white, red, black
    private static final Map<Side, Double> ANGLE_MAP = new EnumMap<>(Map.of(Side.TOP, 0.0, Side.LEFT, -90.0, Side.BOTTOM, 180.0, Side.RIGHT, 90.0));
    private static final String deviceDirectoryPath = DeviceLibrary.INSTANCE.getLibraryPath().get() + File.separator + "devices";

    private final Map<Side, List<ProjectDevice>> deviceMap = new EnumMap<>(Side.class);
    private final Map<ProjectDevice, Side> deviceSideMap = new HashMap<>();
    private final Map<ProjectDevice, Point2D> devicePositionMap = new HashMap<>();
    private final Map<ProjectDevice, DevicePort> deviceControllerPortMap = new HashMap<>();
    private double controllerOffsetX, controllerOffsetY;

    public MPDiagram(Project project) {
        for (Side s :  Side.values()) {
            deviceMap.put(s, new ArrayList<>());
        }

        ActualDevice controller = project.getController();
        if (controller == null) {
            throw new IllegalStateException();
        }

        // get side (top, left, right, bottom) of each device
        for (ProjectDevice projectDevice : project.getAllDeviceUsed()) {
            if (projectDevice.getActualDevice().getFormFactor() == FormFactor.NONE) {
                continue;
            }

            List<Peripheral> deviceConnectivity = projectDevice.getActualDevice().getConnectivity();
            Map<Peripheral, List<DevicePort>> deviceConnection = projectDevice.getDeviceConnection();
            // TODO: find port that we use more e.g. if D2_1, D2_2 and D1_1 is used, we should calculate based on D2
            List<DevicePort> controllerPortList = deviceConnection.get(deviceConnectivity.get(0));
            DevicePort controllerPort;
            if (controllerPortList.size() == 1 && controllerPortList.get(0).getParent() == null) {
                controllerPort = controllerPortList.get(0);
            } else { // TODO: edit this case to support inex
                controllerPort = controllerPortList.get(0).getParent();
            }

            String controllerPortName = controllerPort.getName();
            if (controllerPortName.equals("Internal")) {    // TODO: replace with integrated device json syntax
                continue;
            }

            Side side = getDeviceSide(controller, controllerPort);
            deviceMap.get(side).add(projectDevice);
            deviceSideMap.put(projectDevice, side);
            deviceControllerPortMap.put(projectDevice, controllerPort);
        }

        // draw controller
        Path controllerImagePath = Paths.get(deviceDirectoryPath,project.getController().getId(), "asset", "device.png");
        try (InputStream controllerImageStream = Files.newInputStream(controllerImagePath)) {
            // left offset is equal to the max height of all devices on the left hand side not the max width because the image
            // need to be rotate CW by 90 degree
            controllerOffsetX = PADDING_X + deviceMap.get(Side.LEFT).stream().map(ProjectDevice::getActualDevice)
                    .mapToDouble(ActualDevice::getHeight).max().orElse(0) + DEVICE_TO_BOARD_SPACING;
            controllerOffsetY = PADDING_Y + deviceMap.get(Side.TOP).stream().map(ProjectDevice::getActualDevice)
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
        left = deviceMap.get(Side.LEFT).stream().map(ProjectDevice::getActualDevice)        // left offset is equal to the max height of all devices on the left hand side
                .mapToDouble(ActualDevice::getHeight).max().orElse(0) + PADDING_X;    // not the max width because the image need to be rotate CW by 90 degree
        bottom = deviceMap.get(Side.TOP).stream().map(ProjectDevice::getActualDevice)
                .mapToDouble(ActualDevice::getHeight).max().orElse(0) + PADDING_Y;
        List<ProjectDevice> topDevice = deviceMap.get(Side.TOP);
        topDevice.sort(Comparator.comparingDouble(o -> deviceControllerPortMap.get(o).getX()));
        for (ProjectDevice device : topDevice) {
            devicePositionMap.put(device, new Point2D(left, bottom - device.getActualDevice().getHeight()));
            drawDevice(device);
            left += device.getActualDevice().getWidth() + DEVICE_SPACING;
        }

        // draw left device
        right = deviceMap.get(Side.LEFT).stream().map(ProjectDevice::getActualDevice)       // left offset is equal to the max height of all devices on the left hand side
                .mapToDouble(ActualDevice::getHeight).max().orElse(0) + PADDING_X;    // not the max width because the image need to be rotate CW by 90 degree
        top = deviceMap.get(Side.TOP).stream().map(ProjectDevice::getActualDevice)
                .mapToDouble(ActualDevice::getHeight).max().orElse(0) + DEVICE_TO_BOARD_SPACING + PADDING_Y;
        List<ProjectDevice> leftDevice = deviceMap.get(Side.LEFT);
        leftDevice.sort(Comparator.comparingDouble(o -> deviceControllerPortMap.get(o).getY()));
        for (ProjectDevice device : leftDevice) {
            devicePositionMap.put(device, new Point2D(right - device.getActualDevice().getHeight()
                    , top + device.getActualDevice().getWidth()));
            drawDevice(device);
            top += device.getActualDevice().getWidth() + DEVICE_SPACING;
        }

        // draw bottom device
        left = deviceMap.get(Side.LEFT).stream().map(ProjectDevice::getActualDevice)        // left offset is equal to the max height of all devices on the left hand side
                .mapToDouble(ActualDevice::getHeight).max().orElse(0) + PADDING_X;    // not the max width because the image need to be rotate CW by 90 degree
        top = deviceMap.get(Side.TOP).stream().map(ProjectDevice::getActualDevice)
                .mapToDouble(ActualDevice::getHeight).max().orElse(0) + DEVICE_TO_BOARD_SPACING + PADDING_Y
                + controller.getHeight() + DEVICE_TO_BOARD_SPACING;
        List<ProjectDevice> bottomDevice = deviceMap.get(Side.BOTTOM);
        bottomDevice.sort(Comparator.comparingDouble(o -> deviceControllerPortMap.get(o).getX()));
        for (ProjectDevice device : bottomDevice) {
            devicePositionMap.put(device, new Point2D(left + device.getActualDevice().getWidth()
                    , top + device.getActualDevice().getHeight()));
            drawDevice(device);
            left += device.getActualDevice().getWidth() + DEVICE_SPACING;
        }

        // draw right device
        left = deviceMap.get(Side.LEFT).stream().map(ProjectDevice::getActualDevice)   // left offset is equal to the max height of all devices on the left hand side
                .mapToDouble(ActualDevice::getHeight).max().orElse(0)            // not the max width because the image need to be rotate CW by 90 degree
                + DEVICE_TO_BOARD_SPACING + controller.getWidth() + DEVICE_TO_BOARD_SPACING + PADDING_X;
        top = deviceMap.get(Side.TOP).stream().map(ProjectDevice::getActualDevice)
                .mapToDouble(ActualDevice::getHeight).max().orElse(0) + DEVICE_TO_BOARD_SPACING + PADDING_Y;
        List<ProjectDevice> rightDevice = deviceMap.get(Side.RIGHT);
        rightDevice.sort(Comparator.comparingDouble(o -> deviceControllerPortMap.get(o).getY()));
        for (ProjectDevice device : deviceMap.get(Side.RIGHT)) {
            devicePositionMap.put(device, new Point2D(left + device.getActualDevice().getHeight(), top));
            drawDevice(device);
            top += device.getActualDevice().getWidth() + DEVICE_SPACING;
        }

        // draw wire
        for (ProjectDevice device : devicePositionMap.keySet()) {
            drawCable(device);
        }
    }

    private Side getDeviceSide(ActualDevice controller, DevicePort port) {
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

    private Point2D getTransformPortLocation(ProjectDevice device, DevicePort port) {
//        drawCircle(devicePositionMap.get(device).getX(), devicePositionMap.get(device).getY(), Color.PINK);
        if (deviceSideMap.get(device) == Side.TOP) {
            return devicePositionMap.get(device).add(port.getX(), port.getY());
        } else if (deviceSideMap.get(device) == Side.LEFT) {
            return devicePositionMap.get(device).add(port.getY(), -port.getX());
        } else if (deviceSideMap.get(device) == Side.BOTTOM) {
            return devicePositionMap.get(device).add(-port.getX(), -port.getY());
        } else {    // RIGHT
            return devicePositionMap.get(device).add(-port.getY(), port.getX());
        }
    }

    private void drawDevice(ProjectDevice device) {
        double left = devicePositionMap.get(device).getX();
        double top = devicePositionMap.get(device).getY();
        double angle = ANGLE_MAP.get(deviceSideMap.get(device));

        Path deviceImagePath = Paths.get(deviceDirectoryPath,device.getActualDevice().getId(), "asset", "device.png");
        try (InputStream deviceImageStream = Files.newInputStream(deviceImagePath)){
            Image deviceImage = new Image(deviceImageStream);
            ImageView deviceImageView = new ImageView(deviceImage);
            deviceImageView.setRotate(angle);
            if (Double.compare(angle, 0) == 0) {
                deviceImageView.setLayoutX(left);
                deviceImageView.setLayoutY(top);
            } else if (Double.compare(angle, 180) == 0) {
                deviceImageView.setLayoutX(left - deviceImage.getWidth());
                deviceImageView.setLayoutY(top - deviceImage.getHeight());
            } else if (Double.compare(angle, 90) == 0) {
                deviceImageView.setLayoutX(left - (deviceImage.getWidth() / 2.0) - (deviceImage.getHeight() / 2.0));
                deviceImageView.setLayoutY(top - (deviceImage.getHeight() / 2.0) + (deviceImage.getWidth() / 2.0));
            } else if (Double.compare(angle, -90) == 0) {
                deviceImageView.setLayoutX(left - (deviceImage.getWidth() / 2.0) + (deviceImage.getHeight() / 2.0));
                deviceImageView.setLayoutY(top - (deviceImage.getHeight() / 2.0) - (deviceImage.getWidth() / 2.0));
            } else {
                throw new IllegalArgumentException();
            }
            getChildren().addAll(deviceImageView);
        } catch (IOException e) {
            throw new IllegalStateException("Image not found : " + deviceDirectoryPath + device.getActualDevice().getId() + "/asset/device.png");
        }
    }

    private void drawCable(ProjectDevice device) {
        List<Peripheral> deviceConnectivity = device.getActualDevice().getConnectivity();
        Map<Peripheral, List<DevicePort>> deviceConnection = device.getDeviceConnection();
        boolean hasConnectedPower = false;

        for (Peripheral peripheral : deviceConnectivity) {
            List<DevicePort> devicePort = device.getActualDevice().getPort(peripheral);
            List<DevicePort> controllerPort = deviceConnection.get(peripheral);
            if (devicePort.size() != controllerPort.size()) {
                throw new IllegalStateException("");
            }

            if (devicePort.size() == 1 && devicePort.get(0).getType() == DevicePort.Type.MP     // MP to MP device
                    && controllerPort.size() == 1 && controllerPort.get(0).getType() == DevicePort.Type.MP) {
                drawMPToMPConnector(device, devicePort.get(0), controllerPort.get(0));
            } else if ((devicePort.size() == controllerPort.size()) && controllerPort.stream()  // MP to breakout board
                    .allMatch(port -> (port.getParent() != null) && (port.getParent().getType() == DevicePort.Type.MP))) {
                drawPinHeaderToMPSignal(device, devicePort, controllerPort);
                // connect power/gnd for device connected to split port only once in case that the signal wires come from different ports
                if (!hasConnectedPower) {
                    drawPinHeaderToMPPower(device, controllerPort);
                }
                hasConnectedPower = true;
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

    private void drawMPToMPConnector(ProjectDevice device, DevicePort devicePort, DevicePort controllerPort) {
        double startX = getTransformPortLocation(device, devicePort).getX();
        double startY = getTransformPortLocation(device, devicePort).getY();
        double startAngle = ANGLE_MAP.get(deviceSideMap.get(device));
        DevicePort.SubType startType = devicePort.getSubType();
//        drawCircle(startX, startY, Color.BLUE);

        double endX = controllerOffsetX + controllerPort.getX();
        double endY = controllerOffsetY + controllerPort.getY();
        double endAngle = controllerPort.getAngle();
        DevicePort.SubType endType = controllerPort.getSubType();

        for (int i=0; i<4; i++) {
            double centerOffset = PIN_PITCH * (i - 1.5);
            double startFlip = (startType == DevicePort.SubType.RIGHTANGLE_BOTTOM) ? 1 : -1;
            double sx = startX + (centerOffset * Math.cos(Math.toRadians(startAngle)) * startFlip);
            double sy = startY + (centerOffset * Math.sin(Math.toRadians(startAngle)) * startFlip);
//            drawCircle(sx, sy, WIRE_COLOR.get(i));

            double endFlip = (endType == DevicePort.SubType.RIGHTANGLE_TOP) ? 1 : -1;
            double ex = endX + (centerOffset * Math.sin(Math.toRadians(endAngle)) * endFlip);
            double ey = endY + (centerOffset * Math.cos(Math.toRadians(endAngle)) * endFlip);
//            drawCircle(ex, ey, WIRE_COLOR.get(i));

            drawWire(sx, sy, startAngle, ex, ey, i);
        }
    }

    private void drawPinHeaderToMPSignal(ProjectDevice device, List<DevicePort> devicePortList, List<DevicePort> controllerPortList) {
        for (int i=0; i<devicePortList.size(); i++) {
            DevicePort controllerPort = controllerPortList.get(i);
            int cableIndex = (controllerPort.getName().charAt(controllerPort.getName().length() - 1) - '0') - 1;  // TODO: broken
            drawPinHeaderToMPConnector(device, devicePortList.get(i), controllerPort, cableIndex);
        }
    }

    private void drawPinHeaderToMPPower(ProjectDevice device, List<DevicePort> controllerPortList) {
        // controllerPortList should contains only one DevicePort except when the port is an I2C port in this case
        // the parent of those ports should be the same because we only let them use SCL and SDA from the same connector
        if (controllerPortList.stream().map(DevicePort::getParent).distinct().count() != 1) {
            throw new IllegalStateException();
        }
        DevicePort controllerPort = controllerPortList.get(0);
        Optional<DevicePort> powerPort = device.getActualDevice().getPort(Peripheral.POWER).stream().filter(DevicePort::isVcc).findAny();
        powerPort.ifPresent(devicePort -> drawPinHeaderToMPConnector(device, devicePort, controllerPort, 2));
        Optional<DevicePort> gndPort = device.getActualDevice().getPort(Peripheral.POWER).stream().filter(DevicePort::isGnd).findAny();
        gndPort.ifPresent(devicePort -> drawPinHeaderToMPConnector(device, devicePort, controllerPort, 3));
    }

    private void drawPinHeaderToMPConnector(ProjectDevice device, DevicePort start, DevicePort end, int cableIndex) {
        double sx = getTransformPortLocation(device, start).getX();
        double sy = getTransformPortLocation(device, start).getY();
        double startAngle = ANGLE_MAP.get(deviceSideMap.get(device));

        DevicePort parentPort = end.getParent();
        double endX = controllerOffsetX + parentPort.getX();
        double endY = controllerOffsetY + parentPort.getY();
        double endAngle = parentPort.getAngle();
        DevicePort.SubType endType = parentPort.getSubType();

        double centerOffset = PIN_PITCH * (cableIndex - 1.5);
        double endFlip = (endType == DevicePort.SubType.RIGHTANGLE_TOP) ? 1 : -1;
        double ex = endX + (centerOffset * Math.sin(Math.toRadians(endAngle)) * endFlip);
        double ey = endY + (centerOffset * Math.cos(Math.toRadians(endAngle)) * endFlip);

        drawWire(sx, sy, startAngle, ex, ey, cableIndex);
    }

    private void drawWire(double sx, double sy, double startAngle, double ex, double ey, int colorIndex) {
        Polyline line1 = new Polyline();
        line1.setStroke(WIRE_COLOR.get(colorIndex));
        line1.setStrokeLineCap(StrokeLineCap.ROUND);
        line1.setStrokeLineJoin(StrokeLineJoin.ROUND);
        line1.setStrokeWidth(WIRE_WIDTH);
        Polyline line2 = new Polyline();
        line2.setStroke(WIRE_COLOR.get(colorIndex));
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

    private void drawCircle(double left, double top, Color color) {
        Circle c = new Circle();
        c.setCenterX(left);
        c.setCenterY(top);
        c.setRadius(2);
        c.setFill(color);
        getChildren().add(c);
    }
}
