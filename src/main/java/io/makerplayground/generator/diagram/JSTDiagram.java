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
import io.makerplayground.device.actual.*;
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
import java.util.stream.Collectors;

class JSTDiagram extends Pane {
    private enum Side {
        TOP, RIGHT, BOTTOM, LEFT
    }

    private static final double PADDING_X = 20;
    private static final double PADDING_Y = 20;
    private static final double DEVICE_TO_BOARD_SPACING = 100;
    private static final double DEVICE_SPACING = 20;
    private static final double MM_TO_PX = 144 / 25.4;  // 144 pixel per inch / 25.4 mm per inch
    private static final double PITCH_TO_WIDTH = 0.5;   // constant to multiply to pin pitch to calculate each wire width in px
    private static final Map<Side, Double> ANGLE_MAP = new EnumMap<>(Map.of(Side.TOP, 0.0, Side.LEFT, -90.0, Side.BOTTOM, 180.0, Side.RIGHT, 90.0));
    private static final String deviceDirectoryPath = DeviceLibrary.INSTANCE.getLibraryPath().get() + File.separator + "devices";

    private final Map<Side, List<ProjectDevice>> deviceMap = new EnumMap<>(Side.class);
    private final Map<ProjectDevice, Side> deviceSideMap = new HashMap<>();
    private final Map<ProjectDevice, Point2D> devicePositionMap = new HashMap<>();
    private final Map<ProjectDevice, DevicePort> deviceControllerPortMap = new HashMap<>();
    private double controllerOffsetX, controllerOffsetY;
    private static final List<Color> colorSet = Arrays.asList(Color.BLUE, Color.HOTPINK, Color.ORANGE, Color.GRAY
            , Color.CYAN, Color.PURPLE, Color.DARKBLUE, Color.LIMEGREEN);

    public JSTDiagram(Project project) {
        for (Side s : Side.values()) {
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

            Map<Peripheral, List<DevicePort>> deviceConnection = projectDevice.getDeviceConnection();
            // find port that we use more e.g. if D2_1, D2_2 and D1_1 is used, we should calculate based on D2
            Map<DevicePort, Long> count = deviceConnection.values().stream().flatMap(Collection::stream).distinct()
                    .collect(Collectors.groupingBy(port -> Objects.requireNonNullElse(port.getParent(), port), Collectors.counting()));
            DevicePort controllerPort = Collections.max(count.entrySet(), Map.Entry.comparingByValue()).getKey();

            String controllerPortName = controllerPort.getName();
            if (controllerPortName.equals("Internal")) {    // TODO: remove after we finish migrate to integrated device json syntax
                continue;
            }

            Side side = getDeviceSide(controller, controllerPort);
            deviceMap.get(side).add(projectDevice);
            deviceSideMap.put(projectDevice, side);
            deviceControllerPortMap.put(projectDevice, controllerPort);
        }

        // draw controller
        Path controllerImagePath = Paths.get(deviceDirectoryPath, project.getController().getId(), "asset", "device.png");
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
            drawCable(device, controller);
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

        Path deviceImagePath = Paths.get(deviceDirectoryPath, device.getActualDevice().getId(), "asset", "device.png");
        try (InputStream deviceImageStream = Files.newInputStream(deviceImagePath)) {
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

    private void drawCable(ProjectDevice device, ActualDevice controller) {
//        boolean deviceHasWire = deviceConnectivity.stream().flatMap(p -> device.getActualDevice().getPort(p).stream()).anyMatch(p -> p.getType() == DevicePortType.WIRE);
        List<Peripheral> deviceConnectivity = device.getActualDevice().getConnectivity();
        Map<Peripheral, List<DevicePort>> deviceConnection = device.getDeviceConnection();
        boolean hasConnectedPower = false;

        for (Peripheral peripheral : deviceConnectivity) {
            List<DevicePort> devicePort = device.getActualDevice().getPort(peripheral);
            List<DevicePort> controllerPort = deviceConnection.get(peripheral);
            if (devicePort.size() != controllerPort.size()) {
                throw new IllegalStateException("");
            }

            if (devicePort.size() == 1 && controllerPort.size() == 1
                    && devicePort.get(0).getType().getPinCount() > 1 && controllerPort.get(0).getType().getPinCount() > 1
                    && devicePort.get(0).getType() == controllerPort.get(0).getType()) {   // JST to JST device (works with MP, GROVE and INEX)
                drawJSTToJSTConnector(device, devicePort.get(0), controllerPort.get(0));
            } else if (devicePort.size() == 1 && controllerPort.size() == 1
                    && devicePort.get(0).getType().getPinCount() == 1 && controllerPort.get(0).getType().getPinCount() == 1
                    && devicePort.get(0).getType() == controllerPort.get(0).getType()) {
                drawWireToWire(device, devicePort.get(0), controllerPort.get(0));
                if (!hasConnectedPower) {
                    drawPinHeaderToWirePower(device, controller.getPort(Peripheral.POWER));
                    hasConnectedPower = true;
                }
            } else if (devicePort.size() == 2 && controllerPort.size() == 2
                    && devicePort.get(0).getType().getPinCount() == 1 && controllerPort.get(0).getType().getPinCount() == 3
                    && devicePort.get(1).getType().getPinCount() == 1 && controllerPort.get(1).getType().getPinCount() == 3
                    && devicePort.get(0).getType() != controllerPort.get(0).getType()
                    && devicePort.get(1).getType() != controllerPort.get(1).getType()) {   // JST-INEX I2C and WIRE I2C
                // TODO: JST-INEX I2C and WIRE I2C Connector
            } else if ((devicePort.size() == controllerPort.size()) && controllerPort.stream().allMatch(port -> (port.getParent() != null)
                    && (port.getParent().getType() == DevicePortType.MP))) {   // MP to breakout board (Note that size is usually equal to 1 except when peripheral is I2C
                drawPinHeaderToMPSignal(device, devicePort, controllerPort);
                // connect power/gnd for device connected to split port only once in case that the signal wires come from different ports
                if (!hasConnectedPower) {
                    drawPinHeaderToMPPower(device, controllerPort);
                    hasConnectedPower = true;
                }
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

    private void drawJSTToJSTConnector(ProjectDevice device, DevicePort devicePort, DevicePort controllerPort) {
        if (devicePort.getType() != controllerPort.getType()) {
            throw new IllegalArgumentException("Port must has the same type");
        }

        double startX = getTransformPortLocation(device, devicePort).getX();
        double startY = getTransformPortLocation(device, devicePort).getY();
        double startAngle = ANGLE_MAP.get(deviceSideMap.get(device));
        DevicePortSubType startType = devicePort.getSubType();
//        drawCircle(startX, startY, Color.BLUE);

        double endX = controllerOffsetX + controllerPort.getX();
        double endY = controllerOffsetY + controllerPort.getY();
        double endAngle = controllerPort.getAngle();
        DevicePortSubType endType = controllerPort.getSubType();

        int pinCount = controllerPort.getType().getPinCount();
        double pinPitch = controllerPort.getType().getPinPitch() * MM_TO_PX;
        double wireWidth = pinPitch * PITCH_TO_WIDTH;

        for (int i = 0; i < pinCount; i++) {
            double centerOffset = pinPitch * (i - 1.5);
            double startFlip = (startType == DevicePortSubType.RIGHTANGLE_BOTTOM) ? 1 : -1;
            double sx = startX + (centerOffset * Math.cos(Math.toRadians(startAngle)) * startFlip);
            double sy = startY + (centerOffset * Math.sin(Math.toRadians(startAngle)) * startFlip);
//            drawCircle(sx, sy, WIRE_COLOR.get(i));

            double endFlip = (endType == DevicePortSubType.RIGHTANGLE_TOP) ? 1 : -1;
            double ex = endX + (centerOffset * Math.sin(Math.toRadians(endAngle)) * endFlip);
            double ey = endY + (centerOffset * Math.cos(Math.toRadians(endAngle)) * endFlip);
//            drawCircle(ex, ey, WIRE_COLOR.get(i));

            drawWire(sx, sy, startAngle, ex, ey, controllerPort.getType().getWireColor().get(i), wireWidth);
        }
    }

    private void drawPinHeaderToMPSignal(ProjectDevice device, List<DevicePort> devicePortList, List<DevicePort> controllerPortList) {
        for (int i = 0; i < devicePortList.size(); i++) {
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
        DevicePortSubType endType = parentPort.getSubType();

        double pinPitch = parentPort.getType().getPinPitch() * MM_TO_PX;
        Color color = parentPort.getType().getWireColor().get(cableIndex);
        double wireWidth = pinPitch * PITCH_TO_WIDTH;

        double centerOffset = pinPitch * (cableIndex - 1.5);
        double endFlip = (endType == DevicePortSubType.RIGHTANGLE_TOP) ? 1 : -1;
        double ex = endX + (centerOffset * Math.sin(Math.toRadians(endAngle)) * endFlip);
        double ey = endY + (centerOffset * Math.cos(Math.toRadians(endAngle)) * endFlip);

        drawWire(sx, sy, startAngle, ex, ey, color, wireWidth);
    }

    private void drawPinHeaderToWirePower(ProjectDevice device, List<DevicePort> controllerPortList) {
        // controllerPortList should contains only one DevicePort except when the port is an I2C port in this case
        // the parent of those ports should be the same because we only let them use SCL and SDA from the same connector

        Optional<DevicePort> deviceVccPort = device.getActualDevice().getPort(Peripheral.POWER).stream().filter(DevicePort::isVcc).findAny();
        Optional<DevicePort> controllerVccPort = controllerPortList.stream().filter(DevicePort::isVcc).findAny();
        if (deviceVccPort.isPresent() && controllerVccPort.isPresent()) {
            drawWireToWire(device, deviceVccPort.get(), controllerVccPort.get(),Color.RED);
        }

        Optional<DevicePort> deviceGndPort = device.getActualDevice().getPort(Peripheral.POWER).stream().filter(DevicePort::isGnd).findAny();
        Optional<DevicePort> controllerGndPort = controllerPortList.stream().filter(DevicePort::isGnd).findAny();
        if (deviceGndPort.isPresent() && controllerGndPort.isPresent()) {
            drawWireToWire(device, deviceGndPort.get(), controllerGndPort.get(),Color.BLACK);
        }
    }

    private static final Random random = new Random();
    private void drawWireToWire(ProjectDevice device, DevicePort devicePort,  DevicePort controllerPort) {
        Color randomColor = colorSet.get(random.nextInt(colorSet.size()));
        drawWireToWire(device,devicePort,controllerPort, randomColor);
    }

    private void drawWireToWire(ProjectDevice device, DevicePort devicePort,  DevicePort controllerPort,Color color) {

        double sx = getTransformPortLocation(device, devicePort).getX();
        double sy = getTransformPortLocation(device, devicePort).getY();
        double startAngle = ANGLE_MAP.get(deviceSideMap.get(device));

        double endX = controllerOffsetX + controllerPort.getX();
        double endY = controllerOffsetY + controllerPort.getY();

        drawWire(sx, sy, startAngle, endX, endY, color, 3);
    }

    private void drawWire(double sx, double sy, double startAngle, double ex, double ey, Color color, double wireWidth) {
        Polyline line1 = new Polyline();
        line1.setStroke(color);
        line1.setStrokeLineCap(StrokeLineCap.ROUND);
        line1.setStrokeLineJoin(StrokeLineJoin.ROUND);
        line1.setStrokeWidth(wireWidth);
        Polyline line2 = new Polyline();
        line2.setStroke(color);
        line2.setStrokeLineCap(StrokeLineCap.ROUND);
        line2.setStrokeLineJoin(StrokeLineJoin.ROUND);
        line2.setStrokeWidth(wireWidth);
        if (Double.compare(startAngle, 0) == 0 || Double.compare(startAngle, 180) == 0) {
            line1.getPoints().addAll(sx, sy, sx, (ey - sy) / 2 + sy, (ex - sx) / 2 + sx, (ey - sy) / 2 + sy);
            line2.getPoints().addAll((ex - sx) / 2 + sx, (ey - sy) / 2 + sy, ex, (ey - sy) / 2 + sy, ex, ey);
        } else {
            line1.getPoints().addAll(sx, sy, (ex - sx) / 2 + sx, sy, (ex - sx) / 2 + sx, (ey - sy) / 2 + sy);
            line2.getPoints().addAll((ex - sx) / 2 + sx, (ey - sy) / 2 + sy, (ex - sx) / 2 + sx, ey, ex, ey);
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
