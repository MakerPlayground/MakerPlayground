package io.makerplayground.ui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.io.IOException;

/**
 *
 * Created by Nuntipat Narkthong on 6/8/2017 AD.
 */
public class DevicePanelIcon extends VBox {

    private final DevicePanelIconViewModel viewModel;

    @FXML private ImageView imageView;
    @FXML private TextField nameTextField;
    @FXML private Button removeButton;

    public DevicePanelIcon(DevicePanelIconViewModel viewModel) {
        this.viewModel = viewModel;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/DevicePanelIcon.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        imageView.setImage(new Image(getClass().getResourceAsStream("/icons/colorIcons/" + viewModel.getDeviceName() + ".png")));
        nameTextField.textProperty().bindBidirectional(viewModel.nameProperty());
    }

    public void setOnAction(EventHandler<ActionEvent> event) {
        removeButton.setOnAction(event);
    }
}
