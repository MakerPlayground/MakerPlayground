package io.makerplayground.generator.diagram;

import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.device.actual.*;
import io.makerplayground.project.DeviceConnection;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectConfiguration;
import io.makerplayground.project.ProjectDevice;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.CubicCurve;
import javafx.scene.text.Text;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/* 1) The devices are placing in one of the following regions
 *      -----------------------------
 *      |      |  top-mid   |       |
 *      |      |------------|       |
 *      | left | controller | right |
 *      |      |------------|       |
 *      |      | bottom-mid |       |
 *      -----------------------------
 * 2) To know the device region
 *      2.1 On the controller, calculate the center of the positions of all pins that connect to the device.
 * 3) The devices in each region would be rotated to make the shortest line to the controller.
 * 4) On left and right region, the devices would be sorted by the y-position of the center of the connection on the controller.
 * 5) On top-mid and bottom-mid, the devices would be sorted by the x-position of the center of the connection on the controller.
 * 6) Breadboards will always in the top-mid region.
 * 7) The width and height of each region is calculated by the device in the region.
 * 8) The total width and height of diagram is calculated to recenter the diagram.
 */

class DiagramV1 {

    @Data @AllArgsConstructor
    static class Coordinate {
        double x, y;

        void addX(double offset) {
            x += offset;
        }

        void addY(double offset) {
            y += offset;
        }

        void addXY(Coordinate offset) {
            x += offset.getX();
            y += offset.getY();
        }
    }

    @Data @AllArgsConstructor
    static class Size {
        double width, height;
    }

    enum ConnectionPosition {
        LEFT, RIGHT, TOP, BOTTOM
    }

    enum Region {
        LEFT, RIGHT, TOP_MID, BOTTOM_MID, CONTROLLER
    }

    private static final String deviceDirectoryPath = DeviceLibrary.INSTANCE.getLibraryPath().get() + File.separator + "devices";
    private static List<DeviceType> DRAWABLE_DEVICE_TYPES = List.of(DeviceType.CONTROLLER, DeviceType.MODULE);

    private static final double DEVICE_NAME_FONT_SIZE = 18.0;

    private static final double LEFT_REGION_V_GAP = 30.0;
    private static final double LEFT_REGION_H_MARGIN = 80.0;
    private static final double RIGHT_REGION_V_GAP = 30.0;
    private static final double RIGHT_REGION_H_MARGIN = 80.0;

    private static final double TOP_MID_REGION_H_GAP = 30.0;
    private static final double TOP_MID_REGION_V_MARGIN = 80.0;
    private static final double BOTTOM_MID_REGION_H_GAP = 30.0;
    private static final double BOTTOM_MID_REGION_V_MARGIN = 80.0;

    private final ProjectConfiguration config;
    private final SortedMap<ProjectDevice, ActualDevice> deviceMap;
    private final SortedMap<ProjectDevice, DeviceConnection> deviceConnectionMap;

    private Size topMidRegionSize;
    private Size leftRegionSize;
    private Size controllerRegionSize;
    private Size bottomMidRegionSize;
    private Size rightRegionSize;
    private Size globalRegionSize;

    private ArrayList<ProjectDevice> deviceNeedBreadboard = new ArrayList<>();

    private ArrayList<ProjectDevice> deviceOnLeftRegion = new ArrayList<>();
    private ArrayList<ProjectDevice> deviceOnRightRegion = new ArrayList<>();
    private ArrayList<ProjectDevice> deviceOnTopRegion = new ArrayList<>();
    private ArrayList<ProjectDevice> deviceOnBottomRegion = new ArrayList<>();

    private Map<ProjectDevice, Coordinate> deviceCoordinates = new HashMap<>();   // keep the coordinates of the device centroid in local region and later replaced by global region.
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


        Size controllerPreferredSize = new Size(config.getController().getWidth(), config.getController().getHeight());
        Size topMidPreferredSize = calculateHorizontalAlignedDevicesSize(deviceOnTopRegion, TOP_MID_REGION_H_GAP, TOP_MID_REGION_V_MARGIN);
        Size bottomMidPreferredSize = calculateHorizontalAlignedDevicesSize(deviceOnBottomRegion, BOTTOM_MID_REGION_H_GAP, BOTTOM_MID_REGION_V_MARGIN);
        Size leftPreferredSize = calculateVerticalAlignedDevicesSize(deviceOnLeftRegion, LEFT_REGION_V_GAP, LEFT_REGION_H_MARGIN);
        Size rightPreferredSize = calculateVerticalAlignedDevicesSize(deviceOnRightRegion, RIGHT_REGION_V_GAP, RIGHT_REGION_H_MARGIN);

        double midActualWidth = max(controllerPreferredSize.getWidth(), topMidPreferredSize.getWidth(), bottomMidPreferredSize.getWidth());
        double midPreferredHeight = controllerPreferredSize.getHeight() + topMidPreferredSize.getHeight() + bottomMidPreferredSize.getHeight();
        double globalActualWidth = midActualWidth + leftPreferredSize.getWidth() + rightPreferredSize.getWidth();
        double globalActualHeight = max(midPreferredHeight, leftPreferredSize.getHeight(), rightPreferredSize.getHeight());

        this.controllerRegionSize = new Size(midActualWidth, controllerPreferredSize.getHeight());
        this.topMidRegionSize = new Size(midActualWidth, topMidPreferredSize.getHeight());
        this.bottomMidRegionSize = new Size(midActualWidth, bottomMidPreferredSize.getHeight());
        this.leftRegionSize = new Size(leftPreferredSize.getWidth(), globalActualHeight);
        this.rightRegionSize = new Size(rightPreferredSize.getWidth(), globalActualHeight);
        this.globalRegionSize = new Size(globalActualWidth, globalActualHeight);

        Coordinate leftRegionOffset = new Coordinate(0.0, 0.0);
        Coordinate topMidRegionOffset = new Coordinate(leftRegionSize.getWidth(), 0.0);
        Coordinate controllerRegionOffset = new Coordinate(leftRegionSize.getWidth(), topMidRegionSize.getHeight());
        Coordinate bottomMidRegionOffset = new Coordinate(leftRegionSize.getWidth(), topMidRegionSize.getHeight() + controllerRegionSize.getHeight());
        Coordinate rightRegionOffset = new Coordinate(leftRegionSize.getWidth() + midActualWidth, 0.0);

        double topMidSpaceBegin = 0.5 * (topMidRegionSize.getWidth() - topMidPreferredSize.getWidth());
        double bottomMidSpaceBegin = 0.5 * (bottomMidRegionSize.getWidth() - bottomMidPreferredSize.getWidth());
        double leftSpaceBegin = 0.5 * (leftRegionSize.getHeight() - leftPreferredSize.getHeight());
        double rightSpaceBegin = 0.5 * (rightRegionSize.getHeight() - rightPreferredSize.getHeight());

        /* calculate global coordinate */
        this.deviceCoordinates.put(ProjectDevice.CONTROLLER, new Coordinate(controllerRegionOffset.getX() + 0.5 * controllerRegionSize.getWidth(), controllerRegionOffset.getY() + 0.5 * controllerRegionSize.getHeight()));
        calculateTopMidDeviceCoordinates(topMidRegionOffset, topMidSpaceBegin);
        calculateBottomMidDeviceCoordinates(bottomMidRegionOffset, bottomMidSpaceBegin);
        calculateLeftDeviceCoordinates(leftRegionOffset, leftSpaceBegin);
        calculateRightDeviceCoordinates(rightRegionOffset, rightSpaceBegin);
    }

    private void calculateRightDeviceCoordinates(Coordinate regionOffset, double spaceBegin) {
        double accumulateY = spaceBegin;
        for (ProjectDevice projectDevice: this.deviceOnRightRegion) {
            Size size = deviceSizeAfterRotation.get(projectDevice);
            this.deviceCoordinates.put(projectDevice, new Coordinate(regionOffset.getX() + 0.5 * size.getWidth() + RIGHT_REGION_H_MARGIN, regionOffset.getY() + accumulateY + 0.5 * size.getHeight()));
            accumulateY += (size.getHeight() + RIGHT_REGION_V_GAP + DEVICE_NAME_FONT_SIZE);
        }
    }

    private void calculateLeftDeviceCoordinates(Coordinate regionOffset, double spaceBegin) {
        double accumulateY = spaceBegin;
        for (ProjectDevice projectDevice: this.deviceOnLeftRegion) {
            Size size = deviceSizeAfterRotation.get(projectDevice);
            accumulateY += DEVICE_NAME_FONT_SIZE;
            this.deviceCoordinates.put(projectDevice, new Coordinate(regionOffset.getX() + leftRegionSize.getWidth() - 0.5 * size.getWidth() - LEFT_REGION_H_MARGIN, regionOffset.getY() + accumulateY + 0.5 * size.getHeight()));
            accumulateY += (size.getHeight() + LEFT_REGION_V_GAP);
        }
    }

    private void calculateTopMidDeviceCoordinates(Coordinate regionOffset, double spaceBegin) {
        double accumulateX = spaceBegin;
        for (ProjectDevice projectDevice: this.deviceOnTopRegion) {
            Size size = deviceSizeAfterRotation.get(projectDevice);
            this.deviceCoordinates.put(projectDevice, new Coordinate(regionOffset.getX() + accumulateX + 0.5 * size.getWidth(), regionOffset.getY() + topMidRegionSize.getHeight() - 0.5 * size.getHeight() - TOP_MID_REGION_V_MARGIN));
            accumulateX += (size.getWidth() + TOP_MID_REGION_H_GAP);
        }
    }

    private void calculateBottomMidDeviceCoordinates(Coordinate regionOffset, double spaceBegin) {
        double accumulateX = spaceBegin;
        for (ProjectDevice projectDevice: this.deviceOnBottomRegion) {
            Size size = deviceSizeAfterRotation.get(projectDevice);
            this.deviceCoordinates.put(projectDevice, new Coordinate(regionOffset.getX() + accumulateX + 0.5 * size.getWidth(), regionOffset.getY() + 0.5 * size.getHeight() + BOTTOM_MID_REGION_V_MARGIN));
            accumulateX += (size.getWidth() + BOTTOM_MID_REGION_H_GAP);
        }
    }
    private Size calculateVerticalAlignedDevicesSize(List<ProjectDevice> projectDeviceList, double gapBetweenDevice, double gapToController) {
        double totalWidth = 0.0;
        double totalHeight = 0.0;
        for (ProjectDevice projectDevice: projectDeviceList) {
            Size size = deviceSizeAfterRotation.get(projectDevice);
            totalWidth += Math.max(totalWidth, size.getWidth());
            totalHeight += size.getHeight();
        }
        if (!projectDeviceList.isEmpty()) {
            totalWidth += gapToController;                                      // gap between devices.
            totalHeight += (projectDeviceList.size() - 1) * gapBetweenDevice;   // gap to controller region.
            totalHeight += projectDeviceList.size() * DEVICE_NAME_FONT_SIZE;
        }
        return new Size(totalWidth, totalHeight);
    }

    private Size calculateHorizontalAlignedDevicesSize(List<ProjectDevice> projectDeviceList, double gapBetweenDevice, double gapToController) {
        double totalWidth = 0.0;
        double totalHeight = 0.0;
        for (ProjectDevice projectDevice: projectDeviceList) {
            Size size = deviceSizeAfterRotation.get(projectDevice);
            totalWidth += size.getWidth();
            totalHeight = Math.max(totalHeight, size.getHeight());
        }
        if (!projectDeviceList.isEmpty()) {
            totalWidth += (projectDeviceList.size() - 1) * gapBetweenDevice;    // gap between devices.
            totalHeight += gapToController;                                     // gap to controller region.
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
        deviceMap.keySet().stream()
            .filter(projectDevice -> projectDevice != ProjectDevice.CONTROLLER)
            .filter(projectDevice -> DRAWABLE_DEVICE_TYPES.contains(deviceMap.get(projectDevice).getDeviceType()))
            .forEach(projectDevice -> {
                ActualDevice actualDevice = deviceMap.get(projectDevice);
                if (actualDevice instanceof IntegratedActualDevice) {
                    return;
                }
                if (actualDevice.isNeedBreadboard()) {
                    deviceNeedBreadboard.add(projectDevice);
                    return;
                }
                Region region = getDeviceRegion(projectDevice);
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
         *                     |   \     /   |
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

    private Region getDeviceRegion(ProjectDevice module) {
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

    private void drawDevice(Pane drawingPane, ProjectDevice projectDevice) {
        if (deviceMap.get(projectDevice) instanceof IntegratedActualDevice) {
            return;
        }
        try(InputStream deviceImageStream = Files.newInputStream(Paths.get(deviceDirectoryPath, deviceMap.get(projectDevice).getId(), "asset", "device.png"))) {
            Image image = new Image(deviceImageStream);
            ImageView imageView = new ImageView(image);
            Coordinate coordinate = deviceCoordinates.get(projectDevice);
            ActualDevice actualDevice = deviceMap.get(projectDevice);
            imageView.setLayoutX(coordinate.getX() - 0.5 * actualDevice.getWidth());
            imageView.setLayoutY(coordinate.getY() - 0.5 * actualDevice.getHeight());
            imageView.setRotate(deviceRotationAngle.get(projectDevice));
            drawingPane.getChildren().add(imageView);
            if (deviceOnBottomRegion.contains(projectDevice)) {
                Text text = new Text(projectDevice.getName());
                text.setX(coordinate.getX() - 0.5 * actualDevice.getWidth());
                text.setY(coordinate.getY() + 0.5 * actualDevice.getHeight() + DEVICE_NAME_FONT_SIZE);
                text.setStyle("-fx-font-size: " + DEVICE_NAME_FONT_SIZE);
                drawingPane.getChildren().add(text);
            }
            else if (deviceOnTopRegion.contains(projectDevice) || deviceOnLeftRegion.contains(projectDevice) || deviceOnRightRegion.contains(projectDevice)) {
                Text text = new Text(projectDevice.getName());
                text.setX(coordinate.getX() - 0.5 * actualDevice.getWidth());
                text.setY(coordinate.getY() - 0.5 * actualDevice.getHeight());
                text.setStyle("-fx-font-size: " + DEVICE_NAME_FONT_SIZE);
                drawingPane.getChildren().add(text);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Image not found for : " + deviceMap.get(projectDevice).getId());
        }
    }

    Pane make() {
        Pane wiringDiagram = new Pane();
        wiringDiagram.setPrefWidth(globalRegionSize.getWidth());
        wiringDiagram.setPrefHeight(globalRegionSize.getHeight());
        for (ProjectDevice projectDevice: deviceCoordinates.keySet()) {
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
                } else {
                    throw new UnsupportedOperationException();
                }
                CubicCurve curve = new CubicCurve();
                curve.setStartX(pinProvidePosition.getX());
                curve.setStartY(pinProvidePosition.getY());
                curve.setControlX1(pinProvidePosition.getX() + controlRatioX1 * (pinConsumePosition.getX() - pinProvidePosition.getX()));
                curve.setControlY1(pinProvidePosition.getY() + controlRatioY1 * (pinConsumePosition.getY() - pinProvidePosition.getY()));
                curve.setControlX2(pinProvidePosition.getX() + controlRatioX2 * (pinConsumePosition.getX() - pinProvidePosition.getX()));
                curve.setControlY2(pinProvidePosition.getY() + controlRatioY2 * (pinConsumePosition.getY() - pinProvidePosition.getY()));
                curve.setEndX(pinConsumePosition.getX());
                curve.setEndY(pinConsumePosition.getY());
                curve.setStrokeWidth(lineWidth);
                Effect effect;
                if (providerConnection.getType() == ConnectionType.WIRE && pinFunctions.get(i) == PinFunction.GND) {
                    effect = new DropShadow(1.0, Color.BLACK);
                    curve.setStroke(Color.BLACK);
                } else if (providerConnection.getType() == ConnectionType.WIRE && pinFunctions.get(i) == PinFunction.VCC) {
                    effect = new DropShadow(1.0, Color.RED);
                    curve.setStroke(Color.RED);
                } else if (providerConnection.getType() == ConnectionType.WIRE) {
                    effect = new DropShadow(1.0, WIRE_COLOR_LIST.get(countConnection % WIRE_COLOR_LIST.size()));
                    curve.setStroke(WIRE_COLOR_LIST.get(countConnection % WIRE_COLOR_LIST.size()));
                    countConnection++;
                } else {
                    effect = new DropShadow(1.0, pinColors.get(i));
                    curve.setStroke(pinColors.get(i));
                }
                curve.setFill(Color.TRANSPARENT);
                curve.setEffect(effect);
                drawingPane.getChildren().add(curve);
            }
        }
    }

    private Coordinate calculatePinPosition(ProjectDevice projectDevice, Pin pin) {
        ActualDevice actualDevice = deviceMap.get(projectDevice);
        Coordinate deviceCenter = deviceCoordinates.get(projectDevice);
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
