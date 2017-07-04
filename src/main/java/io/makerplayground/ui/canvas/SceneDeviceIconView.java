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
 * Created by tanyagorn on 6/12/2017.
 */
public class SceneDeviceIconView extends VBox {

    private final SceneDeviceIconViewModel viewModel;

    @FXML private Label nameIconImageView;
    @FXML private ImageView iconImageView;
    @FXML private Label action;
    @FXML private Button removeStateDeviceBtn;


    public SceneDeviceIconView(SceneDeviceIconViewModel viewModel) {
        this.viewModel = viewModel;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/StateDeviceIconView.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        nameIconImageView.textProperty().bindBidirectional(viewModel.nameProperty());
        action.setText(viewModel.getAction().getName());
        viewModel.actionProperty().addListener((observable, oldValue, newValue) -> action.setText(newValue.getName()));
        iconImageView.setImage(new Image(getClass().getResourceAsStream("/icons/" + viewModel.getImageName() + ".png" )));

        iconImageView.setOnMouseClicked(e -> {
            DevicePropertyWindow devicePropertyWindow = new DevicePropertyWindow(viewModel);
            devicePropertyWindow.show(SceneDeviceIconView.this);
        });
    }

    public void setOnRemove(EventHandler<ActionEvent> e) {
        removeStateDeviceBtn.setOnAction(e);
    }
}
