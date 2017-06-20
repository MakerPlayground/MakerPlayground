package io.makerplayground.ui;

import io.makerplayground.device.GenericDevice;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Created by tanyagorn on 6/20/2017.
 */
public class ControlAddDevicePane extends VBox {
    private GenericDevice genericDevice;
    private int count;

    public ControlAddDevicePane(GenericDevice genericDevice) {
        this.genericDevice = genericDevice;
        this.count = 0;

        Image img = new Image(getClass().getResourceAsStream("/icons/" + genericDevice.getName() + ".png"));
        ImageView imageView = new ImageView(img);
        Label nameLabel = new Label(genericDevice.getName());

        HBox hbox = new HBox();
        setAlignment(Pos.CENTER);
        Button decBtn = new Button("-");
        decBtn.setPrefWidth(35);
        TextField textField = new TextField("0");
        textField.setPrefWidth(35);
        Button incBtn = new Button("+");
        incBtn.setPrefWidth(35);
        hbox.getChildren().addAll(decBtn, textField, incBtn);
        getChildren().addAll(imageView, nameLabel, hbox);

        textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                this.count = Integer.parseInt(textField.getText());
            }
        });

        decBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                decrease();
                textField.setText(String.valueOf(count));
            }
        });

        incBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                increase();
                textField.setText(String.valueOf(count));
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