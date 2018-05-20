package io.makerplayground.ui.canvas;

import io.makerplayground.project.TimeCondition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class TimeConditionIconView extends VBox {

    private final TimeCondition timeCondition;

    @FXML private Label nameIconImageView;
    @FXML private ImageView iconImageView;
    @FXML private Button removeConditionDeviceBtn;

    public TimeConditionIconView(TimeCondition timeCondition) {
        this.timeCondition = timeCondition;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/ConditionDeviceIconView.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        nameIconImageView.setText("Time");

        iconImageView.setImage(new Image(getClass().getResourceAsStream("/icons/colorIcons/Time.png" )));
        iconImageView.setOnMouseClicked(e -> {
            TimeConditionPropertyWindow timePropertyWindow = new TimeConditionPropertyWindow(timeCondition);
            timePropertyWindow.show(TimeConditionIconView.this);
        });

        nameIconImageView.setOnMouseClicked(e -> {
            TimeConditionPropertyWindow timePropertyWindow = new TimeConditionPropertyWindow(timeCondition);
            timePropertyWindow.show(TimeConditionIconView.this);
        });
    }

    public void setOnRemove(EventHandler<ActionEvent> e) {
        removeConditionDeviceBtn.setOnAction(e);
    }
}
