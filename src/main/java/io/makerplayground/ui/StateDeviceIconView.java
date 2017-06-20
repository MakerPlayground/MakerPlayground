package io.makerplayground.ui;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PopupControl;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;


import java.io.IOException;
import java.util.Optional;

/**
 * Created by tanyagorn on 6/12/2017.
 */
public class StateDeviceIconView extends VBox {

    private final StateDeviceIconViewModel viewModel;

    @FXML private Label nameIconImageView;
    @FXML private ImageView iconImageView;

    public StateDeviceIconView(StateDeviceIconViewModel viewModel) {
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
        iconImageView.setImage(new Image(getClass().getResourceAsStream("/icons/" + viewModel.getImageName() + ".png" )));

        this.setOnMouseClicked(e -> {
            DevicePropertyWindow devicePropertyWindow = new DevicePropertyWindow(viewModel);
            devicePropertyWindow.show(StateDeviceIconView.this.getParent());

        });

    }

}
