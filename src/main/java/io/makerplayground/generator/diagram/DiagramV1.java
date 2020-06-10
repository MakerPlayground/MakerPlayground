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
import io.makerplayground.device.actual.*;
import io.makerplayground.project.*;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Text;
import lombok.*;
import org.controlsfx.control.PopOver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/* 1) The devices are placing in one of the following regions
 *      -----------------------------
 *      |           Top             |
 *      -----------------------------
 *      |      |  top-mid   |       |
 *      |      |------------|       |
 *      | left | controller | right |
 *      |      |------------|       |
 *      |      | bottom-mid |       |
 *      -----------------------------
 *      |          Bottom           |
 *      -----------------------------
 * 2) To know the device region
 *      2.1 For each device, calculate the centroid of all pins on controller that connect to the device.
 *      2.2 The centroid will be used for determining the region of the device. Call the getDeviceRegionNoBreadboard method to know the region
 *      2.3 In case that breadboard is needed. The top-mid region will be reserved for breadboard. Call the getDeviceRegionNoBreadboard method instead
 *      2.4 Cloud device should be in the Top region (No line to controller)
 *      2.5 Not connected device should be in the Bottom region (No line to controller)
 * 3) The devices in each region would be rotated to make the shortest line to the controller except the device that needs breadboard.
 * 4) On left and right region, the devices would be sorted by the y-position of centroid.
 * 5) On top-mid and bottom-mid, the devices would be sorted by the x-position of centroid.
 * 6) The width and height of each region is calculated by the device in the region.
 * 7) The total width and height of diagram is calculated to recenter the diagram.
 */

class DiagramV1 {

    @Value @AllArgsConstructor
    static class Coordinate {
        double x, y;
        Coordinate add(double dx, double dy) {
            return new Coordinate(x + dx, y + dy);
        }
        Coordinate add(Coordinate coor) {
            return new Coordinate(x + coor.getX(), y + coor.getY());
        }
    }

    @Data @AllArgsConstructor
    static class Size {
        double width, height;
    }

    enum ConnectionPosition {
        LEFT, RIGHT, TOP, BOTTOM;
    }

    enum Region {
        LEFT, RIGHT, TOP_MID, BOTTOM_MID, TOP, BOTTOM, CONTROLLER;
    }

    public static final double UNIT_HOLE_DISTANCE = 14.4;

    enum Breadboard {
        /*
         *  Layout for normal version
         *   ======================
         *  |         Upper        |
         *  |======================|
         *  |         Lower        |
         *   ======================
         *
         *  Layout for EXTEND version
         *   ======================
         *  |         Upper        |
         *  |======================|
         *  |     Upper Center     |
         *  |======================|
         *  |     Lower Center     |
         *  |======================|
         *  |         Lower        |
         *   ======================
         */

        SMALL(30, 468.0, 303.0,
                new Coordinate(22.0, 72.0),
                new Coordinate(22.0, 72.0 + 11 * UNIT_HOLE_DISTANCE),
                new Coordinate(22.0, 72.0 + 7 * UNIT_HOLE_DISTANCE),
                new Coordinate(27.0, 289.0),
                new Coordinate(27.0, 274.4),
                new Coordinate(27.0 + 58 * UNIT_HOLE_DISTANCE, 289.0),
                new Coordinate(27.0 + 58 * UNIT_HOLE_DISTANCE, 274.4),
                new Coordinate(27.0, 29.4),
                new Coordinate(27.0, 15.0),
                new Coordinate(27.0 + 58 * UNIT_HOLE_DISTANCE, 29.4),
                new Coordinate(27.0 + 58 * UNIT_HOLE_DISTANCE, 15.0)),
        LARGE(63, 937.0, 303.0,
                new Coordinate(22.0, 72.0),
                new Coordinate(22.0, 72.0 + 11 * UNIT_HOLE_DISTANCE),
                new Coordinate(22.0, 72.0 + 7 * UNIT_HOLE_DISTANCE),
                new Coordinate(50.0, 289.0),
                new Coordinate(50.0, 274.4),
                new Coordinate(50.0 + 58 * UNIT_HOLE_DISTANCE, 289.0),
                new Coordinate(50.0 + 58 * UNIT_HOLE_DISTANCE, 274.4),
                new Coordinate(50.0, 29.4),
                new Coordinate(50.0, 15.0),
                new Coordinate(50.0 + 58 * UNIT_HOLE_DISTANCE, 29.4),
                new Coordinate(50.0 + 58 * UNIT_HOLE_DISTANCE, 15.0)),
        LARGE_EXTEND(63, 937.0, 563.0,
                new Coordinate(22.0, 72.0),
                new Coordinate(22.0, 72.0 + 29 * UNIT_HOLE_DISTANCE),
                new Coordinate(22.0, 72.0 + 25 * UNIT_HOLE_DISTANCE),
                new Coordinate(50.0, 549.4),
                new Coordinate(50.0, 535.0),
                new Coordinate(50.0 + 58 * UNIT_HOLE_DISTANCE, 549.4),
                new Coordinate(50.0 + 58 * UNIT_HOLE_DISTANCE, 535.0),
                new Coordinate(50.0, 29.4),
                new Coordinate(50.0, 15.0),
                new Coordinate(50.0 + 58 * UNIT_HOLE_DISTANCE, 29.4),
                new Coordinate(50.0 + 58 * UNIT_HOLE_DISTANCE, 15.0),
                new Coordinate(22.0, 72.0 + 7 * UNIT_HOLE_DISTANCE),
                new Coordinate(22.0, 72.0 + 11 * UNIT_HOLE_DISTANCE),
                new Coordinate(22.0, 72.0 + 18 * UNIT_HOLE_DISTANCE),
                new Coordinate(22.0, 72.0 + 22 * UNIT_HOLE_DISTANCE));

        @Getter private final int numColumns;
        @Getter private final double width;
        @Getter private final double height;
        @Getter private final Coordinate upperTopLeftHoleCoordinate;
        @Getter private final Coordinate lowerBottomLeftHoleCoordinate;
        @Getter private final Coordinate lowerTopLeftHoleCoordinate;
        @Getter private final Coordinate lowerPowerBottomLeftHoleCoordinate;
        @Getter private final Coordinate lowerPowerTopLeftHoleCoordinate;
        @Getter private final Coordinate lowerPowerBottomRightHoleCoordinate;
        @Getter private final Coordinate lowerPowerTopRightHoleCoordinate;
        @Getter private final Coordinate upperPowerBottomLeftHoleCoordinate;
        @Getter private final Coordinate upperPowerTopLeftHoleCoordinate;
        @Getter private final Coordinate upperPowerBottomRightHoleCoordinate;
        @Getter private final Coordinate upperPowerTopRightHoleCoordinate;
        @Getter private final Coordinate upperCenterTopLeftHoleCoordinate;
        @Getter private final Coordinate upperCenterBottomLeftHoleCoordinate;
        @Getter private final Coordinate lowerCenterTopLeftHoleCoordinate;
        @Getter private final Coordinate lowerCenterBottomLeftHoleCoordinate;

        Breadboard(int numColumns, double width, double height,
                   Coordinate upperTopLeftHoleCoordinate,
                   Coordinate lowerBottomLeftHoleCoordinate,
                   Coordinate lowerTopLeftHoleCoordinate,
                   Coordinate lowerPowerBottomLeftHoleCoordinate,
                   Coordinate lowerPowerTopLeftHoleCoordinate,
                   Coordinate lowerPowerBottomRightHoleCoordinate,
                   Coordinate lowerPowerTopRightHoleCoordinate,
                   Coordinate upperPowerBottomLeftHoleCoordinate,
                   Coordinate upperPowerTopLeftHoleCoordinate,
                   Coordinate upperPowerBottomRightHoleCoordinate,
                   Coordinate upperPowerTopRightHoleCoordinate) {
            this(numColumns, width, height,
                    upperTopLeftHoleCoordinate,
                    lowerBottomLeftHoleCoordinate,
                    lowerTopLeftHoleCoordinate,
                    lowerPowerBottomLeftHoleCoordinate,
                    lowerPowerTopLeftHoleCoordinate,
                    lowerPowerBottomRightHoleCoordinate,
                    lowerPowerTopRightHoleCoordinate,
                    upperPowerBottomLeftHoleCoordinate,
                    upperPowerTopLeftHoleCoordinate,
                    upperPowerBottomRightHoleCoordinate,
                    upperPowerTopRightHoleCoordinate,
                    null,
                    null,
                    null,
                    null);
        }

        Breadboard(int numColumns, double width, double height,
                   Coordinate upperTopLeftHoleCoordinate,
                   Coordinate lowerBottomLeftHoleCoordinate,
                   Coordinate lowerTopLeftHoleCoordinate,
                   Coordinate lowerPowerBottomLeftHoleCoordinate,
                   Coordinate lowerPowerTopLeftHoleCoordinate,
                   Coordinate lowerPowerBottomRightHoleCoordinate,
                   Coordinate lowerPowerTopRightHoleCoordinate,
                   Coordinate upperPowerBottomLeftHoleCoordinate,
                   Coordinate upperPowerTopLeftHoleCoordinate,
                   Coordinate upperPowerBottomRightHoleCoordinate,
                   Coordinate upperPowerTopRightHoleCoordinate,
                   // extended
                   Coordinate upperCenterTopLeftHoleCoordinate,
                   Coordinate upperCenterBottomLeftHoleCoordinate,
                   Coordinate lowerCenterTopLeftHoleCoordinate,
                   Coordinate lowerCenterBottomLeftHoleCoordinate) {
            this.numColumns = numColumns;
            this.width = width;
            this.height = height;
            this.upperTopLeftHoleCoordinate = upperTopLeftHoleCoordinate;
            this.lowerBottomLeftHoleCoordinate = lowerBottomLeftHoleCoordinate;
            this.lowerTopLeftHoleCoordinate = lowerTopLeftHoleCoordinate;
            this.lowerPowerBottomLeftHoleCoordinate = lowerPowerBottomLeftHoleCoordinate;
            this.lowerPowerTopLeftHoleCoordinate = lowerPowerTopLeftHoleCoordinate;
            this.lowerPowerBottomRightHoleCoordinate = lowerPowerBottomRightHoleCoordinate;
            this.lowerPowerTopRightHoleCoordinate = lowerPowerTopRightHoleCoordinate;
            this.upperPowerBottomLeftHoleCoordinate = upperPowerBottomLeftHoleCoordinate;
            this.upperPowerTopLeftHoleCoordinate = upperPowerTopLeftHoleCoordinate;
            this.upperPowerBottomRightHoleCoordinate = upperPowerBottomRightHoleCoordinate;
            this.upperPowerTopRightHoleCoordinate = upperPowerTopRightHoleCoordinate;
            // extended
            this.upperCenterTopLeftHoleCoordinate = upperCenterTopLeftHoleCoordinate;
            this.upperCenterBottomLeftHoleCoordinate = upperCenterBottomLeftHoleCoordinate;
            this.lowerCenterTopLeftHoleCoordinate = lowerCenterTopLeftHoleCoordinate;
            this.lowerCenterBottomLeftHoleCoordinate = lowerCenterBottomLeftHoleCoordinate;
        }

        public void draw(Pane drawingPane, Coordinate coordinate) {
            if (this == SMALL) {
                String path = "/device/breadboard_small@2x.png";
                try(InputStream deviceImageStream = getClass().getResourceAsStream(path)) {
                    Image image = new Image(deviceImageStream);
                    ImageView imageView = new ImageView(image);
                    imageView.setLayoutX(coordinate.getX());
                    imageView.setLayoutY(coordinate.getY());
                    drawingPane.getChildren().add(imageView);
                } catch (IOException e) {
                    throw new IllegalStateException("Image not found for breadboard: " + SMALL);
                }
            } else if (this == LARGE) {
                String path = "/device/breadboard_large@2x.png";
                try(InputStream deviceImageStream = getClass().getResourceAsStream(path)) {
                    Image image = new Image(deviceImageStream);
                    ImageView imageView = new ImageView(image);
                    imageView.setLayoutX(coordinate.getX());
                    imageView.setLayoutY(coordinate.getY());
                    drawingPane.getChildren().add(imageView);
                } catch (IOException e) {
                    throw new IllegalStateException("Image not found for breadboard: " + LARGE);
                }
            } else if (this == LARGE_EXTEND) {
                String path1 = "/device/breadboard_large@2x.png";
                String path2 = "/device/breadboard_large_extend@2x.png";
                try(InputStream deviceImageStream1 = getClass().getResourceAsStream(path1);
                    InputStream deviceImageStream2 = getClass().getResourceAsStream(path2)) {
                    Image image = new Image(deviceImageStream1);
                    ImageView imageView = new ImageView(image);
                    imageView.setLayoutX(coordinate.getX());
                    imageView.setLayoutY(coordinate.getY());
                    drawingPane.getChildren().add(imageView);

                    Image image2 = new Image(deviceImageStream2);
                    ImageView imageView2 = new ImageView(image2);
                    imageView2.setLayoutX(coordinate.getX());
                    imageView2.setLayoutY(coordinate.getY() + LARGE.getHeight());
                    drawingPane.getChildren().add(imageView2);
                } catch (IOException e) {
                    throw new IllegalStateException("Image not found for breadboard: " + LARGE_EXTEND);
                }
            }
        }

        public Coordinate getGndRightHoleCoordinate() {
            return this.lowerPowerTopRightHoleCoordinate;
        }

        public Coordinate getGndLeftHoleCoordinate() {
            return this.lowerPowerTopLeftHoleCoordinate;
        }

        public Coordinate getVccRightHoleCoordinate(int index) {
            if (index == 0) {
                return this.lowerPowerBottomRightHoleCoordinate;
            } else if (index == 1) {
                return this.upperPowerTopRightHoleCoordinate;
            } else if (index == 2) {
                return this.upperPowerBottomRightHoleCoordinate;
            }
            throw new UnsupportedOperationException("Breadboard could handle only 3 voltage levels");
        }

        public Coordinate getVccLeftHoleCoordinate(int index) {
            if (index == 0) {
                return this.lowerPowerBottomLeftHoleCoordinate;
            } else if (index == 1) {
                return this.upperPowerTopLeftHoleCoordinate;
            } else if (index == 2) {
                return this.upperPowerBottomLeftHoleCoordinate;
            }
            throw new UnsupportedOperationException("Breadboard could handle only 3 voltage levels");
        }

        public List<Double> getAllPowerOffsetFromLeftX() {
            List<Double> offsetXList = new ArrayList<>();
            double offsetX = 0;
            double distance = getGndRightHoleCoordinate().getX() - getGndLeftHoleCoordinate().getX();
            int count = 0;
            while(distance > 0) {
                offsetXList.add(offsetX);
                offsetX += UNIT_HOLE_DISTANCE;
                distance -= UNIT_HOLE_DISTANCE;
                count += 1;
                if (count % 6 == 5) {
                    offsetX += UNIT_HOLE_DISTANCE;
                    distance -= UNIT_HOLE_DISTANCE;
                    count += 1;
                }
            }
            return offsetXList;
        }
    }

    static class BreadboardDeviceGroup {
        @Getter final Breadboard breadboard;
        @Getter final List<BreadboardDevice> deviceList;
        @Setter @Getter Coordinate breadboardCoordinate;
        private boolean isGndConnected;
        private Map<Integer, Boolean> isVoltageConnected = new HashMap<>();
        private Map<Integer, List<Double>> availableVccOffsetX;
        private List<Double> availableGndOffsetX;

        BreadboardDeviceGroup(Breadboard breadboard) {
            this.breadboard = breadboard;
            this.deviceList = new ArrayList<>();
            this.availableGndOffsetX = new ArrayList<>(breadboard.getAllPowerOffsetFromLeftX());
            this.availableGndOffsetX.remove(0);
            this.availableGndOffsetX.remove(this.availableGndOffsetX.size()-1);
            this.availableVccOffsetX = new HashMap<>();
            for (int i=0; i<3; i++) {
                List<Double> temp = new ArrayList<>(breadboard.getAllPowerOffsetFromLeftX());
                temp.remove(0);
                temp.remove(temp.size()-1);
                this.availableVccOffsetX.put(i, temp);
            }
        }

        void drawBreadboard(Pane drawingPane) {
            this.breadboard.draw(drawingPane, breadboardCoordinate.add(GLOBAL_LEFT_MARGIN, GLOBAL_TOP_MARGIN));
        }

        Coordinate getPinCoordinate(BreadboardDevice device, Pin pin) {
            double x = breadboardCoordinate.getX() + breadboard.getUpperTopLeftHoleCoordinate().getX() + UNIT_HOLE_DISTANCE;
            for (BreadboardDevice breadboardDevice: deviceList) {
                if (breadboardDevice == device) {
                    break;
                }
                x += (breadboardDevice.getNumColumnsWholeDevice() + 1) * UNIT_HOLE_DISTANCE;
            }
            double xShift = UNIT_HOLE_DISTANCE * Math.ceil(device.getXLeftHolePixel() / UNIT_HOLE_DISTANCE) - device.getXLeftHolePixel();
            x += device.getXLeftHolePixel() + xShift + device.getOffsetXHoles().get(pin) * UNIT_HOLE_DISTANCE;
            double y = breadboardCoordinate.getY();
            boolean upperPin = pin.getY() <= 0.5 * device.getActualDevice().getHeight();
            if (device.getBreadboardPlacement() == BreadboardPlacement.ONE_SIDE) {
                y += breadboard.getLowerTopLeftHoleCoordinate().getY() + device.getOffsetYHoles().get(pin) * UNIT_HOLE_DISTANCE;
            }
            else if (device.getBreadboardPlacement() == BreadboardPlacement.TWO_SIDES) {
                if (device.needExtendedBreadboard() && breadboard == Breadboard.LARGE_EXTEND) {
                    y += (upperPin ?
                            breadboard.getUpperCenterTopLeftHoleCoordinate().getY() + device.getOffsetYHoles().get(pin) * UNIT_HOLE_DISTANCE :
                            breadboard.getLowerCenterBottomLeftHoleCoordinate().getY() - device.getOffsetYHoles().get(pin) * UNIT_HOLE_DISTANCE);
                } else if (!device.needExtendedBreadboard() && (breadboard == Breadboard.LARGE || breadboard == Breadboard.SMALL)) {
                    y += (upperPin ?
                            breadboard.getUpperTopLeftHoleCoordinate().getY() + device.getOffsetYHoles().get(pin) * UNIT_HOLE_DISTANCE :
                            breadboard.getLowerBottomLeftHoleCoordinate().getY() - device.getOffsetYHoles().get(pin) * UNIT_HOLE_DISTANCE);
                } else if (!device.needExtendedBreadboard() && (breadboard == Breadboard.LARGE_EXTEND)) {
                    y += (upperPin ?
                            breadboard.getUpperTopLeftHoleCoordinate().getY() + device.getOffsetYHoles().get(pin) * UNIT_HOLE_DISTANCE :
                            breadboard.getUpperCenterBottomLeftHoleCoordinate().getY() - device.getOffsetYHoles().get(pin) * UNIT_HOLE_DISTANCE);
                } else {
                    throw new IllegalStateException("No implementation for placement: " + device.getBreadboardPlacement());
                }
            } else {
                throw new IllegalStateException("No implementation for breadboard: " + breadboard);
            }
            return new Coordinate(x, y);
        }

        void setGndConnected() {
            this.isGndConnected = true;
        }

        void setVccConnected(int voltageIndex) {
            this.isVoltageConnected.put(voltageIndex, true);
        }

        boolean isGndConnected() {
            return this.isGndConnected;
        }

        boolean isVccConnected(int voltageIndex) {
            return this.isVoltageConnected.containsKey(voltageIndex) && this.isVoltageConnected.get(voltageIndex);
        }

        Coordinate reserveVccCoordinate(int voltageIndex, double prefer_x) {
            double temp = this.getBreadboardCoordinate().add(breadboard.getVccLeftHoleCoordinate(voltageIndex)).getX();
            Optional<Double> offset = availableVccOffsetX.get(voltageIndex).stream().min(Comparator.comparing(value->Math.abs(prefer_x - (temp+value))));
            if (offset.isEmpty()) {
                throw new IllegalStateException("The breadboard vcc line is not enough for devices");
            }
            availableVccOffsetX.get(voltageIndex).remove(offset.get());
            return this.getBreadboardCoordinate().add(breadboard.getVccLeftHoleCoordinate(voltageIndex)).add(offset.get(), 0);
        }

        Coordinate reserveGndCoordinate(double prefer_x) {
            double temp = this.getBreadboardCoordinate().add(breadboard.getGndLeftHoleCoordinate()).getX();
            Optional<Double> offset = availableGndOffsetX.stream().min(Comparator.comparing(value->Math.abs(prefer_x - (temp+value))));
            if (offset.isEmpty()) {
                throw new IllegalStateException("The breadboard gnd line is not enough for devices");
            }
            availableGndOffsetX.remove(offset.get());
            return this.getBreadboardCoordinate().add(breadboard.getGndLeftHoleCoordinate()).add(offset.get(), 0);
        }
    }

    @Data
    static class BreadboardDevice {
        private final ProjectDevice projectDevice;
        private final ActualDevice actualDevice;
        private int numColumnsWholeDevice; // number of holes

        Map<Pin, Integer> offsetXHoles = new HashMap<>();
        Map<Pin, Integer> offsetYHoles = new HashMap<>();

        double yTopHolePixel = Double.MAX_VALUE;
        double yBottomHolePixel = Double.MIN_VALUE;
        double xLeftHolePixel = Double.MAX_VALUE;

        BreadboardDevice(ProjectDevice projectDevice, ActualDevice actualDevice) {
            this.projectDevice = projectDevice;
            this.actualDevice = actualDevice;
            List<Connection> connections = new ArrayList<>();
            connections.addAll(actualDevice.getConnectionConsumeByOwnerDevice(projectDevice));
            connections.addAll(actualDevice.getConnectionProvideByOwnerDevice(projectDevice));

            // Find the top and left holes
            for (Connection connection: connections) {
                // we care only the pin that uses hole
                if (connection.getType() != ConnectionType.WIRE) {
                    continue;
                }
                Pin pin = connection.getPins().get(0);
                xLeftHolePixel = Math.min(pin.getX(), xLeftHolePixel);
                yTopHolePixel = Math.min(pin.getY(), yTopHolePixel);
                yBottomHolePixel = Math.max(pin.getY(), yBottomHolePixel);
            }

            // Calculate Hole offset
            for (Connection connection: connections) {
                if (connection.getType() != ConnectionType.WIRE) {
                    continue;
                }
                Pin pin = connection.getPins().get(0);
                offsetXHoles.put(pin, (int) Math.round((pin.getX() - xLeftHolePixel) / UNIT_HOLE_DISTANCE));
                int offsetY;
                if (actualDevice.getBreadboardPlacement() == BreadboardPlacement.ONE_SIDE) {
                    offsetY = 4;
                } else if (actualDevice.getBreadboardPlacement() == BreadboardPlacement.TWO_SIDES) {
                    offsetY = 0; // offset from lower/lower-center of bottom pin or upper/upper-center of top pin
                } else {
                    throw new IllegalStateException("There is no implementation for placement: " + actualDevice.getBreadboardPlacement());
                }
                offsetYHoles.put(pin, offsetY);
            }

            // Calculate num of rows and columns
            numColumnsWholeDevice = (int) Math.ceil(xLeftHolePixel / UNIT_HOLE_DISTANCE) + (int) Math.ceil((actualDevice.getWidth() - xLeftHolePixel) / UNIT_HOLE_DISTANCE);
        }

        BreadboardPlacement getBreadboardPlacement() {
            return actualDevice.getBreadboardPlacement();
        }

        boolean needExtendedBreadboard() {
            return yBottomHolePixel - yTopHolePixel + 2 * UNIT_HOLE_DISTANCE > Breadboard.LARGE.getLowerBottomLeftHoleCoordinate().getY() - Breadboard.LARGE.getUpperTopLeftHoleCoordinate().getY();
        }
    }

    private static final String deviceDirectoryPath = DeviceLibrary.INSTANCE.getLibraryPath().get() + File.separator + "devices";
    private static List<DeviceType> DRAWABLE_DEVICE_TYPES = List.of(DeviceType.CONTROLLER, DeviceType.MODULE, DeviceType.VIRTUAL);

    private static final double DEVICE_NAME_FONT_SIZE = 18.0;

    private static final double GLOBAL_TOP_MARGIN = 50.0;
    private static final double GLOBAL_LEFT_MARGIN = 50.0;

    private static final double BREADBOARD_REGION_H_GAP = 50.0;
    private static final double BREADBOARD_REGION_V_MARGIN = 80.0;

    private static final double TOP_REGION_H_GAP = 60.0;
    private static final double TOP_REGION_V_MARGIN = 50.0;
    private static final double BOTTOM_REGION_H_GAP = 30.0;
    private static final double BOTTOM_REGION_V_MARGIN = 50.0;
    private static final double TOP_MID_REGION_H_GAP = 30.0;
    private static final double TOP_MID_REGION_V_MARGIN = 80.0;
    private static final double BOTTOM_MID_REGION_H_GAP = 30.0;
    private static final double BOTTOM_MID_REGION_V_MARGIN = 80.0;
    private static final double LEFT_REGION_V_GAP = 30.0;
    private static final double LEFT_REGION_H_MARGIN = 80.0;
    private static final double RIGHT_REGION_V_GAP = 30.0;
    private static final double RIGHT_REGION_H_MARGIN = 80.0;

    private final Project project;
    private final ProjectConfiguration config;
    private final InteractiveModel interactiveModel;
    private final Map<ProjectDevice, ActualDevice> deviceMap;
    private final Map<ProjectDevice, DeviceConnection> deviceConnectionMap;

    private boolean controllerUseBreadboard;
    private Size breadboardRegionSize;
    private Size topMidRegionSize;
    private Size topRegionSize;
    private Size leftRegionSize;
    private Size controllerRegionSize;
    private Size bottomMidRegionSize;
    private Size bottomRegionSize;
    private Size rightRegionSize;
    private Size globalRegionSize;

    private List<VoltageLevel> breadboardVoltageLevelList = new ArrayList<>();

    private List<ProjectDevice> deviceNeedBreadboard = new ArrayList<>();
    private List<ProjectDevice> deviceOnLeftRegion = new ArrayList<>();
    private List<ProjectDevice> deviceOnRightRegion = new ArrayList<>();
    private List<ProjectDevice> deviceOnTopMidRegion = new ArrayList<>();
    private List<ProjectDevice> deviceOnBottomMidRegion = new ArrayList<>();
    private List<ProjectDevice> deviceOnTopRegion = new ArrayList<>();
    private List<ProjectDevice> deviceOnBottomRegion = new ArrayList<>();

    private List<BreadboardDeviceGroup> breadboardDeviceGroupList = new ArrayList<>();
    private Map<ProjectDevice, Coordinate> deviceCenterCoordinates = new HashMap<>();   // keep the coordinates of the device centroid in local region and later replaced by global region.
    private Map<ProjectDevice, Double> deviceRotationAngle = new HashMap<>();     // keep the angle that the device must be turned when drawing
    private Map<ProjectDevice, Size> deviceSizeAfterRotation = new HashMap<>();   // keep the device size after the rotation.

    private static double max(double... items) {
        if (items.length == 0) {
            throw new UnsupportedOperationException();
        }
        double maxValue = items[0];
        for (double x: items) {
            if (x > maxValue) {
                maxValue = x;
            }
        }
        return maxValue;
    }

    DiagramV1(Project project) {
        this.project = project;
        this.config = project.getProjectConfiguration();
        this.interactiveModel = project.getInteractiveModel();
        this.deviceMap = config.getUnmodifiableDeviceMap();
        this.deviceConnectionMap = config.getUnmodifiableDeviceConnections();

        assignDeviceRegion();
        calculateDeviceRotationAngle();
        calculateDeviceSizeAfterRotation();
        sortDevice();
        calculateRegionSizeAndDeviceCoordinates();
    }

    private void calculateDeviceSizeAfterRotation() {
        for (ProjectDevice projectDevice: deviceRotationAngle.keySet()) {
            ActualDevice actualDevice = deviceMap.get(projectDevice);
            if (deviceRotationAngle.get(projectDevice) == 0.0 || deviceRotationAngle.get(projectDevice) == 180.0) {
                deviceSizeAfterRotation.put(projectDevice, new Size(actualDevice.getWidth(), actualDevice.getHeight()));
            } else {
                deviceSizeAfterRotation.put(projectDevice, new Size(actualDevice.getHeight(), actualDevice.getWidth()));
            }
        }
    }

    private void calculateRegionSizeAndDeviceCoordinates() {
        Size controllerPreferredSize;
        if (controllerUseBreadboard) {
            controllerPreferredSize = new Size(0, 0);
        } else {
            controllerPreferredSize = new Size(config.getController().getWidth(), config.getController().getHeight());
        }

        Size breadboardRegionPreferredSize = calculateBreadboardRegionSizeAndAssignDeviceToBreadboardGroup(deviceNeedBreadboard, BREADBOARD_REGION_H_GAP, BREADBOARD_REGION_V_MARGIN);
        Size topMidPreferredSize = calculateHorizontalAlignedDevicesSize(deviceOnTopMidRegion, TOP_MID_REGION_H_GAP, TOP_MID_REGION_V_MARGIN);
        Size bottomMidPreferredSize = calculateHorizontalAlignedDevicesSize(deviceOnBottomMidRegion, BOTTOM_MID_REGION_H_GAP, BOTTOM_MID_REGION_V_MARGIN);
        Size leftPreferredSize = calculateVerticalAlignedDevicesSize(deviceOnLeftRegion, LEFT_REGION_V_GAP, LEFT_REGION_H_MARGIN);
        Size rightPreferredSize = calculateVerticalAlignedDevicesSize(deviceOnRightRegion, RIGHT_REGION_V_GAP, RIGHT_REGION_H_MARGIN);
        Size topPreferredSize = calculateHorizontalAlignedDevicesSize(deviceOnTopRegion, TOP_REGION_H_GAP, TOP_REGION_V_MARGIN);
        Size bottomPreferredSize = calculateHorizontalAlignedDevicesSize(deviceOnBottomRegion, BOTTOM_REGION_H_GAP, BOTTOM_REGION_V_MARGIN);

        double midActualWidth = max(controllerPreferredSize.getWidth(), topMidPreferredSize.getWidth(), bottomMidPreferredSize.getWidth(), breadboardRegionPreferredSize.getWidth());
        double midPreferredHeight = controllerPreferredSize.getHeight() + topMidPreferredSize.getHeight() + bottomMidPreferredSize.getHeight() + breadboardRegionPreferredSize.getHeight();
        double innerActualWidth = midActualWidth + leftPreferredSize.getWidth() + rightPreferredSize.getWidth();
        double innerActualHeight = max(midPreferredHeight, leftPreferredSize.getHeight(), rightPreferredSize.getHeight());
        double outerActualWidth = max(innerActualWidth, topPreferredSize.getWidth(), bottomPreferredSize.getWidth());
        double outerActualHeight = innerActualHeight + topPreferredSize.getHeight() + bottomPreferredSize.getHeight();

        this.controllerRegionSize = new Size(midActualWidth, controllerPreferredSize.getHeight());
        this.breadboardRegionSize = new Size(midActualWidth, breadboardRegionPreferredSize.getHeight());
        this.topMidRegionSize = new Size(midActualWidth, topMidPreferredSize.getHeight());
        this.bottomMidRegionSize = new Size(midActualWidth, bottomMidPreferredSize.getHeight());
        this.leftRegionSize = new Size(leftPreferredSize.getWidth(), innerActualHeight);
        this.rightRegionSize = new Size(rightPreferredSize.getWidth(), innerActualHeight);
        this.topRegionSize = new Size(outerActualWidth, topPreferredSize.getHeight());
        this.bottomRegionSize = new Size(outerActualWidth, bottomPreferredSize.getHeight());
        this.globalRegionSize = new Size(outerActualWidth, outerActualHeight);

        Coordinate topRegionTopLeft = new Coordinate(0.0, 0.0);
        Coordinate leftRegionTopLeft = new Coordinate(0.0, topRegionSize.getHeight());
        Coordinate topMidRegionTopLeft = new Coordinate(leftRegionSize.getWidth(), topRegionSize.getHeight());
        Coordinate breadboardRegionTopLeft = new Coordinate(leftRegionSize.getWidth(), topRegionSize.getHeight() + topMidRegionSize.getHeight());
        Coordinate controllerRegionTopLeft = new Coordinate(leftRegionSize.getWidth(), topRegionSize.getHeight() + topMidRegionSize.getHeight() + breadboardRegionSize.getHeight());
        Coordinate bottomMidRegionTopLeft = new Coordinate(leftRegionSize.getWidth(), topRegionSize.getHeight() + topMidRegionSize.getHeight() + breadboardRegionSize.getHeight() + controllerRegionSize.getHeight());
        Coordinate rightRegionTopLeft = new Coordinate(leftRegionSize.getWidth() + midActualWidth, topRegionSize.getHeight());
        Coordinate bottomRegionTopLeft = new Coordinate(0.0, topRegionSize.getHeight() + topMidRegionSize.getHeight() + breadboardRegionSize.getHeight() + controllerRegionSize.getHeight() + bottomMidPreferredSize.getHeight());

        double breadboardSpaceBegin = 0.5 * (breadboardRegionSize.getWidth() - breadboardRegionPreferredSize.getWidth());
        double topMidSpaceBegin = 0.5 * (topMidRegionSize.getWidth() - topMidPreferredSize.getWidth());
        double bottomMidSpaceBegin = 0.5 * (bottomMidRegionSize.getWidth() - bottomMidPreferredSize.getWidth());
        double leftSpaceBegin = 0.5 * (leftRegionSize.getHeight() - leftPreferredSize.getHeight());
        double rightSpaceBegin = 0.5 * (rightRegionSize.getHeight() - rightPreferredSize.getHeight());
        double topSpaceBegin = 0.5 * (topRegionSize.getWidth() - topPreferredSize.getWidth());
        double bottomSpaceBegin = 0.5 * (bottomRegionSize.getWidth() - bottomPreferredSize.getWidth());

        /* calculate global coordinate */
        if (!controllerUseBreadboard) {
            this.deviceCenterCoordinates.put(ProjectDevice.CONTROLLER, new Coordinate(controllerRegionTopLeft.getX() + 0.5 * controllerRegionSize.getWidth(), controllerRegionTopLeft.getY() + 0.5 * controllerRegionSize.getHeight()));
        }
        calculateBreadboardDeviceCoordinates(breadboardRegionTopLeft, breadboardSpaceBegin);
        calculateTopMidDeviceCoordinates(topMidRegionTopLeft, topMidSpaceBegin);
        calculateBottomMidDeviceCoordinates(bottomMidRegionTopLeft, bottomMidSpaceBegin);
        calculateLeftDeviceCoordinates(leftRegionTopLeft, leftSpaceBegin);
        calculateRightDeviceCoordinates(rightRegionTopLeft, rightSpaceBegin);
        calculateBottomDeviceCoordinates(bottomRegionTopLeft, bottomSpaceBegin);
        calculateTopDeviceCoordinates(topRegionTopLeft, topSpaceBegin);
    }

    private void calculateBreadboardDeviceCoordinates(Coordinate regionTopLeft, double spaceBegin) {
        double breadboardX = regionTopLeft.getX() + spaceBegin;
        for (BreadboardDeviceGroup group: breadboardDeviceGroupList) {
            Breadboard breadboard = group.getBreadboard();
            Coordinate breadboardTopLeftCoordinate = new Coordinate(breadboardX, regionTopLeft.getY() + breadboardRegionSize.getHeight() - breadboard.getHeight() - BREADBOARD_REGION_V_MARGIN);
            group.setBreadboardCoordinate(breadboardTopLeftCoordinate);
            int holeOffsetX = 1; // reserve 1 hole for leftmost blank space
            for (BreadboardDevice device: group.getDeviceList()) {
                double xShift = UNIT_HOLE_DISTANCE * Math.ceil(device.getXLeftHolePixel() / UNIT_HOLE_DISTANCE) - device.getXLeftHolePixel();
                if (device.getBreadboardPlacement() == BreadboardPlacement.ONE_SIDE) {
                    double centerX = breadboardTopLeftCoordinate.getX() + breadboard.getLowerTopLeftHoleCoordinate().getX() + xShift + holeOffsetX * UNIT_HOLE_DISTANCE + 0.5 * device.getActualDevice().getWidth();
                    double centerY = breadboardTopLeftCoordinate.getY() + breadboard.getLowerTopLeftHoleCoordinate().getY() - device.getYTopHolePixel() + 0.5 * device.getActualDevice().getHeight();
                    this.deviceCenterCoordinates.put(device.getProjectDevice(), new Coordinate(centerX, centerY));
                } else if (device.getBreadboardPlacement() == BreadboardPlacement.TWO_SIDES) {
                    double dy = device.getYBottomHolePixel() - device.getYTopHolePixel();
                    int devicePinRows = (int) Math.ceil(dy / UNIT_HOLE_DISTANCE) + 1;
                    double centerX = breadboardTopLeftCoordinate.getX() + breadboard.getUpperTopLeftHoleCoordinate().getX() + xShift + holeOffsetX * UNIT_HOLE_DISTANCE + 0.5 * device.getActualDevice().getWidth();
                    double topLeftY;
                    double bottomLeftY;
                    boolean halfHole = devicePinRows % 2 == 0;
                    if (device.needExtendedBreadboard() && breadboard == Breadboard.LARGE_EXTEND) {
                        bottomLeftY = breadboard.getLowerCenterBottomLeftHoleCoordinate().getY();
                        topLeftY = breadboard.getUpperCenterTopLeftHoleCoordinate().getY();
                    } else if (!device.needExtendedBreadboard() && (breadboard == Breadboard.LARGE || breadboard == Breadboard.SMALL)) {
                        bottomLeftY = breadboard.getLowerBottomLeftHoleCoordinate().getY();
                        topLeftY = breadboard.getUpperTopLeftHoleCoordinate().getY();
                    } else if (!device.needExtendedBreadboard() && (breadboard == Breadboard.LARGE_EXTEND)) {
                        bottomLeftY = breadboard.getUpperCenterBottomLeftHoleCoordinate().getY();
                        topLeftY = breadboard.getUpperTopLeftHoleCoordinate().getY();
                    } else {
                        throw new IllegalStateException("There is no implementation for " + breadboard + " breadboard");
                    }
                    double centerY = breadboardTopLeftCoordinate.getY()
                                    + 0.5 * (bottomLeftY
                                            + topLeftY
                                            - (devicePinRows - (halfHole ? 1 : 0)) * UNIT_HOLE_DISTANCE
                                            + device.getActualDevice().getHeight())
                                    - device.getYTopHolePixel();
                    this.deviceCenterCoordinates.put(device.getProjectDevice(), new Coordinate(centerX, centerY));
                } else {
                    throw new IllegalStateException("There is no implementation for " + device.getBreadboardPlacement());
                }
                holeOffsetX += (device.getNumColumnsWholeDevice() + 1);  // +1 for space between device
            }
            breadboardX += breadboard.getWidth();
            breadboardX += BREADBOARD_REGION_H_GAP;
        }
    }

    private void calculateTopDeviceCoordinates(Coordinate regionTopLeft, double spaceBegin) {
        double accumulateX = spaceBegin;
        for (ProjectDevice projectDevice: this.deviceOnTopRegion) {
            Size size = deviceSizeAfterRotation.get(projectDevice);
            this.deviceCenterCoordinates.put(projectDevice, new Coordinate(regionTopLeft.getX() + accumulateX + 0.5 * size.getWidth(), regionTopLeft.getY() + topRegionSize.getHeight() - 0.5 * size.getHeight() - TOP_REGION_V_MARGIN));
            accumulateX += (size.getWidth() + TOP_REGION_H_GAP);
        }
    }

    private void calculateBottomDeviceCoordinates(Coordinate regionTopLeft, double spaceBegin) {
        double accumulateX = spaceBegin;
        for (ProjectDevice projectDevice: this.deviceOnBottomRegion) {
            Size size = deviceSizeAfterRotation.get(projectDevice);
            this.deviceCenterCoordinates.put(projectDevice, new Coordinate(regionTopLeft.getX() + accumulateX + 0.5 * size.getWidth(), regionTopLeft.getY() + bottomRegionSize.getHeight() - 0.5 * size.getHeight() - BOTTOM_REGION_V_MARGIN));
            accumulateX += (size.getWidth() + BOTTOM_REGION_H_GAP);
        }
    }

    private void calculateRightDeviceCoordinates(Coordinate regionTopLeft, double spaceBegin) {
        double accumulateY = spaceBegin;
        for (ProjectDevice projectDevice: this.deviceOnRightRegion) {
            Size size = deviceSizeAfterRotation.get(projectDevice);
            this.deviceCenterCoordinates.put(projectDevice, new Coordinate(regionTopLeft.getX() + 0.5 * size.getWidth() + RIGHT_REGION_H_MARGIN, regionTopLeft.getY() + accumulateY + 0.5 * size.getHeight()));
            accumulateY += (size.getHeight() + RIGHT_REGION_V_GAP + DEVICE_NAME_FONT_SIZE);
        }
    }

    private void calculateLeftDeviceCoordinates(Coordinate regionTopLeft, double spaceBegin) {
        double accumulateY = spaceBegin;
        for (ProjectDevice projectDevice: this.deviceOnLeftRegion) {
            Size size = deviceSizeAfterRotation.get(projectDevice);
            accumulateY += DEVICE_NAME_FONT_SIZE;
            this.deviceCenterCoordinates.put(projectDevice, new Coordinate(regionTopLeft.getX() + leftRegionSize.getWidth() - 0.5 * size.getWidth() - LEFT_REGION_H_MARGIN, regionTopLeft.getY() + accumulateY + 0.5 * size.getHeight()));
            accumulateY += (size.getHeight() + LEFT_REGION_V_GAP + DEVICE_NAME_FONT_SIZE);
        }
    }

    private void calculateTopMidDeviceCoordinates(Coordinate regionTopLeft, double spaceBegin) {
        double accumulateX = spaceBegin;
        for (ProjectDevice projectDevice: this.deviceOnTopMidRegion) {
            Size size = deviceSizeAfterRotation.get(projectDevice);
            this.deviceCenterCoordinates.put(projectDevice, new Coordinate(regionTopLeft.getX() + accumulateX + 0.5 * size.getWidth(), regionTopLeft.getY() + topMidRegionSize.getHeight() - 0.5 * size.getHeight() - TOP_MID_REGION_V_MARGIN));
            accumulateX += (size.getWidth() + TOP_MID_REGION_H_GAP);
        }
    }

    private void calculateBottomMidDeviceCoordinates(Coordinate regionTopLeft, double spaceBegin) {
        double accumulateX = spaceBegin;
        for (ProjectDevice projectDevice: this.deviceOnBottomMidRegion) {
            Size size = deviceSizeAfterRotation.get(projectDevice);
            this.deviceCenterCoordinates.put(projectDevice, new Coordinate(regionTopLeft.getX() + accumulateX + 0.5 * size.getWidth(), regionTopLeft.getY() + 0.5 * size.getHeight() + BOTTOM_MID_REGION_V_MARGIN));
            accumulateX += (size.getWidth() + BOTTOM_MID_REGION_H_GAP);
        }
    }

    private List<VoltageLevel> getVoltageLevelUsed(ProjectDevice projectDevice) {
        List<VoltageLevel> voltageLevelList = new ArrayList<>();
        if (deviceConnectionMap.containsKey(projectDevice)) {
            Map<Connection, Connection> connectionMap = deviceConnectionMap.get(projectDevice).getConsumerProviderConnections();
            for (Connection consumerConnection: connectionMap.keySet()) {
                Connection providerConnection = connectionMap.get(consumerConnection);
                if (providerConnection != null) {
                    voltageLevelList.addAll(providerConnection.getPins().stream()
                            .filter(pin -> pin.getFunction().contains(PinFunction.VCC))
                            .map(Pin::getVoltageLevel)
                            .collect(Collectors.toList()));
                }
            }
        }
        return voltageLevelList;
    }

    private List<VoltageLevel> getVoltageLevelUsed(BreadboardDevice breadboardDevice) {
        return getVoltageLevelUsed(breadboardDevice.getProjectDevice());
    }

    private Size calculateBreadboardRegionSizeAndAssignDeviceToBreadboardGroup(List<ProjectDevice> projectDeviceList, double gapBetweenBreadboard, double gapToController) {
        if (projectDeviceList.isEmpty()) {
            return new Size(0, 0);
        }
        List<BreadboardDevice> breadboardDeviceList = projectDeviceList.stream()
                .map((ProjectDevice projectDevice1) -> new BreadboardDevice(projectDevice1, deviceMap.get(projectDevice1)))
                .collect(Collectors.toList());

        Map<VoltageLevel, Long> voltageLevelCount = breadboardDeviceList.stream()
                .map(this::getVoltageLevelUsed)
                .flatMap(Collection::stream)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        if (voltageLevelCount.size() > 3) {
            throw new UnsupportedOperationException("Breadboard diagram cannot handle more than 3 voltage levels.");
        }

        breadboardVoltageLevelList.clear();
        breadboardVoltageLevelList.addAll(voltageLevelCount.keySet());

        // maximum count comes first
        breadboardVoltageLevelList.sort((v1, v2) -> (int) (voltageLevelCount.get(v2) - voltageLevelCount.get(v1)));

        breadboardDeviceList.sort((d1, d2) -> {
            if (d1.getProjectDevice() == ProjectDevice.CONTROLLER) {
                return -1;
            } else if (d2.getProjectDevice() == ProjectDevice.CONTROLLER) {
                return 1;
            }
            List<VoltageLevel> lvl1 = getVoltageLevelUsed(d1);
            List<VoltageLevel> lvl2 = getVoltageLevelUsed(d2);
            for (VoltageLevel v: breadboardVoltageLevelList) {
                if (lvl1.contains(v) && !lvl2.contains(v)) { return -1; }
                if (!lvl1.contains(v) && lvl2.contains(v)) { return 1; }
            }
            // multiple voltage levels before one voltage level
            return lvl2.size() - lvl1.size();
        });

        double regionHeight = Breadboard.LARGE.getHeight();
        double regionWidth = 0;
        while(!breadboardDeviceList.isEmpty()) {
            // Find total number of holes for all devices in remainingDeviceList
            int numberOfColumnsUsed = breadboardDeviceList.stream().mapToInt(BreadboardDevice::getNumColumnsWholeDevice).sum() + (breadboardDeviceList.size() - 1);
            boolean needExtended = breadboardDeviceList.stream().anyMatch(BreadboardDevice::needExtendedBreadboard);
            Breadboard breadboard;
            if (needExtended) { // +2 for allowing wire on top and bottom
                breadboard = Breadboard.LARGE_EXTEND;
            } else if (numberOfColumnsUsed > Breadboard.SMALL.getNumColumns()) {
                breadboard = Breadboard.LARGE;
            } else {
                breadboard = Breadboard.SMALL;
            }
            BreadboardDeviceGroup group = new BreadboardDeviceGroup(breadboard);
            int availableColumns = breadboard.getNumColumns() - 2; // leftmost and rightmost columns are reserved for blank space.
            // Calculate width
            regionWidth += breadboard.getWidth();
            while (availableColumns > 0 && !breadboardDeviceList.isEmpty()) {
                BreadboardDevice device = breadboardDeviceList.get(0);
                if (device.getNumColumnsWholeDevice() == 0) {
                    throw new IllegalStateException("It's look like the device does not require breadboard hole. It is not possible for device that needs breadboard");
                }
                if (device.getNumColumnsWholeDevice() > availableColumns) break;
                availableColumns -= (device.getNumColumnsWholeDevice() + 1); // +1 for gap between device
                group.getDeviceList().add(device);
                breadboardDeviceList.remove(device);

                // calculate possible new max height
                double height = 0.0;
                if (device.getBreadboardPlacement() == BreadboardPlacement.ONE_SIDE) {
                    height = breadboard.getHeight() - breadboard.getUpperTopLeftHoleCoordinate().getY() + device.getYTopHolePixel();
                } else if (device.getBreadboardPlacement() == BreadboardPlacement.TWO_SIDES) {
                    if (breadboard == Breadboard.LARGE_EXTEND && device.getYBottomHolePixel() - device.getYTopHolePixel() > breadboard.getLowerCenterBottomLeftHoleCoordinate().getY() - breadboard.getUpperCenterTopLeftHoleCoordinate().getY() + 2 * UNIT_HOLE_DISTANCE) {
                        throw new IllegalStateException("Device have larger size than breadboard");
                    } else if ((breadboard == Breadboard.LARGE || breadboard == Breadboard.SMALL) && device.getYBottomHolePixel() - device.getYTopHolePixel() > breadboard.getLowerBottomLeftHoleCoordinate().getY() - breadboard.getUpperTopLeftHoleCoordinate().getY() + 2 * UNIT_HOLE_DISTANCE) {
                        throw new IllegalStateException("Device have larger size than breadboard");
                    }
                } else {
                    throw new IllegalStateException("There is no implementation for " + device.getBreadboardPlacement());
                }
                regionHeight = Math.max(height, regionHeight);
            }
            if (group.getDeviceList().isEmpty()) {
                throw new UnsupportedOperationException("Device length seems longer than breadboard.");
            } else {
                breadboardDeviceGroupList.add(group);
            }
        }
        regionWidth += (breadboardDeviceGroupList.size() - 1) * gapBetweenBreadboard;
        regionHeight += gapToController;
        return new Size(regionWidth, regionHeight);
    }

    private Size calculateVerticalAlignedDevicesSize(List<ProjectDevice> projectDeviceList, double gapBetweenDevice, double gapToCenterRegion) {
        double totalWidth = 0.0;
        double totalHeight = 0.0;
        for (ProjectDevice projectDevice: projectDeviceList) {
            Size size = deviceSizeAfterRotation.get(projectDevice);
            totalWidth = Math.max(totalWidth, size.getWidth());
            totalHeight += size.getHeight();
        }
        if (!projectDeviceList.isEmpty()) {
            totalWidth += gapToCenterRegion;                                    // gap between devices.
            totalHeight += (projectDeviceList.size() - 1) * gapBetweenDevice;   // gap to center region.
            totalHeight += projectDeviceList.size() * DEVICE_NAME_FONT_SIZE;
        }
        return new Size(totalWidth, totalHeight);
    }

    private Size calculateHorizontalAlignedDevicesSize(List<ProjectDevice> projectDeviceList, double gapBetweenDevice, double gapToCenterRegion) {
        double totalWidth = 0.0;
        double totalHeight = 0.0;
        for (ProjectDevice projectDevice: projectDeviceList) {
            Size size = deviceSizeAfterRotation.get(projectDevice);
            totalWidth += size.getWidth();
            totalHeight = Math.max(totalHeight, size.getHeight());
        }
        if (!projectDeviceList.isEmpty()) {
            totalWidth += (projectDeviceList.size() - 1) * gapBetweenDevice;    // gap between devices.
            totalHeight += gapToCenterRegion;                                   // gap to center region.
            totalHeight += DEVICE_NAME_FONT_SIZE;
        }
        return new Size(totalWidth, totalHeight);
    }

    private void sortDevice() {
        deviceOnLeftRegion.sort(Comparator.comparingDouble(device -> {
            Coordinate coordinate = getConnectionCentroidToDevice(ProjectDevice.CONTROLLER, device);
            return coordinate != null ? coordinate.getY() : Double.MAX_VALUE;
        }));
        deviceOnRightRegion.sort(Comparator.comparingDouble(device -> {
            Coordinate coordinate = getConnectionCentroidToDevice(ProjectDevice.CONTROLLER, device);
            return coordinate != null ? coordinate.getY() : Double.MAX_VALUE;
        }));
        deviceOnTopMidRegion.sort(Comparator.comparingDouble(device -> {
            Coordinate coordinate = getConnectionCentroidToDevice(ProjectDevice.CONTROLLER, device);
            return coordinate != null ? coordinate.getX() : Double.MAX_VALUE;
        }));
        deviceOnBottomMidRegion.sort(Comparator.comparingDouble(device -> {
            Coordinate coordinate = getConnectionCentroidToDevice(ProjectDevice.CONTROLLER, device);
            return coordinate != null ? coordinate.getX() : Double.MAX_VALUE;
        }));
        deviceOnTopRegion.sort(ProjectDevice.NAME_COMPARATOR);
        deviceOnBottomRegion.sort(ProjectDevice.NAME_COMPARATOR);
        deviceNeedBreadboard.sort(Comparator.comparingDouble(device -> {
            Coordinate coordinate = getConnectionCentroidToDevice(ProjectDevice.CONTROLLER, device);
            return coordinate != null ? coordinate.getX() : Double.MAX_VALUE;
        }));
    }

    private void calculateDeviceRotationAngle() {
        deviceRotationAngle.put(ProjectDevice.CONTROLLER, 0.0);
        for (ProjectDevice projectDevice: deviceOnLeftRegion) {
            ConnectionPosition position = getConnectionPosition(projectDevice);
            if (position == ConnectionPosition.LEFT) {
                deviceRotationAngle.put(projectDevice, 180.0); // 180 degrees
            } else if (position == ConnectionPosition.TOP) {
                deviceRotationAngle.put(projectDevice, 90.0); // clock wise
            } else if (position == ConnectionPosition.BOTTOM) {
                deviceRotationAngle.put(projectDevice, -90.0); // counter clockwise
            } else if (position == ConnectionPosition.RIGHT) {
                deviceRotationAngle.put(projectDevice, 0.0); // no rotation
            } else {
                throw new UnsupportedOperationException();
            }
        }
        for (ProjectDevice projectDevice: deviceOnRightRegion) {
            ConnectionPosition position = getConnectionPosition(projectDevice);
            if (position == ConnectionPosition.LEFT) {
                deviceRotationAngle.put(projectDevice, 0.0); // 180 degrees
            } else if (position == ConnectionPosition.TOP) {
                deviceRotationAngle.put(projectDevice, -90.0); // counter clockwise
            } else if (position == ConnectionPosition.BOTTOM) {
                deviceRotationAngle.put(projectDevice, 90.0); // clockwise
            } else if (position == ConnectionPosition.RIGHT) {
                deviceRotationAngle.put(projectDevice, 180.0); // no rotation
            } else {
                throw new UnsupportedOperationException();
            }
        }
        for (ProjectDevice projectDevice: deviceOnTopMidRegion) {
            ConnectionPosition position = getConnectionPosition(projectDevice);
            if (position == ConnectionPosition.LEFT) {
                deviceRotationAngle.put(projectDevice, -90.0); // counter clockwise
            } else if (position == ConnectionPosition.TOP) {
                deviceRotationAngle.put(projectDevice, 180.0); // 180 degrees
            } else if (position == ConnectionPosition.BOTTOM) {
                deviceRotationAngle.put(projectDevice, 0.0); // no rotation
            } else if (position == ConnectionPosition.RIGHT) {
                deviceRotationAngle.put(projectDevice, 90.0); // clock wise
            } else {
                throw new UnsupportedOperationException();
            }
        }
        for (ProjectDevice projectDevice: deviceOnBottomMidRegion) {
            ConnectionPosition position = getConnectionPosition(projectDevice);
            if (position == ConnectionPosition.LEFT) {
                deviceRotationAngle.put(projectDevice, 90.0); // clock wise
            } else if (position == ConnectionPosition.TOP) {
                deviceRotationAngle.put(projectDevice, 0.0); // no rotation
            } else if (position == ConnectionPosition.BOTTOM) {
                deviceRotationAngle.put(projectDevice, 180.0); // 180 degrees
            } else if (position == ConnectionPosition.RIGHT) {
                deviceRotationAngle.put(projectDevice, -90.0); // counter clockwise
            } else {
                throw new UnsupportedOperationException();
            }
        }
        for (ProjectDevice projectDevice: deviceOnTopRegion) {
            deviceRotationAngle.put(projectDevice, 0.0); // no rotation
        }
        for (ProjectDevice projectDevice: deviceOnBottomRegion) {
            deviceRotationAngle.put(projectDevice, 0.0); // no rotation
        }
        for (ProjectDevice projectDevice: deviceNeedBreadboard) {
            deviceRotationAngle.put(projectDevice, 0.0); // no rotation
        }
    }

    private void assignDeviceRegion() {
        List<ProjectDevice> allDevices = deviceMap.keySet().stream()
                .filter(projectDevice -> projectDevice != ProjectDevice.CONTROLLER)
                .filter(projectDevice -> DRAWABLE_DEVICE_TYPES.contains(deviceMap.get(projectDevice).getDeviceType()))
                .collect(Collectors.toList());

        boolean deviceUseBreadboard = allDevices.stream().anyMatch(projectDevice -> deviceMap.get(projectDevice).isNeedBreadboard());
        this.controllerUseBreadboard = deviceMap.get(ProjectDevice.CONTROLLER).isNeedBreadboard();

        if (deviceUseBreadboard && !controllerUseBreadboard) {
            allDevices.forEach(projectDevice -> {
                ActualDevice actualDevice = deviceMap.get(projectDevice);
                if (actualDevice instanceof IntegratedActualDevice) {
                    return;
                }
                if (actualDevice.isNeedBreadboard()) {
                    deviceNeedBreadboard.add(projectDevice);
                    return;
                }
                Region region = getDeviceRegionHaveBreadboard(projectDevice);
                if (region == Region.LEFT) {
                    deviceOnLeftRegion.add(projectDevice);
                } else if (region == Region.RIGHT) {
                    deviceOnRightRegion.add(projectDevice);
                } else if (region == Region.BOTTOM_MID) {
                    deviceOnBottomMidRegion.add(projectDevice);
                } else if (region == Region.BOTTOM) {
                    deviceOnBottomRegion.add(projectDevice);
                } else if (region == Region.TOP) {
                    deviceOnTopRegion.add(projectDevice);
                } else {
                    throw new UnsupportedOperationException();
                }
            });
        } else {
            if (controllerUseBreadboard) {
                deviceNeedBreadboard.add(ProjectDevice.CONTROLLER);
            }
            allDevices.forEach(projectDevice -> {
                ActualDevice actualDevice = deviceMap.get(projectDevice);
                if (actualDevice instanceof IntegratedActualDevice) {
                    return;
                }
                if (actualDevice.isNeedBreadboard()) {
                    deviceNeedBreadboard.add(projectDevice);
                    return;
                }
                Region region = getDeviceRegionNoBreadboard(projectDevice);
                if (region == Region.LEFT) {
                    deviceOnLeftRegion.add(projectDevice);
                } else if (region == Region.RIGHT) {
                    deviceOnRightRegion.add(projectDevice);
                } else if (region == Region.TOP_MID) {
                    deviceOnTopMidRegion.add(projectDevice);
                } else if (region == Region.BOTTOM_MID) {
                    deviceOnBottomMidRegion.add(projectDevice);
                } else if (region == Region.BOTTOM) {
                    deviceOnBottomRegion.add(projectDevice);
                } else if (region == Region.TOP) {
                    deviceOnTopRegion.add(projectDevice);
                } else {
                    throw new UnsupportedOperationException();
                }
            });
        }
    }

    private List<Pin> getAllConnectedPin(ProjectDevice device) {
        List<Pin> allPins = new ArrayList<>();
        for (DeviceConnection connection: deviceConnectionMap.values()) {
            connection.getConsumerProviderConnections().forEach((connection1, connection2) -> {
                if (Objects.isNull(connection1) || Objects.isNull(connection2)) {
                    return;
                }
                if (connection1.getOwnerProjectDevice() == device || connection2.getOwnerProjectDevice() == device) {
                    allPins.addAll(connection1.getPins());
                }
            });
        }
        return allPins;
    }

    private List<Pin> getAllConnectedPinToDevice(ProjectDevice device, ProjectDevice deviceTo) {
        List<Pin> allPins = new ArrayList<>();
        for (DeviceConnection connection: deviceConnectionMap.values()) {
            connection.getConsumerProviderConnections().forEach((connection1, connection2) -> {
                if (Objects.isNull(connection1) || Objects.isNull(connection2)) {
                    return;
                }
                if (connection1.getOwnerProjectDevice() == device && connection2.getOwnerProjectDevice() == deviceTo) {
                    allPins.addAll(connection1.getPins());
                }
                if (connection1.getOwnerProjectDevice() == deviceTo && connection2.getOwnerProjectDevice() == device) {
                    allPins.addAll(connection2.getPins());
                }
            });
        }
        return allPins;
    }

    private Coordinate getConnectionCentroid(ProjectDevice device) {
        List<Pin> connectedPins = getAllConnectedPin(device);
        if (connectedPins.isEmpty()) {
            return new Coordinate(0, 0);
        }
        double sumX = 0.0, sumY = 0.0;
        for (Pin pin: connectedPins) {
            sumX += pin.getX();
            sumY += pin.getY();
        }
        return new Coordinate(sumX / connectedPins.size(), sumY / connectedPins.size());
    }

    private Coordinate getConnectionCentroidToDevice(ProjectDevice device, ProjectDevice deviceTo) {
        List<Pin> connectedPins = getAllConnectedPinToDevice(device, deviceTo);
        if (connectedPins.isEmpty()) {
            return null;
        }
        double sumX = 0.0, sumY = 0.0;
        for (Pin pin: connectedPins) {
            sumX += pin.getX();
            sumY += pin.getY();
        }
        return new Coordinate(sumX / connectedPins.size(), sumY / connectedPins.size());
    }

    private ConnectionPosition getConnectionPosition(ProjectDevice projectDevice) {
        Coordinate center = getConnectionCentroid(projectDevice);
        Optional<ActualDevice> actualDevice = config.getActualDevice(projectDevice);
        if (actualDevice.isEmpty() || !DRAWABLE_DEVICE_TYPES.contains(actualDevice.get().getDeviceType())) {
            throw new IllegalStateException();
        }
        double width = actualDevice.get().getWidth();
        double height = actualDevice.get().getHeight();
        if (width == 0 || height == 0) {
            throw new IllegalStateException();
        }
        /* Use center of device as origin. the coordinates are as follows
         *                            top
         *     A (-w/2, h/2)   \ ----------- /  B (w/2, h/2)
         *                     | \         / |
         *                     |   DEVICE/   |
         *                     |     \ /   o <------(x-w/2, h/2-y)
         *              left   |     / \     | right
         *                     |   /     \   |
         *                     | /         \ |
         *     C (-w/2, -h/2)  / ----------- \  D (w/2, -h/2)
         *                  bottom
         */

        // check whether (x, y) is rely on what region by calculating the angle offset around the origin.
        // Note that: atan2 return [-pi, pi]
        double angle = Math.atan2(0.5 * height - center.getY(), center.getX() - 0.5 * width);
        double angleA = Math.atan2(0.5 * height, -0.5 * width);
        double angleB = Math.atan2(0.5 * height, 0.5 * width);
        double angleC = Math.atan2(-0.5 * height, -0.5 * width);
        double angleD = Math.atan2(-0.5 * height, 0.5 * width);
        if (angle <= angleA && angle >= angleB) {
            return ConnectionPosition.TOP;
        }
        if (angle > angleA || angle < angleC) {
            return ConnectionPosition.LEFT;
        }
        if (angle >= angleC && angle <= angleD) {
            return ConnectionPosition.BOTTOM;
        }
        return ConnectionPosition.RIGHT;
    }


    /* Use connection centroid on the controller to determine the region of the module.
     *                         top-mid
     *                     ---------------
     *                     |  |       |  |
     *                     |  CONTROLLER |
     *                     |  |_______|  |
     *              left   |  |       |  | right
     *                     |  |       |  |
     *                     |  |       |  |
     *                     ---------------
     *                        bottom-mid
     */
    private Region getDeviceRegionNoBreadboard(ProjectDevice module) {
        if (DeviceLibrary.INSTANCE.getGenericCloudDevice().contains(module.getGenericDevice())) {
            return Region.TOP;
        }
        Coordinate coordinate = getConnectionCentroidToDevice(ProjectDevice.CONTROLLER, module);
        if (coordinate == null) {
            return Region.BOTTOM;
        }
        ActualDevice controller = config.getController();
        if (coordinate.getX() < 0.2 * controller.getWidth()) {
            return Region.LEFT;
        }
        if (coordinate.getX() > 0.8 * controller.getWidth()) {
            return Region.RIGHT;
        }
        if (coordinate.getY() > 0.5 * controller.getHeight()) {
            return Region.BOTTOM_MID;
        }
        return Region.TOP_MID;
    }

    /* Use connection centroid on the controller to determine the region of the module.
     *                 top-mid zone is reserved for breadboard
     *                     ---------------
     *                     |      |      |
     *                left |      |      | right
     *                     |  CONTROLLER |
     *                     |      |      |
     *                     |-------------|
     *                     |             |
     *                     ---------------
     *                        bottom-mid
     */
    private Region getDeviceRegionHaveBreadboard(ProjectDevice module) {
        if (DeviceLibrary.INSTANCE.getGenericCloudDevice().contains(module.getGenericDevice())) {
            return Region.TOP;
        }
        Coordinate coordinate = getConnectionCentroidToDevice(ProjectDevice.CONTROLLER, module);
        if (coordinate == null) {
            return Region.BOTTOM;
        }
        ActualDevice controller = config.getController();
        if (coordinate.getY() > 0.65 * controller.getHeight()) {
            return Region.BOTTOM_MID;
        }
        if (coordinate.getX() < 0.5 * controller.getWidth()) {
            return Region.LEFT;
        }
        if (coordinate.getX() >= 0.5 * controller.getWidth()) {
            return Region.RIGHT;
        }
        throw new UnsupportedOperationException();
    }

    private void drawDevice(Pane drawingPane, ProjectDevice projectDevice) {
        if (deviceMap.get(projectDevice) instanceof IntegratedActualDevice) {
            return;
        }
        try (InputStream deviceImageStream = Files.newInputStream(DeviceLibrary.getDeviceImagePath(deviceMap.get(projectDevice)))) {
            double deviceAngle = deviceRotationAngle.get(projectDevice);
            Image image = new Image(deviceImageStream);
            ImageView imageView = new ImageView(image);
            Coordinate coordinate = deviceCenterCoordinates.get(projectDevice);
            ActualDevice actualDevice = deviceMap.get(projectDevice);
            imageView.setOnMouseEntered(event -> {
                if (interactiveModel.isStarted()) {
                    imageView.setEffect(new DropShadow(20.0, Color.web("#2673fd")));
                }
            });
            imageView.setOnMousePressed(event -> {
                if (interactiveModel.isStarted()) {
                    showInteractiveDevicePropertyWindow(imageView, projectDevice);
                }
            });
            imageView.setOnMouseExited(event -> {
                if (interactiveModel.isStarted()) {
                    imageView.setEffect(null);
                }
            });
            imageView.setLayoutX(coordinate.getX() - 0.5 * actualDevice.getWidth() + GLOBAL_LEFT_MARGIN);
            imageView.setLayoutY(coordinate.getY() - 0.5 * actualDevice.getHeight() + GLOBAL_TOP_MARGIN);
            imageView.setRotate(deviceAngle);
            Size sizeAfterRotation = deviceSizeAfterRotation.get(projectDevice);
            drawingPane.getChildren().add(imageView);

            // this actual device may be shared by multiple project device so we list their name separate by a comma as the actual device name
            List<ProjectDevice> projectDeviceList = new ArrayList<>();
            projectDeviceList.add(projectDevice);
            projectDeviceList.addAll(config.getDeviceWithSameIdenticalDevice(projectDevice));
            String deviceName = projectDeviceList.stream().map(ProjectDevice::getName).collect(Collectors.joining(",\n"));
            // DEVICE_NAME_FONT_SIZE * projectDeviceList.size() is not the theoretically correct way to measure text height as text height
            // is not equal to the font size for every font (most of them don't) but it works fine with the standard font we use and JavaFX
            // doesn't provide a pubic API for measure text size yet so we do it this way for now
            if (deviceOnBottomMidRegion.contains(projectDevice) || deviceOnBottomRegion.contains(projectDevice)) {
                Text text = new Text(deviceName);
                text.setX(coordinate.getX() - 0.5 * sizeAfterRotation.getWidth() + GLOBAL_LEFT_MARGIN);
                text.setY(coordinate.getY() + 0.5 * sizeAfterRotation.getHeight() + (DEVICE_NAME_FONT_SIZE * projectDeviceList.size()) + GLOBAL_TOP_MARGIN);
                text.setStyle("-fx-font-size: " + DEVICE_NAME_FONT_SIZE);
                drawingPane.getChildren().add(text);
            } else if (deviceOnTopMidRegion.contains(projectDevice) || deviceOnTopRegion.contains(projectDevice)) {
                Text text = new Text(deviceName);
                text.setX(coordinate.getX() - 0.5 * sizeAfterRotation.getWidth() + GLOBAL_LEFT_MARGIN);
                text.setY(coordinate.getY() - 0.5 * sizeAfterRotation.getHeight() - (DEVICE_NAME_FONT_SIZE * projectDeviceList.size())  + GLOBAL_TOP_MARGIN);
                text.setStyle("-fx-font-size: " + DEVICE_NAME_FONT_SIZE);
                drawingPane.getChildren().add(text);
            } else if (deviceOnLeftRegion.contains(projectDevice) || deviceOnRightRegion.contains(projectDevice)) {
                Text text = new Text(deviceName);
                text.setX(coordinate.getX() - 0.45 * sizeAfterRotation.getWidth() + GLOBAL_LEFT_MARGIN);
                text.setY(coordinate.getY() - 0.5 * sizeAfterRotation.getHeight() - (DEVICE_NAME_FONT_SIZE * projectDeviceList.size()) + GLOBAL_TOP_MARGIN);
                text.setStyle("-fx-font-size: " + DEVICE_NAME_FONT_SIZE);
                drawingPane.getChildren().add(text);
            } else if (deviceNeedBreadboard.contains(projectDevice)) {
                Text text = new Text(deviceName);
                text.setX(coordinate.getX() - 0.5 * sizeAfterRotation.getWidth() + GLOBAL_LEFT_MARGIN);
                text.setY(coordinate.getY() - 0.5 * sizeAfterRotation.getHeight() - (DEVICE_NAME_FONT_SIZE * projectDeviceList.size())  + GLOBAL_TOP_MARGIN);
                text.setStyle("-fx-font-size: " + DEVICE_NAME_FONT_SIZE);
                drawingPane.getChildren().add(text);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Image not found for : " + deviceMap.get(projectDevice).getId());
        }
    }

    private void showInteractiveDevicePropertyWindow(Node button, ProjectDevice device) {
        List<ProjectDevice> deviceList;
        if (device == ProjectDevice.CONTROLLER) {
            deviceList = project.getUnmodifiableProjectDevice().stream()
                    .filter(projectDevice -> config.getActualDevice(projectDevice).orElse(null) instanceof IntegratedActualDevice)
                    .collect(Collectors.toUnmodifiableList());
        } else {
            deviceList = new ArrayList<>();
            deviceList.add(device);
            deviceList.addAll(config.getDeviceWithSameIdenticalDevice(device));
        }
        if (!deviceList.isEmpty()) {
            InteractiveDevicePropertyWindow interactiveDevicePropertyWindow = new InteractiveDevicePropertyWindow(deviceList, interactiveModel, project);
            interactiveDevicePropertyWindow.setArrowLocation(PopOver.ArrowLocation.TOP_LEFT);
            interactiveDevicePropertyWindow.show(button);
        }
    }

    Pane make() {
        Pane wiringDiagram = new Pane();
        wiringDiagram.setPrefWidth(globalRegionSize.getWidth() + 2 * GLOBAL_TOP_MARGIN);
        wiringDiagram.setPrefHeight(globalRegionSize.getHeight() + 2 * GLOBAL_LEFT_MARGIN);
        for (BreadboardDeviceGroup group: breadboardDeviceGroupList) {
            group.drawBreadboard(wiringDiagram);
        }
        for (ProjectDevice projectDevice: deviceCenterCoordinates.keySet()) {
            drawDevice(wiringDiagram, projectDevice);
        }
        for (ProjectDevice projectDevice: deviceOnLeftRegion) {
            drawConnection(wiringDiagram, deviceConnectionMap.get(projectDevice));
        }
        for (ProjectDevice projectDevice: deviceOnRightRegion) {
            drawConnection(wiringDiagram, deviceConnectionMap.get(projectDevice));
        }
        for (ProjectDevice projectDevice: deviceOnTopMidRegion) {
            drawConnection(wiringDiagram, deviceConnectionMap.get(projectDevice));
        }
        for (ProjectDevice projectDevice: deviceOnBottomMidRegion) {
            drawConnection(wiringDiagram, deviceConnectionMap.get(projectDevice));
        }
        for (BreadboardDeviceGroup group: breadboardDeviceGroupList) {
            for (BreadboardDevice device: group.getDeviceList()) {
                drawConnection(wiringDiagram, deviceConnectionMap.get(device.getProjectDevice()));
            }
        }
        return wiringDiagram;
    }

    private static void drawCubicCurve(Pane drawingPane, Coordinate coordinateFrom, Coordinate coordinateTo,
                                       double controlX1, double controlY1,
                                       double controlX2, double controlY2,
                                       double lineWidth, Color color) {
        Path path = new Path();
        path.getElements().add(new MoveTo(coordinateFrom.getX() + GLOBAL_LEFT_MARGIN, coordinateFrom.getY() + GLOBAL_TOP_MARGIN));
        path.getElements().add(new CubicCurveTo(controlX1 + GLOBAL_LEFT_MARGIN,
                                                controlY1 + GLOBAL_TOP_MARGIN,
                                                controlX2 + GLOBAL_LEFT_MARGIN,
                                                controlY2 + GLOBAL_TOP_MARGIN,
                                                coordinateTo.getX() + GLOBAL_LEFT_MARGIN,
                                                coordinateTo.getY() + GLOBAL_TOP_MARGIN));
        path.setStrokeWidth(lineWidth);
        path.setStroke(color);
        path.setStrokeLineCap(StrokeLineCap.ROUND);
        path.setSmooth(true);
        path.setEffect(new DropShadow(1.0, color.darker().darker()));
        path.addEventFilter(MouseEvent.MOUSE_ENTERED_TARGET, event -> path.setEffect(new DropShadow(5.0, color.brighter().brighter())));
        path.addEventFilter(MouseEvent.MOUSE_EXITED_TARGET, event -> path.setEffect(new DropShadow(1.0, color.darker().darker())));
        path.setPickOnBounds(false);
        drawingPane.getChildren().add(path);
    }

    private static void drawCubicCurveByControlRatio(Pane drawingPane, Coordinate coordinateFrom, Coordinate coordinateTo,
                                                     double controlRatioX1, double controlRatioY1,
                                                     double controlRatioX2, double controlRatioY2,
                                                     double lineWidth, Color color) {
        double controlX1 = coordinateFrom.getX() + controlRatioX1 * (coordinateTo.getX() - coordinateFrom.getX());
        double controlY1 = coordinateFrom.getY() + controlRatioY1 * (coordinateTo.getY() - coordinateFrom.getY());
        double controlX2 = coordinateFrom.getX() + controlRatioX2 * (coordinateTo.getX() - coordinateFrom.getX());
        double controlY2 = coordinateFrom.getY() + controlRatioY2 * (coordinateTo.getY() - coordinateFrom.getY());
        drawCubicCurve(drawingPane, coordinateFrom, coordinateTo, controlX1, controlY1, controlX2, controlY2, lineWidth, color);
    }

    private static void drawLineSegment(Pane drawingPane, Coordinate coordinateFrom, Coordinate coordinateTo, double lineWidth, Color color) {
        drawCubicCurveByControlRatio(drawingPane, coordinateFrom, coordinateTo, 0, 0, 1, 1, lineWidth, color);
    }

    private static final List<Color> WIRE_COLOR_LIST = List.of(Color.BLUE, Color.ORANGE, Color.YELLOW);

    private void drawGndBreadboard(Pane drawingPane, ProjectDevice consumerDevice, ProjectDevice providerDevice, Pin consumerPin, Pin providerPin) {
        BreadboardDeviceGroup currentGroup = null;
        Deque<BreadboardDeviceGroup> stack = new ArrayDeque<>();
        for (BreadboardDeviceGroup group: breadboardDeviceGroupList) {
            if (group.getDeviceList().stream().noneMatch(breadboardDevice -> breadboardDevice.getProjectDevice().equals(consumerDevice))) {
                stack.push(group);
            } else {
                currentGroup = group;
                break;
            }
        }
        if (currentGroup == null) {
            throw new IllegalStateException("Breadboard device must have a group.");
        }
        Coordinate consumerDeviceGndCoordinate = calculatePinPosition(consumerDevice, consumerPin);
        Coordinate breadboardGndCoordinate = currentGroup.reserveGndCoordinate(consumerDeviceGndCoordinate.getX());
        drawLineSegment(drawingPane, breadboardGndCoordinate, consumerDeviceGndCoordinate, ConnectionType.WIRE.getLineWidth(), Color.BLACK);
        while (!currentGroup.isGndConnected()) {
            if (!stack.isEmpty()) {
                BreadboardDeviceGroup prevGroup = stack.pop();
                Coordinate vccFrom = prevGroup.getBreadboardCoordinate().add(prevGroup.getBreadboard().getGndRightHoleCoordinate());
                Coordinate vccTo = currentGroup.getBreadboardCoordinate().add(currentGroup.getBreadboard().getGndLeftHoleCoordinate());
                drawLineSegment(drawingPane, vccFrom, vccTo, ConnectionType.WIRE.getLineWidth(), Color.BLACK);
                currentGroup.setGndConnected();
                currentGroup = prevGroup;
            } else {
                Coordinate vccFrom = calculatePinPosition(providerDevice, providerPin);
                Coordinate vccTo = currentGroup.getBreadboardCoordinate().add(currentGroup.getBreadboard().getGndLeftHoleCoordinate());
                drawLineSegment(drawingPane, vccFrom, vccTo, ConnectionType.WIRE.getLineWidth(), Color.BLACK);
                currentGroup.setGndConnected();
            }
        }
    }

    private void drawVccBreadboard(Pane drawingPane, ProjectDevice consumerDevice, ProjectDevice providerDevice, Pin consumerPin, Pin providerPin) {
        VoltageLevel voltageLevel = providerPin.getVoltageLevel();
        BreadboardDeviceGroup currentGroup = null;
        Deque<BreadboardDeviceGroup> stack = new ArrayDeque<>();
        for (BreadboardDeviceGroup group: breadboardDeviceGroupList) {
            if (group.getDeviceList().stream().noneMatch(breadboardDevice -> breadboardDevice.getProjectDevice().equals(consumerDevice))) {
                stack.push(group);
            } else {
                currentGroup = group;
                break;
            }
        }
        if (currentGroup == null) {
            throw new IllegalStateException("Breadboard device must have a group.");
        }
        int voltageIndex = breadboardVoltageLevelList.indexOf(voltageLevel);
        Coordinate consumerDeviceVccCoordinate = calculatePinPosition(consumerDevice, consumerPin);
        Coordinate breadboardVccCoordinate = currentGroup.reserveVccCoordinate(voltageIndex, consumerDeviceVccCoordinate.getX());
        drawLineSegment(drawingPane, breadboardVccCoordinate, consumerDeviceVccCoordinate, ConnectionType.WIRE.getLineWidth(), Color.RED);
        while (!currentGroup.isVccConnected(voltageIndex)) {
            if (!stack.isEmpty()) {
                BreadboardDeviceGroup prevGroup = stack.pop();
                Coordinate vccFrom = prevGroup.getBreadboardCoordinate().add(prevGroup.getBreadboard().getVccRightHoleCoordinate(voltageIndex));
                Coordinate vccTo = currentGroup.getBreadboardCoordinate().add(currentGroup.getBreadboard().getVccLeftHoleCoordinate(voltageIndex));
                drawLineSegment(drawingPane, vccFrom, vccTo, ConnectionType.WIRE.getLineWidth(), Color.RED);
                currentGroup.setVccConnected(voltageIndex);
                currentGroup = prevGroup;
            } else {
                Coordinate vccFrom = calculatePinPosition(providerDevice, providerPin);
                Coordinate vccTo = currentGroup.getBreadboardCoordinate().add(currentGroup.getBreadboard().getVccLeftHoleCoordinate(voltageIndex));
                drawLineSegment(drawingPane, vccFrom, vccTo, ConnectionType.WIRE.getLineWidth(), Color.RED);
                currentGroup.setVccConnected(voltageIndex);
            }
        }
    }

    private void drawConnection(Pane drawingPane, DeviceConnection deviceConnection) {
        // This case is happened when drawing a controller on the breadboard device.
        if (deviceConnection == null) {
            return;
        }
        Map<Connection, Connection> connectionMap = deviceConnection.getConsumerProviderConnections();
        int countConnection = 0;
        for (Connection consumerConnection: connectionMap.keySet()) {
            Connection providerConnection = connectionMap.get(consumerConnection);
            if (providerConnection == null) {
                continue;
            }
            List<PinFunction> pinFunctions = deviceConnection.getProviderFunction().get(providerConnection);
            ProjectDevice consumerDevice = consumerConnection.getOwnerProjectDevice();
            ProjectDevice providerDevice = providerConnection.getOwnerProjectDevice();
            if (deviceMap.get(consumerDevice) instanceof IntegratedActualDevice) {
                continue;
            }
            List<Pin> consumerPins = consumerConnection.getPins();
            List<Pin> providerPins = providerConnection.getPins();
            List<Color> pinColors = consumerConnection.getType().getPinColors();
            double lineWidth = providerConnection.getType().getLineWidth();
            boolean needBreadboard = deviceMap.get(consumerDevice).isNeedBreadboard();
            for (int i=0; i<consumerPins.size(); i++) {
                Pin consumerPin = consumerPins.get(i);
                Pin providerPin = providerPins.get(i);
                boolean isVcc = consumerPin.getFunction().contains(PinFunction.VCC);
                boolean isGnd = consumerPin.getFunction().contains(PinFunction.GND);
                if (needBreadboard && isVcc) {
                    drawVccBreadboard(drawingPane, consumerDevice, providerDevice, consumerPin, providerPin);
                } else if (needBreadboard && isGnd) {
                    drawGndBreadboard(drawingPane, consumerDevice, providerDevice, consumerPin, providerPin);
                } else {
                    Coordinate pinConsumePosition = calculatePinPosition(consumerDevice, consumerPin);
                    Coordinate pinProvidePosition = calculatePinPosition(providerDevice, providerPin);

                    Color color;
                    if (providerConnection.getType() == ConnectionType.WIRE && pinFunctions.get(i) == PinFunction.GND) {
                        color = Color.BLACK;
                    } else if (providerConnection.getType() == ConnectionType.WIRE && pinFunctions.get(i) == PinFunction.VCC) {
                        color = Color.RED;
                    } else if (providerConnection.getType() == ConnectionType.WIRE) {
                        color = WIRE_COLOR_LIST.get(countConnection % WIRE_COLOR_LIST.size());
                        countConnection++;
                    } else {
                        color = pinColors.get(i);
                    }

                    if (deviceOnRightRegion.contains(consumerDevice) || deviceOnLeftRegion.contains(consumerDevice)) {
                        // ref: https://cubic-bezier.com/#.5,0,.5,1
                        drawCubicCurveByControlRatio(drawingPane, pinProvidePosition, pinConsumePosition, 0.5, 0, 0.5, 1, lineWidth, color);
                    } else if (deviceOnTopMidRegion.contains(consumerDevice) || deviceOnBottomMidRegion.contains(consumerDevice)) {
                        // ref: https://cubic-bezier.com/#.0,.35,1,.65
                        drawCubicCurveByControlRatio(drawingPane, pinProvidePosition, pinConsumePosition, 0, 0.35, 1, 0.65, lineWidth, color);
                    } else if(deviceNeedBreadboard.contains(consumerDevice) && pinConsumePosition.getY() != pinProvidePosition.getY()) {
                        drawCubicCurveByControlRatio(drawingPane, pinProvidePosition, pinConsumePosition, 0, 0, 1, 1, lineWidth, color);
                    } else if(deviceNeedBreadboard.contains(consumerDevice) && pinConsumePosition.getY() == pinProvidePosition.getY()) {
                        double controlX1 = pinProvidePosition.getX() + 0.33 * (pinConsumePosition.getX() - pinProvidePosition.getX());
                        double controlX2 = pinProvidePosition.getX() + 0.67 * (pinConsumePosition.getX() - pinProvidePosition.getX());
                        double controlY = pinProvidePosition.getY() + 25 + 0.05 * (pinConsumePosition.getX() - pinProvidePosition.getX());
                        drawCubicCurve(drawingPane, pinProvidePosition, pinConsumePosition, controlX1, controlY, controlX2, controlY, lineWidth, color);
                    } else {
                        throw new UnsupportedOperationException();
                    }
                }
            }
        }
    }

    private Coordinate calculatePinPosition(ProjectDevice projectDevice, Pin pin) {
        if (deviceNeedBreadboard.contains(projectDevice)) {
            for (BreadboardDeviceGroup group: breadboardDeviceGroupList) {
                Optional<BreadboardDevice> deviceOptional = group.getDeviceList().stream().filter(breadboardDevice -> breadboardDevice.getProjectDevice() == projectDevice).findFirst();
                if (deviceOptional.isPresent()) {
                    return group.getPinCoordinate(deviceOptional.get(), pin);
                }
            }
            throw new IllegalStateException("All device in breadboard region must be existed in one of the breadboard group.");
        } else {
            ActualDevice actualDevice = deviceMap.get(projectDevice);
            Coordinate deviceCenter = deviceCenterCoordinates.get(projectDevice);
            double deviceRotation = deviceRotationAngle.get(projectDevice);
            if (deviceRotation == 0.0) {
                return new Coordinate(deviceCenter.getX() - 0.5 * actualDevice.getWidth() + pin.getX(), deviceCenter.getY() - 0.5 * actualDevice.getHeight() + pin.getY());
            } else if (deviceRotation == 90.0) { // clockwise
                return new Coordinate(deviceCenter.getX() + 0.5 * actualDevice.getHeight() - pin.getY(), deviceCenter.getY() - 0.5 * actualDevice.getWidth() + pin.getX());
            } else if (deviceRotation == -90.0) { // counter-clockwise
                return new Coordinate(deviceCenter.getX() - 0.5 * actualDevice.getHeight() + pin.getY(), deviceCenter.getY() + 0.5 * actualDevice.getWidth() - pin.getX());
            } else if (deviceRotation == 180.0) {
                return new Coordinate(deviceCenter.getX() + 0.5 * actualDevice.getWidth() - pin.getX(), deviceCenter.getY() + 0.5 * actualDevice.getHeight() - pin.getY());
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }
}
