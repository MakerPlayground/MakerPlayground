package io.makerplayground.ui;

import io.makerplayground.project.DeviceSetting;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.converter.NumberStringConverter;


import java.io.IOException;

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

    }
}
