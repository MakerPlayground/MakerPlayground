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
import io.makerplayground.project.DeviceConnection;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectConfiguration;
import io.makerplayground.project.ProjectDevice;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.CubicCurve;
import javafx.scene.text.Text;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/* 1) The devices are placing in one of the following regions
 *      -----------------------------
 *      |      |  top-mid   |       |
 *      |      |------------|       |
 *      | left | controller | right |
 *      |      |------------|       |
 *      |      | bottom-mid |       |
 *      -----------------------------
 * 2) To know the device region
 *      2.1 For each device, calculate the centroid of all pins on controller that connect to the device.
 *      2.2 The centroid will be used for determining the region of the device. Call the getDeviceRegionNoBreadboard method to know the region
 *      2.3 In case that breadboard is needed. The top-mid region will be reserved for breadboard. Call the getDeviceRegionNoBreadboard method instead
 * 3) The devices in each region would be rotated to make the shortest line to the controller except the device that needs breadboard.
 * 4) On left and right region, the devices would be sorted by the y-position of centroid.
 * 5) On top-mid and bottom-mid, the devices would be sorted by the x-position of centroid.
 * 6) The width and height of each region is calculated by the device in the region.
 * 7) The total width and height of diagram is calculated to recenter the diagram.
 */

class DiagramV1 {

    @Data @AllArgsConstructor
    static class Coordinate {
        double x, y;

        void increaseX(double dx) {
            x += dx;
        }

        void increaseY(double dy) {
            y += dy;
        }

        void increaseXY(double dx, double dy) {
            x += dx;
            y += dy;
        }

        Coordinate add(double dx, double dy) {
            return new Coordinate(x + dx, y + dy);
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
        LEFT, RIGHT, TOP_MID, BOTTOM_MID, CONTROLLER;
    }

    public static final double UNIT_HOLE_DISTANCE = 14.4;

    enum Breadboard {
        SMALL(30, 468.0, 303.0,
                new Coordinate(22.0, 72.0),
                new Coordinate(22.0, 72.0 + 11 * UNIT_HOLE_DISTANCE),
                new Coordinate(22.0, 72.0 + 7 * UNIT_HOLE_DISTANCE),
                new Coordinate(27.0, 289.0),
                new Coordinate(27.0, 274.4)),
        LARGE(63, 937.0, 303.0,
                new Coordinate(22.0, 72.0),
                new Coordinate(22.0, 72.0 + 11 * UNIT_HOLE_DISTANCE),
                new Coordinate(22.0, 72.0 + 7 * UNIT_HOLE_DISTANCE),
                new Coordinate(50.0, 289.0),
                new Coordinate(50.0, 274.4)),
        LARGE_EXTEND(63, 937.0, 563.0,
                new Coordinate(22.0, 72.0),
                new Coordinate(22.0, 72.0 + 29 * UNIT_HOLE_DISTANCE),
                new Coordinate(22.0, 72.0 + 25 * UNIT_HOLE_DISTANCE),
                new Coordinate(50.0, 491.0),
                new Coordinate(50.0, 505.4));

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

        @Getter private final int numColumns;
        @Getter private final double width;
        @Getter private final double height;
        @Getter private final Coordinate upperTopLeftHoleCoordinate;
        @Getter private final Coordinate lowerBottomLeftHoleCoordinate;
        @Getter private final Coordinate lowerTopLeftHoleCoordinate;
        @Getter private final Coordinate lowerVccHoleCoordinate;
        @Getter private final Coordinate lowerGndHoleCoordinate;
        @Getter private final Coordinate upperCenterTopLeftHoleCoordinate;
        @Getter private final Coordinate lowerCenterBottomLeftHoleCoordinate;

        Breadboard(int numColumns, double width, double height,
                   Coordinate upperTopLeftHoleCoordinate,
                   Coordinate lowerBottomLeftHoleCoordinate,
                   Coordinate lowerTopLeftHoleCoordinate,
                   Coordinate lowerVccHoleCoordinate,
                   Coordinate lowerGndHoleCoordinate) {
            this(numColumns, width, height,
                    upperTopLeftHoleCoordinate,
                    lowerBottomLeftHoleCoordinate,
                    lowerTopLeftHoleCoordinate,
                    lowerVccHoleCoordinate,
                    lowerGndHoleCoordinate,
                    null,
                    null);
        }

        Breadboard(int numColumns, double width, double height,
                   Coordinate upperTopLeftHoleCoordinate,
                   Coordinate lowerBottomLeftHoleCoordinate,
                   Coordinate lowerTopLeftHoleCoordinate,
                   Coordinate lowerVccHoleCoordinate,
                   Coordinate lowerGndHoleCoordinate,
                   Coordinate upperCenterTopLeftHoleCoordinate,
                   Coordinate lowerCenterBottomLeftHoleCoordinate) {
            this.numColumns = numColumns;
            this.width = width;
            this.height = height;
            this.upperTopLeftHoleCoordinate = upperTopLeftHoleCoordinate;
            this.lowerBottomLeftHoleCoordinate = lowerBottomLeftHoleCoordinate;
            this.lowerTopLeftHoleCoordinate = lowerTopLeftHoleCoordinate;
            this.lowerVccHoleCoordinate = lowerVccHoleCoordinate;
            this.lowerGndHoleCoordinate = lowerGndHoleCoordinate;
            this.upperCenterTopLeftHoleCoordinate = upperCenterTopLeftHoleCoordinate;
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
    }

    static class BreadboardDeviceGroup {
        @Getter final Breadboard breadboard;
        @Getter final List<BreadboardDevice> deviceList;

        @Setter @Getter Coordinate breadboardCoordinate;

        BreadboardDeviceGroup(Breadboard breadboard) {
            this.breadboard = breadboard;
            this.deviceList = new ArrayList<>();
        }

        void drawBreadboard(Pane drawingPane) {
            this.breadboard.draw(drawingPane, breadboardCoordinate.add(GLOBAL_LEFT_MARGIN, GLOBAL_TOP_MARGIN));
        }

        public Coordinate getPinCoordinate(BreadboardDevice device, Pin pin) {
            double x = breadboardCoordinate.getX() + breadboard.getUpperTopLeftHoleCoordinate().getX() + UNIT_HOLE_DISTANCE;
            for (BreadboardDevice breadboardDevice: deviceList) {
                if (breadboardDevice == device) {
                    break;
                }
                x += (breadboardDevice.getNumColumnsWholeDevice() + 1) * UNIT_HOLE_DISTANCE;
            }
            double xShift = UNIT_HOLE_DISTANCE * Math.ceil(device.getXLeftHolePixel() / UNIT_HOLE_DISTANCE) - device.getXLeftHolePixel();
            x += device.getXLeftHolePixel() + xShift + device.getOffsetXHoles().get(pin) * UNIT_HOLE_DISTANCE;
            double y;
            if (breadboard == Breadboard.LARGE_EXTEND) {
                if (device.getBreadboardPlacement() == BreadboardPlacement.TWO_SIDES) {
                    if (pin.getY() <= 0.5 * device.getActualDevice().getHeight()) {
                        y = breadboardCoordinate.getY() + breadboard.getUpperCenterTopLeftHoleCoordinate().getY() + device.getOffsetYHoles().get(pin) * UNIT_HOLE_DISTANCE;
                    } else {
                        y = breadboardCoordinate.getY() + breadboard.getLowerCenterBottomLeftHoleCoordinate().getY() - device.getOffsetYHoles().get(pin) * UNIT_HOLE_DISTANCE;
                    }
                } else if (device.getBreadboardPlacement() == BreadboardPlacement.ONE_SIDE) {
                    y = breadboardCoordinate.getY() + breadboard.getUpperTopLeftHoleCoordinate().getY() + device.getOffsetYHoles().get(pin) * UNIT_HOLE_DISTANCE;
                } else {
                    throw new IllegalStateException("No implementation for placement: " + device.getBreadboardPlacement());
                }
            } else if (breadboard == Breadboard.LARGE || breadboard == Breadboard.SMALL) {
                if (device.getBreadboardPlacement() == BreadboardPlacement.TWO_SIDES) {
                    if (pin.getY() <= 0.5 * device.getActualDevice().getHeight()) {
                        y = breadboardCoordinate.getY() + breadboard.getUpperTopLeftHoleCoordinate().getY() + device.getOffsetYHoles().get(pin) * UNIT_HOLE_DISTANCE;
                    } else {
                        y = breadboardCoordinate.getY() + breadboard.getLowerBottomLeftHoleCoordinate().getY() - device.getOffsetYHoles().get(pin) * UNIT_HOLE_DISTANCE;
                    }
                } else if (device.getBreadboardPlacement() == BreadboardPlacement.ONE_SIDE) {
                    y = breadboardCoordinate.getY() + breadboard.getUpperTopLeftHoleCoordinate().getY() + device.getOffsetYHoles().get(pin) * UNIT_HOLE_DISTANCE;
                } else {
                    throw new IllegalStateException("No implementation for placement: " + device.getBreadboardPlacement());
                }
            } else {
                throw new IllegalStateException("No implementation for breadboard: " + breadboard);
            }
            return new Coordinate(x, y);
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
            List<Connection> connectionConsume = actualDevice.getConnectionConsumeByOwnerDevice(projectDevice);

            // Find the top and left holes
            for (Connection connection: connectionConsume) {
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
            for (Connection connection: connectionConsume) {
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
            return actualDevice.getHeight() > Breadboard.LARGE.getLowerBottomLeftHoleCoordinate().getY() - Breadboard.LARGE.getUpperTopLeftHoleCoordinate().getY() + 2 * UNIT_HOLE_DISTANCE;
        }
    }

    private static final String deviceDirectoryPath = DeviceLibrary.INSTANCE.getLibraryPath().get() + File.separator + "devices";
    private static List<DeviceType> DRAWABLE_DEVICE_TYPES = List.of(DeviceType.CONTROLLER, DeviceType.MODULE);

    private static final double DEVICE_NAME_FONT_SIZE = 18.0;

    private static final double GLOBAL_TOP_MARGIN = 50.0;
    private static final double GLOBAL_LEFT_MARGIN = 50.0;

    private static final double BREADBOARD_REGION_H_GAP = 50.0;
    private static final double BREADBOARD_REGION_V_MARGIN = 120.0;

    private static final double TOP_MID_REGION_H_GAP = 30.0;
    private static final double TOP_MID_REGION_V_MARGIN = 80.0;
    private static final double BOTTOM_MID_REGION_H_GAP = 30.0;
    private static final double BOTTOM_MID_REGION_V_MARGIN = 80.0;
    private static final double LEFT_REGION_V_GAP = 30.0;
    private static final double LEFT_REGION_H_MARGIN = 80.0;
    private static final double RIGHT_REGION_V_GAP = 30.0;
    private static final double RIGHT_REGION_H_MARGIN = 80.0;

    private final ProjectConfiguration config;
    private final SortedMap<ProjectDevice, ActualDevice> deviceMap;
    private final SortedMap<ProjectDevice, DeviceConnection> deviceConnectionMap;

    private boolean controllerUseBreadboard;
    private Size breadboardRegionSize;
    private Size topMidRegionSize;
    private Size leftRegionSize;
    private Size controllerRegionSize;
    private Size bottomMidRegionSize;
    private Size rightRegionSize;
    private Size globalRegionSize;

    private List<ProjectDevice> deviceNeedBreadboard = new ArrayList<>();
    private List<ProjectDevice> deviceOnLeftRegion = new ArrayList<>();
    private List<ProjectDevice> deviceOnRightRegion = new ArrayList<>();
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
        this.config = project.getProjectConfiguration();
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
        Size topMidPreferredSize = calculateHorizontalAlignedDevicesSize(deviceOnTopRegion, TOP_MID_REGION_H_GAP, TOP_MID_REGION_V_MARGIN);
        Size bottomMidPreferredSize = calculateHorizontalAlignedDevicesSize(deviceOnBottomRegion, BOTTOM_MID_REGION_H_GAP, BOTTOM_MID_REGION_V_MARGIN);
        Size leftPreferredSize = calculateVerticalAlignedDevicesSize(deviceOnLeftRegion, LEFT_REGION_V_GAP, LEFT_REGION_H_MARGIN);
        Size rightPreferredSize = calculateVerticalAlignedDevicesSize(deviceOnRightRegion, RIGHT_REGION_V_GAP, RIGHT_REGION_H_MARGIN);

        double midActualWidth = max(controllerPreferredSize.getWidth(), topMidPreferredSize.getWidth(), bottomMidPreferredSize.getWidth(), breadboardRegionPreferredSize.getWidth());
        double midPreferredHeight = controllerPreferredSize.getHeight() + topMidPreferredSize.getHeight() + bottomMidPreferredSize.getHeight() + breadboardRegionPreferredSize.getHeight();
        double globalActualWidth = midActualWidth + leftPreferredSize.getWidth() + rightPreferredSize.getWidth();
        double globalActualHeight = max(midPreferredHeight, leftPreferredSize.getHeight(), rightPreferredSize.getHeight());

        this.controllerRegionSize = new Size(midActualWidth, controllerPreferredSize.getHeight());
        this.breadboardRegionSize = new Size(midActualWidth, breadboardRegionPreferredSize.getHeight());
        this.topMidRegionSize = new Size(midActualWidth, topMidPreferredSize.getHeight());
        this.bottomMidRegionSize = new Size(midActualWidth, bottomMidPreferredSize.getHeight());
        this.leftRegionSize = new Size(leftPreferredSize.getWidth(), globalActualHeight);
        this.rightRegionSize = new Size(rightPreferredSize.getWidth(), globalActualHeight);
        this.globalRegionSize = new Size(globalActualWidth, globalActualHeight);

        Coordinate leftRegionTopLeft = new Coordinate(0.0, 0.0);
        Coordinate topMidRegionTopLeft = new Coordinate(leftRegionSize.getWidth(), 0.0);
        Coordinate breadboardRegionTopLeft = new Coordinate(leftRegionSize.getWidth(), topMidRegionSize.getHeight());
        Coordinate controllerRegionTopLeft = new Coordinate(leftRegionSize.getWidth(), topMidRegionSize.getHeight() + breadboardRegionSize.getHeight());
        Coordinate bottomMidRegionTopLeft = new Coordinate(leftRegionSize.getWidth(), topMidRegionSize.getHeight() + breadboardRegionSize.getHeight() + controllerRegionSize.getHeight());
        Coordinate rightRegionTopLeft = new Coordinate(leftRegionSize.getWidth() + midActualWidth, 0.0);

        double breadboardSpaceBegin = 0.5 * (breadboardRegionSize.getWidth() - breadboardRegionPreferredSize.getWidth());
        double topMidSpaceBegin = 0.5 * (topMidRegionSize.getWidth() - topMidPreferredSize.getWidth());
        double bottomMidSpaceBegin = 0.5 * (bottomMidRegionSize.getWidth() - bottomMidPreferredSize.getWidth());
        double leftSpaceBegin = 0.5 * (leftRegionSize.getHeight() - leftPreferredSize.getHeight());
        double rightSpaceBegin = 0.5 * (rightRegionSize.getHeight() - rightPreferredSize.getHeight());

        /* calculate global coordinate */
        if (!controllerUseBreadboard) {
            this.deviceCenterCoordinates.put(ProjectDevice.CONTROLLER, new Coordinate(controllerRegionTopLeft.getX() + 0.5 * controllerRegionSize.getWidth(), controllerRegionTopLeft.getY() + 0.5 * controllerRegionSize.getHeight()));
        }
        calculateBreadboardDeviceCoordinates(breadboardRegionTopLeft, breadboardSpaceBegin);
        calculateTopMidDeviceCoordinates(topMidRegionTopLeft, topMidSpaceBegin);
        calculateBottomMidDeviceCoordinates(bottomMidRegionTopLeft, bottomMidSpaceBegin);
        calculateLeftDeviceCoordinates(leftRegionTopLeft, leftSpaceBegin);
        calculateRightDeviceCoordinates(rightRegionTopLeft, rightSpaceBegin);
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
                    double centerX = breadboardTopLeftCoordinate.getX() + breadboard.getUpperTopLeftHoleCoordinate().getX() + xShift + holeOffsetX * UNIT_HOLE_DISTANCE + 0.5 * device.getActualDevice().getWidth();
                    double centerY = breadboardTopLeftCoordinate.getY() + breadboard.getUpperTopLeftHoleCoordinate().getY() - device.getYTopHolePixel() + 0.5 * device.getActualDevice().getHeight();
                    this.deviceCenterCoordinates.put(device.getProjectDevice(), new Coordinate(centerX, centerY));
                } else if (device.getBreadboardPlacement() == BreadboardPlacement.TWO_SIDES) {
                    double dy = device.getYBottomHolePixel() - device.getYTopHolePixel();
                    int devicePinRows = (int) Math.ceil(dy / UNIT_HOLE_DISTANCE) + 1;
                    double centerX = breadboardTopLeftCoordinate.getX() + breadboard.getUpperTopLeftHoleCoordinate().getX() + xShift + holeOffsetX * UNIT_HOLE_DISTANCE + 0.5 * device.getActualDevice().getWidth();
                    double centerY;
                    if (breadboard == Breadboard.LARGE_EXTEND) {
                        if (devicePinRows % 2 == 0) {
                            centerY = breadboardTopLeftCoordinate.getY()
                                    + breadboard.getUpperTopLeftHoleCoordinate().getY()
                                    + 0.5 * (breadboard.getLowerCenterBottomLeftHoleCoordinate().getY()
                                            - breadboard.getUpperCenterTopLeftHoleCoordinate().getY()
                                            - (devicePinRows - 1) * UNIT_HOLE_DISTANCE
                                            + device.getActualDevice().getHeight())
                                    - device.getYTopHolePixel();
                        } else {
                            centerY = breadboardTopLeftCoordinate.getY()
                                    + breadboard.getUpperTopLeftHoleCoordinate().getY()
                                    + 0.5 * (breadboard.getLowerCenterBottomLeftHoleCoordinate().getY()
                                            - breadboard.getUpperCenterTopLeftHoleCoordinate().getY()
                                            - devicePinRows * UNIT_HOLE_DISTANCE
                                            + device.getActualDevice().getHeight())
                                    - device.getYTopHolePixel();
                        }
                    } else if (breadboard == Breadboard.LARGE || breadboard == Breadboard.SMALL) {
                        if (devicePinRows % 2 == 0) {
                            centerY = breadboardTopLeftCoordinate.getY()
                                    + breadboard.getUpperTopLeftHoleCoordinate().getY()
                                    + 0.5 * (breadboard.getLowerBottomLeftHoleCoordinate().getY()
                                            - breadboard.getUpperTopLeftHoleCoordinate().getY()
                                            - (devicePinRows - 1) * UNIT_HOLE_DISTANCE
                                            + device.getActualDevice().getHeight())
                                    - device.getYTopHolePixel();
                        } else {
                            centerY = breadboardTopLeftCoordinate.getY()
                                    + breadboard.getUpperTopLeftHoleCoordinate().getY()
                                    + 0.5 * (breadboard.getLowerBottomLeftHoleCoordinate().getY()
                                            - breadboard.getUpperTopLeftHoleCoordinate().getY()
                                            - devicePinRows * UNIT_HOLE_DISTANCE
                                            + device.getActualDevice().getHeight())
                                    - device.getYTopHolePixel();
                        }
                    } else {
                        throw new IllegalStateException("There is no implementation for " + breadboard + " breadboard");
                    }
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
            accumulateY += (size.getHeight() + LEFT_REGION_V_GAP);
        }
    }

    private void calculateTopMidDeviceCoordinates(Coordinate regionTopLeft, double spaceBegin) {
        double accumulateX = spaceBegin;
        for (ProjectDevice projectDevice: this.deviceOnTopRegion) {
            Size size = deviceSizeAfterRotation.get(projectDevice);
            this.deviceCenterCoordinates.put(projectDevice, new Coordinate(regionTopLeft.getX() + accumulateX + 0.5 * size.getWidth(), regionTopLeft.getY() + topMidRegionSize.getHeight() - 0.5 * size.getHeight() - TOP_MID_REGION_V_MARGIN));
            accumulateX += (size.getWidth() + TOP_MID_REGION_H_GAP);
        }
    }

    private void calculateBottomMidDeviceCoordinates(Coordinate regionTopLeft, double spaceBegin) {
        double accumulateX = spaceBegin;
        for (ProjectDevice projectDevice: this.deviceOnBottomRegion) {
            Size size = deviceSizeAfterRotation.get(projectDevice);
            this.deviceCenterCoordinates.put(projectDevice, new Coordinate(regionTopLeft.getX() + accumulateX + 0.5 * size.getWidth(), regionTopLeft.getY() + 0.5 * size.getHeight() + BOTTOM_MID_REGION_V_MARGIN));
            accumulateX += (size.getWidth() + BOTTOM_MID_REGION_H_GAP);
        }
    }

    private Size calculateBreadboardRegionSizeAndAssignDeviceToBreadboardGroup(List<ProjectDevice> projectDeviceList, double gapBetweenBreadboard, double gapToController) {
        if (projectDeviceList.isEmpty()) {
            return new Size(0, 0);
        }
        List<BreadboardDevice> breadboardDeviceList = projectDeviceList.stream()
                .map((ProjectDevice projectDevice1) -> new BreadboardDevice(projectDevice1, deviceMap.get(projectDevice1)))
                .collect(Collectors.toList());
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
        deviceOnLeftRegion.sort(Comparator.comparingDouble(device -> getConnectionCentroidToDevice(ProjectDevice.CONTROLLER, device).getY()));
        deviceOnRightRegion.sort(Comparator.comparingDouble(device -> getConnectionCentroidToDevice(ProjectDevice.CONTROLLER, device).getY()));
        deviceOnTopRegion.sort(Comparator.comparingDouble(device -> getConnectionCentroidToDevice(ProjectDevice.CONTROLLER, device).getX()));
        deviceOnBottomRegion.sort(Comparator.comparingDouble(device -> getConnectionCentroidToDevice(ProjectDevice.CONTROLLER, device).getX()));
        deviceNeedBreadboard.sort(Comparator.comparingDouble(device -> getConnectionCentroidToDevice(ProjectDevice.CONTROLLER, device).getX()));
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
        for (ProjectDevice projectDevice: deviceOnTopRegion) {
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
        for (ProjectDevice projectDevice: deviceOnBottomRegion) {
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
                    deviceOnBottomRegion.add(projectDevice);
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
                    deviceOnTopRegion.add(projectDevice);
                } else if (region == Region.BOTTOM_MID) {
                    deviceOnBottomRegion.add(projectDevice);
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
            return new Coordinate(0, 0);
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
        Coordinate coordinate = getConnectionCentroidToDevice(ProjectDevice.CONTROLLER, module);
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
        Coordinate coordinate = getConnectionCentroidToDevice(ProjectDevice.CONTROLLER, module);
        ActualDevice controller = config.getController();
        if (coordinate.getY() > 0.65 * controller.getHeight()) {
            return Region.BOTTOM_MID;
        }
        if (coordinate.getX() < 0.5 * controller.getWidth()) {
            return Region.LEFT;
        }
        if (coordinate.getX() > 0.5 * controller.getWidth()) {
            return Region.RIGHT;
        }
        return Region.TOP_MID;
    }

    private void drawDevice(Pane drawingPane, ProjectDevice projectDevice) {
        if (deviceMap.get(projectDevice) instanceof IntegratedActualDevice) {
            return;
        }
        try(InputStream deviceImageStream = Files.newInputStream(Paths.get(deviceDirectoryPath, deviceMap.get(projectDevice).getId(), "asset", "device.png"))) {
            double deviceAngle = deviceRotationAngle.get(projectDevice);
            Image image = new Image(deviceImageStream);
            ImageView imageView = new ImageView(image);
            Coordinate coordinate = deviceCenterCoordinates.get(projectDevice);
            ActualDevice actualDevice = deviceMap.get(projectDevice);
            imageView.setLayoutX(coordinate.getX() - 0.5 * actualDevice.getWidth() + GLOBAL_LEFT_MARGIN);
            imageView.setLayoutY(coordinate.getY() - 0.5 * actualDevice.getHeight() + GLOBAL_TOP_MARGIN);
            imageView.setRotate(deviceAngle);
            Size sizeAfterRotation = deviceSizeAfterRotation.get(projectDevice);
            drawingPane.getChildren().add(imageView);
            if (deviceOnBottomRegion.contains(projectDevice)) {
                Text text = new Text(projectDevice.getName());
                text.setX(coordinate.getX() - 0.5 * sizeAfterRotation.getWidth() + GLOBAL_LEFT_MARGIN);
                text.setY(coordinate.getY() + 0.5 * sizeAfterRotation.getHeight() + DEVICE_NAME_FONT_SIZE + GLOBAL_TOP_MARGIN);
                text.setStyle("-fx-font-size: " + DEVICE_NAME_FONT_SIZE);
                drawingPane.getChildren().add(text);
            } else if (deviceOnTopRegion.contains(projectDevice)) {
                Text text = new Text(projectDevice.getName());
                text.setX(coordinate.getX() - 0.5 * sizeAfterRotation.getWidth() + GLOBAL_LEFT_MARGIN);
                text.setY(coordinate.getY() - 0.5 * sizeAfterRotation.getHeight() - DEVICE_NAME_FONT_SIZE  + GLOBAL_TOP_MARGIN);
                text.setStyle("-fx-font-size: " + DEVICE_NAME_FONT_SIZE);
                drawingPane.getChildren().add(text);
            } else if (deviceOnLeftRegion.contains(projectDevice) || deviceOnRightRegion.contains(projectDevice)) {
                Text text = new Text(projectDevice.getName());
                text.setX(coordinate.getX() - 0.45 * sizeAfterRotation.getWidth() + GLOBAL_LEFT_MARGIN);
                text.setY(coordinate.getY() - 0.5 * sizeAfterRotation.getHeight() - DEVICE_NAME_FONT_SIZE + GLOBAL_TOP_MARGIN);
                text.setStyle("-fx-font-size: " + DEVICE_NAME_FONT_SIZE);
                drawingPane.getChildren().add(text);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Image not found for : " + deviceMap.get(projectDevice).getId());
        }
    }

    Pane make() {
        Pane wiringDiagram = new Pane();
        wiringDiagram.setPrefWidth(globalRegionSize.getWidth() + GLOBAL_TOP_MARGIN);
        wiringDiagram.setPrefHeight(globalRegionSize.getHeight() + GLOBAL_LEFT_MARGIN);
        for (BreadboardDeviceGroup group: this.breadboardDeviceGroupList) {
            group.drawBreadboard(wiringDiagram);
        }
        for (ProjectDevice projectDevice: deviceCenterCoordinates.keySet()) {
            drawDevice(wiringDiagram, projectDevice);
        }
        for (ProjectDevice projectDevice: deviceConnectionMap.keySet()) {
            drawLine(wiringDiagram, deviceConnectionMap.get(projectDevice));
        }
        return wiringDiagram;
    }

    private static final List<Color> WIRE_COLOR_LIST = List.of(Color.BLUE, Color.ORANGE, Color.YELLOW);

    private void drawLine(Pane drawingPane, DeviceConnection deviceConnection) {
        Map<Connection, Connection> connectionMap = deviceConnection.getConsumerProviderConnections();
        int countConnection = 0;
        for (Connection consumerConnection: connectionMap.keySet()) {
            Connection providerConnection = connectionMap.get(consumerConnection);
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
            for (int i=0; i<consumerPins.size(); i++) {
                Coordinate pinConsumePosition = calculatePinPosition(consumerDevice, consumerPins.get(i));
                Coordinate pinProvidePosition = calculatePinPosition(providerDevice, providerPins.get(i));
                double controlRatioX1;
                double controlRatioY1;
                double controlRatioX2;
                double controlRatioY2;
                if (deviceOnRightRegion.contains(consumerDevice) || deviceOnLeftRegion.contains(consumerDevice)) {
                    // ref: https://cubic-bezier.com/#.5,0,.5,1
                    controlRatioX1 = 0.5;
                    controlRatioY1 = 0;
                    controlRatioX2 = 0.5;
                    controlRatioY2 = 1;
                } else if (deviceOnTopRegion.contains(consumerDevice) || deviceOnBottomRegion.contains(consumerDevice)) {
                    // ref: https://cubic-bezier.com/#.0,.35,1,.65
                    controlRatioX1 = 0.0;
                    controlRatioY1 = 0.35;
                    controlRatioX2 = 1.0;
                    controlRatioY2 = 0.65;
                } else if(deviceNeedBreadboard.contains(consumerDevice)) {
                    controlRatioX1 = 0.0;
                    controlRatioY1 = 0.0;
                    controlRatioX2 = 1.0;
                    controlRatioY2 = 1.0;
                } else {
                    throw new UnsupportedOperationException();
                }
                CubicCurve curve = new CubicCurve();
                curve.setStartX(pinProvidePosition.getX() + GLOBAL_LEFT_MARGIN);
                curve.setStartY(pinProvidePosition.getY() + GLOBAL_TOP_MARGIN);
                curve.setControlX1(pinProvidePosition.getX() + controlRatioX1 * (pinConsumePosition.getX() - pinProvidePosition.getX()) + GLOBAL_LEFT_MARGIN);
                curve.setControlY1(pinProvidePosition.getY() + controlRatioY1 * (pinConsumePosition.getY() - pinProvidePosition.getY()) + GLOBAL_TOP_MARGIN);
                curve.setControlX2(pinProvidePosition.getX() + controlRatioX2 * (pinConsumePosition.getX() - pinProvidePosition.getX()) + GLOBAL_LEFT_MARGIN);
                curve.setControlY2(pinProvidePosition.getY() + controlRatioY2 * (pinConsumePosition.getY() - pinProvidePosition.getY()) + GLOBAL_TOP_MARGIN);
                curve.setEndX(pinConsumePosition.getX() + GLOBAL_LEFT_MARGIN);
                curve.setEndY(pinConsumePosition.getY() + GLOBAL_TOP_MARGIN);
                curve.setStrokeWidth(lineWidth);
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
                curve.setStroke(color);
                curve.setFill(Color.TRANSPARENT);
                curve.setEffect(new DropShadow(1.0, color.darker().darker()));
                drawingPane.getChildren().add(curve);
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
