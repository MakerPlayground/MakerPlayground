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
import javafx.scene.shape.Line;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.QuadCurveTo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

class WireAndBreadboardDiagram extends Pane {
    private static final double BREADBOARD_TOP_MARGIN = 100;
    private static final double BREADBOARD_LEFT_MARGIN = 30;
    private static final double BREADBOARD_WIDTH = 936.48;
    private static final double BREADBOARD_HEIGHT = 302.4;
    private static final double BREADBOARD_EXTEND_HEIGHT = 258.4;
    private static final int    BREADBOARD_NUM_COLUMN = 5;
    private static final int    BREADBOARD_NUM_COLUMN_WITH_CENTER_SPACE = 12;
    private static final int    BREADBOARD_EXTEND_NUM_COLUMN_WITH_CENTER_SPACE = 30;
    private static final double BREADBOARD_GND_BOT_X = 50.769;
    private static final double BREADBOARD_GND_BOT_Y = 274.145;
    private static final double BREADBOARD_PWR_BOT_X = 50.769;
    private static final double BREADBOARD_PWR_BOT_Y = 288.574;
    private static final double BREADBOARD_GND_TOP_X = 50.769;
    private static final double BREADBOARD_GND_TOP_Y = 14.429;
    private static final double BREADBOARD_PWR_TOP_X = 50.769;
    private static final double BREADBOARD_PWR_TOP_Y = 28.857;
    private static final double HOLE_SPACE = 14.4;
    private static final double CENTER_SPACE = 43.2;
    private static final double J1_POS_X = 21.841;
    private static final double J1_POS_Y = 72;

    private static final double CONTROLLER_Y_MARGIN = 30;
    private static final int STROKE_WIDTH = 3;
    private static final double DEVICE_MARGIN = 10;

    private static final List<Color> colorSet = Arrays.asList(Color.BLUE, Color.HOTPINK, Color.ORANGE, Color.GRAY
            , Color.CYAN, Color.PURPLE, Color.DARKBLUE, Color.LIMEGREEN);
    private static List<Integer> powerUsed = new ArrayList<>(Arrays.asList(1,2,5,11,17,23,29,35,41,47,53));
    private static List<Integer> groundUsed = new ArrayList<>(Arrays.asList(1,2,5,11,17,23,29,35,41,47,53));

    protected final Project project;

    protected Point2D controllerPosition;
    protected Map<ProjectDevice, Point2D> deviceTopLeftPos;

    public WireAndBreadboardDiagram(Project project) {
        this.project = project;
        initDiagram();
    }

    private void initDiagram() {
        powerUsed = new ArrayList<>(Arrays.asList(1,2,5,11,17,23,29,35,41,47,53));
        groundUsed = new ArrayList<>(Arrays.asList(1,2,5,11,17,23,29,35,41,47,53));
        this.deviceTopLeftPos = new HashMap<>();

        String deviceDirectoryPath = DeviceLibrary.INSTANCE.getLibraryPath().get() + File.separator + "devices";

        int currentRow = 0;
        double lastY = 0;
        // draw breadboard
        if(useBreadboard()) {
            ImageView breadBoard = new ImageView(new Image(getClass().getResourceAsStream("/device/breadboard_large@2x.png")));
            breadBoard.setLayoutX(BREADBOARD_LEFT_MARGIN);
            breadBoard.setLayoutY(BREADBOARD_TOP_MARGIN);
            getChildren().add(breadBoard);

            // skip first 5 rows (reserved for vertical power+gnd lines)
            currentRow = 5;
            lastY += BREADBOARD_TOP_MARGIN + BREADBOARD_HEIGHT;
        }

        // draw controller
        lastY += CONTROLLER_Y_MARGIN;
        ActualDevice controller = project.getController();
        ImageView controllerImage = null;
        try (InputStream controllerImageStream = Files.newInputStream(Paths.get(deviceDirectoryPath, controller.getId(), "asset", "controller.png"))){
            controllerImage = new ImageView(new Image(controllerImageStream));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (controller.getFormFactor() == FormFactor.BREAKOUT_BOARD_ONESIDE) {
            throw new IllegalStateException();
        } else if (controller.getFormFactor() == FormFactor.BREAKOUT_BOARD_TWOSIDE) {
            // (bottom.y - top.y) / HOLE_SPACE should be an integer but we may get 7.9X which is actually 8 not 7
            int heightHole = (int) Math.ceil((getBottomLeftHole(controller).getY() - getTopLeftHole(controller).getY()) / HOLE_SPACE);

            double leftHoleOffset = Math.ceil(getTopLeftHole(controller).getX() / HOLE_SPACE);
            double offsetY = getTopLeftHole(controller).getY();

            // if number of hole left is less than 2, we extend the baseboard vertically
            if ((BREADBOARD_NUM_COLUMN_WITH_CENTER_SPACE - heightHole) < 2) {
                // TODO: we should also draw the extend baseboard when there is a very wide device with BREAKOUT_BOARD_TWOSIDE formfactor
                // draw the extend baseboard
                ImageView breadBoardExtend = new ImageView(new Image(getClass().getResourceAsStream("/device/breadboard_large_extend@2x.png")));
                breadBoardExtend.setLayoutX(BREADBOARD_LEFT_MARGIN);
                breadBoardExtend.setLayoutY(BREADBOARD_TOP_MARGIN + BREADBOARD_HEIGHT);
                getChildren().add(breadBoardExtend);
                lastY += BREADBOARD_EXTEND_HEIGHT;

                controllerPosition = new Position(BREADBOARD_LEFT_MARGIN + J1_POS_X + (currentRow * HOLE_SPACE) + (leftHoleOffset * HOLE_SPACE) - getTopLeftHole(controller).getX()
                        , BREADBOARD_TOP_MARGIN + J1_POS_Y + Math.floor((BREADBOARD_EXTEND_NUM_COLUMN_WITH_CENTER_SPACE - heightHole) / 2.0) * HOLE_SPACE - offsetY);
                currentRow += leftHoleOffset + calculateNumberOfHoleWithoutLeftWing(controller);
            } else {
                controllerPosition = new Position(BREADBOARD_LEFT_MARGIN + J1_POS_X + (currentRow * HOLE_SPACE) + (leftHoleOffset * HOLE_SPACE) - getTopLeftHole(controller).getX()
                        , BREADBOARD_TOP_MARGIN + J1_POS_Y + Math.floor((BREADBOARD_NUM_COLUMN_WITH_CENTER_SPACE - heightHole) / 2.0) * HOLE_SPACE - offsetY);
                currentRow += leftHoleOffset + calculateNumberOfHoleWithoutLeftWing(controller);
            }
        } else if (controller.getFormFactor() == FormFactor.STANDALONE) {
            controllerPosition = new Point2D(BREADBOARD_LEFT_MARGIN, lastY);
            lastY = lastY + controller.getHeight();
        }
        controllerImage.setLayoutX(controllerPosition.getX());
        controllerImage.setLayoutY(controllerPosition.getY());
        this.getChildren().add(controllerImage);

        // draw other device excepts grove
        double lastX = BREADBOARD_LEFT_MARGIN;
        int deviceCount = 0;
        int maxHeight = 0;
        for (ProjectDevice projectDevice : project.getDevice()) {
            if (projectDevice.isMergeToOtherDevice()) {
                continue;
            }
            ActualDevice device = projectDevice.getActualDevice();
            if (device instanceof IntegratedActualDevice) {
                continue;
            }
            if (device.getFormFactor() == FormFactor.GROVE) {
                continue;
            } else if (device.getFormFactor() == FormFactor.NONE) {
                continue;
            }
            try (InputStream deviceImageStream = Files.newInputStream(Paths.get(deviceDirectoryPath,device.getId(), "asset", "device.png"))){
                ImageView deviceImage = new ImageView(new Image(deviceImageStream));
                if (device.getFormFactor() == FormFactor.BREAKOUT_BOARD_ONESIDE) {
                    DevicePort topLeftPort = getTopLeftHole(device);
                    currentRow += calculateNumberOfHoleWithCurrentDeviceLeftWing(topLeftPort);
                    deviceTopLeftPos.put(projectDevice, new Point2D(BREADBOARD_LEFT_MARGIN + J1_POS_X + (currentRow * HOLE_SPACE) - topLeftPort.getX()
                            , BREADBOARD_TOP_MARGIN + J1_POS_Y - topLeftPort.getY()));

                    currentRow += calculateNumberOfHoleWithoutLeftWing(device);
                } else if (device.getFormFactor() == FormFactor.BREAKOUT_BOARD_TWOSIDE) {
                    int heightHole = (int) ((getBottomLeftHole(device).getY() - getTopLeftHole(device).getY()) / HOLE_SPACE) + 1;
                    deviceTopLeftPos.put(projectDevice, new Point2D(BREADBOARD_LEFT_MARGIN + J1_POS_X + (currentRow * HOLE_SPACE) - getTopLeftHole(device).getX()
                            , BREADBOARD_TOP_MARGIN + J1_POS_Y + ((BREADBOARD_NUM_COLUMN - ((heightHole - 2) / 2)) * HOLE_SPACE)));
                    currentRow += calculateNumberOfHoleWithoutLeftWing(device);
                } else if (device.getFormFactor() == FormFactor.STANDALONE) {
                    deviceTopLeftPos.put(projectDevice, new Point2D(lastX, lastY + CONTROLLER_Y_MARGIN));
                    maxHeight = maxHeight < device.getHeight() ? (int) device.getHeight() : maxHeight;
                } else if (device.getFormFactor() == FormFactor.SHIELD) {
                    deviceTopLeftPos.put(projectDevice, controllerPosition);
                    maxHeight = maxHeight < device.getHeight() ? (int) device.getHeight() : maxHeight;
                } // TODO: add new form factor here
                if( lastX + device.getWidth() > 750){
                    lastX = BREADBOARD_LEFT_MARGIN;
                    lastY = lastY + CONTROLLER_Y_MARGIN + maxHeight;
                }
                else{
                    lastX = lastX + device.getWidth() + DEVICE_MARGIN;
                }
                deviceImage.setLayoutX(deviceTopLeftPos.get(projectDevice).getX());
                deviceImage.setLayoutY(deviceTopLeftPos.get(projectDevice).getY());
                this.getChildren().add(deviceImage);
                deviceCount++;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        lastY += (maxHeight + CONTROLLER_Y_MARGIN);

        int numberOfPwrPinUsed = 0;
        int numberOfGndPinUsed = 1;

        if(useBreadboard()) {
            // connect power
            boolean connectGnd = false;
            boolean connectVcc = false;

            // connect power to both side of breadboard
            createPowerLine(BREADBOARD_LEFT_MARGIN + BREADBOARD_PWR_BOT_X, BREADBOARD_TOP_MARGIN + BREADBOARD_PWR_BOT_Y,
                    BREADBOARD_LEFT_MARGIN + BREADBOARD_PWR_TOP_X, BREADBOARD_TOP_MARGIN + BREADBOARD_PWR_TOP_Y);
            numberOfPwrPinUsed++;
            // connect gnd to both side of breadboard (currently unused by any device)
            createGndLine(BREADBOARD_LEFT_MARGIN + BREADBOARD_GND_BOT_X + (numberOfGndPinUsed * HOLE_SPACE), BREADBOARD_TOP_MARGIN + BREADBOARD_GND_BOT_Y,
                    BREADBOARD_LEFT_MARGIN + BREADBOARD_GND_TOP_X + (numberOfGndPinUsed * HOLE_SPACE), BREADBOARD_TOP_MARGIN + BREADBOARD_GND_TOP_Y);
            numberOfGndPinUsed++;

            if (controller.getFormFactor() == FormFactor.BREAKOUT_BOARD_TWOSIDE || controller.getFormFactor() == FormFactor.BREAKOUT_BOARD_ONESIDE) {
                // connect the first hole of breadboard to Arduino board
                for (DevicePort p : controller.getPort(Peripheral.POWER)) {
                    if (!connectVcc && p.isVcc()) {
                        // TODO: there is a case that use multiple voltages in the same project.
                        double farFromTop = Math.abs(p.getY() - getTopLeftHole(controller).getY());
                        double farFromBottom = Math.abs(p.getY() - getBottomLeftHole(controller).getY());
                        double startX = controllerPosition.getX() + p.getX();
                        // ((Math.floor(BREADBOARD_NUM_COLUMN_WITH_CENTER_SPACE - heightHole) / 2.0)) * HOLE_SPACE - offsetY
                        double startY = controllerPosition.getY() + p.getY() + (farFromTop < farFromBottom ? (-HOLE_SPACE) : HOLE_SPACE); // go up or down one hole
                        double endX = BREADBOARD_LEFT_MARGIN + BREADBOARD_PWR_BOT_X + (numberOfPwrPinUsed * HOLE_SPACE);
                        double endY = BREADBOARD_TOP_MARGIN + BREADBOARD_PWR_BOT_Y;
                        createPowerLine(startX, startY, endX, endY);
                        numberOfPwrPinUsed++;
                        connectVcc = true;
                    } else if (!connectGnd && p.isGnd()) {
                        double farFromTop = Math.abs(p.getY() - getTopLeftHole(controller).getY());
                        double farFromBottom = Math.abs(p.getY() - getBottomLeftHole(controller).getY());
                        double startX = controllerPosition.getX() + p.getX();
                        double startY = controllerPosition.getY() + p.getY() + (farFromTop < farFromBottom ? (-HOLE_SPACE) : HOLE_SPACE); // go up or down one hole
                        double endX = BREADBOARD_LEFT_MARGIN + BREADBOARD_GND_BOT_X + (numberOfGndPinUsed * HOLE_SPACE);
                        double endY = BREADBOARD_TOP_MARGIN + BREADBOARD_GND_BOT_Y;
                        createGndLine(startX, startY, endX, endY);
                        numberOfGndPinUsed++;
                        connectGnd = true;
                    }
                }
            }
            else {
                // connect the first hole of breadboard to Arduino board
                for (DevicePort p : controller.getPort(Peripheral.POWER)) {
                    if (!connectVcc && p.isVcc()) {
                        // TODO: there is a case that use multiple voltages in the same project.
                        createPowerLine(BREADBOARD_LEFT_MARGIN + BREADBOARD_PWR_BOT_X + (numberOfPwrPinUsed * HOLE_SPACE), BREADBOARD_TOP_MARGIN + BREADBOARD_PWR_BOT_Y,
                                BREADBOARD_LEFT_MARGIN + p.getX(), controllerPosition.getY() + p.getY());
                        numberOfPwrPinUsed++;
                        connectVcc = true;
                    } else if (!connectGnd && p.isGnd()) {
                        createGndLine(BREADBOARD_LEFT_MARGIN + BREADBOARD_GND_BOT_X + (numberOfGndPinUsed * HOLE_SPACE), BREADBOARD_TOP_MARGIN + BREADBOARD_GND_BOT_Y,
                                BREADBOARD_LEFT_MARGIN + p.getX(), controllerPosition.getY() + p.getY());
                        numberOfGndPinUsed++;
                        connectGnd = true;
                    }
                }
            }
        }


        // connect power for other devices excepts grove
        for (ProjectDevice projectDevice : project.getDevice()) {
            if (projectDevice.isMergeToOtherDevice()) {
                continue;
            }
            ActualDevice device = projectDevice.getActualDevice();
            List<DevicePort> powerPort = device.getPort(Peripheral.POWER);

            if (device instanceof IntegratedActualDevice) {
                continue;
            }

            if (device.getFormFactor() == FormFactor.SHIELD) {
                continue;
            }

            if (device.getFormFactor() == FormFactor.BREAKOUT_BOARD_ONESIDE) {
                for (DevicePort port : powerPort) {
                    double calculatedYPadding = calculateNumberOfHoleBottomWing(device);
                    if (port.isVcc()) {
                        int holePosition = (int)Math.round(((deviceTopLeftPos.get(projectDevice).getX() + port.getX()) - (BREADBOARD_PWR_BOT_X + BREADBOARD_LEFT_MARGIN ))/HOLE_SPACE);
                        int availablePosition = getAvailablePowerPort(holePosition);
                        createPowerLine(deviceTopLeftPos.get(projectDevice).getX() + port.getX(), deviceTopLeftPos.get(projectDevice).getY() + port.getY() + calculatedYPadding
                                ,BREADBOARD_LEFT_MARGIN + J1_POS_X + (HOLE_SPACE*2) + (availablePosition * HOLE_SPACE), BREADBOARD_PWR_BOT_Y + BREADBOARD_TOP_MARGIN);
                        numberOfPwrPinUsed++;
                        powerUsed.add(availablePosition);
                    } else if (port.isGnd()) {
                        int holePosition = (int)Math.round(((deviceTopLeftPos.get(projectDevice).getX() + port.getX()) - (BREADBOARD_GND_BOT_X + BREADBOARD_LEFT_MARGIN))/HOLE_SPACE);
                        int availablePosition = getAvailableGndPort(holePosition);
                        createGndLine(deviceTopLeftPos.get(projectDevice).getX() + port.getX(), deviceTopLeftPos.get(projectDevice).getY() + port.getY() + calculatedYPadding
                                , BREADBOARD_LEFT_MARGIN + J1_POS_X + (HOLE_SPACE*2) + (availablePosition * HOLE_SPACE), BREADBOARD_GND_BOT_Y + BREADBOARD_TOP_MARGIN);
                        numberOfGndPinUsed++;
                        groundUsed.add(availablePosition);
                    }
                }
            } else if (device.getFormFactor() == FormFactor.BREAKOUT_BOARD_TWOSIDE) {
                DevicePort topLeftPort = getTopLeftHole(device);
                for (DevicePort port : powerPort) {
                    if (port.getY() != topLeftPort.getY() ) {
                        if (port.isVcc()) {
                            int holePosition = (int)Math.round(((deviceTopLeftPos.get(projectDevice).getX() + port.getX()) - (BREADBOARD_PWR_BOT_X + BREADBOARD_LEFT_MARGIN))/HOLE_SPACE);
                            int availablePosition = getAvailablePowerPort(holePosition);
                            createPowerLine(deviceTopLeftPos.get(projectDevice).getX() + port.getX(), deviceTopLeftPos.get(projectDevice).getY() + port.getY() + HOLE_SPACE
                                    , BREADBOARD_LEFT_MARGIN + J1_POS_X + (HOLE_SPACE*2) + (availablePosition * HOLE_SPACE), BREADBOARD_PWR_BOT_Y + BREADBOARD_TOP_MARGIN);
                            numberOfPwrPinUsed++;
                            powerUsed.add(availablePosition);
                        } else if (port.isGnd()) {
                            int holePosition = (int)Math.round(((deviceTopLeftPos.get(projectDevice).getX() + port.getX()) - (BREADBOARD_GND_BOT_X + BREADBOARD_LEFT_MARGIN))/HOLE_SPACE);
                            int availablePosition = getAvailableGndPort(holePosition);
                            createGndLine(deviceTopLeftPos.get(projectDevice).getX() + port.getX(), deviceTopLeftPos.get(projectDevice).getY() + port.getY() + HOLE_SPACE
                                    , BREADBOARD_LEFT_MARGIN + J1_POS_X + (HOLE_SPACE*2) + (availablePosition * HOLE_SPACE), BREADBOARD_GND_BOT_Y + BREADBOARD_TOP_MARGIN);
                            numberOfGndPinUsed++;
                            groundUsed.add(availablePosition);
                        }
                    } else { // connect to upper part of bread board
                        if (port.isVcc()) {
                            int holePosition = (int)Math.round(((deviceTopLeftPos.get(projectDevice).getX() + port.getX()) - (BREADBOARD_PWR_BOT_X + BREADBOARD_LEFT_MARGIN))/HOLE_SPACE);
                            if ((holePosition == 5) || (holePosition == 11) || (holePosition == 17) || (holePosition == 23)
                                    || (holePosition == 29) || (holePosition == 35) || (holePosition == 41) || (holePosition == 47) || (holePosition == 53)) {
                                createPowerLine(deviceTopLeftPos.get(projectDevice).getX() + port.getX(), deviceTopLeftPos.get(projectDevice).getY() + port.getY() - HOLE_SPACE
                                        , deviceTopLeftPos.get(projectDevice).getX() + port.getX() + HOLE_SPACE, BREADBOARD_PWR_TOP_Y + BREADBOARD_TOP_MARGIN);
                            } else {
                                createPowerLine(deviceTopLeftPos.get(projectDevice).getX() + port.getX(), deviceTopLeftPos.get(projectDevice).getY() + port.getY() - HOLE_SPACE
                                        , deviceTopLeftPos.get(projectDevice).getX() + port.getX(), BREADBOARD_PWR_TOP_Y + BREADBOARD_TOP_MARGIN);
                            }
                            numberOfPwrPinUsed++;
                        } else if (port.isGnd()) {
                            int holePosition = (int)Math.round(((deviceTopLeftPos.get(projectDevice).getX() + port.getX()) - (BREADBOARD_GND_BOT_X + BREADBOARD_LEFT_MARGIN))/HOLE_SPACE);
                            if ((holePosition == 5) || (holePosition == 11) || (holePosition == 17) || (holePosition == 23)
                                    || (holePosition == 29) || (holePosition == 35) || (holePosition == 41) || (holePosition == 47) || (holePosition == 53)) {
                                createGndLine(deviceTopLeftPos.get(projectDevice).getX() + port.getX(), deviceTopLeftPos.get(projectDevice).getY() + port.getY() - HOLE_SPACE
                                        , deviceTopLeftPos.get(projectDevice).getX() + port.getX() - HOLE_SPACE, BREADBOARD_GND_TOP_Y + BREADBOARD_TOP_MARGIN);
                            } else {
                                createGndLine(deviceTopLeftPos.get(projectDevice).getX() + port.getX(), deviceTopLeftPos.get(projectDevice).getY() + port.getY() - HOLE_SPACE
                                        , deviceTopLeftPos.get(projectDevice).getX() + port.getX(), BREADBOARD_GND_TOP_Y + BREADBOARD_TOP_MARGIN);
                            }
                            numberOfGndPinUsed++;
                        }
                    }
                }
            } else if (device.getFormFactor() == FormFactor.STANDALONE) {
                if (useBreadboard()) { // When has breadboard, connect like this
                    for (DevicePort port : powerPort) {
                        if (port.isVcc()) {
                            int holePosition = getAvailablePowerPort();
                            createPowerLine(deviceTopLeftPos.get(projectDevice).getX() + port.getX(), deviceTopLeftPos.get(projectDevice).getY() + port.getY()
                                    , BREADBOARD_PWR_BOT_X + BREADBOARD_LEFT_MARGIN + (holePosition * HOLE_SPACE), BREADBOARD_PWR_BOT_Y + BREADBOARD_TOP_MARGIN);
                            numberOfPwrPinUsed++;
                            powerUsed.add(holePosition);
                        } else if (port.isGnd()) {
                            int holePosition = getAvailableGndPort();
                            createGndLine(deviceTopLeftPos.get(projectDevice).getX() + port.getX(),deviceTopLeftPos.get(projectDevice).getY() + port.getY()
                                    , BREADBOARD_GND_BOT_X + BREADBOARD_LEFT_MARGIN + (holePosition * HOLE_SPACE), BREADBOARD_GND_BOT_Y + BREADBOARD_TOP_MARGIN);
                            numberOfGndPinUsed++;
                            groundUsed.add(holePosition);
                        }
                    }
                }
                else {
                    boolean connectGnd = false;
                    for (DevicePort port : powerPort) {
                        for (DevicePort p : controller.getPort()) {
                        if (port.isVcc()) {
                            if (p.isVcc()) {
                                int holePosition = getAvailablePowerPort();
                                createPowerLine(deviceTopLeftPos.get(projectDevice).getX() + port.getX(), deviceTopLeftPos.get(projectDevice).getY() + port.getY()
                                        , BREADBOARD_LEFT_MARGIN + p.getX(), CONTROLLER_Y_MARGIN  + p.getY());
                                numberOfPwrPinUsed++;
                                powerUsed.add(holePosition);
                            }
                        } else if (port.isGnd()) {
                            if ((p.isGnd()) && (!connectGnd)) {
                                    int holePosition = getAvailableGndPort();
                                    createGndLine(deviceTopLeftPos.get(projectDevice).getX() + port.getX(), deviceTopLeftPos.get(projectDevice).getY() + port.getY()
                                            ,  BREADBOARD_LEFT_MARGIN + p.getX(),  CONTROLLER_Y_MARGIN  + p.getY());
                                    numberOfGndPinUsed++;
                                    groundUsed.add(holePosition);
                                    connectGnd = true;
                                }
                            }
                        }
                    }
                }
            }
        }

        // connect i2c on breadboard
        double sdaStartX = 0, sdaStartY = 0;
        double sclStartX = 0, sclStartY = 0;
        List<DevicePort> controllerI2CPort = controller.getPort(ConnectionType.I2C);  // TODO: assume that we have only 1 I2C
        if (!controllerI2CPort.isEmpty()) {
            DevicePort startSDA = controllerI2CPort.stream().filter(DevicePort::isSDA).findFirst().get();
            DevicePort startSCL = controllerI2CPort.stream().filter(DevicePort::isSCL).findFirst().get();
            if (controller.getFormFactor() == FormFactor.BREAKOUT_BOARD_TWOSIDE) {  // TODO: not tested yet
                // SDA: top side - go up
                if (startSDA.getY() == getTopLeftHole(controller).getY()) {
                    sdaStartX = controllerPosition.getX() + startSDA.getX();
                    sdaStartY = controllerPosition.getY() + startSDA.getY() - HOLE_SPACE;
                }
                // SDA: Bottom side - go down
                if (startSDA.getY() != getTopLeftHole(controller).getY()) {
                    sdaStartX = controllerPosition.getX() + startSDA.getX();
                    sdaStartY = controllerPosition.getY() + startSDA.getY() + HOLE_SPACE;
                }
                // SCL: top side - go up
                if (startSCL.getY() == getTopLeftHole(controller).getY()) {
                    sclStartX = controllerPosition.getX() + startSCL.getX() + BREADBOARD_LEFT_MARGIN;
                    sclStartY = controllerPosition.getY() + startSCL.getY() - HOLE_SPACE;
                }
                // SCL: bottom side - go down
                if (startSCL.getY() != getTopLeftHole(controller).getY()) {
                    sclStartX = controllerPosition.getX() + startSCL.getX();
                    sclStartY = controllerPosition.getY() + startSCL.getY() + HOLE_SPACE;
                }
            } else if (controller.getFormFactor() == FormFactor.BREAKOUT_BOARD_ONESIDE) {
                //TODO: Implement this
            } else if (controller.getFormFactor() == FormFactor.STANDALONE) {
                sdaStartX = startSDA.getX() + BREADBOARD_LEFT_MARGIN;
                sdaStartY = startSDA.getY() + controllerPosition.getY();
                sclStartX = startSCL.getX() + BREADBOARD_LEFT_MARGIN;
                sclStartY = startSCL.getY() + controllerPosition.getY();
            }

            for (ProjectDevice projectDevice : project.getDevice()) {
                if (projectDevice.isMergeToOtherDevice()) {
                    continue;
                }
                ActualDevice device = projectDevice.getActualDevice();
                if (device.getFormFactor() == FormFactor.SHIELD) {
                    continue;
                }
                double calculatedYPadding = calculateNumberOfHoleBottomWing(device);
                for (Peripheral sourcePeripheral : projectDevice.getDeviceConnection().keySet()) {
                    if (sourcePeripheral == Peripheral.I2C_1) { // TODO: bug if device has more than 1 I2C which is unlike -> sourcePeripheral.getConnectionType() == ConnectionType.I2C
                        DevicePort desSDA = device.getPort(sourcePeripheral).stream().filter(DevicePort::isSDA).findFirst().get();
                        DevicePort desSCL = device.getPort(sourcePeripheral).stream().filter(DevicePort::isSCL).findFirst().get();

                        if (device.getFormFactor() == FormFactor.BREAKOUT_BOARD_ONESIDE) {
                            createSdaLine(sdaStartX, sdaStartY, desSDA.getX() + deviceTopLeftPos.get(projectDevice).getX(), desSDA.getY() + deviceTopLeftPos.get(projectDevice).getY() + calculatedYPadding);
                            createSclLine(sclStartX, sclStartY, desSCL.getX() + deviceTopLeftPos.get(projectDevice).getX(), desSCL.getY() + deviceTopLeftPos.get(projectDevice).getY() + calculatedYPadding);
                            sdaStartX = desSDA.getX() + deviceTopLeftPos.get(projectDevice).getX();
                            sdaStartY = desSDA.getY() + deviceTopLeftPos.get(projectDevice).getY() + calculatedYPadding;
                            sclStartX = desSCL.getX() + deviceTopLeftPos.get(projectDevice).getX();
                            sclStartY = desSCL.getY() + deviceTopLeftPos.get(projectDevice).getY() + calculatedYPadding;
                        } else if (device.getFormFactor() == FormFactor.BREAKOUT_BOARD_TWOSIDE) {   // TODO: not tested yet
                            DevicePort topLeftPort = getTopLeftHole(device);

                            //SDA: top side - go up
                            if (desSDA.getY() == topLeftPort.getY()) {
                                createPowerLine(sdaStartX, sdaStartY, desSDA.getX(), desSDA.getY() - HOLE_SPACE);
                                sdaStartY = desSDA.getY() - (HOLE_SPACE * 2);
                            } //SDA: bottom side - go down
                            else if (desSDA.getY() != topLeftPort.getY()) {
                                createPowerLine(sdaStartX, sdaStartY, desSDA.getX(), desSDA.getY() + HOLE_SPACE);
                                sdaStartY = desSDA.getY() + (HOLE_SPACE * 2);
                            } //SCL: top side - go up
                            else if (desSCL.getY() == topLeftPort.getY()) {
                                createPowerLine(sdaStartX, sdaStartY, desSCL.getX(), desSCL.getY() - HOLE_SPACE);
                                sclStartY = desSCL.getY() - (HOLE_SPACE * 2);
                            } //SCL: bottom side - go down
                            else if (desSCL.getY() != topLeftPort.getY()) {
                                createPowerLine(sdaStartX, sdaStartY, desSCL.getX(), desSCL.getY() + HOLE_SPACE);
                                sclStartY = desSCL.getY() + (HOLE_SPACE * 2);
                            }
                            sdaStartX = desSDA.getX();
                            sclStartX = desSCL.getX();
                        } else if (device.getFormFactor() == FormFactor.STANDALONE) {
                            createPowerLine(sdaStartX, sdaStartY, desSDA.getX(), desSDA.getY());
                            createPowerLine(sclStartX, sclStartY, desSCL.getX(), desSCL.getY());
                        }
                    }
                }
            }
        }


        // connect SPI, UART, PWM, GPIO, ...
        for (ProjectDevice projectDevice : project.getDevice()) {
            if (projectDevice.isMergeToOtherDevice()) {
                continue;
            }
            ActualDevice device = projectDevice.getActualDevice();
            if (device instanceof IntegratedActualDevice) {
                continue;
            }
            if (device.getFormFactor() == FormFactor.NONE) {
                continue;
            }
            if (device.getFormFactor() == FormFactor.SHIELD) {
                continue;
            }
            for (Peripheral sourcePeripheral : projectDevice.getDeviceConnection().keySet()) {
                // TODO: add description for grove device
                // skip if this is a grove device
                if (device.getFormFactor() == FormFactor.GROVE) {
                    break;
                }

                if (sourcePeripheral == Peripheral.SPI_1) { // TODO: not tested yet   -> sourcePeripheral.getConnectionType() == ConnectionType.SPI
//                    DevicePort sourceMOSI = device.getPort(sourcePeripheral).stream().filter(DevicePort::isMOSI).findFirst().get(); // TODO: shouldn't use findfirst
//                    DevicePort sourceMISO = device.getPort(sourcePeripheral).stream().filter(DevicePort::isMISO).findFirst().get();
//                    DevicePort sourceSCK = device.getPort(sourcePeripheral).stream().filter(DevicePort::isSCK).findFirst().get();
//                    DevicePort sourceSS = device.getPort(sourcePeripheral).stream().filter(DevicePort::isSS).findFirst().get();
//
//                    DevicePort desMOSI = controller.getPort(destPeripheral).stream().filter(DevicePort::isMOSI).findFirst().get();
//                    DevicePort desMISO = controller.getPort(destPeripheral).stream().filter(DevicePort::isMISO).findFirst().get();
//                    DevicePort desSCK = controller.getPort(destPeripheral).stream().filter(DevicePort::isSCK).findFirst().get();
//                    DevicePort desSS = controller.getPort(destPeripheral).stream().filter(DevicePort::isSS).findFirst().get();
//
//                    createLine(projectDevice, sourceMOSI, project.getController(), desMOSI);
//                    createLine(projectDevice, sourceMISO, project.getController(), desMISO);
//                    createLine(projectDevice, sourceSCK, project.getController(), desSCK);
//                    createLine(projectDevice, sourceSS, project.getController(), desSS);
                } else if (sourcePeripheral == Peripheral.UART_1) { // TODO: not tested yet   -> sourcePeripheral.getConnectionType() == ConnectionType.UART
//                    DevicePort sourceRX = device.getPort(sourcePeripheral).stream().filter(DevicePort::isRX).findFirst().get();
//                    DevicePort sourceTX = device.getPort(sourcePeripheral).stream().filter(DevicePort::isTX).findFirst().get();
//
//                    DevicePort desRX = controller.getPort(destPeripheral).stream().filter(DevicePort::isRX).findFirst().get();
//                    DevicePort desTX = controller.getPort(destPeripheral).stream().filter(DevicePort::isTX).findFirst().get();
//
//                    createLine(projectDevice, sourceRX, project.getController(), desRX);
//                    createLine(projectDevice, sourceTX, project.getController(), desTX);
                } else if (sourcePeripheral.getConnectionType() == ConnectionType.GPIO) {
                    DevicePort sourcePort = device.getPort(sourcePeripheral).get(0);
                    DevicePort destPort = projectDevice.getDeviceConnection().get(sourcePeripheral).get(0);
                    createLine(projectDevice, sourcePort, project.getController(), destPort);
                } else if (sourcePeripheral.getConnectionType() == ConnectionType.PWM) {
                    DevicePort sourcePort = device.getPort(sourcePeripheral).get(0);
                    DevicePort destPort = projectDevice.getDeviceConnection().get(sourcePeripheral).get(0);
                    createLine(projectDevice, sourcePort, project.getController(), destPort);
                } else if (sourcePeripheral.getConnectionType() == ConnectionType.ANALOG) {
                    DevicePort sourcePort = device.getPort(sourcePeripheral).get(0);
                    DevicePort destPort = projectDevice.getDeviceConnection().get(sourcePeripheral).get(0);
                    createLine(projectDevice, sourcePort, project.getController(), destPort);
                }
            }
        }

        setPrefSize(lastX + 500, lastY + 500);
    }

    private boolean useBreadboard() {
        List<FormFactor> formFactors = List.of(FormFactor.BREAKOUT_BOARD_ONESIDE,
                                                FormFactor.BREAKOUT_BOARD_TWOSIDE,
                                                FormFactor.BREADBOARD_CUSTOM);
        if (formFactors.contains(project.getController().getFormFactor())){
            return true;
        }
        int countStandAlone = 0;
        for(ProjectDevice device : project.getDevice()) {
            if (device.isMergeToOtherDevice()) {
                continue;
            }
            FormFactor f = device.getActualDevice().getFormFactor();
            if(f == FormFactor.STANDALONE){
                countStandAlone += 1;
            }
            if(formFactors.contains(f) || countStandAlone > 1) {
                return true;
            }
        }
        return false;
    }

    private int getAvailablePowerPort() {
        for (int i = 3; i < 60; i++) {
            if (!powerUsed.contains(i))
                return i;
        }
        return 0;
    }

    private int getAvailablePowerPort(int pos) {
        if (!powerUsed.contains(pos))
            return pos;

        int min = pos;
        int max = pos;
        while ((min > 3) && (min < 60) && (max > 3) && (max < 60)) {
            min = min - 1;
            if (!powerUsed.contains(min))
                return min;
            max = max + 1;
            if (!powerUsed.contains(max))
                return max;
        }
        return 0;
    }

    private int getAvailableGndPort() {
        for (int i = 3; i < 60; i++) {
            if (!groundUsed.contains(i))
                return i;
        }
        return 0;
    }

    private int getAvailableGndPort(int pos) {
        if (!groundUsed.contains(pos))
            return pos;

        int min = pos;
        int max = pos;
        while ((min > 3) && (min < 60) && (max > 3) && (max < 60)) {
            min = min - 1;
            if (!groundUsed.contains(min))
                return min;
            max = max + 1;
            if (!groundUsed.contains(max))
                return max;
        }
        return 0;
    }

    private int calculateNumberOfHole(ActualDevice device) {
        DevicePort leftPort = device.getPort().stream().min(Comparator.comparingDouble(DevicePort::getX)).get();
        int leftPaddingHoleCount = (int) Math.ceil(leftPort.getX() / HOLE_SPACE);
        DevicePort rightPort = device.getPort().stream().max(Comparator.comparingDouble(DevicePort::getX)).get();
        int rightPaddingHoldCount = (int) Math.ceil(rightPort.getX() / HOLE_SPACE);
        return leftPaddingHoleCount + device.getPort().size() + rightPaddingHoldCount;
    }

    private double calculateNumberOfHoleBottomWing(ActualDevice device) {
        DevicePort topLeftPort = getTopLeftHole(device);
        int bottomPaddingHoleCount = (int) Math.ceil((device.getHeight() - topLeftPort.getY()) / HOLE_SPACE);
        return (bottomPaddingHoleCount * HOLE_SPACE);
    }

    private int calculateNumberOfHoleWithCurrentDeviceLeftWing(DevicePort port) {
        int rightPaddingHoleCount = (int) Math.ceil(port.getX()/HOLE_SPACE);
        return rightPaddingHoleCount;
    }

    private int calculateNumberOfHoleWithoutLeftWing(ActualDevice device) {
        DevicePort leftPort = device.getPort().stream().min(Comparator.comparingDouble(DevicePort::getX)).get();
        DevicePort rightPort = device.getPort().stream().max(Comparator.comparingDouble(DevicePort::getX)).get();
        int rightPaddingHoldCount = (int) Math.ceil((device.getWidth() - rightPort.getX()) / HOLE_SPACE);
        return (int) ((rightPort.getX() - leftPort.getX()) / HOLE_SPACE) + 1 + rightPaddingHoldCount;
    }

    private DevicePort getTopLeftHole(ActualDevice device) {
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

    private DevicePort getBottomLeftHole(ActualDevice device) {
        return device.getPort().stream().min((d1, d2) -> {
//            if (d1.getX() < d2.getX())
//                return -1;
//            else if (d1.getX() == d2.getX()) {
//                if (d1.getY() > d2.getY())
//                    return -1;
//                else
//                    return 1;
//            }
//            else
//                return 1;
            if (d1.getY() > d2.getY()) {
                return -1;
            } else if (d1.getY() == d2.getY()) {
                if (d1.getX() < d2.getX())
                    return -1;
                else
                    return 1;
            } else {
                return 1;
            }

        }).get();
    }

    private void createLine(ProjectDevice source, DevicePort sourcePort, ActualDevice dest, DevicePort destPort) {
        double startX = 0, startY = 0;
        double endX = 0, endY = 0;

        if (source.getActualDevice().getFormFactor() == FormFactor.BREAKOUT_BOARD_ONESIDE) {
            startX = deviceTopLeftPos.get(source).getX() + sourcePort.getX();
            startY = deviceTopLeftPos.get(source).getY() + sourcePort.getY() + HOLE_SPACE;
        } else if (source.getActualDevice().getFormFactor() == FormFactor.BREAKOUT_BOARD_TWOSIDE) {
            DevicePort srcTopLeftPort = getTopLeftHole(source.getActualDevice());

            //top side - go up
            if (sourcePort.getY() == srcTopLeftPort.getY()) {
                startX = deviceTopLeftPos.get(source).getX() + sourcePort.getX();
                startY = deviceTopLeftPos.get(source).getY() + sourcePort.getY() - HOLE_SPACE;
            } //bottom side - go down
            else if (sourcePort.getY() != srcTopLeftPort.getY()) {
                startX = deviceTopLeftPos.get(source).getX() + sourcePort.getX();
                startY = deviceTopLeftPos.get(source).getY() + sourcePort.getY() + HOLE_SPACE;
            }
        } else if (source.getActualDevice().getFormFactor() == FormFactor.STANDALONE) {
            startX = deviceTopLeftPos.get(source).getX() + sourcePort.getX();
            startY = deviceTopLeftPos.get(source).getY() + sourcePort.getY();
        }

        if (dest.getFormFactor() == FormFactor.BREAKOUT_BOARD_ONESIDE) {
            endX = controllerPosition.getX()  + destPort.getX();
            endY = controllerPosition.getY() + destPort.getY() + HOLE_SPACE;
        } else if (dest.getFormFactor() == FormFactor.BREAKOUT_BOARD_TWOSIDE) {
            DevicePort desTopLeftPort = getTopLeftHole(dest);

            //top side - go up
            if (destPort.getY() == desTopLeftPort.getY()) {
                endX = controllerPosition.getX()  + destPort.getX();
                endY = controllerPosition.getY() + destPort.getY() - HOLE_SPACE;
            } //bottom side - go down
            else if (destPort.getY() != desTopLeftPort.getY()) {
                endX = controllerPosition.getX() + destPort.getX();
                endY = controllerPosition.getY() + destPort.getY() + HOLE_SPACE;
            }
        } else if (dest.getFormFactor() == FormFactor.STANDALONE) {
            endX = controllerPosition.getX() + destPort.getX();
            endY = controllerPosition.getY() + destPort.getY();
        }

        int random = (int) (Math.random() * colorSet.size());

        Line line = new Line(startX, startY, endX, endY);
        line.setStroke(colorSet.get(random));
        line.setStrokeWidth(STROKE_WIDTH);
        this.getChildren().add(line);
    }

    private void createPowerLine(double x1, double y1, double x2, double y2) {
        Line line = new Line(x1, y1, x2, y2);
        line.setStroke(Color.RED);
        line.setStrokeWidth(STROKE_WIDTH);
        this.getChildren().add(line);
    }

    private void createGndLine(double x1, double y1, double x2, double y2) {
        Line line = new Line(x1, y1, x2, y2);
        line.setStroke(Color.BLACK);
        line.setStrokeWidth(STROKE_WIDTH);
        this.getChildren().add(line);
    }

    private void createSdaLine(double x1, double y1, double x2, double y2) {
        if (Math.abs(y1 - y2) < 100) {
            Path path = new Path();

            MoveTo moveTo = new MoveTo();
            moveTo.setX(x1);
            moveTo.setY(y1);

            QuadCurveTo quadTo = new QuadCurveTo();
            quadTo.setControlX((x1+x2)/2);
            quadTo.setControlY(y1+40);
            quadTo.setX(x2);
            quadTo.setY(y2);

            path.getElements().add(moveTo);
            path.getElements().add(quadTo);

            path.setStroke(Color.GREEN);
            path.setStrokeWidth(STROKE_WIDTH);
            this.getChildren().add(path);
        } else {
            Line line = new Line(x1, y1, x2, y2);
            line.setStroke(Color.GREEN);
            line.setStrokeWidth(STROKE_WIDTH);
            this.getChildren().add(line);
        }
    }

    private void createSclLine(double x1, double y1, double x2, double y2) {
        if (Math.abs(y1 - y2) < 100) {
            Path path = new Path();

            MoveTo moveTo = new MoveTo();
            moveTo.setX(x1);
            moveTo.setY(y1);

            QuadCurveTo quadTo = new QuadCurveTo();
            quadTo.setControlX((x1+x2)/2);
            quadTo.setControlY(y1+30);
            quadTo.setX(x2);
            quadTo.setY(y2);

            path.getElements().add(moveTo);
            path.getElements().add(quadTo);

            path.setStroke(Color.YELLOW);
            path.setStrokeWidth(STROKE_WIDTH);
            this.getChildren().add(path);
        } else {
            Line line = new Line(x1, y1, x2, y2);
            line.setStroke(Color.YELLOW);
            line.setStrokeWidth(STROKE_WIDTH);
            this.getChildren().add(line);
        }
    }

    private void createGpioLine(double x1, double y1, double x2, double y2) {
        Line line = new Line(x1, y1, x2, y2);
        line.setStroke(Color.BLUE);
        line.setStrokeWidth(STROKE_WIDTH);
        this.getChildren().add(line);
    }

    private void createPWMLine(double x1, double y1, double x2, double y2) {
        Line line = new Line(x1, y1, x2, y2);
        line.setStroke(Color.BLUE);
        line.setStrokeWidth(STROKE_WIDTH);
        this.getChildren().add(line);
    }
}
