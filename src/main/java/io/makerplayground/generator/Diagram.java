package io.makerplayground.generator;

import io.makerplayground.device.Device;
import io.makerplayground.device.DevicePort;
import io.makerplayground.helper.ConnectionType;
import io.makerplayground.helper.FormFactor;
import io.makerplayground.helper.Peripheral;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectController;
import io.makerplayground.project.ProjectDevice;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.QuadCurveTo;

import java.util.*;

/**
 * Created by tanyagorn on 7/17/2017.
 */
public class Diagram extends Pane {
    private static final double BREADBOARD_TOP_MARGIN = 100;
    private static final double BREADBOARD_LEFT_MARGIN = 30;
    private static final double BREADBOARD_WIDTH = 936.48;
    private static final double BREADBOARD_HEIGHT = 302.4;
    private static final int    BREADBOARD_NUM_COLUMN = 5;
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
    public static final int STROKE_WIDTH = 3;

    private final List<Color> colorSet = new ArrayList<Color>(Arrays.asList(Color.BLUE, Color.HOTPINK,
            Color.ORANGE, Color.GRAY, Color.CYAN, Color.PURPLE, Color.DARKBLUE, Color.LIMEGREEN));

    private final Project project;

    private Position controllerPosition;
    private Map<ProjectDevice, Position> deviceTopLeftPos;

    public Diagram(Project project) {
        this.project = project;
        initDiagram();
    }

    private void initDiagram() {
        this.deviceTopLeftPos = new HashMap<>();
        setPrefSize(1000, 1200);

        // draw breadboard
        ImageView breadBoard = new ImageView(new Image(getClass().getResourceAsStream("/device/breadboard_large@2x.png")));
        breadBoard.setLayoutX(BREADBOARD_LEFT_MARGIN);
        breadBoard.setLayoutY(BREADBOARD_TOP_MARGIN);
        getChildren().add(breadBoard);

        // skip first 5 rows (reserved for vertical power+gnd lines)
        int currentRow = 5;

        // draw controller
        double lastY = BREADBOARD_TOP_MARGIN + BREADBOARD_HEIGHT + CONTROLLER_Y_MARGIN;
        Device controller = project.getController().getController();
        ImageView controllerImage = new ImageView(new Image(getClass().getResourceAsStream("/device/" + controller.getId() + ".png")));
        if (controller.getFormFactor() == FormFactor.BREAKOUT_BOARD_ONESIDE) {
            currentRow += calculateNumberOfHoleWithoutLeftWing(controller);
            DevicePort topLeftPort = getTopLeftHole(controller);
            controllerPosition = new Position(BREADBOARD_LEFT_MARGIN + J1_POS_X - topLeftPort.getX()
                                            , BREADBOARD_TOP_MARGIN + J1_POS_Y - topLeftPort.getY());
        } else if (controller.getFormFactor() == FormFactor.BREAKOUT_BOARD_TWOSIDE) {   // TODO: not tested yet
            currentRow += calculateNumberOfHoleWithoutLeftWing(controller);
            int heightHole = (int) ((getBottomLeftHole(controller).getY() - getTopLeftHole(controller).getY()) / HOLE_SPACE);
            controllerPosition = new Position(BREADBOARD_LEFT_MARGIN + J1_POS_X - getTopLeftHole(controller).getX()
                                            , BREADBOARD_TOP_MARGIN + J1_POS_Y + ((BREADBOARD_NUM_COLUMN - ((heightHole - 2) / 2)) * HOLE_SPACE));
        } else if (controller.getFormFactor() == FormFactor.STANDALONE) {
            controllerPosition = new Position(BREADBOARD_LEFT_MARGIN, lastY);
            lastY = lastY + controller.getHeight();
        }
        controllerImage.setLayoutX(controllerPosition.getX());
        controllerImage.setLayoutY(controllerPosition.getY());
        this.getChildren().add(controllerImage);

        // draw other device
        double lastX = BREADBOARD_LEFT_MARGIN;
        int deviceCount = 0;
        for (ProjectDevice projectDevice : project.getAllDeviceUsed()) {
            Device device = projectDevice.getActualDevice();
            ImageView deviceImage = new ImageView(new Image(getClass().getResourceAsStream("/device/" + device.getId() + ".png")));
            if (device.getFormFactor() == FormFactor.BREAKOUT_BOARD_ONESIDE) {
                DevicePort topLeftPort = getTopLeftHole(device);
                currentRow += calculateNumberOfHoleWithCurrentDeviceLeftWing(topLeftPort);
                deviceTopLeftPos.put(projectDevice, new Position(BREADBOARD_LEFT_MARGIN + J1_POS_X + (currentRow * HOLE_SPACE) - topLeftPort.getX()
                        , BREADBOARD_TOP_MARGIN + J1_POS_Y - topLeftPort.getY()));

                currentRow += calculateNumberOfHoleWithoutLeftWing(device);
            } else if (device.getFormFactor() == FormFactor.BREAKOUT_BOARD_TWOSIDE) {   // TODO: not tested yet
                int heightHole = (int) ((getBottomLeftHole(device).getY() - getTopLeftHole(device).getY()) / HOLE_SPACE) + 1;
                 deviceTopLeftPos.put(projectDevice, new Position(BREADBOARD_LEFT_MARGIN + J1_POS_X + (currentRow * HOLE_SPACE) - getTopLeftHole(device).getX()
                        , BREADBOARD_TOP_MARGIN + J1_POS_Y + ((BREADBOARD_NUM_COLUMN - ((heightHole - 2) / 2)) * HOLE_SPACE)));
                currentRow += calculateNumberOfHoleWithoutLeftWing(device);
            } else if (device.getFormFactor() == FormFactor.STANDALONE) {
                deviceTopLeftPos.put(projectDevice, new Position(lastX, lastY + CONTROLLER_Y_MARGIN));
                lastX = lastX + device.getWidth();
            } else if (device.getFormFactor() == FormFactor.SHIELD) {
                deviceTopLeftPos.put(projectDevice, controllerPosition);
            }
            deviceImage.setLayoutX(deviceTopLeftPos.get(projectDevice).getX());
            deviceImage.setLayoutY(deviceTopLeftPos.get(projectDevice).getY());
            this.getChildren().add(deviceImage);
            deviceCount++;
        }

        // connect power
        int numberOfPwrPinUsed = 0;
        int numberOfGndPinUsed = 1;
        boolean connectGnd = false;

        // connect power to both side of breadboard
        createPowerLine(BREADBOARD_LEFT_MARGIN + BREADBOARD_PWR_BOT_X, BREADBOARD_TOP_MARGIN + BREADBOARD_PWR_BOT_Y,
                BREADBOARD_LEFT_MARGIN + BREADBOARD_PWR_TOP_X, BREADBOARD_TOP_MARGIN + BREADBOARD_PWR_TOP_Y);
        numberOfPwrPinUsed++;
        // connect gnd to both side of breadboard (currently unused by any device)
        createGndLine(BREADBOARD_LEFT_MARGIN + BREADBOARD_GND_BOT_X + (numberOfGndPinUsed * HOLE_SPACE), BREADBOARD_TOP_MARGIN + BREADBOARD_GND_BOT_Y,
                BREADBOARD_LEFT_MARGIN + BREADBOARD_GND_TOP_X + (numberOfGndPinUsed * HOLE_SPACE), BREADBOARD_TOP_MARGIN + BREADBOARD_GND_TOP_Y);
        numberOfGndPinUsed++;

        // connect the first hole of breadboard to Arduino board
        for (DevicePort p : controller.getPort()) {
            if (p.isVcc()) {
                createPowerLine(BREADBOARD_LEFT_MARGIN + BREADBOARD_PWR_BOT_X + (numberOfPwrPinUsed * HOLE_SPACE), BREADBOARD_TOP_MARGIN + BREADBOARD_PWR_BOT_Y,
                                BREADBOARD_LEFT_MARGIN + p.getX(), BREADBOARD_TOP_MARGIN + BREADBOARD_HEIGHT + CONTROLLER_Y_MARGIN + p.getY());
                numberOfPwrPinUsed++;
            } else if ((p.isGnd()) && (!connectGnd)) {
                createGndLine(BREADBOARD_LEFT_MARGIN + BREADBOARD_GND_BOT_X + (numberOfGndPinUsed * HOLE_SPACE), BREADBOARD_TOP_MARGIN + BREADBOARD_GND_BOT_Y,
                            BREADBOARD_LEFT_MARGIN + p.getX(), BREADBOARD_TOP_MARGIN + BREADBOARD_HEIGHT + CONTROLLER_Y_MARGIN + p.getY());
                numberOfGndPinUsed++;
                connectGnd = true;
            }
        }

        // connect power for other devices
        for (ProjectDevice projectDevice : project.getAllDeviceUsed()) {
            Device device = projectDevice.getActualDevice();
            List<DevicePort> powerPort = device.getPort(Peripheral.POWER);

            if (device.getFormFactor() == FormFactor.BREAKOUT_BOARD_ONESIDE) {
                for (DevicePort port : powerPort) {
                    double calculatedYPadding = calculateNumberOfHoleBottomWing(device);
                    if (port.isVcc()) {
                        int holePosition = (int)Math.round(((deviceTopLeftPos.get(projectDevice).getX() + port.getX()) - (BREADBOARD_PWR_BOT_X + BREADBOARD_LEFT_MARGIN ))/HOLE_SPACE);
                        if ((holePosition == 5) || (holePosition == 11) || (holePosition == 17) || (holePosition == 23)
                                || (holePosition == 29) || (holePosition == 35) || (holePosition == 41) || (holePosition == 47) || (holePosition == 53)) {
                            createPowerLine(deviceTopLeftPos.get(projectDevice).getX() + port.getX(), deviceTopLeftPos.get(projectDevice).getY() + port.getY() + calculatedYPadding
                                    , deviceTopLeftPos.get(projectDevice).getX() + port.getX() - HOLE_SPACE, BREADBOARD_PWR_BOT_Y + BREADBOARD_TOP_MARGIN);
                        } else {
                            createPowerLine(deviceTopLeftPos.get(projectDevice).getX() + port.getX(), deviceTopLeftPos.get(projectDevice).getY() + port.getY() + calculatedYPadding
                                    , deviceTopLeftPos.get(projectDevice).getX() + port.getX(), BREADBOARD_PWR_BOT_Y + BREADBOARD_TOP_MARGIN);
                        }
                        numberOfPwrPinUsed++;
                    } else if (port.isGnd()) {
                        int holePosition = (int)Math.round(((deviceTopLeftPos.get(projectDevice).getX() + port.getX()) - (BREADBOARD_GND_BOT_X + BREADBOARD_LEFT_MARGIN))/HOLE_SPACE);
                        if ((holePosition == 5) || (holePosition == 11) || (holePosition == 17) || (holePosition == 23)
                                || (holePosition == 29) || (holePosition == 35) || (holePosition == 41) || (holePosition == 47) || (holePosition == 53)) {
                            System.out.println("1");
                            createGndLine(deviceTopLeftPos.get(projectDevice).getX() + port.getX(), deviceTopLeftPos.get(projectDevice).getY() + port.getY() + calculatedYPadding
                                    , deviceTopLeftPos.get(projectDevice).getX() + port.getX() - HOLE_SPACE, BREADBOARD_GND_BOT_Y + BREADBOARD_TOP_MARGIN);
                        } else {
                            System.out.println("2");
                            createGndLine(deviceTopLeftPos.get(projectDevice).getX() + port.getX(), deviceTopLeftPos.get(projectDevice).getY() + port.getY() + calculatedYPadding
                                    , deviceTopLeftPos.get(projectDevice).getX() + port.getX(), BREADBOARD_GND_BOT_Y + BREADBOARD_TOP_MARGIN);
                        }
                        numberOfGndPinUsed++;
                    }
                }
            } else if (device.getFormFactor() == FormFactor.BREAKOUT_BOARD_TWOSIDE) {
                DevicePort topLeftPort = getTopLeftHole(device);
                for (DevicePort port : powerPort) {
                    if (port.getY() != topLeftPort.getY() ) {
                        if (port.isVcc()) {
                            int holePosition = (int)Math.round(((deviceTopLeftPos.get(projectDevice).getX() + port.getX()) - (BREADBOARD_PWR_BOT_X + BREADBOARD_LEFT_MARGIN))/HOLE_SPACE);
                            if ((holePosition == 5) || (holePosition == 11) || (holePosition == 17) || (holePosition == 23)
                                    || (holePosition == 29) || (holePosition == 35) || (holePosition == 41) || (holePosition == 47) || (holePosition == 53)) {
                                createPowerLine(deviceTopLeftPos.get(projectDevice).getX() + port.getX(), deviceTopLeftPos.get(projectDevice).getY() + port.getY() + HOLE_SPACE
                                        , deviceTopLeftPos.get(projectDevice).getX() + port.getX() - HOLE_SPACE, BREADBOARD_PWR_BOT_Y + BREADBOARD_TOP_MARGIN);
                            } else {
                                createPowerLine(deviceTopLeftPos.get(projectDevice).getX() + port.getX(), deviceTopLeftPos.get(projectDevice).getY() + port.getY() + HOLE_SPACE
                                        , deviceTopLeftPos.get(projectDevice).getX() + port.getX(), BREADBOARD_PWR_BOT_Y + BREADBOARD_TOP_MARGIN);
                            }

                            numberOfPwrPinUsed++;
                        } else if (port.isGnd()) {
                            int holePosition = (int)Math.round(((deviceTopLeftPos.get(projectDevice).getX() + port.getX()) - (BREADBOARD_GND_BOT_X + BREADBOARD_LEFT_MARGIN))/HOLE_SPACE);
                            if ((holePosition == 5) || (holePosition == 11) || (holePosition == 17) || (holePosition == 23)
                                    || (holePosition == 29) || (holePosition == 35) || (holePosition == 41) || (holePosition == 47) || (holePosition == 53)) {
                                createGndLine(deviceTopLeftPos.get(projectDevice).getX() + port.getX(), deviceTopLeftPos.get(projectDevice).getY() + port.getY() + HOLE_SPACE
                                        , deviceTopLeftPos.get(projectDevice).getX() + port.getX() - HOLE_SPACE, BREADBOARD_GND_BOT_Y + BREADBOARD_TOP_MARGIN);
                            } else {
                                createGndLine(deviceTopLeftPos.get(projectDevice).getX() + port.getX(), deviceTopLeftPos.get(projectDevice).getY() + port.getY() + HOLE_SPACE
                                        , deviceTopLeftPos.get(projectDevice).getX() + port.getX(), BREADBOARD_GND_BOT_Y + BREADBOARD_TOP_MARGIN);
                            }
                            numberOfGndPinUsed++;
                        }
                    }
                    else {
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
//                            createPowerLine(deviceTopLeftPos.get(projectDevice).getX() + port.getX(), deviceTopLeftPos.get(projectDevice).getY() + port.getY() - HOLE_SPACE
//                                    , BREADBOARD_PWR_TOP_X + BREADBOARD_LEFT_MARGIN + (numberOfPwrPinUsed * HOLE_SPACE), BREADBOARD_PWR_TOP_Y + BREADBOARD_TOP_MARGIN);
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
//                            createGndLine(deviceTopLeftPos.get(projectDevice).getX() + port.getX(), deviceTopLeftPos.get(projectDevice).getY() + port.getY() - HOLE_SPACE
//                                    , BREADBOARD_GND_TOP_X + BREADBOARD_LEFT_MARGIN + (numberOfGndPinUsed * HOLE_SPACE), BREADBOARD_GND_TOP_Y + BREADBOARD_TOP_MARGIN);
                            numberOfGndPinUsed++;
                        }
                    }

                }
            }
            else if (device.getFormFactor() == FormFactor.STANDALONE) {
                for (DevicePort port : powerPort) {
                    if (port.isVcc()) {
                        createPowerLine(deviceTopLeftPos.get(projectDevice).getX() + port.getX(), deviceTopLeftPos.get(projectDevice).getY() + port.getY()
                                , BREADBOARD_PWR_BOT_X + BREADBOARD_LEFT_MARGIN + (numberOfPwrPinUsed * HOLE_SPACE), BREADBOARD_PWR_BOT_Y + BREADBOARD_TOP_MARGIN);
                        numberOfPwrPinUsed++;
                    } else if (port.isGnd()) {
                        createGndLine(deviceTopLeftPos.get(projectDevice).getX() + port.getX(),deviceTopLeftPos.get(projectDevice).getY() + port.getY()
                                , BREADBOARD_GND_BOT_X + BREADBOARD_LEFT_MARGIN + (numberOfGndPinUsed * HOLE_SPACE), BREADBOARD_GND_BOT_Y + BREADBOARD_TOP_MARGIN);
                        numberOfGndPinUsed++;
                    }
                }
            }
        }


        // connect i2c
        double sdaStartX = 0, sdaStartY = 0;
        double sclStartX = 0, sclStartY = 0;
        List<DevicePort> controllerI2CPort = controller.getPort(Peripheral.I2C_1);  // TODO: assume that we have only 1 I2C
        DevicePort startSDA = controllerI2CPort.stream().filter(DevicePort::isSDA).findFirst().get();
        DevicePort startSCL = controllerI2CPort.stream().filter(DevicePort::isSCL).findFirst().get();
        if (controller.getFormFactor() == FormFactor.BREAKOUT_BOARD_TWOSIDE) {  // TODO: not tested yet
            // SDA: top side - go up
            if (startSDA.getY() == getTopLeftHole(controller).getY()) {
                sdaStartX = startSDA.getX() + BREADBOARD_LEFT_MARGIN;
                sdaStartY = startSDA.getY() - HOLE_SPACE;
            }
            // SDA: Bottom side - go down
            if (startSDA.getY() != getTopLeftHole(controller).getY()) {
                sdaStartX = startSDA.getX();
                sdaStartY = startSDA.getY() + HOLE_SPACE;
            }
            // SCL: top side - go up
            if (startSCL.getY() == getTopLeftHole(controller).getY()) {
                sclStartX = startSCL.getX() + BREADBOARD_LEFT_MARGIN;
                sclStartY = startSCL.getY() - HOLE_SPACE;
            }
            // SCL: bottom side - go down
            if (startSCL.getY() != getTopLeftHole(controller).getY()) {
                sclStartX = startSCL.getX();
                sclStartY = startSCL.getY() + HOLE_SPACE;
            }
        } else if (controller.getFormFactor() == FormFactor.BREAKOUT_BOARD_ONESIDE) {
            //TODO: Implement this
        } else if (controller.getFormFactor() == FormFactor.STANDALONE) {
            sdaStartX = startSDA.getX() + BREADBOARD_LEFT_MARGIN;
            sdaStartY = startSDA.getY() + BREADBOARD_TOP_MARGIN + BREADBOARD_HEIGHT + CONTROLLER_Y_MARGIN;
            sclStartX = startSCL.getX() + BREADBOARD_LEFT_MARGIN;
            sclStartY = startSCL.getY() + BREADBOARD_TOP_MARGIN + BREADBOARD_HEIGHT + CONTROLLER_Y_MARGIN;
        }

        for (ProjectDevice projectDevice : project.getAllDeviceUsed()) {
            Device device = projectDevice.getActualDevice();
            double calculatedYPadding = calculateNumberOfHoleBottomWing(device);
            for (Peripheral sourcePeripheral : projectDevice.getDeviceConnection().keySet()) {
                if (sourcePeripheral == Peripheral.I2C_1) { // TODO: bug if device has more than 1 I2C which is unlikely
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


        // connect SPI, UART, PWM
        for (ProjectDevice projectDevice : project.getAllDeviceUsed()) {
            Device device = projectDevice.getActualDevice();
            for (Peripheral sourcePeripheral : projectDevice.getDeviceConnection().keySet()) {

                if (sourcePeripheral == Peripheral.SPI_1) { // TODO: not tested yet
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
                } else if (sourcePeripheral == Peripheral.UART_1) { // TODO: not tested yet
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
                } else if (sourcePeripheral == Peripheral.PWM_1) {
                    DevicePort sourcePort = device.getPort(sourcePeripheral).get(0);
                    DevicePort destPort = projectDevice.getDeviceConnection().get(sourcePeripheral).get(0);
                    createLine(projectDevice, sourcePort, project.getController(), destPort);
                } else if (sourcePeripheral == Peripheral.ANALOG_1) {
                    DevicePort sourcePort = device.getPort(sourcePeripheral).get(0);
                    DevicePort destPort = projectDevice.getDeviceConnection().get(sourcePeripheral).get(0);
                    createLine(projectDevice, sourcePort, project.getController(), destPort);
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

    private double calculateNumberOfHoleBottomWing(Device device) {
        DevicePort topLeftPort = getTopLeftHole(device);
        int bottomPaddingHoleCount = (int) Math.ceil((device.getHeight() - topLeftPort.getY()) / HOLE_SPACE);
        return (bottomPaddingHoleCount * HOLE_SPACE);
    }

    private int calculateNumberOfHoleWithCurrentDeviceLeftWing(DevicePort port) {
        int rightPaddingHoleCount = (int) Math.ceil(port.getX()/HOLE_SPACE);
        return rightPaddingHoleCount;
    }

    private int calculateNumberOfHoleWithoutLeftWing(Device device) {
        DevicePort leftPort = device.getPort().stream().min(Comparator.comparingDouble(DevicePort::getX)).get();
        DevicePort rightPort = device.getPort().stream().max(Comparator.comparingDouble(DevicePort::getX)).get();
        int rightPaddingHoldCount = (int) Math.ceil((device.getWidth() - rightPort.getX()) / HOLE_SPACE);
        return (int) ((rightPort.getX() - leftPort.getX()) / HOLE_SPACE) + 1 + rightPaddingHoldCount;
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

    private void createLine(ProjectDevice source, DevicePort sourcePort, ProjectController dest, DevicePort destPort) {
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
        }
        else if (source.getActualDevice().getFormFactor() == FormFactor.STANDALONE) {
            startX = deviceTopLeftPos.get(source).getX() + sourcePort.getX();
            startY = deviceTopLeftPos.get(source).getY() + sourcePort.getY();
        }

        if (dest.getController().getFormFactor() == FormFactor.BREAKOUT_BOARD_ONESIDE) {
            endX = controllerPosition.getX()  + destPort.getX();
            endY = controllerPosition.getY() + destPort.getY() + HOLE_SPACE;
        } else if (dest.getController().getFormFactor() == FormFactor.BREAKOUT_BOARD_TWOSIDE) {
            DevicePort desTopLeftPort = getTopLeftHole(dest.getController());

            //top side - go up
            if (destPort.getY() == desTopLeftPort.getY()) {
                endX = controllerPosition.getX()  + destPort.getX();
                endY = controllerPosition.getY() + destPort.getY() - HOLE_SPACE;
            } //bottom side - go down
            else if (destPort.getY() != desTopLeftPort.getY()) {
                endX = controllerPosition.getX() + destPort.getX();
                endY = controllerPosition.getY() + destPort.getY() + HOLE_SPACE;
            }
        } else if (dest.getController().getFormFactor() == FormFactor.STANDALONE) {
            endX = controllerPosition.getX() + destPort.getX();
            endY = controllerPosition.getY() + destPort.getY();
        }

        int random = (int )(Math.random() * colorSet.size());

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
