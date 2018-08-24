package io.makerplayground.ui.dialog.devicepane.output;

import io.makerplayground.project.ProjectDevice;
import io.makerplayground.ui.Main;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;

import java.io.IOException;

/**
 * Created by tanyagorn on 6/26/2017.
 */
public class OutputDeviceIconSelectorView extends VBox {
    ProjectDevice projectDevice;
    @FXML private ImageView imv;
    @FXML private Label name;
    public OutputDeviceIconSelectorView(ProjectDevice projectDevice) {
        this.projectDevice = projectDevice;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/dialog/devicepane/output/OutputDeviceIconSelector.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Image image = new Image(Main.class.getResourceAsStream("/icons/colorIcons-3/" + projectDevice.getGenericDevice().getName() + ".png"));
        imv.setImage(image);

        name.setText(projectDevice.getName());
    }
}
