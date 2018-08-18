package io.makerplayground.ui.dialog.devicepane.input;

import io.makerplayground.project.ProjectDevice;
import io.makerplayground.ui.Main;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.io.IOException;

/**
 * Created by USER on 05-Jul-17.
 */
public class InputDeviceIconSelectorView extends  VBox {
    ProjectDevice projectDevice;
    @FXML private ImageView imv;
    @FXML private Label name;
    public InputDeviceIconSelectorView(ProjectDevice projectDevice) {
        this.projectDevice = projectDevice;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/dialog/devicepane/input/InputDeviceIconSelector.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Image image = new Image(Main.class.getResourceAsStream("/icons/colorIcons-2/" + projectDevice.getGenericDevice().getName() + ".png"));
        imv.setImage(image);

        name.setText(projectDevice.getName());
    }
}
