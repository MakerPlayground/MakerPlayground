package io.makerplayground.generator;

import io.makerplayground.device.Device;
import io.makerplayground.device.DevicePort;
import io.makerplayground.helper.FormFactor;
import io.makerplayground.helper.Peripheral;

import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectDevice;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Created by tanyagorn on 7/17/2017.
 */
public class Diagram extends Pane {
    private static final double BREADBOARD_TOP_MARGIN = 100;
    private static final double BREADBOARD_LEFT_MARGIN = 30;
    private static final double BREADBOARD_WIDTH = 936.48;
    private static final double BREADBOARD_HEIGHT = 302.4;
    private static final int    BREADBOARD_NUM_COLUMN = 5;
    private static final double BREADBOARD_GND_BOT_X = 45.88;
    private static final double BREADBOARD_GND_BOT_Y = 283.21;
    private static final double BREADBOARD_PWR_BOT_X = 45.88;
    private static final double BREADBOARD_PWR_BOT_Y = 268.81;
    private static final double BREADBOARD_GND_TOP_X = 45.88;
    private static final double BREADBOARD_GND_TOP_Y = 24.01;
    private static final double BREADBOARD_PWR_TOP_X = 45.88;
    private static final double BREADBOARD_PWR_TOP_Y = 9.61;
    private static final double HOLE_SPACE = 14.4;
    private static final double CENTER_SPACE = 43.2;
    private static final double J1_POS_X = 17.04;
    private static final double J1_POS_Y = 67.21;

    private static final double CONTROLLER_Y_MARGIN = 30;

    private final Project project;

    private Position controllerPosition;
    private Map<ProjectDevice, Position> deviceTopLeftPos;

    public Diagram(Project project) {
        this.project = project;
        initDiagram();
    }

    private void initDiagram() {
        this.deviceTopLeftPos = new HashMap<>();
        setPrefSize(1000, 600);

        // draw breadboard
        ImageView breadBoard = new ImageView(new Image(getClass().getResourceAsStream("/device/breadboard_large@2x.png")));
        breadBoard.setLayoutX(BREADBOARD_LEFT_MARGIN);
        breadBoard.setLayoutY(BREADBOARD_TOP_MARGIN);
        getChildren().add(breadBoard);

        int currentRow = 1;

        // draw controller
        Device controller = project.getController().getController();
        ImageView controllerImage = new ImageView(new Image(getClass().getResourceAsStream("/device/" + controller.getId() + ".png")));
        if (controller.getFormFactor() == FormFactor.BREAKOUT_BOARD_ONESIDE) {
            currentRow += calculateNumberOfHoleWithoutLeftWing(controller);
            DevicePort topLeftPort = getTopLeftHole(controller);
            controllerPosition = new Position(BREADBOARD_LEFT_MARGIN + J1_POS_X - topLeftPort.getX()
                                            , BREADBOARD_TOP_MARGIN + J1_POS_Y - topLeftPort.getY());
        } else if (controller.getFormFactor() == FormFactor.BREAKOUT_BOARD_TWOSIDE) {
            currentRow += calculateNumberOfHoleWithoutLeftWing(controller);
            int heightHole = (int) ((getBottomLeftHole(controller).getY() - getTopLeftHole(controller).getY()) / HOLE_SPACE);
            controllerPosition = new Position(BREADBOARD_LEFT_MARGIN + J1_POS_X - getTopLeftHole(controller).getX()
                                            , BREADBOARD_TOP_MARGIN + J1_POS_Y + ((BREADBOARD_NUM_COLUMN - ((heightHole - 2) / 2)) * HOLE_SPACE));
        } else if (controller.getFormFactor() == FormFactor.STANDALONE) {
            controllerPosition = new Position(BREADBOARD_LEFT_MARGIN, BREADBOARD_TOP_MARGIN + BREADBOARD_HEIGHT + CONTROLLER_Y_MARGIN);
        }
        controllerImage.setLayoutX(controllerPosition.getX());
        controllerImage.setLayoutY(controllerPosition.getY());
        this.getChildren().add(controllerImage);

        // draw breakout board
        for (ProjectDevice projectDevice : project.getAllDevice()) {
            Device device = projectDevice.getActualDevice();
            ImageView deviceImage = new ImageView(new Image(getClass().getResourceAsStream("/device/" + device.getId() + ".png")));
            if (device.getFormFactor() == FormFactor.BREAKOUT_BOARD_ONESIDE) {
                currentRow += calculateNumberOfHoleWithoutLeftWing(device);
                DevicePort topLeftPort = getTopLeftHole(device);
                deviceTopLeftPos.put(projectDevice, new Position(BREADBOARD_LEFT_MARGIN + J1_POS_X - topLeftPort.getX()
                        , BREADBOARD_TOP_MARGIN + J1_POS_Y - topLeftPort.getY()));
            } else if (device.getFormFactor() == FormFactor.BREAKOUT_BOARD_TWOSIDE) {
                currentRow += calculateNumberOfHoleWithoutLeftWing(device);
                int heightHole = (int) ((getBottomLeftHole(device).getY() - getTopLeftHole(device).getY()) / HOLE_SPACE);
                 deviceTopLeftPos.put(projectDevice, new Position(BREADBOARD_LEFT_MARGIN + J1_POS_X - getTopLeftHole(device).getX()
                        , BREADBOARD_TOP_MARGIN + J1_POS_Y + ((BREADBOARD_NUM_COLUMN - ((heightHole - 2) / 2)) * HOLE_SPACE)));
            } else if (device.getFormFactor() == FormFactor.STANDALONE) {
                // TODO: do it now!!!
            }
            deviceImage.setLayoutX(deviceTopLeftPos.get(projectDevice).getX());
            deviceImage.setLayoutY(deviceTopLeftPos.get(projectDevice).getY());
            this.getChildren().add(deviceImage);
        }

        // connect power
        for (ProjectDevice projectDevice : project.getAllDevice()) {
            Device device = projectDevice.getActualDevice();
            List<DevicePort> powerPort = device.getPort(Peripheral.POWER);

            if (device.getFormFactor() == FormFactor.BREAKOUT_BOARD_ONESIDE) {
                for (DevicePort port : powerPort) {
                    if (port.isVcc()) {
                        createPowerLine(port.getX(), port.getY() + HOLE_SPACE, BREADBOARD_PWR_BOT_X, BREADBOARD_PWR_BOT_Y);
                    } else if (port.isGnd()) {
                        createPowerLine(port.getX(), port.getY() + HOLE_SPACE, BREADBOARD_GND_BOT_X, BREADBOARD_GND_BOT_Y);
                    }
                }
            } else if (device.getFormFactor() == FormFactor.BREAKOUT_BOARD_TWOSIDE) {
                DevicePort topLeftPort = getTopLeftHole(device);
                for (DevicePort port : powerPort) {
                    if (port.getY() != topLeftPort.getY() ) {
                        if (port.isVcc()) {
                            createPowerLine(port.getX(), port.getY() + HOLE_SPACE, BREADBOARD_PWR_BOT_X, BREADBOARD_PWR_BOT_Y);
                        } else if (port.isGnd()) {
                            createPowerLine(port.getX(), port.getY() + HOLE_SPACE, BREADBOARD_GND_BOT_X, BREADBOARD_GND_BOT_Y);
                        }
                    }
                    else {
                        if (port.isVcc()) {
                            createPowerLine(port.getX(), port.getY() - HOLE_SPACE, BREADBOARD_PWR_TOP_X, BREADBOARD_PWR_TOP_Y);
                        } else if (port.isGnd()) {
                            createPowerLine(port.getX(), port.getY() - HOLE_SPACE, BREADBOARD_GND_TOP_X, BREADBOARD_GND_TOP_Y);
                        }
                    }

                }
            }
        }

        // connect i2c
        //double sdaStartX = 0, sdaStartY = 0;
        //double sclStartX = 0, sclStartY = 0;
        List<DevicePort> controllerI2CPort = controller.getPort(Peripheral.I2C_1);  // TODO: assume that we have only 1 I2C
        DevicePort startSDA = controllerI2CPort.stream().filter(DevicePort::isSDA).findFirst().get();
        DevicePort startSCL = controllerI2CPort.stream().filter(DevicePort::isSCL).findFirst().get();
        for (ProjectDevice projectDevice : project.getAllDevice()) {
            Device device = projectDevice.getActualDevice();
            List<DevicePort> powerPort = device.getPort(Peripheral.I2C_1);  // TODO: bug if device has more than 1 I2C which is unlikely
            if (powerPort.isEmpty()) {
                continue;
            }

            if (device.getFormFactor() == FormFactor.BREAKOUT_BOARD_ONESIDE) {
                for (DevicePort port : powerPort) {
                    if (port.isVcc()) {
                        createPowerLine(port.getX(), port.getY() + HOLE_SPACE, BREADBOARD_PWR_BOT_X, BREADBOARD_PWR_BOT_Y);
                    } else if (port.isGnd()) {
                        createPowerLine(port.getX(), port.getY() + HOLE_SPACE, BREADBOARD_GND_BOT_X, BREADBOARD_GND_BOT_Y);
                    }
                }
            } else if (device.getFormFactor() == FormFactor.BREAKOUT_BOARD_TWOSIDE) {
                DevicePort topLeftPort = getTopLeftHole(device);
                for (DevicePort port : powerPort) {
                    if (port.getY() != topLeftPort.getY() ) {
                        if (port.isVcc()) {
                            createPowerLine(port.getX(), port.getY() + HOLE_SPACE, BREADBOARD_PWR_BOT_X, BREADBOARD_PWR_BOT_Y);
                        } else if (port.isGnd()) {
                            createPowerLine(port.getX(), port.getY() + HOLE_SPACE, BREADBOARD_GND_BOT_X, BREADBOARD_GND_BOT_Y);
                        }
                    }
                    else {
                        if (port.isVcc()) {
                            createPowerLine(port.getX(), port.getY() - HOLE_SPACE, BREADBOARD_PWR_TOP_X, BREADBOARD_PWR_TOP_Y);
                        } else if (port.isGnd()) {
                            createPowerLine(port.getX(), port.getY() - HOLE_SPACE, BREADBOARD_GND_TOP_X, BREADBOARD_GND_TOP_Y);
                        }
                    }

                }
            }
        }
    }

    private int calculateNumberOfHole(Device device) {
        DevicePort leftPort = device.getPort().stream().min(Comparator.comparingDouble(DevicePort::getX)).get();
        int leftPaddingHoleCount = (int) Math.ceil(leftPort.getX() / HOLE_SPACE);
        DevicePort rightPort = device.getPort().stream().max(Comparator.comparingDouble(DevicePort::getX)).get();
        int rightPaddingHoldCount = (int) Math.ceil(rightPort.getX() / HOLE_SPACE);
        return leftPaddingHoleCount + device.getPort().size() + rightPaddingHoldCount;
    }

    private int calculateNumberOfHoleWithoutLeftWing(Device device) {
        DevicePort rightPort = device.getPort().stream().max(Comparator.comparingDouble(DevicePort::getX)).get();
        int rightPaddingHoldCount = (int) Math.ceil(rightPort.getX() / HOLE_SPACE);
        return device.getPort().size() + rightPaddingHoldCount;
    }

    private DevicePort getTopLeftHole(Device device) {
        return device.getPort().stream().min((d1, d2) -> {
            if (d1.getX() < d2.getX())
                return -1;
            else if (d1.getX() == d2.getX()) {
                if (d1.getY() < d2.getY())
                    return -1;
                else
                    return 1;
            }
            else
                return 1;
        }).get();
    }

    private DevicePort getBottomLeftHole(Device device) {
        return device.getPort().stream().min((d1, d2) -> {
            if (d1.getX() < d2.getX())
                return -1;
            else if (d1.getX() == d2.getX()) {
                if (d1.getY() > d2.getY())
                    return -1;
                else
                    return 1;
            }
            else
                return 1;
        }).get();
    }

    private void createPowerLine(double x1, double y1, double x2, double y2) {
        Line line = new Line(x1, y1, x2, y2);
        this.getChildren().add(line);
    }

    public static class Position {
        private double x;
        private double y;

        public Position(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public double getX() {
            return x;
        }

        public void setX(double x) {
            this.x = x;
        }

        public double getY() {
            return y;
        }

        public void setY(double y) {
            this.y = y;
        }
    }
}
