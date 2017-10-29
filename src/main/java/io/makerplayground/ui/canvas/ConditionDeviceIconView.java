package io.makerplayground.ui.canvas;

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

/**
 * Created by USER on 05-Jul-17.
 */
public class ConditionDeviceIconView extends VBox {

    private final SceneDeviceIconViewModel viewModel;

    @FXML private Label nameIconImageView;
    @FXML private ImageView iconImageView;
    @FXML private Button removeConditionDeviceBtn;

    public ConditionDeviceIconView(SceneDeviceIconViewModel viewModel) {
        this.viewModel = viewModel;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/ConditionDeviceIconView.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        nameIconImageView.textProperty().bindBidirectional(viewModel.nameProperty());
        //action.setText(viewModel.getAction().getName());
        //viewModel.actionProperty().addListener((observable, oldValue, newValue) -> action.setText(newValue.getName()));
        iconImageView.setImage(new Image(getClass().getResourceAsStream("/icons/colorIcons/" + viewModel.getImageName() + ".png" )));

        iconImageView.setOnMouseClicked(e -> {
            ConditionDevicePropertyWindow devicePropertyWindow = new ConditionDevicePropertyWindow(viewModel);
            devicePropertyWindow.show(ConditionDeviceIconView.this);
        });

        nameIconImageView.setOnMouseClicked(e -> {
            ConditionDevicePropertyWindow devicePropertyWindow = new ConditionDevicePropertyWindow(viewModel);
            devicePropertyWindow.show(ConditionDeviceIconView.this);
        });
    }

    public void setOnRemove(EventHandler<ActionEvent> e) {
        removeConditionDeviceBtn.setOnAction(e);
    }

}
