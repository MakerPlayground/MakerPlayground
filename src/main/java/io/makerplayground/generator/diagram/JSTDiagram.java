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

package io.makerplayground.generator.diagram;

import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.device.actual.ActualDevice;
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
//    private enum Side {
//        TOP, RIGHT, BOTTOM, LEFT
//    }
//
//    private static final double PADDING_X = 20;
//    private static final double PADDING_Y = 20;
//    private static final double DEVICE_TO_BOARD_SPACING = 100;
//    private static final double DEVICE_SPACING = 20;
//    private static final double MM_TO_PX = 144 / 25.4;  // 144 pixel per inch / 25.4 mm per inch
//    private static final double PITCH_TO_WIDTH = 0.5;   // constant to multiply to pin pitch to calculate each wire width in px
//    private static final double WIRE_WIDTH = 4;
//    private static final List<Color> WIRE_COLOR = Arrays.asList(Color.BLUE, Color.HOTPINK, Color.ORANGE, Color.GRAY
//            , Color.CYAN, Color.PURPLE, Color.DARKBLUE, Color.LIMEGREEN);
//    private static final Color POWER_WIRE_COLOR = Color.RED;
//    private static final Color GND_WIRE_COLOR = Color.BLACK;
//    private static final Map<Side, Double> ANGLE_MAP = new EnumMap<>(Map.of(Side.TOP, 0.0, Side.LEFT, -90.0, Side.BOTTOM, 180.0, Side.RIGHT, 90.0));
//    private static final String deviceDirectoryPath = DeviceLibrary.INSTANCE.getLibraryPath().get() + File.separator + "devices";
//
//    private final Map<Side, List<ProjectDevice>> deviceMap = new EnumMap<>(Side.class);
//    private final Map<ProjectDevice, Side> deviceSideMap = new HashMap<>();
//    private final Map<ProjectDevice, Point2D> devicePositionMap = new HashMap<>();
//    private final Map<ProjectDevice, DevicePort> deviceControllerPortMap = new HashMap<>();
//    private final List<DevicePort> usedPowerPort = new ArrayList<>();   // list of used power/gnd port when connecting from wire to wire
//    private double controllerOffsetX, controllerOffsetY;
//    private final Random random = new Random();
//
//    public JSTDiagram(Project project) {
//        for (Side s :  Side.values()) {
//            deviceMap.put(s, new ArrayList<>());
//        }
//
//        ActualDevice controller = project.getSelectedController();
//        if (controller == null) {
//            throw new IllegalStateException();
//        }
//
//        // get side (top, left, right, bottom) of each device
//        for (ProjectDevice projectDevice : project.getAllDeviceUsed()) {
//            if (projectDevice.isMergeToOtherDevice()) {
//                continue;
//            }
//            if (projectDevice.getCompatibleDevice().getFormFactor() == FormFactor.NONE) {
//                continue;
//            }
//
//            Map<Peripheral, List<DevicePort>> deviceConnection = projectDevice.getDeviceConnection();
//            // find port that we use more e.g. if D2_1, D2_2 and D1_1 is used, we should calculate based on D2
//            Map<DevicePort, Long> count = deviceConnection.values().stream().flatMap(Collection::stream).distinct()
//                    .collect(Collectors.groupingBy(port -> Objects.requireNonNullElse(port.getParent(), port), Collectors.counting()));
//            DevicePort controllerPort = Collections.max(count.entrySet(), Map.Entry.comparingByValue()).getKey();
//
//            String controllerPortName = controllerPort.getName();
//            if (controllerPortName.equals("Internal")) {    // TODO: remove after we finish migrate to integrated device json syntax
//                continue;
//            }
//
//            Side side = getDeviceSide(controller, controllerPort);
//            deviceMap.get(side).add(projectDevice);
//            deviceSideMap.put(projectDevice, side);
//            deviceControllerPortMap.put(projectDevice, controllerPort);
//        }
//
//        // draw controller
//        Path controllerImagePath = Paths.get(deviceDirectoryPath,project.getSelectedController().getId(), "asset", "device.png");
//        try (InputStream controllerImageStream = Files.newInputStream(controllerImagePath)) {
//            // left offset is equal to the max height of all devices on the left hand side not the max width because the image
//            // need to be rotate CW by 90 degree
//            controllerOffsetX = PADDING_X + deviceMap.get(Side.LEFT).stream().map(ProjectDevice::getCompatibleDevice)
//                    .mapToDouble(ActualDevice::getHeight).max().orElse(0) + DEVICE_TO_BOARD_SPACING;
//            controllerOffsetY = PADDING_Y + deviceMap.get(Side.TOP).stream().map(ProjectDevice::getCompatibleDevice)
//                    .mapToDouble(ActualDevice::getHeight).max().orElse(0) + DEVICE_TO_BOARD_SPACING;
//            Image controllerImage = new Image(controllerImageStream);
//            ImageView controllerImageView = new ImageView(controllerImage);
//            controllerImageView.setLayoutX(controllerOffsetX);
//            controllerImageView.setLayoutY(controllerOffsetY);
//            getChildren().add(controllerImageView);
//        } catch (IOException e) {
//            throw new IllegalStateException("Image not found : " + controllerImagePath);
//        }
//
//        double left = 0, right = 0, top = 0, bottom = 0;
//
//        // draw top device
//        left = deviceMap.get(Side.LEFT).stream().map(ProjectDevice::getCompatibleDevice)        // left offset is equal to the max height of all devices on the left hand side
//                .mapToDouble(ActualDevice::getHeight).max().orElse(0) + PADDING_X;    // not the max width because the image need to be rotate CW by 90 degree
//        bottom = deviceMap.get(Side.TOP).stream().map(ProjectDevice::getCompatibleDevice)
//                .mapToDouble(ActualDevice::getHeight).max().orElse(0) + PADDING_Y;
//        List<ProjectDevice> topDevice = deviceMap.get(Side.TOP);
//        topDevice.sort(Comparator.comparingDouble(o -> deviceControllerPortMap.get(o).getX()));
//        for (ProjectDevice device : topDevice) {
//            devicePositionMap.put(device, new Point2D(left, bottom - device.getCompatibleDevice().getHeight()));
//            drawDevice(device);
//            left += device.getCompatibleDevice().getWidth() + DEVICE_SPACING;
//        }
//
//        // draw left device
//        right = deviceMap.get(Side.LEFT).stream().map(ProjectDevice::getCompatibleDevice)       // left offset is equal to the max height of all devices on the left hand side
//                .mapToDouble(ActualDevice::getHeight).max().orElse(0) + PADDING_X;    // not the max width because the image need to be rotate CW by 90 degree
//        top = deviceMap.get(Side.TOP).stream().map(ProjectDevice::getCompatibleDevice)
//                .mapToDouble(ActualDevice::getHeight).max().orElse(0) + DEVICE_TO_BOARD_SPACING + PADDING_Y;
//        List<ProjectDevice> leftDevice = deviceMap.get(Side.LEFT);
//        leftDevice.sort(Comparator.comparingDouble(o -> deviceControllerPortMap.get(o).getY()));
//        for (ProjectDevice device : leftDevice) {
//            devicePositionMap.put(device, new Point2D(right - device.getCompatibleDevice().getHeight()
//                    , top + device.getCompatibleDevice().getWidth()));
//            drawDevice(device);
//            top += device.getCompatibleDevice().getWidth() + DEVICE_SPACING;
//        }
//
//        // draw bottom device
//        left = deviceMap.get(Side.LEFT).stream().map(ProjectDevice::getCompatibleDevice)        // left offset is equal to the max height of all devices on the left hand side
//                .mapToDouble(ActualDevice::getHeight).max().orElse(0) + PADDING_X;    // not the max width because the image need to be rotate CW by 90 degree
//        top = deviceMap.get(Side.TOP).stream().map(ProjectDevice::getCompatibleDevice)
//                .mapToDouble(ActualDevice::getHeight).max().orElse(0) + DEVICE_TO_BOARD_SPACING + PADDING_Y
//                + controller.getHeight() + DEVICE_TO_BOARD_SPACING;
//        List<ProjectDevice> bottomDevice = deviceMap.get(Side.BOTTOM);
//        bottomDevice.sort(Comparator.comparingDouble(o -> deviceControllerPortMap.get(o).getX()));
//        for (ProjectDevice device : bottomDevice) {
//            devicePositionMap.put(device, new Point2D(left + device.getCompatibleDevice().getWidth()
//                    , top + device.getCompatibleDevice().getHeight()));
//            drawDevice(device);
//            left += device.getCompatibleDevice().getWidth() + DEVICE_SPACING;
//        }
//
//        // draw right device
//        left = deviceMap.get(Side.LEFT).stream().map(ProjectDevice::getCompatibleDevice)   // left offset is equal to the max height of all devices on the left hand side
//                .mapToDouble(ActualDevice::getHeight).max().orElse(0)            // not the max width because the image need to be rotate CW by 90 degree
//                + DEVICE_TO_BOARD_SPACING + controller.getWidth() + DEVICE_TO_BOARD_SPACING + PADDING_X;
//        top = deviceMap.get(Side.TOP).stream().map(ProjectDevice::getCompatibleDevice)
//                .mapToDouble(ActualDevice::getHeight).max().orElse(0) + DEVICE_TO_BOARD_SPACING + PADDING_Y;
//        List<ProjectDevice> rightDevice = deviceMap.get(Side.RIGHT);
//        rightDevice.sort(Comparator.comparingDouble(o -> deviceControllerPortMap.get(o).getY()));
//        for (ProjectDevice device : deviceMap.get(Side.RIGHT)) {
//            devicePositionMap.put(device, new Point2D(left + device.getCompatibleDevice().getHeight(), top));
//            drawDevice(device);
//            top += device.getCompatibleDevice().getWidth() + DEVICE_SPACING;
//        }
//
//        // draw wire
//        for (ProjectDevice device : devicePositionMap.keySet()) {
//            drawCable(device, controller);
//        }
//    }
//
//    private Side getDeviceSide(ActualDevice controller, DevicePort port) {
//        Side side = Side.TOP;
//        double distance = port.getY();
//        if (port.getX() < distance) {
//            side = Side.LEFT;
//            distance = port.getX();
//        }
//        if (controller.getWidth() - port.getX() < distance) {
//            side = Side.RIGHT;
//            distance = controller.getWidth() - port.getX();
//        }
//        if (controller.getHeight() - port.getY() < distance) {
//            side = Side.BOTTOM;
//            distance = controller.getHeight() - port.getY();
//        }
//        return side;
//    }
//
//    private Point2D getTransformPortLocation(ProjectDevice device, DevicePort port) {
////        drawCircle(devicePositionMap.get(device).getX(), devicePositionMap.get(device).getY(), Color.PINK);
//        if (deviceSideMap.get(device) == Side.TOP) {
//            return devicePositionMap.get(device).add(port.getX(), port.getY());
//        } else if (deviceSideMap.get(device) == Side.LEFT) {
//            return devicePositionMap.get(device).add(port.getY(), -port.getX());
//        } else if (deviceSideMap.get(device) == Side.BOTTOM) {
//            return devicePositionMap.get(device).add(-port.getX(), -port.getY());
//        } else {    // RIGHT
//            return devicePositionMap.get(device).add(-port.getY(), port.getX());
//        }
//    }
//
//    private void drawDevice(ProjectDevice device) {
//        double left = devicePositionMap.get(device).getX();
//        double top = devicePositionMap.get(device).getY();
//        double angle = ANGLE_MAP.get(deviceSideMap.get(device));
//
//        Path deviceImagePath = Paths.get(deviceDirectoryPath,device.getCompatibleDevice().getId(), "asset", "device.png");
//        try (InputStream deviceImageStream = Files.newInputStream(deviceImagePath)){
//            Image deviceImage = new Image(deviceImageStream);
//            ImageView deviceImageView = new ImageView(deviceImage);
//            deviceImageView.setRotate(angle);
//            if (Double.compare(angle, 0) == 0) {
//                deviceImageView.setLayoutX(left);
//                deviceImageView.setLayoutY(top);
//            } else if (Double.compare(angle, 180) == 0) {
//                deviceImageView.setLayoutX(left - deviceImage.getWidth());
//                deviceImageView.setLayoutY(top - deviceImage.getHeight());
//            } else if (Double.compare(angle, 90) == 0) {
//                deviceImageView.setLayoutX(left - (deviceImage.getWidth() / 2.0) - (deviceImage.getHeight() / 2.0));
//                deviceImageView.setLayoutY(top - (deviceImage.getHeight() / 2.0) + (deviceImage.getWidth() / 2.0));
//            } else if (Double.compare(angle, -90) == 0) {
//                deviceImageView.setLayoutX(left - (deviceImage.getWidth() / 2.0) + (deviceImage.getHeight() / 2.0));
//                deviceImageView.setLayoutY(top - (deviceImage.getHeight() / 2.0) - (deviceImage.getWidth() / 2.0));
//            } else {
//                throw new IllegalArgumentException();
//            }
//            getChildren().addAll(deviceImageView);
//        } catch (IOException e) {
//            throw new IllegalStateException("Image not found : " + deviceDirectoryPath + device.getCompatibleDevice().getId() + "/asset/device.png");
//        }
//    }
//
//    private void drawCable(ProjectDevice device, ActualDevice controller) {
//        List<Peripheral> deviceConnectivity = device.getCompatibleDevice().getConnectivity();
//        Map<Peripheral, List<DevicePort>> deviceConnection = device.getDeviceConnection();
//        boolean hasConnectedPower = false;
//
//        for (Peripheral peripheral : deviceConnectivity) {
//            List<DevicePort> devicePort = device.getCompatibleDevice().getPort(peripheral);
//            List<DevicePort> controllerPort = deviceConnection.get(peripheral);
//            if (devicePort.size() != controllerPort.size()) {
//                throw new IllegalStateException("");
//            }
//
//            if (devicePort.size() == 1 && controllerPort.size() == 1
//                    && devicePort.get(0).getType().getPinCount() > 1 && controllerPort.get(0).getType().getPinCount() > 1
//                    && devicePort.get(0).getType() == controllerPort.get(0).getType()) {   // JST to JST device (works with MP, GROVE, JR3_SERVO and INEX)
//                drawJSTToJSTConnector(device, devicePort.get(0), controllerPort.get(0));
//            } else if (controllerPort.stream().allMatch(DevicePort::isSplittedPort)) {   // JST to breakout board
//                drawPinHeaderToJSTSignal(device, devicePort, controllerPort);
//                // connect power/gnd for device connected to split port only once in case that the signal wires come from different ports
//                if (!hasConnectedPower) {
//                    drawPinHeaderToJSTPower(device, controllerPort);
//                }
//                hasConnectedPower = true;
//            } else if (controllerPort.stream().noneMatch(DevicePort::isSplittedPort)) { // wire to wire
//                drawWireToWireSignal(device, devicePort, controllerPort);
//                if (!hasConnectedPower) {
//                    drawWireToWirePower(device, controller);
//                }
//                hasConnectedPower = true;
//            } else {
//                throw new UnsupportedOperationException();
//            }
//        }
//    }
//
//    private void drawJSTToJSTConnector(ProjectDevice device, DevicePort devicePort, DevicePort controllerPort) {
//        if (devicePort.getType() != controllerPort.getType()) {
//            throw new IllegalArgumentException("Port must has the same type");
//        }
//
//        double startX = getTransformPortLocation(device, devicePort).getX();
//        double startY = getTransformPortLocation(device, devicePort).getY();
//        double startAngle = ANGLE_MAP.get(deviceSideMap.get(device));
//        DevicePortSubType startType = devicePort.getSubType();
////        drawCircle(startX, startY, Color.BLUE);
//
//        double endX = controllerOffsetX + controllerPort.getX();
//        double endY = controllerOffsetY + controllerPort.getY();
//        double endAngle = controllerPort.getAngle();
//        DevicePortSubType endType = controllerPort.getSubType();
//
//        int pinCount = controllerPort.getType().getPinCount();
//        double pinPitch = controllerPort.getType().getPinPitch() * MM_TO_PX;
//        double wireWidth = pinPitch * PITCH_TO_WIDTH;
//
//        for (int i=0; i<pinCount; i++) {
//            double centerOffset = pinPitch * (i - ((pinCount-1)/2.0));
//            double startFlip = (startType == DevicePortSubType.RIGHTANGLE_BOTTOM) ? 1 : -1;
//            double sx = startX + (centerOffset * Math.cos(Math.toRadians(startAngle)) * startFlip);
//            double sy = startY + (centerOffset * Math.sin(Math.toRadians(startAngle)) * startFlip);
////            drawCircle(sx, sy, WIRE_COLOR.get(i));
//
//            double endFlip = (endType == DevicePortSubType.RIGHTANGLE_TOP) ? 1 : -1;
//            double ex = endX + (centerOffset * Math.sin(Math.toRadians(endAngle)) * endFlip);
//            double ey = endY + (centerOffset * Math.cos(Math.toRadians(endAngle)) * endFlip);
////            drawCircle(ex, ey, WIRE_COLOR.get(i));
//
//            drawWire(sx, sy, startAngle, ex, ey, controllerPort.getType().getPinType(i).getColor(), wireWidth);
//        }
//    }
//
//    private void drawPinHeaderToJSTSignal(ProjectDevice device, List<DevicePort> devicePortList, List<DevicePort> controllerPortList) {
//        for (int i=0; i<devicePortList.size(); i++) {
//            // get port of the controller that current port of this device is connected to
//            DevicePort controllerPort = controllerPortList.get(i);
//            // get type of pin based on the name of the split port _1 is SIGNAL_1, _2 is SIGNAL_2 etc.
//            // TODO: find better way than string manipulation
//            DevicePortPinType type = null;
//            int splitPinIndex = controllerPort.getName().charAt(controllerPort.getName().length() - 1) - '0';
//            if (splitPinIndex == 1) {
//                type = DevicePortPinType.SIGNAL_1;
//            } else if (splitPinIndex == 2) {
//                type = DevicePortPinType.SIGNAL_2;
//            } else {
//                throw new IllegalStateException();
//            }
//            // get index of wire in the the connector that has this pin type (SIGNAL_1 is pin 1 for MP but it is pin 2 for INEX)
//            // use controllerPort.getParent() as the controllerPort is a split port which has type WIRE
//            int wireIndex =  controllerPort.getParent().getType().getPinIndex(type).orElseThrow();
//            drawPinHeaderToJSTConnector(device, devicePortList.get(i), controllerPort, wireIndex);
//        }
//    }
//
//    private void drawPinHeaderToJSTPower(ProjectDevice device, List<DevicePort> controllerPortList) {
//        DevicePort controllerPort = controllerPortList.get(0);
//        int powerPinIndex = controllerPort.getParent().getType().getPinIndex(DevicePortPinType.POWER).orElseThrow();
//        Optional<DevicePort> powerPort = device.getCompatibleDevice().getPort(Peripheral.POWER).stream().filter(DevicePort::isVcc).findAny();
//        powerPort.ifPresent(devicePort -> drawPinHeaderToJSTConnector(device, devicePort, controllerPort, powerPinIndex));
//        int groundPinIndex = controllerPort.getParent().getType().getPinIndex(DevicePortPinType.GROUND).orElseThrow();
//        Optional<DevicePort> gndPort = device.getCompatibleDevice().getPort(Peripheral.POWER).stream().filter(DevicePort::isGnd).findAny();
//        gndPort.ifPresent(devicePort -> drawPinHeaderToJSTConnector(device, devicePort, controllerPort, groundPinIndex));
//    }
//
//    private void drawPinHeaderToJSTConnector(ProjectDevice device, DevicePort start, DevicePort end, int cableIndex) {
//        double sx = getTransformPortLocation(device, start).getX();
//        double sy = getTransformPortLocation(device, start).getY();
//        double startAngle = ANGLE_MAP.get(deviceSideMap.get(device));
//
//        DevicePort parentPort = end.getParent();
//        double endX = controllerOffsetX + parentPort.getX();
//        double endY = controllerOffsetY + parentPort.getY();
//        double endAngle = parentPort.getAngle();
//        DevicePortSubType endType = parentPort.getSubType();
//
//        double pinPitch = parentPort.getType().getPinPitch() * MM_TO_PX;
//        Color color = parentPort.getType().getPinType(cableIndex).getColor();
//        double wireWidth = pinPitch * PITCH_TO_WIDTH;
//
//        double centerOffset = pinPitch * (cableIndex - 1.5);
//        double endFlip = (endType == DevicePortSubType.RIGHTANGLE_TOP) ? 1 : -1;
//        double ex = endX + (centerOffset * Math.sin(Math.toRadians(endAngle)) * endFlip);
//        double ey = endY + (centerOffset * Math.cos(Math.toRadians(endAngle)) * endFlip);
//
//        drawWire(sx, sy, startAngle, ex, ey, color, wireWidth);
//    }
//
//    private void drawWireToWireSignal(ProjectDevice device, List<DevicePort> devicePort, List<DevicePort> controllerPort) {
//        for (int i=0; i<devicePort.size(); i++) {
//            DevicePort dp = devicePort.get(i);
//            DevicePort cp = controllerPort.get(i);
//            drawWire(getTransformPortLocation(device, dp).getX(), getTransformPortLocation(device, dp).getY(), ANGLE_MAP.get(deviceSideMap.get(device))
//                    , controllerOffsetX + cp.getX(), controllerOffsetY + cp.getY()
//                    , WIRE_COLOR.get(random.nextInt(WIRE_COLOR.size())), WIRE_WIDTH);
//        }
//    }
//
//    private void drawWireToWirePower(ProjectDevice device, ActualDevice controller) {
//        List<DevicePort> unusedPowerPort = controller.getPort(Peripheral.POWER);
//        unusedPowerPort.removeAll(usedPowerPort);
//        Optional<DevicePort> controllerVccPort = unusedPowerPort.stream().filter(DevicePort::isVcc).findAny();
//        Optional<DevicePort> controllerGroundPort = unusedPowerPort.stream().filter(DevicePort::isGnd).findAny();
//
//        List<DevicePort> devicePowerPort = device.getCompatibleDevice().getPort(Peripheral.POWER);
//        Optional<DevicePort> deviceVccPort = devicePowerPort.stream().filter(DevicePort::isVcc).findAny();
//        Optional<DevicePort> deviceGroundPort = devicePowerPort.stream().filter(DevicePort::isGnd).findAny();
//
//        boolean drawPower = controllerVccPort.isPresent() && deviceVccPort.isPresent();
//        boolean drawGround = controllerGroundPort.isPresent() && deviceGroundPort.isPresent();
//        if (!drawPower && !drawGround) {
//            throw new IllegalStateException("The controller has insufficient power/ground port or the device doesn't define both power and ground port.");
//        }
//        if (drawPower) {
//            usedPowerPort.add(controllerVccPort.get());
//            drawWire(getTransformPortLocation(device, deviceVccPort.get()).getX(), getTransformPortLocation(device, deviceVccPort.get()).getY()
//                    , ANGLE_MAP.get(deviceSideMap.get(device))
//                    , controllerOffsetX + controllerVccPort.get().getX(), controllerOffsetY + controllerVccPort.get().getY()
//                    , POWER_WIRE_COLOR, WIRE_WIDTH);
//        }
//        if (drawGround) {
//            usedPowerPort.add(controllerGroundPort.get());
//            drawWire(getTransformPortLocation(device, deviceGroundPort.get()).getX(), getTransformPortLocation(device, deviceGroundPort.get()).getY()
//                    , ANGLE_MAP.get(deviceSideMap.get(device))
//                    , controllerOffsetX + controllerGroundPort.get().getX(), controllerOffsetY + controllerGroundPort.get().getY()
//                    , GND_WIRE_COLOR, WIRE_WIDTH);
//        }
//    }
//
//    private void drawWire(double sx, double sy, double startAngle, double ex, double ey, Color color, double wireWidth) {
//        Polyline line1 = new Polyline();
//        line1.setStroke(color);
//        line1.setStrokeLineCap(StrokeLineCap.ROUND);
//        line1.setStrokeLineJoin(StrokeLineJoin.ROUND);
//        line1.setStrokeWidth(wireWidth);
//        Polyline line2 = new Polyline();
//        line2.setStroke(color);
//        line2.setStrokeLineCap(StrokeLineCap.ROUND);
//        line2.setStrokeLineJoin(StrokeLineJoin.ROUND);
//        line2.setStrokeWidth(wireWidth);
//        if (Double.compare(startAngle, 0) == 0 || Double.compare(startAngle, 180) == 0) {
//            line1.getPoints().addAll(sx, sy, sx, (ey-sy)/2+sy, (ex-sx)/2+sx, (ey-sy)/2+sy);
//            line2.getPoints().addAll((ex-sx)/2+sx, (ey-sy)/2+sy, ex, (ey-sy)/2+sy, ex, ey);
//        } else {
//            line1.getPoints().addAll(sx, sy, (ex-sx)/2+sx, sy, (ex-sx)/2+sx, (ey-sy)/2+sy);
//            line2.getPoints().addAll((ex-sx)/2+sx, (ey-sy)/2+sy, (ex-sx)/2+sx, ey, ex, ey);
//        }
//        getChildren().addAll(line1, line2);
//    }
//
//    private void drawCircle(double left, double top, Color color) {
//        Circle c = new Circle();
//        c.setCenterX(left);
//        c.setCenterY(top);
//        c.setRadius(2);
//        c.setFill(color);
//        getChildren().add(c);
//    }
}
