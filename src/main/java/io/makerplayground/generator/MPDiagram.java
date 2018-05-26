package io.makerplayground.generator;

import io.makerplayground.device.DevicePort;
import io.makerplayground.helper.Peripheral;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectDevice;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class MPDiagram extends Pane {
    private static final Point2D BASEBOARD_CENTER_POSITION = new Point2D(500, 400);
    private static final Map<String, Point2D> WIRE_POSITION = Map.of(
            "D1", new Point2D(248, 433),
            "D2", new Point2D(248, 380),
            "D3", new Point2D(254, 274),
            "D4", new Point2D(250, 170),
            "D5/A1", new Point2D(255, 54),
            "D6/A2", new Point2D(510, 54),
            "I2C1", new Point2D(590, 170),
            "I2C2", new Point2D(620, 274),
            "I2C3", new Point2D(620, 380),
            "I2C4", new Point2D(590, 433)
    );
    private static final Map<String, Point2D> DEVICE_POSITION = Map.of(
            "D1", new Point2D(270, 521),
            "D2", new Point2D(270, 418),
            "D3", new Point2D(270, 292),
            "D4", new Point2D(270, 189),
            "D5/A1", new Point2D(270, 72),
            "D6/A2", new Point2D(730, 72),
            "I2C1", new Point2D(730, 189),
            "I2C2", new Point2D(730, 292),
            "I2C3", new Point2D(730, 418),
            "I2C4", new Point2D(730, 521)
    );
    private static final List<String> LEFT_PORT_NAME = List.of("D1", "D2", "D3", "D4", "D5/A1");
    private static final List<String> RIGHT_PORT_NAME = List.of("D6/A2", "I2C1", "I2C2", "I2C3", "I2C4");

    public MPDiagram(Project project) {
        setPrefSize(1000, 600);

        // draw all devices
        for (ProjectDevice projectDevice : project.getAllDevice()) {
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

            String controllerPortName = controllerPort.get(0).getName();

            // draw wire
            if (!controllerPortName.equals("Internal")) {
                InputStream wireImageStream = getClass().getResourceAsStream("/device/MP_WIRE_" + controllerPortName.replace('/', '-') + ".png");
                if (wireImageStream == null) {
                    throw new IllegalStateException("Image not found");
                }
                ImageView wireImageView = new ImageView(new Image(wireImageStream));
                wireImageView.setLayoutX(WIRE_POSITION.get(controllerPortName).getX());
                wireImageView.setLayoutY(WIRE_POSITION.get(controllerPortName).getY());

                getChildren().add(wireImageView);


                // draw device
                Point2D devicePosition = DEVICE_POSITION.get(controllerPortName);

                InputStream deviceImageStream = getClass().getResourceAsStream("/device/" + projectDevice.getActualDevice().getId() + ".png");
                if (deviceImageStream == null) {
                    throw new IllegalStateException("Image not found : " + "/device/" + projectDevice.getActualDevice().getId() + ".png");
                }
                Image deviceImage = new Image(deviceImageStream);
                ImageView deviceImageView = new ImageView(deviceImage);
                if (LEFT_PORT_NAME.contains(controllerPortName)) {
                    deviceImageView.setRotate(90);
                    deviceImageView.setLayoutX(devicePosition.getX() + (deviceImage.getHeight() / 2 - deviceImage.getWidth() / 2)
                            - deviceImage.getHeight());
                } else if (RIGHT_PORT_NAME.contains(controllerPortName)) {
                    deviceImageView.setRotate(-90);
                    deviceImageView.setLayoutX(devicePosition.getX() + (deviceImage.getHeight() / 2 - deviceImage.getWidth() / 2));
                } else {
                    throw new IllegalStateException("Invalid port");
                }
                deviceImageView.setLayoutY(devicePosition.getY() - (deviceImage.getHeight() / 2.0 - deviceImage.getWidth() / 2.0)
                        - deviceImage.getWidth() / 2.0);

                getChildren().addAll(deviceImageView);
            }
        }

        // draw controller
        InputStream controllerImageStream = getClass().getResourceAsStream("/device/" + project.getController().getId() + ".png");
        if (controllerImageStream == null) {
            throw new IllegalStateException("Image not found");
        }
        Image controllerImage = new Image(controllerImageStream);
        ImageView controllerImageView = new ImageView(controllerImage);
        controllerImageView.setLayoutX(BASEBOARD_CENTER_POSITION.getX() - (controllerImage.getWidth() / 2.0));
        controllerImageView.setLayoutY(BASEBOARD_CENTER_POSITION.getY() - (controllerImage.getHeight() / 2.0));
        getChildren().add(controllerImageView);
    }
}
