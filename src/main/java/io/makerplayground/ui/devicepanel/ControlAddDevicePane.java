package io.makerplayground.ui.devicepanel;

import io.makerplayground.device.GenericDevice;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.io.IOException;

/**
 * Created by tanyagorn on 6/20/2017.
 */
public class ControlAddDevicePane extends VBox {
    private GenericDevice genericDevice;
    private int count;
    @FXML private VBox controllAddDevicePane;
    @FXML private ImageView imageView;
    @FXML private Label nameLabel;
    @FXML private Button incBtn;
    @FXML private Button decBtn;
    @FXML private TextField numberTextField;

    public ControlAddDevicePane(GenericDevice genericDevice) {
        this.genericDevice = genericDevice;
        this.count = 0;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/ControlAddDevicePane.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        imageView.setImage(new Image(getClass().getResourceAsStream("/icons/colorIcons/" + genericDevice.getName() + ".png")));
        nameLabel.setText(genericDevice.getName());

        Tooltip.install(imageView,  new Tooltip(genericDevice.getDescription()));

        numberTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                this.count = Integer.parseInt(numberTextField.getText());
            }
        });

        decBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                decrease();
                numberTextField.setText(String.valueOf(count));
            }
        });

        incBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                increase();
                numberTextField.setText(String.valueOf(count));
            }
        });
    }

    public GenericDevice getGenericDevice() {
        return genericDevice;
    }

    public int getCount() {
        return this.count;
    }

    public void decrease() {
        if (this.count > 0) {
            this.count = count - 1;
        }
    }

    public void increase() {
        this.count = count + 1;
    }
}