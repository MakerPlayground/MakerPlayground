package io.makerplayground.ui;

import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.device.generic.GenericDevice;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

public class DeviceLibraryPanel extends TabPane {

    @FXML private VBox actuatorVBox;
    @FXML private VBox sensorVBox;
    @FXML private VBox utilityVBox;
    @FXML private VBox cloudVBox;
    @FXML private VBox interfaceVBox;

    private Consumer<GenericDevice> devicePressHandler;

    public DeviceLibraryPanel() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/DeviceLibraryPanel.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (GenericDevice genericDevice : DeviceLibrary.INSTANCE.getGenericActuatorDevice()) {
            DeviceLibraryListCell deviceLibraryListCell = new DeviceLibraryListCell(genericDevice);
            deviceLibraryListCell.setOnAddButtonPressed(event -> {
                if (devicePressHandler != null) {
                    devicePressHandler.accept(genericDevice);
                }
            });
            actuatorVBox.getChildren().add(deviceLibraryListCell);
        }

        for (GenericDevice genericDevice : DeviceLibrary.INSTANCE.getGenericSensorDevice()) {
            DeviceLibraryListCell deviceLibraryListCell = new DeviceLibraryListCell(genericDevice);
            deviceLibraryListCell.setOnAddButtonPressed(event -> {
                if (devicePressHandler != null) {
                    devicePressHandler.accept(genericDevice);
                }
            });
            sensorVBox.getChildren().add(deviceLibraryListCell);
        }

        for (GenericDevice genericDevice : DeviceLibrary.INSTANCE.getGenericUtilityDevice()) {
            DeviceLibraryListCell deviceLibraryListCell = new DeviceLibraryListCell(genericDevice);
            deviceLibraryListCell.setOnAddButtonPressed(event -> {
                if (devicePressHandler != null) {
                    devicePressHandler.accept(genericDevice);
                }
            });
            utilityVBox.getChildren().add(deviceLibraryListCell);
        }

        for (GenericDevice genericDevice : DeviceLibrary.INSTANCE.getGenericCloudDevice()) {
            DeviceLibraryListCell deviceLibraryListCell = new DeviceLibraryListCell(genericDevice);
            deviceLibraryListCell.setOnAddButtonPressed(event -> {
                if (devicePressHandler != null) {
                    devicePressHandler.accept(genericDevice);
                }
            });
            cloudVBox.getChildren().add(deviceLibraryListCell);
        }

        for (GenericDevice genericDevice : DeviceLibrary.INSTANCE.getGenericInterfaceDevice()) {
            DeviceLibraryListCell deviceLibraryListCell = new DeviceLibraryListCell(genericDevice);
            deviceLibraryListCell.setOnAddButtonPressed(event -> {
                if (devicePressHandler != null) {
                    devicePressHandler.accept(genericDevice);
                }
            });
            interfaceVBox.getChildren().add(deviceLibraryListCell);
        }
    }

    public void setOnDevicePressed(Consumer<GenericDevice> consumer) {
        devicePressHandler = consumer;
    }

    private static class DeviceLibraryListCell extends HBox {
        @FXML private ImageView imageView;
        @FXML private Label nameLabel;
        @FXML private Button addButton;

        public DeviceLibraryListCell(GenericDevice genericDevice) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/DeviceLibraryListCell.fxml"));
            fxmlLoader.setRoot(this);
            fxmlLoader.setController(this);
            try {
                fxmlLoader.load();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            try (InputStream imageStream = getClass().getResourceAsStream("/icons/colorIcons-3/" + genericDevice.getName() + ".png")) {
                imageView.setImage(new Image(imageStream));
            } catch (NullPointerException | IOException e) {
                throw new IllegalStateException("Missing icon of " + genericDevice.getName());
            }
            nameLabel.setText(genericDevice.getName());
        }

        public void setOnAddButtonPressed(EventHandler<ActionEvent> event) {
            addButton.setOnAction(event);
        }
    }
}
