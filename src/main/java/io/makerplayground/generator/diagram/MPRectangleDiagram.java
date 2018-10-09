package io.makerplayground.generator.diagram;

import io.makerplayground.device.DevicePort;
import io.makerplayground.helper.FormFactor;
import io.makerplayground.helper.Peripheral;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectDevice;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class MPRectangleDiagram extends Pane {
    private static final Point2D BASEBOARD_CENTER_POSITION = new Point2D(500, 450);
    private static final Map<String, Point2D> WIRE_POSITION = Map.ofEntries(
            Map.entry("D1", new Point2D(705, 499.5)),
            Map.entry("D2", new Point2D(705, 595.5)),
            Map.entry("D3", new Point2D(295, 595.5)),
            Map.entry("D4", new Point2D(295, 499.5)),
            Map.entry("D5", new Point2D(295, 404.5)),
            Map.entry("D6", new Point2D(295, 308.5)),
            Map.entry("A1", new Point2D(449.5, 210)),
            Map.entry("A2", new Point2D(550.5, 210)),
            Map.entry("I2C (#1)", new Point2D(705, 308.5)),
            Map.entry("I2C (#2)", new Point2D(705, 404.5))
    );
    private static final Map<String, Point2D> DEVICE_POSITION = Map.ofEntries(
            Map.entry("D1", new Point2D(750, 509)),
            Map.entry("D2", new Point2D(750, 624)),
            Map.entry("D3", new Point2D(250, 624)),
            Map.entry("D4", new Point2D(250, 509)),
            Map.entry("D5", new Point2D(250, 395)),
            Map.entry("D6", new Point2D(250, 280)),
            Map.entry("A1", new Point2D(440, 165)),
            Map.entry("A2", new Point2D(560, 165)),
            Map.entry("I2C (#1)", new Point2D(750, 280)),
            Map.entry("I2C (#2)", new Point2D(750, 395))
    );
    private static final List<String> LEFT_PORT_NAME = List.of("D3", "D4", "D5", "D6");
    private static final List<String> TOP_PORT_NAME = List.of("A1", "A2");
    private static final List<String> RIGHT_PORT_NAME = List.of("D1", "D2", "I2C (#1)", "I2C (#2)");

    public MPRectangleDiagram(Project project) {
        setPrefSize(1000, 800);

        // draw all devices
        for (ProjectDevice projectDevice : project.getAllDeviceUsed()) {
            if (projectDevice.getActualDevice().getFormFactor() == FormFactor.NONE) {
                continue;
            }
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
                String wireImageFileName = "/wiring/MP_RECTANGLE/MP_WIRE_" + controllerPortName.replace('/', '-') + ".png";
                InputStream wireImageStream = getClass().getResourceAsStream(wireImageFileName);
                if (wireImageStream == null) {
                    throw new IllegalStateException("Image not found: " + wireImageFileName);
                }
                ImageView wireImageView = new ImageView(new Image(wireImageStream));
                if (WIRE_POSITION.get(controllerPortName) == null) {
                    throw new IllegalStateException(controllerPortName);
                }
                wireImageView.setLayoutX(WIRE_POSITION.get(controllerPortName).getX());
                wireImageView.setLayoutY(WIRE_POSITION.get(controllerPortName).getY());

                double startX = WIRE_POSITION.get(controllerPortName).getX();
                double startY = WIRE_POSITION.get(controllerPortName).getY();
                double endX = DEVICE_POSITION.get(controllerPortName).getX();
                double endY = DEVICE_POSITION.get(controllerPortName).getY();

                Line line = new Line(startX, startY, endX, endY);
                getChildren().add(line);
//                getChildren().add(wireImageView);


                // draw device
                Point2D devicePosition = DEVICE_POSITION.get(controllerPortName);
                Path deviceImagePath = Paths.get("library/devices",projectDevice.getActualDevice().getId(),"asset","MPdevice.png");
                try (InputStream deviceImageStream = Files.newInputStream(deviceImagePath)){
                    Image deviceImage = new Image(deviceImageStream);
                    ImageView deviceImageView = new ImageView(deviceImage);
                    if (LEFT_PORT_NAME.contains(controllerPortName)) {
                        deviceImageView.setRotate(90);
                        deviceImageView.setLayoutX(devicePosition.getX() + (deviceImage.getHeight() / 2.0 - deviceImage.getWidth() / 2.0)
                                - deviceImage.getHeight());
                        deviceImageView.setLayoutY(devicePosition.getY() - (deviceImage.getHeight() / 2.0 - deviceImage.getWidth() / 2.0)
                                - deviceImage.getWidth() / 2.0);
                    } else if (RIGHT_PORT_NAME.contains(controllerPortName)) {
                        deviceImageView.setRotate(-90);
                        deviceImageView.setLayoutX(devicePosition.getX() + (deviceImage.getHeight() / 2.0 - deviceImage.getWidth() / 2.0));
                        deviceImageView.setLayoutY(devicePosition.getY() - (deviceImage.getHeight() / 2.0 - deviceImage.getWidth() / 2.0)
                                - deviceImage.getWidth() / 2.0);
                    } else if (TOP_PORT_NAME.contains(controllerPortName)) {
                        deviceImageView.setRotate(180);
                        deviceImageView.setLayoutX(devicePosition.getX() - deviceImage.getWidth() / 2.0);
                        deviceImageView.setLayoutY(devicePosition.getY() - deviceImage.getHeight());
                    } else {
                        throw new IllegalStateException("Invalid port");
                    }

                    getChildren().addAll(deviceImageView);
                } catch (IOException e) {
                    throw new IllegalStateException("Image not found : " + "library/devices/" + projectDevice.getActualDevice().getId() + "/asset/MPdevice.png");
                }
            }
        }

        // draw controller
        Path controllerImagePath = Paths.get("library/devices",project.getController().getId(),"asset","MPcontroller.png");
        try (InputStream controllerImageStream = Files.newInputStream(controllerImagePath)) {
            Image controllerImage = new Image(controllerImageStream);
            ImageView controllerImageView = new ImageView(controllerImage);
            controllerImageView.setLayoutX(BASEBOARD_CENTER_POSITION.getX() - (controllerImage.getWidth() / 2.0));
            controllerImageView.setLayoutY(BASEBOARD_CENTER_POSITION.getY() - (controllerImage.getHeight() / 2.0));
            getChildren().add(controllerImageView);
        } catch (IOException e) {
            throw new IllegalStateException("Image not found : " + "library/devices/" + project.getController().getId() + "asset/MPcontroller.png");
        }
    }
}
